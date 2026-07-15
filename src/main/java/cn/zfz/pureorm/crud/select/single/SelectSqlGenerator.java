package cn.zfz.pureorm.crud.select.single;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.condition.ConditionSqlBuilder;
import cn.zfz.pureorm.dialect.Dialect;
import cn.zfz.pureorm.enums.LockMode;

public class SelectSqlGenerator {

	public static SqlAndParams buildSql(SelectWrapper<?, ?> wrapper, Dialect dialect) {
		StringBuilder sql = new StringBuilder("SELECT ");
		List<Object> params = new ArrayList<>();

		// 1. 查询字段
		List<String> selectFields = wrapper.getSelectFields();
		if (selectFields.isEmpty()) {
			sql.append("*");
		} else {
			for (int i = 0; i < selectFields.size(); i++) {
				if (i > 0)
					sql.append(", ");
				sql.append(dialect.wrap(selectFields.get(i)));
			}
		}

		// 2. 表名
		sql.append(" FROM ").append(dialect.wrap(wrapper.getTableName()));

		// 3. WHERE 条件
		SqlAndParams where = ConditionSqlBuilder.buildWhere(wrapper.getConditionNodes());
		if (!where.getSql().isEmpty()) {
			sql.append(" WHERE ").append(where.getSql());
			params.addAll(where.getParams());
		}

		// 4. GROUP BY
		List<String> groupBys = wrapper.getGroupBys();
		if (!groupBys.isEmpty()) {
			sql.append(" GROUP BY ");
			for (int i = 0; i < groupBys.size(); i++) {
				if (i > 0) {
					sql.append(", ");
				}
				sql.append(dialect.wrap(groupBys.get(i)));
			}
		}

		// 5. HAVING
		SqlAndParams having = wrapper.getHaving();
		if (having != null && !having.getSql().isEmpty()) {
			sql.append(" HAVING ").append(having.getSql());
			params.addAll(having.getParams());
		}

		// 6. ORDER BY
		List<OrderBy> orderByList = wrapper.getOrderBys();
		if (!orderByList.isEmpty()) {
			sql.append(" ORDER BY ");
			for (int i = 0; i < orderByList.size(); i++) {
				if (i > 0) {
					sql.append(", ");
				}
				sql.append(dialect.wrap(orderByList.get(i).getName()));
				sql.append(" ");
				sql.append(orderByList.get(i).getOrder().name());
			}
		}

		// 7. 分页（使用方言生成，参数由方言决定顺序）
		if (wrapper.getOffset() != null && wrapper.getLimit() != null) {
			SqlAndParams pageSqlAndParams = dialect.buildPageSql(sql.toString(), wrapper.getOffset(), wrapper.getLimit());
			sql = new StringBuilder(pageSqlAndParams.getSql());
			params.addAll(pageSqlAndParams.getParams());
		}

		// 8. 锁
		if (wrapper.getLockMode() != null) {
			if (wrapper.getLockMode() == LockMode.FOR_UPDATE) {
				sql.append(dialect.forUpdate());
			} else if (wrapper.getLockMode() == LockMode.FOR_SHARE) {
				sql.append(dialect.forShare());
			}
		}

		return new SqlAndParams(sql.toString(), params);
	}

}
