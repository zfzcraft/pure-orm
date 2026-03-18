package cn.zfz.pureorm.core;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.crud.batch.InsertBatchSqlBuilder;
import cn.zfz.pureorm.crud.batch.LambadaInsertBatchWrapper;
import cn.zfz.pureorm.crud.delete.DeleteSqlGenerator;
import cn.zfz.pureorm.crud.delete.LambadaDeleteWrapper;
import cn.zfz.pureorm.crud.insert.InsertSqlBuilder;
import cn.zfz.pureorm.crud.insert.InsertWrapper;
import cn.zfz.pureorm.crud.insert.LambadaInsertWrapper;
import cn.zfz.pureorm.crud.select.UpdateWrapper;
import cn.zfz.pureorm.crud.select.single.LambadaSelectWrapper;
import cn.zfz.pureorm.crud.select.single.ResultSetMapper;
import cn.zfz.pureorm.crud.select.single.SelectSqlGenerator;
import cn.zfz.pureorm.crud.select.single.SelectWrapper;
import cn.zfz.pureorm.crud.update.UpdateSqlGenerator;
import cn.zfz.pureorm.crud.upsert.LambadaUpsertWrapper;
import cn.zfz.pureorm.crud.upsert.UpsertSqlBuilder;
import cn.zfz.pureorm.crud.upsert.UpsertWrapper;
import cn.zfz.pureorm.dialect.Dialect;
import cn.zfz.pureorm.dialect.DialectFactory;
import cn.zfz.pureorm.enums.DbType;
import cn.zfz.pureorm.utils.ArrayUtils;
import cn.zfz.pureorm.utils.CollectionUtils;
import cn.zfz.pureorm.utils.ObjectUtils;

public class BaseMapperExecutor<T> implements BaseMapper<T> {

	private final DataSource dataSource;

	private final Class<T> entityClass;

	public BaseMapperExecutor(DataSource dataSource, Class<T> entityClass) {
		this.dataSource = dataSource;
		this.entityClass = entityClass;

	}

	@Override
	public long insert(T entity) {
		LambadaInsertWrapper<T> insertWrapper = LambadaInsertWrapper.of(entity, false);
		return insert(insertWrapper);
	}

	@Override
	public long insertNotNull(T entity) {
		LambadaInsertWrapper<T> insertWrapper = LambadaInsertWrapper.of(entity, true);
		return insert(insertWrapper);
	}

	private <W extends InsertWrapper<W, T>> long insert(InsertWrapper<W, T> insertWrapper) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = InsertSqlBuilder.buildSql(insertWrapper, dialect);
			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql(),
					Statement.RETURN_GENERATED_KEYS);

			bindParams(pstmt, sqlAndParams.getParams());
			int affectedRow = pstmt.executeUpdate();
			// 3. 获取自增主键
			Long generatedKey = getGeneratedKey(pstmt);
			if (ObjectUtils.isNotNull(generatedKey)) {
				return generatedKey;
			} else {
				return affectedRow;
			}
		} catch (SQLException e) {
			throw new PureOrmException("插入操作失败", e);
		}
	}

//	@Override
//	public int upsertNotNull(T entity) {
//		LambadaUpsertWrapper<T> upsertWrapper = LambadaUpsertWrapper.of(entity, true);
//		return upsert(upsertWrapper);
//	}

