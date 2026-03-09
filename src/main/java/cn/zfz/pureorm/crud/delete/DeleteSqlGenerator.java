package cn.zfz.pureorm.crud.delete;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.condition.ConditionSqlBuilder;
import cn.zfz.pureorm.dialect.Dialect;

public class DeleteSqlGenerator {

	public static SqlAndParams buildSql(LambadaDeleteWrapper<?> wrapper, Dialect dialect) {
		StringBuilder sql = new StringBuilder("DELETE FROM ").append(dialect.wrap(wrapper.getTableName()));
		List<Object> params = new ArrayList<>();
		SqlAndParams where = ConditionSqlBuilder.buildWhere(wrapper.getConditionNodes());
		if (!where.getSql().isEmpty()) {
			sql.append(" WHERE ").append(where.getSql());
			params.addAll(where.getParams());
		}

		return new SqlAndParams(sql.toString(), params);
	}
}
