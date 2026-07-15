package cn.zfz.pureorm.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import cn.zfz.pureorm.crud.select.highlevel.HighLevelSelectWrapper;
import cn.zfz.pureorm.crud.select.highlevel.HighLevelSqlGenerator;
import cn.zfz.pureorm.dialect.Dialect;
import cn.zfz.pureorm.dialect.DialectFactory;
import cn.zfz.pureorm.enums.DbType;

public class HighLevelMapper {

	private final DataSource dataSource;

	public HighLevelMapper(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public static HighLevelMapper of(DataSource dataSource) {
		return new HighLevelMapper(dataSource);
	}

	public <T> List<T> selectList(HighLevelSelectWrapper wrapper, Class<T> resultType) {
		try (Connection connection = dataSource.getConnection()) {
			DbType dbType = getDbType(connection);
			Dialect dialect = DialectFactory.detect(dbType);
			SqlAndParams sqlAndParams = HighLevelSqlGenerator.buildSql(wrapper, dialect);

			PreparedStatement pstmt = connection.prepareStatement(sqlAndParams.getSql());
			bindParams(pstmt, sqlAndParams.getParams());
			ResultSet rs = pstmt.executeQuery();
			return AutoEntityMapper.map(rs, resultType, wrapper.getRootClass());
		} catch (Exception e) {
			throw new PureOrmException("高级查询失败", e);
		}
	}

	public <T> T selectOne(HighLevelSelectWrapper wrapper, Class<T> resultType) {
		List<T> list = selectList(wrapper, resultType);
		return list.isEmpty() ? null : list.get(0);
	}

	private void bindParams(PreparedStatement pstmt, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			pstmt.setObject(i + 1, params.get(i));
		}
	}

	private DbType getDbType(Connection connection) {
		try {
			String productName = connection.getMetaData().getDatabaseProductName().toLowerCase();
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