//	@Override
//	public int upsert(T entity) {
//		LambadaUpsertWrapper<T> upsertWrapper = LambadaUpsertWrapper.of(entity, false);
//		return upsert(upsertWrapper);
//	}
//
////	@Override
////	public List<Integer> upsertBatch(List<T> entities) {
////		if (CollectionUtils.isEmpty(entities)) {
////			throw new PureOrmException("插入操作失败，传入数据为空");
////		}
////		DbType dbType = getDbType(dataSource);
////		Dialect dialect = DialectDetector.detect(dbType);
////		try (Connection connection = dataSource.getConnection();) {
////			// 1. 拼接批量插入 SQL（单条 SQL 批量插入，性能最优）
////			UpsertWrapper firstWrapper = UpsertWrapper.create(entities.get(0));
////
////			String baseSql = dialect.handleUpsert(firstWrapper);
////			List<List<Object>> allParams = new ArrayList<>();
////			for (T entity : entities) {
////				allParams.add(UpsertWrapper.create(entity).getParams());
////			}
////
////			// 2. 执行批量插入
////			PreparedStatement pstmt = connection.prepareStatement(baseSql, Statement.RETURN_GENERATED_KEYS);
////			bindUpsertBatchParams(pstmt, allParams);
////			int[] affectedRowsArray = pstmt.executeBatch();
////			return ArrayUtils.toList(affectedRowsArray);
////		} catch (SQLException e) {
////			throw new PureOrmException("批量插入操作失败", e);
////		}
////
////	}
//
	private void bindInsertBatchParams(PreparedStatement pstmt, List<List<Object>> params) throws SQLException {
		List<Object> paramList = new ArrayList<>();
		for (List<Object> param : params) {
			paramList.addAll(param);
		}
		for (int i = 0; i < paramList.size(); i++) {
			pstmt.setObject(i + 1, paramList.get(i));
		}

	}
