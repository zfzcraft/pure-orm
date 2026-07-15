package cn.zfz.pureorm.core;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import javax.sql.DataSource;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.crud.batch.InsertBatchSqlBuilder;
import cn.zfz.pureorm.crud.batch.LambadaInsertBatchWrapper;
import cn.zfz.pureorm.crud.delete.DeleteSqlGenerator;
import cn.zfz.pureorm.crud.delete.LambadaDeleteWrapper;
import cn.zfz.pureorm.crud.insert.InsertSqlBuilder;
import cn.zfz.pureorm.crud.insert.InsertWrapper;
import cn.zfz.pureorm.crud.insert.LambadaInsertWrapper;
import cn.zfz.pureorm.crud.select.single.LambadaSelectWrapper;
import cn.zfz.pureorm.crud.select.single.ResultSetMapper;
import cn.zfz.pureorm.crud.select.single.SelectSqlGenerator;
import cn.zfz.pureorm.crud.select.single.SelectWrapper;
import cn.zfz.pureorm.crud.update.LambadaUpdateWrapper;
import cn.zfz.pureorm.crud.update.UpdateSqlGenerator;
import cn.zfz.pureorm.crud.update.UpdateWrapper;
import cn.zfz.pureorm.crud.upsert.LambadaUpsertWrapper;
import cn.zfz.pureorm.crud.upsert.UpsertSqlBuilder;
import cn.zfz.pureorm.dialect.Dialect;
import cn.zfz.pureorm.dialect.DialectFactory;
import cn.zfz.pureorm.enums.DbType;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;
import cn.zfz.pureorm.utils.CollectionUtils;
import cn.zfz.pureorm.utils.ObjectUtils;

public class BaseMapperExecutor<T> implements BaseMapper<T> {

	private final DataSource dataSource;
	private final Class<T> entityClass;

	public BaseMapperExecutor(DataSource dataSource, Class<T> entityClass) {
		this.dataSource = dataSource;
		this.entityClass = entityClass;
	}

	// ===================== 插入操作 =====================

	@Override
	public long insert(T entity) {
		long id = insert(LambadaInsertWrapper.of(entity, false));
		setPrimaryKeyIfNecessary(entity, id);
		return id;
	}

	@Override
	public long insertNotNull(T entity) {
		long id = insert(LambadaInsertWrapper.of(entity, true));
		setPrimaryKeyIfNecessary(entity, id);
		return id;
	}