//
//	@Override
//	public <W extends UpdateWrapper<W, T>> int update(UpdateWrapper<W, T> updateWrapper) {
//		try (Connection connection = dataSource.getConnection();) {
//			// 强制校验 WHERE 条件，防止全表更新
//			if (!updateWrapper.hasCondition()) {
//				throw new PureOrmException("更新操作必须携带WHERE条件，禁止全表更新");
//			}
//			DbType dbType = getDbType(connection);
//			Dialect dialect = DialectFactory.detect(dbType);
//			SqlAndParams sqlAndParams = UpdateSqlGenerator.buildSql(updateWrapper, dialect);
//
//			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
//			bindParams(pstmt, sqlAndParams.getParams());
//			return pstmt.executeUpdate();
//		} catch (SQLException e) {
//			throw new PureOrmException("更新操作失败", e);
//		}
//	}
//
//	// ===================== 删除操作 =====================
//	@Override
//	public int delete(LambadaDeleteWrapper<T> deleteWrapper) {
//		// 强制校验 WHERE 条件，防止全表更新
//		if (!deleteWrapper.hasCondition()) {
//			throw new PureOrmException("删除操作必须携带WHERE条件，禁止全表删除");
//		}
//		try (Connection connection = dataSource.getConnection();) {
//			DbType dbType = getDbType(connection);
//			Dialect dialect = DialectFactory.detect(dbType);
//			SqlAndParams sqlAndParams = DeleteSqlGenerator.buildSql(deleteWrapper, dialect);
//
//			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
//			bindParams(pstmt, sqlAndParams.getParams());
//			return pstmt.executeUpdate();
//		} catch (SQLException e) {
//			throw new PureOrmException("删除操作失败", e);
//		}
//	}
//
////	@Override
////	public List<Integer> deleteBatch(List<DeleteWrapper<T>> deleteWrappers) {
////		if (CollectionUtils.isEmpty(deleteWrappers)) {
////			return Collections.emptyList();
////		}
////		try (Connection connection = dataSource.getConnection();) {
////
////			PreparedStatement pstmt = null;
////			List<Integer> results = new ArrayList<>();
////			for (DeleteWrapper<T> wrapper : deleteWrappers) {
////				if (!wrapper.hasWhereCondition()) {
////					throw new PureOrmException("批量删除中存在无WHERE条件的操作，禁止执行");
////				}
////				String sql = wrapper.getSql();
////				if (pstmt == null || !pstmt.toString().contains(sql)) {
////					if (pstmt != null) {
////						pstmt.close();
////					}
////					pstmt = connection.prepareStatement(sql);
////				}
////				pstmt.clearParameters();
////				bindParams(pstmt, wrapper.getParams());
////				pstmt.addBatch();
////			}
////			int[] affectedRowsArray = pstmt.executeBatch();
////			for (int rows : affectedRowsArray) {
////				results.add(rows);
////			}
////			if (pstmt != null) {
////				pstmt.close();
////			}
////			return results;
////		} catch (SQLException e) {
////			throw new PureOrmException("批量删除操作失败", e);
////		}
////	}
//
//	// ===================== 单表查询操作 =====================
////	@Override
////	public T selectOne(SelectWrapper<T> selectWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			// 单条查询自动拼接 LIMIT 1，防止返回多条结果
////			String sql = selectWrapper.getSql() + " LIMIT 1";
////			List<Object> params = selectWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(sql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			// 结果集映射：单条记录 → 实体对象
////			return rs.next() ? mapResultSetToEntity(rs) : null;
////		} catch (SQLException e) {
////			throw new PureOrmException("单条查询操作失败", e);
////		}
////	}
////
////	@Override
////	public List<T> selectList(SelectWrapper<T> selectWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			String sql = selectWrapper.getSql();
////			List<Object> params = selectWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(sql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			// 结果集映射：多条记录 → 实体列表
////			return mapResultSetToEntityList(rs);
////		} catch (SQLException e) {
////			throw new PureOrmException("列表查询操作失败", e);
////		}
////	}
////
////	@Override
////	public long selectCount(SelectWrapper<T> selectWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			// 拼接 COUNT 专用 SQL（Wrapper 已处理 COUNT 字段）
////			String countSql = selectWrapper.getCountSql();
////			List<Object> params = selectWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(countSql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			return rs.next() ? rs.getLong(1) : 0L;
////		} catch (SQLException e) {
////			throw new PureOrmException("计数查询操作失败", e);
////		}
////	}
////
////	@Override
////	public boolean exists(SelectWrapper<T> selectWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			// 拼接 EXISTS 专用 SQL，性能最优（无需返回实际数据）
////			String existsSql = selectWrapper.getExistsSql();
////			List<Object> params = selectWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(existsSql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			return rs.next() && rs.getBoolean(1);
////		} catch (SQLException e) {
////			throw new PureOrmException("存在性查询操作失败", e);
////		}
////	}
////
////	@Override
////	public Page<T> selectPage(SelectWrapper<T> selectWrapper, int pageNum, int pageSize) {
////		if (pageNum < 1 || pageSize < 1) {
////			throw new PureOrmException("分页参数不合法：pageNum≥1，pageSize≥1");
////		}
////		try (Connection connection = dataSource.getConnection();) {
////			// 1. 查询总条数（独立 COUNT SQL，保证性能）
////			long total = selectCount(selectWrapper);
////			if (total == 0) {
////				return Page.of(Collections.emptyList(), pageNum, pageSize, 0L);
////			}
////			// 2. 拼接分页 SQL（LIMIT + OFFSET，Wrapper 处理排序）
////			int offset = (pageNum - 1) * pageSize;
////			String pageSql = selectWrapper.getSql() + " LIMIT ? OFFSET ?";
////			List<Object> params = new ArrayList<>(selectWrapper.getParams());
////			params.add(pageSize);
////			params.add(offset);
////			// 3. 查询当前页数据
////
////			PreparedStatement pstmt = connection.prepareStatement(pageSql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			List<T> records = mapResultSetToEntityList(rs);
////			// 4. 封装分页结果（纯原生数据，无推导字段）
////			return Page.of(records, pageNum, pageSize, total);
////		} catch (SQLException e) {
////			throw new PureOrmException("分页查询操作失败", e);
////		}
////	}
////
////	// ===================== 加锁查询操作 =====================
////	@Override
////	public T selectOneForUpdate(SelectWrapper<T> selectWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			// 单行加锁：拼接 FOR UPDATE + LIMIT 1
////			String sql = selectWrapper.getSql() + " FOR UPDATE LIMIT 1";
////			List<Object> params = selectWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(sql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			return rs.next() ? mapResultSetToEntityList(rs).get(0) : null;
////		} catch (SQLException e) {
////			throw new PureOrmException("单条加锁查询操作失败", e);
////		}
////	}
////
////	@Override
////	public List<T> selectListForUpdate(SelectWrapper<T> selectWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			// 批量加锁：直接拼接 FOR UPDATE
////			String sql = selectWrapper.getSql() + " FOR UPDATE";
////			List<Object> params = selectWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(sql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			return mapResultSetToEntityList(rs);
////		} catch (SQLException e) {
////			throw new PureOrmException("批量加锁查询操作失败", e);
////		}
////	}
////
////	// ===================== 多表关联查询操作 =====================
////	@Override
////	public List<T> selectJoin(SelectJoinWrapper<T> selectJoinWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			String sql = selectJoinWrapper.getSql();
////			List<Object> params = selectJoinWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(sql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			// 多表结果映射：支持 DTO/VO（由 Wrapper 指定返回字段）
////			return mapResultSetToEntityList(rs);
////		} catch (SQLException e) {
////			throw new PureOrmException("多表关联查询操作失败", e);
////		}
////	}
////
////	@Override
////	public long selectJoinCount(SelectJoinWrapper<T> selectJoinWrapper) {
////		try (Connection connection = dataSource.getConnection();) {
////			String countSql = selectJoinWrapper.getCountSql();
////			List<Object> params = selectJoinWrapper.getParams();
////
////			PreparedStatement pstmt = connection.prepareStatement(countSql);
////			bindParams(pstmt, params);
////			ResultSet rs = pstmt.executeQuery();
////			return rs.next() ? rs.getLong(1) : 0L;
////		} catch (SQLException e) {
////			throw new PureOrmException("多表关联计数查询操作失败", e);
////		}
////	}
//
////	private void bindConditionParams(PreparedStatement pstmt, List<Condition> conditions) throws SQLException {
////		// 核心：JDBC参数索引从1开始，自增记录当前绑定位置
////		int paramIndex = 1;
////
////		// 按Condition顺序遍历，与SQL中?的顺序严格匹配
////		for (Condition condition : conditions) {
////			List<Object> params = condition.getParams();
////			// 跳过无参数的条件（ofNoParam/ofLogic/ofSymbol创建的条件均为此类）
////			if (CollectionUtils.isEmpty(params)) {
////				continue;
////			}
////			// 遍历当前Condition的参数列表，逐个绑定（适配IN/NOT IN多参数场景）
////			for (Object param : params) {
////				// setObject天然支持null值，无需额外判断，直接绑定
////				pstmt.setObject(paramIndex++, param);
////			}
////		}
////	}
//
//	// ===================== 私有工具方法：JDBC 核心操作 =====================
	/**
	 * 绑定单个 SQL 参数（基础版）
	 */
	private void bindParams(PreparedStatement pstmt, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			pstmt.setObject(i + 1, params.get(i));
		}
	}

	/**
	 * 获取单条插入自增主键
	 */
	private Long getGeneratedKey(PreparedStatement pstmt) throws SQLException {
		try (ResultSet rs = pstmt.getGeneratedKeys();) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			return null;
		} catch (Exception e) {
			throw new PureOrmException(e);
		}

	}

	/**
	 * 获取批量插入自增主键（按插入顺序）
	 */
	private List<Long> getBatchGeneratedKey(PreparedStatement pstmt) throws SQLException {
		List<Long> generatedIds = new ArrayList<>();
		try (ResultSet rs = pstmt.getGeneratedKeys();) {
			while (rs.next()) {
				generatedIds.add(rs.getLong(1));
			}
			return generatedIds;
		} catch (Exception e) {
			throw new PureOrmException(e);
		}

	}

	private DbType getDbType(Connection connection) {
		try {
			DatabaseMetaData meta = connection.getMetaData();
			String productName = meta.getDatabaseProductName().toLowerCase(); // 统一小写

			if (productName.contains("mysql")) {
				return DbType.MYSQL;
			}
			if (productName.contains("mariadb")) {
				return DbType.MYSQL;
			}
			if (productName.contains("oracle")) {
				return DbType.ORACLE;
			}
			if (productName.contains("postgresql")) {
				return DbType.POSTGRESQL;
			}
			// 重点：SQL Server 靠关键词识别，不硬匹配全名！
			if (productName.contains("sql server")) {
				return DbType.SQL_SERVER;
			}
			throw new UnsupportedOperationException("不支持的数据库: ");
		} catch (Exception e) {
			throw new PureOrmException(e);
		}

	}
//
//	@Override
//	public List<Integer> upsertBatch(List<T> entities) {
//		return upsertBatch(entities, 500);
//	}
//
//	private void bindUpsertBatchParams(PreparedStatement pstmt, List<SqlAndParams> list) throws SQLException {
//		// 循环每一组参数
//		for (SqlAndParams sqlAndParams : list) {
//			// 清空上一次设置的参数（可省略，但更稳）
//			pstmt.clearParameters();
//			// 给每个 ? 设值
//			for (int i = 0; i < sqlAndParams.getParams().size(); i++) {
//				pstmt.setObject(i + 1, sqlAndParams.getParams().get(i));
//			}
//			// 加入批次（关键！）
//			pstmt.addBatch();
//		}
//	}
//
//	@Override
//	public int deleteByPrimaryKey(Serializable primaryKey) {
//		try (Connection connection = dataSource.getConnection()) {
//			LambadaDeleteWrapper<T> deleteWrapper = new LambadaDeleteWrapper<>();
//			EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
//			String tableName = entityMeta.getTableName();
//			String columnName = entityMeta.getPrimaryKeyField().getColumnName();
//			deleteWrapper.eqPrimaryKey(tableName,columnName, primaryKey);
//			DbType dbType = getDbType(connection);
//			Dialect dialect = DialectFactory.detect(dbType);
//			SqlAndParams sqlAndParams = DeleteSqlGenerator.buildSql(deleteWrapper, dialect);
//
//			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
//			bindParams(pstmt, sqlAndParams.getParams());
//			return pstmt.executeUpdate();
//		} catch (Exception e) {
//			throw new PureOrmException("删除操作失败", e);
//		}
//
//	}
//
//	@Override
//	public T selectByPrimaryKey(Serializable primaryKey) {
//		try (Connection connection = dataSource.getConnection()) {
//			LambadaSelectWrapper<T> selectWrapper = new LambadaSelectWrapper<>();
//			EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
//			String tableName = entityMeta.getTableName();
//			String columnName = entityMeta.getPrimaryKeyField().getColumnName();
//			selectWrapper.eqPrimaryKey(tableName,columnName, primaryKey);
//			DbType dbType = getDbType(connection);
//			Dialect dialect = DialectFactory.detect(dbType);
//			SqlAndParams sqlAndParams = SelectSqlGenerator.buildSql(selectWrapper, dialect);
//			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
//			bindParams(pstmt, sqlAndParams.getParams());
//			ResultSet resultSet = pstmt.executeQuery();
//			return ResultSetMapper.map(resultSet, entityClass).get(0);
//		} catch (Exception e) {
//			throw new PureOrmException("删除操作失败", e);
//		}
//	}
//