	private void setPrimaryKeyIfNecessary(T entity, long id) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta pkField = entityMeta.getPrimaryKeyField();
		if (pkField != null && pkField.getFieldKind() == FieldKind.PRIMARY_KEY_AUTO_INCREMENT) {
			try {
				Field field = pkField.getField();
				field.setAccessible(true);
				Object currentValue = field.get(entity);
				if (currentValue == null) {
					Class<?> fieldType = field.getType();
					if (fieldType == Long.class || fieldType == long.class) {
						field.set(entity, id);
					} else if (fieldType == Integer.class || fieldType == int.class) {
						field.set(entity, (int) id);
					} else if (fieldType == Short.class || fieldType == short.class) {
						field.set(entity, (short) id);
					}
				}
			} catch (IllegalAccessException e) {
				throw new PureOrmException("设置主键失败", e);
			}
		}
	}

	public <W extends InsertWrapper<W, T>> long insert(InsertWrapper<W, T> insertWrapper) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = InsertSqlBuilder.buildSql(insertWrapper, dialect);
			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql(),
					Statement.RETURN_GENERATED_KEYS);
			bindParams(pstmt, sqlAndParams.getParams());
			int affectedRow = pstmt.executeUpdate();
			Long generatedKey = getGeneratedKey(pstmt);
			return ObjectUtils.isNotNull(generatedKey) ? generatedKey : affectedRow;
		} catch (SQLException e) {
			throw new PureOrmException("插入操作失败", e);
		}
	}

	@Override
	public List<Long> insertBatch(List<T> entities) {
		return insertBatch(entities, 500);
	}

	@Override
	public List<Long> insertBatch(List<T> entities, int batchSize) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			LambadaInsertBatchWrapper<T> batchWrapper = LambadaInsertBatchWrapper.of(entities);

			if (dbType == DbType.ORACLE) {
				List<Long> returns = new ArrayList<>();
				List<SqlAndParams> sqlAndParamsList = InsertBatchSqlBuilder.buildBatchInsertSql(batchWrapper,
						batchSize, dialect);
				for (SqlAndParams sqlAndParams : sqlAndParamsList) {
					try (PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql(),
							Statement.RETURN_GENERATED_KEYS)) {
						bindParams(pstmt, sqlAndParams.getParams());
						int affectedRow = pstmt.executeUpdate();
						Long generatedKey = getGeneratedKey(pstmt);
						returns.add(ObjectUtils.isNotNull(generatedKey) ? generatedKey : (long) affectedRow);
					} catch (Exception e) {
						throw new PureOrmException(e);
					}
				}
				return returns;
			} else {
				SqlAndParams sqlAndParams = InsertBatchSqlBuilder.buildInsertBatchSql(batchWrapper, dialect);
				List<List<Object>> params = batchWrapper.getColumnValuesList();
				PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql(),
						Statement.RETURN_GENERATED_KEYS);
				bindInsertBatchParams(pstmt, params);
				int[] affectedRows = pstmt.executeBatch();
				List<Long> affectedRowList = new ArrayList<>();
				for (int affectedRow : affectedRows) {
					affectedRowList.add((long) affectedRow);
				}
				List<Long> batchGeneratedKeys = getBatchGeneratedKey(pstmt);
				return CollectionUtils.isEmpty(batchGeneratedKeys) ? affectedRowList : batchGeneratedKeys;
			}
		} catch (SQLException e) {
			throw new PureOrmException("批量插入操作失败", e);
		}
	}

	private void bindInsertBatchParams(PreparedStatement pstmt, List<List<Object>> params) throws SQLException {
		List<Object> paramList = new ArrayList<>();
		for (List<Object> param : params) {
			paramList.addAll(param);
		}
		for (int i = 0; i < paramList.size(); i++) {
			pstmt.setObject(i + 1, paramList.get(i));
		}
	}

	// ===================== 更新操作 =====================

	@Override
	public <W extends UpdateWrapper<W, T>> int update(W updateWrapper) {
		if (!updateWrapper.hasCondition()) {
			throw new PureOrmException("更新操作必须携带WHERE条件，禁止全表更新");
		}
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = UpdateSqlGenerator.buildSql(updateWrapper, dialect);
			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
			bindParams(pstmt, sqlAndParams.getParams());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new PureOrmException("更新操作失败", e);
		}
	}

	@Override
	public int updateByPrimaryKey(T entity) {
		return updateByPrimaryKey(entity, false);
	}

	@Override
	public int updateNotNullByPrimaryKey(T entity) {
		return updateByPrimaryKey(entity, true);
	}

	private int updateByPrimaryKey(T entity, boolean skipNullFields) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta primaryKeyField = entityMeta.getPrimaryKeyField();
		if (primaryKeyField == null) {
			throw new PureOrmException("实体类没有主键字段，无法使用updateByPrimaryKey");
		}

		Object primaryKeyValue;
		try {
			primaryKeyValue = primaryKeyField.getField().get(entity);
		} catch (IllegalAccessException e) {
			throw new PureOrmException("获取主键值失败", e);
		}
		if (primaryKeyValue == null) {
			throw new PureOrmException("主键值不能为空");
		}

		LambadaUpdateWrapper<T> updateWrapper = buildUpdateWrapper(entity, entityMeta, skipNullFields);
		updateWrapper.eqPrimaryKey(entityMeta.getTableName(), primaryKeyField.getColumnName(),
				(Serializable) primaryKeyValue);
		return update(updateWrapper);
	}

	private LambadaUpdateWrapper<T> buildUpdateWrapper(T entity, EntityMeta entityMeta, boolean skipNullFields) {
		LambadaUpdateWrapper<T> wrapper = new LambadaUpdateWrapper<>();
		wrapper.setTableName(entityMeta.getTableName());

		List<FieldMeta> fieldList = entityMeta.getFieldList();
		for (FieldMeta fieldMeta : fieldList) {
			if (fieldMeta.getFieldKind() == FieldKind.PRIMARY_KEY
					|| fieldMeta.getFieldKind() == FieldKind.PRIMARY_KEY_AUTO_INCREMENT) {
				continue;
			}
			try {
				Object value = fieldMeta.getField().get(entity);
				if (skipNullFields && value == null) {
					continue;
				}
				if (fieldMeta.getFieldKind() == FieldKind.ENUM && value != null) {
					EnumTypeHandler enumTypeHandler = fieldMeta.getEnumTypeHandler();
					Enum<?> enumObject = (Enum<?>) value;
					wrapper.setColumn(fieldMeta.getColumnName(), enumTypeHandler.toDatabase(enumObject));
				} else {
					wrapper.setColumn(fieldMeta.getColumnName(), value);
				}
			} catch (IllegalAccessException e) {
				throw new PureOrmException("获取字段值失败", e);
			}
		}

		if (wrapper.getUpdateNodes().isEmpty()) {
			throw new PureOrmException("没有需要更新的字段");
		}
		return wrapper;
	}

	// ===================== 删除操作 =====================

	@Override
	public int delete(LambadaDeleteWrapper<T> deleteWrapper) {
		if (!deleteWrapper.hasCondition()) {
			throw new PureOrmException("删除操作必须携带WHERE条件，禁止全表删除");
		}
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = DeleteSqlGenerator.buildSql(deleteWrapper, dialect);
			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
			bindParams(pstmt, sqlAndParams.getParams());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new PureOrmException("删除操作失败", e);
		}
	}

	@Override
	public int deleteByPrimaryKey(Serializable primaryKey) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta primaryKeyField = entityMeta.getPrimaryKeyField();
		if (primaryKeyField == null) {
			throw new PureOrmException("实体类没有主键字段，无法使用deleteByPrimaryKey");
		}
		LambadaDeleteWrapper<T> deleteWrapper = new LambadaDeleteWrapper<>();
		deleteWrapper.eqPrimaryKey(entityMeta.getTableName(), primaryKeyField.getColumnName(), primaryKey);
		return delete(deleteWrapper);
	}

	@Override
	public int deleteByPrimaryKeys(List<Serializable> primaryKeys) {
		if (CollectionUtils.isEmpty(primaryKeys)) {
			return 0;
		}
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta primaryKeyField = entityMeta.getPrimaryKeyField();
		if (primaryKeyField == null) {
			throw new PureOrmException("实体类没有主键字段，无法使用deleteByPrimaryKeys");
		}

		LambadaDeleteWrapper<T> deleteWrapper = new LambadaDeleteWrapper<>();
		deleteWrapper.inPrimaryKeys(entityMeta.getTableName(), primaryKeyField.getColumnName(), primaryKeys.toArray());
		return delete(deleteWrapper);
	}

	// ===================== Upsert 操作 =====================

	@Override
	public long upsert(T entity) {
		return upsert(LambadaUpsertWrapper.of(entity, false));
	}

	@Override
	public long upsertNotNull(T entity) {
		return upsert(LambadaUpsertWrapper.of(entity, true));
	}

	@Override
	public int upsert(LambadaUpsertWrapper<T> upsertWrapper) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = UpsertSqlBuilder.buildSql(upsertWrapper, dialect);
			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
			bindParams(pstmt, sqlAndParams.getParams());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new PureOrmException("Upsert操作失败", e);
		}
	}

	// ===================== 查询操作 =====================

	@Override
	public <W extends SelectWrapper<W, T>> T selectOne(W selectWrapper) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			selectWrapper.limit(1);
			SqlAndParams sqlAndParams = SelectSqlGenerator.buildSql(selectWrapper, dialect);
			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
			bindParams(pstmt, sqlAndParams.getParams());
			ResultSet rs = pstmt.executeQuery();
			List<T> results = ResultSetMapper.map(rs, entityClass);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException e) {
			throw new PureOrmException("单条查询操作失败", e);
		}
	}

	@Override
	public T selectByPrimaryKey(Serializable primaryKey) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta primaryKeyField = entityMeta.getPrimaryKeyField();
		if (primaryKeyField == null) {
			throw new PureOrmException("实体类没有主键字段，无法使用selectByPrimaryKey");
		}
		LambadaSelectWrapper<T> selectWrapper = new LambadaSelectWrapper<>();
		selectWrapper.eqPrimaryKey(entityMeta.getTableName(), primaryKeyField.getColumnName(), primaryKey);
		return selectOne(selectWrapper);
	}

	@Override
	public List<T> selectByPrimaryKeyList(List<Serializable> primaryKeys) {
		if (CollectionUtils.isEmpty(primaryKeys)) {
			return Collections.emptyList();
		}
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta primaryKeyField = entityMeta.getPrimaryKeyField();
		if (primaryKeyField == null) {
			throw new PureOrmException("实体类没有主键字段，无法使用selectByPrimaryKeyList");
		}
		LambadaSelectWrapper<T> selectWrapper = new LambadaSelectWrapper<>();
		selectWrapper.inPrimaryKeys(entityMeta.getTableName(), primaryKeyField.getColumnName(), primaryKeys.toArray());
		return selectList(selectWrapper);
	}

	@Override
	public <W extends SelectWrapper<W, T>> List<T> selectList(W selectWrapper) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = SelectSqlGenerator.buildSql(selectWrapper, dialect);
			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
			bindParams(pstmt, sqlAndParams.getParams());
			ResultSet resultSet = pstmt.executeQuery();
			return ResultSetMapper.map(resultSet, entityClass);
		} catch (Exception e) {
			throw new PureOrmException("列表查询操作失败", e);
		}
	}

	@Override
	public <W extends SelectWrapper<W, T>> long count(W selectWrapper) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = SelectSqlGenerator.buildSql(selectWrapper, dialect);
			String countSql = "SELECT COUNT(*) FROM (" + sqlAndParams.getSql() + ") AS _count_table";
			PreparedStatement pstmt = connection.prepareStatement(countSql);
			bindParams(pstmt, sqlAndParams.getParams());
			ResultSet rs = pstmt.executeQuery();
			return rs.next() ? rs.getLong(1) : 0L;
		} catch (SQLException e) {
			throw new PureOrmException("计数查询操作失败", e);
		}
	}

	@Override
	public <W extends SelectWrapper<W, T>> boolean exists(W selectWrapper) {
		return count(selectWrapper) > 0;
	}

	@Override
	public <W extends SelectWrapper<W, T>> Page<T> selectPage(W selectWrapper, int pageNum, int pageSize) {
		if (pageNum < 1 || pageSize < 1) {
			throw new PureOrmException("分页参数不合法：pageNum≥1，pageSize≥1");
		}
		try (Connection connection = dataSource.getConnection()) {
			long total = count(selectWrapper);
			if (total == 0) {
				return Page.of(Collections.emptyList(), pageNum, pageSize, 0L);
			}
			int offset = (pageNum - 1) * pageSize;
			selectWrapper.limit(pageSize).offset(offset);
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = SelectSqlGenerator.buildSql(selectWrapper, dialect);

			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
			bindParams(pstmt, sqlAndParams.getParams());
			ResultSet rs = pstmt.executeQuery();
			List<T> records = ResultSetMapper.map(rs, entityClass);
			return Page.of(records, pageNum, pageSize, total);
		} catch (SQLException e) {
			throw new PureOrmException("分页查询操作失败", e);
		}
	}

	// ===================== 私有工具方法 =====================

	private void bindParams(PreparedStatement pstmt, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			pstmt.setObject(i + 1, params.get(i));
		}
	}

	private Long getGeneratedKey(PreparedStatement pstmt) throws SQLException {
		try (ResultSet rs = pstmt.getGeneratedKeys()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			return null;
		} catch (Exception e) {
			throw new PureOrmException(e);
		}
	}

	private List<Long> getBatchGeneratedKey(PreparedStatement pstmt) throws SQLException {
		List<Long> generatedIds = new ArrayList<>();
		try (ResultSet rs = pstmt.getGeneratedKeys()) {
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
			String productName = meta.getDatabaseProductName().toLowerCase();

			if (productName.contains("mysql") || productName.contains("mariadb")) {
				return DbType.MYSQL;
			}
			if (productName.contains("h2")) {
				return DbType.H2;
			}
			if (productName.contains("oracle")) {
				return DbType.ORACLE;
			}
			if (productName.contains("postgresql")) {
				return DbType.POSTGRESQL;
			}
			if (productName.contains("sql server")) {
				return DbType.SQL_SERVER;
			}
			throw new UnsupportedOperationException("不支持的数据库: " + productName);
		} catch (Exception e) {
			throw new PureOrmException(e);
		}
	}


}