//
//	@Override
//	public <W extends UpsertWrapper<W, T>>  int upsert(UpsertWrapper<W,T> upsertWrapper) {
//		try (Connection connection = dataSource.getConnection()) {
//			DbType dbType = getDbType(connection);
//			Dialect dialect = DialectFactory.detect(dbType);
//			SqlAndParams sqlAndParams = UpsertSqlBuilder.buildSql(upsertWrapper, dialect);
//			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
//
//			bindParams(pstmt, sqlAndParams.getParams());
//			int affectedRow = pstmt.executeUpdate();
//
//			return affectedRow;
//
//		} catch (SQLException e) {
//			throw new PureOrmException("插入操作失败", e);
//		}
//	}
//
//	
//
//	@Override
//	public List<Integer> upsertBatch(List<T> entities, int batchSize) {
//		Connection connection = null;
//		boolean autoCommit = false;
//		try {
//			connection = dataSource.getConnection();
//			autoCommit = connection.getAutoCommit();
//			connection.setAutoCommit(false);
//			DbType dbType = getDbType(connection);
//			Dialect dialect = DialectFactory.detect(dbType);
//			List<SqlAndParams> list = entities.stream().map(entity -> {
//				LambadaUpsertWrapper<T> upsertWrapper = new LambadaUpsertWrapper<>(entity);
//				SqlAndParams sqlAndParams = UpsertSqlBuilder.buildSql(upsertWrapper, dialect);
//				return sqlAndParams;
//			}).collect(Collectors.toList());
//			PreparedStatement pstmt = connection.prepareStatement(list.get(0).getSql());
//			bindUpsertBatchParams(pstmt, list);
//			int[] affectedRows = pstmt.executeBatch();
//			connection.commit();
//			return ArrayUtils.toList(affectedRows);
//		} catch (SQLException e) {
//			try {
//				connection.rollback();
//			} catch (SQLException e1) {
//				throw new PureOrmException("插入操作失败", e1);
//			}
//			throw new PureOrmException("插入操作失败", e);
//		} finally {
//			// 恢复自动提交
//			try {
//				connection.setAutoCommit(autoCommit);
//				connection.close();
//			} catch (SQLException e) {
//				throw new PureOrmException("插入操作失败", e);
//			}
//		}
//		
//	}
//
//	
//	@Override
//	public List<Integer> deleteByPrimaryKeys(List<Serializable> primaryKeys) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	
//
//	@Override
//	public List<T> selectByPrimaryKeyList(List<Serializable> primaryKeys) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//

//

//
////	@Override
////	public List<Long> batchInsert(List<InsertWrapper<?, ?>> insertWrappers) {
////		return batchInsert(insertWrappers, 500);
////	}
//

//
////	@Override
////	public <W extends InsertWrapper<W, T>> List<Long> insertBatchWrapper(List<InsertWrapper<W, T>> insertWrappers,
////			int batchSize) {
////		// TODO Auto-generated method stub
////		return null;
////	}
//
////	@Override
////	public List<Long> insertBatch(List<T> entities, int batchSize) {
////		List<InsertWrapper<LambadaInsertWrapper<T>, T>> insertWrappers = entities.stream()
////				.map(entity -> LambadaInsertWrapper.of(entity, false))
////				// 显式指定类型，让集合泛型匹配
////				.map(wrapper -> (InsertWrapper<LambadaInsertWrapper<T>, T>) wrapper).collect(Collectors.toList());
////		return insertBatchWrapper(insertWrappers, batchSize);
////	}
////
////	@Override
////	public List<Long> insertBatchNotNull(List<T> entities, int batchSize) {
////		List<InsertWrapper<LambadaInsertWrapper<T>, T>> insertWrappers = entities.stream()
////				.map(entity -> LambadaInsertWrapper.of(entity, true))
////				// 显式指定类型，让集合泛型匹配
////				.map(wrapper -> (InsertWrapper<LambadaInsertWrapper<T>, T>) wrapper).collect(Collectors.toList());
////		return insertBatchWrapper(insertWrappers, batchSize);
////	}
////
////	@Override
////	public <W extends InsertWrapper<W, T>> List<Long> insertBatchWrapper(List<InsertWrapper<W, T>> insertWrappers) {
////		return insertBatchWrapper(insertWrappers, 500);
////	}
////
////	@Override
////	public List<Long> insertBatchNotNull(List<T> entities) {
////		return insertBatchNotNull(entities, 500);
////	}
////
////	@Override
////	public List<Long> insertBatch(List<T> entities) {
////		return insertBatch(entities, 500);
////	}
//
//	@Override
//	public <W extends UpdateWrapper<W, T>> List<Integer> batchUpdate(List<UpdateWrapper<W, T>> updateWrappers) {
//		return batchUpdate(updateWrappers, 500);
//	}
//
//	@Override
//	public <W extends UpdateWrapper<W, T>> List<Integer> batchUpdate(List<UpdateWrapper<W, T>> updateWrappers,
//			int batchSize) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <W extends SelectWrapper<W, T>> T selectOne(SelectWrapper<W, T> selectWrapper) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <W extends SelectWrapper<W, T>> long count(SelectWrapper<W, T> selectWrapper) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public <W extends SelectWrapper<W, T>> boolean exists(SelectWrapper<W, T> selectWrapper) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public <W extends SelectWrapper<W, T>> Page<T> selectPage(SelectWrapper<W, T> selectWrapper, int pageNum,
//			int pageSize) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <W extends SelectWrapper<W, T>> List<T> selectList(SelectWrapper<W, T> selectWrapper) {
//		try (Connection connection = dataSource.getConnection()) {
//
//			DbType dbType = getDbType(connection);
//			Dialect dialect = DialectFactory.detect(dbType);
//			SqlAndParams sqlAndParams = SelectSqlGenerator.buildSql(selectWrapper, dialect);
//
//			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
//			bindParams(pstmt, sqlAndParams.getParams());
//			ResultSet resultSet = pstmt.executeQuery();
//			return ResultSetMapper.map(resultSet, entityClass);
//		} catch (Exception e) {
//			throw new PureOrmException("删除操作失败", e);
//		}
//
//	}

	@Override
	public List<Long> insertBatch(List<T> entities) {
		return insertBatch(entities, 500);
	}

	@Override
	public List<Long> insertBatch(List<T> entities, int batchSize) {

		try (Connection connection = dataSource.getConnection();) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			LambadaInsertBatchWrapper<T> batchWrapper = LambadaInsertBatchWrapper.of(entities);
			if (dbType == DbType.ORACLE) {
				List<Long> returns = new ArrayList<>();
				List<SqlAndParams> sqlAndParamsList = InsertBatchSqlBuilder.buildBatchInsertSql(batchWrapper, batchSize,
						dialect);
				for (SqlAndParams sqlAndParams : sqlAndParamsList) {
					try (PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql(),
							Statement.RETURN_GENERATED_KEYS)) {
						bindParams(pstmt, sqlAndParams.getParams());
						int affectedRow = pstmt.executeUpdate();

						Long generatedKey = getGeneratedKey(pstmt);
						if (ObjectUtils.isNotNull(generatedKey)) {
							returns.add(generatedKey);
						} else {
							returns.add((long) affectedRow);
						}
					} catch (Exception e) {
						throw new PureOrmException(e);
					}

				}
				return returns;
			} else {
				// 1. 拼接批量插入 SQL（单条 SQL 批量插入，性能最优）
				SqlAndParams sqlAndParams = InsertBatchSqlBuilder.buildInsertBatchSql(batchWrapper, dialect);
				List<List<Object>> params = batchWrapper.getColumnValuesList();
				// 2. 执行批量插入
				PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql(),
						Statement.RETURN_GENERATED_KEYS);
				bindInsertBatchParams(pstmt, params);
				int[] affectedRows = pstmt.executeBatch();
				List<Long> affectedRowList = new ArrayList<>();
				for (int affectedRow : affectedRows) {
					affectedRowList.add(Long.valueOf(affectedRow));
				}
				// 3. 获取批量自增主键（按插入顺序）
				List<Long> batchGeneratedKeys = getBatchGeneratedKey(pstmt);
				if (CollectionUtils.isEmpty(batchGeneratedKeys)) {
					return affectedRowList;
				} else {
					return batchGeneratedKeys;
				}
			}

		} catch (SQLException e) {
			throw new PureOrmException("批量插入操作失败", e);
		}
	}


}