package cn.zfz.pureorm.crud.select.highlevel;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.PureOrmException;
import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.select.single.OrderByType;
import cn.zfz.pureorm.dialect.Dialect;
import cn.zfz.pureorm.enums.Op;

public class HighLevelSqlGenerator {

	public static SqlAndParams buildSql(HighLevelSelectWrapper wrapper, Dialect dialect) {
		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		buildSelect(wrapper, sql, dialect);
		buildFrom(wrapper, sql, dialect);
		buildJoin(wrapper, sql, params, dialect);
		buildWhere(wrapper, sql, params, dialect);
		buildGroupBy(wrapper, sql, dialect);
		buildOrderBy(wrapper, sql, dialect);

		if (wrapper.getOffset() != null && wrapper.getLimit() != null) {
			SqlAndParams pageSql = dialect.buildPageSql(sql.toString(), wrapper.getOffset(), wrapper.getLimit());
			sql = new StringBuilder(pageSql.getSql());
			params.addAll(pageSql.getParams());
		}

		return new SqlAndParams(sql.toString(), params);
	}

	private static void buildSelect(HighLevelSelectWrapper wrapper, StringBuilder sql, Dialect dialect) {
		sql.append("SELECT ");
		List<SelectColumn> fields = wrapper.getSelectFields();

		if (fields.isEmpty()) {
			selectAllColumns(wrapper, sql, dialect);
			return;
		}

		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			SelectColumn f = fields.get(i);
			String tableAlias = f.getTableName();

			if (f.getSelectType() == SelectType.COLUMN) {
				if ("*".equals(f.getColumnName())) {
					appendAllColumns(wrapper, tableAlias, sql, dialect);
				} else {
					String columnExpr = tableAlias + "." + dialect.wrap(f.getColumnName());
					String asName = tableAlias + "." + f.getColumnName();
					sql.append(columnExpr).append(" AS ").append(quoteAlias(asName));
				}
			} else {
				String func = f.getSelectType().name();
				String columnExpr = tableAlias + "." + dialect.wrap(f.getColumnName());
				sql.append(func).append("(").append(columnExpr).append(")");
				if (f.getAlias() != null && !f.getAlias().isEmpty()) {
					sql.append(" AS ").append(quoteAlias(f.getAlias()));
				}
			}
		}
	}

	private static void selectAllColumns(HighLevelSelectWrapper wrapper, StringBuilder sql, Dialect dialect) {
		appendAllColumns(wrapper, wrapper.getRootAlias(), sql, dialect);
		for (JoinNode join : wrapper.getJoins()) {
			sql.append(", ");
			appendAllColumns(wrapper, join.getAlias(), sql, dialect);
		}
	}

	private static void appendAllColumns(HighLevelSelectWrapper wrapper, String alias, StringBuilder sql,
			Dialect dialect) {
		Class<?> entityClass = findClassByAlias(wrapper, alias);
		if (entityClass == null) {
			sql.append(alias).append(".*");
			return;
		}
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		List<FieldMeta> fields = entityMeta.getFieldList();
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			String column = fields.get(i).getColumnName();
			sql.append(alias).append(".").append(dialect.wrap(column));
			sql.append(" AS ").append(quoteAlias(alias + "." + column));
		}
	}

	private static Class<?> findClassByAlias(HighLevelSelectWrapper wrapper, String alias) {
		for (java.util.Map.Entry<Class<?>, String> entry : wrapper.getAliasMap().entrySet()) {
			if (entry.getValue().equals(alias)) {
				return entry.getKey();
			}
		}
		return null;
	}

	private static String quoteAlias(String alias) {
		return "`" + alias + "`";
	}

	private static void buildFrom(HighLevelSelectWrapper wrapper, StringBuilder sql, Dialect dialect) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(wrapper.getRootClass());
		sql.append(" FROM ").append(dialect.wrap(entityMeta.getTableName())).append(" ").append(wrapper.getRootAlias());
	}

	private static void buildJoin(HighLevelSelectWrapper wrapper, StringBuilder sql, List<Object> params,
			Dialect dialect) {
		for (JoinNode join : wrapper.getJoins()) {
			sql.append(" ").append(join.getType().name()).append(" JOIN ");
			EntityMeta entityMeta = EntityMetaCache.getEntityMeta(join.getEntityClass());
			sql.append(dialect.wrap(entityMeta.getTableName())).append(" ").append(join.getAlias());
			sql.append(" ON ");
			buildCondition(wrapper, join.getOn(), sql, params, dialect, true);
		}
	}

	private static void buildWhere(HighLevelSelectWrapper wrapper, StringBuilder sql, List<Object> params,
			Dialect dialect) {
		if (wrapper.getWhere().getChildren().isEmpty()) {
			return;
		}
		sql.append(" WHERE ");
		buildCondition(wrapper, wrapper.getWhere(), sql, params, dialect, false);
	}

	private static void buildCondition(HighLevelSelectWrapper wrapper, HighLevelConditionNode node, StringBuilder sql,
			List<Object> params, Dialect dialect, boolean isJoinOn) {
		if (node.getChildren().isEmpty() && !node.isLeaf()) {
			return;
		}

		if (node.isLeaf()) {
			appendLeafCondition(wrapper, node, sql, params, dialect, isJoinOn);
			return;
		}

		boolean first = true;
		for (HighLevelConditionNode child : node.getChildren()) {
			if (!first) {
				sql.append(" ").append(node.getLogic().name()).append(" ");
			}
			first = false;

			if (child.isLeaf()) {
				appendLeafCondition(wrapper, child, sql, params, dialect, isJoinOn);
			} else {
				sql.append("(");
				buildCondition(wrapper, child, sql, params, dialect, isJoinOn);
				sql.append(")");
			}
		}
	}

	private static void appendLeafCondition(HighLevelSelectWrapper wrapper, HighLevelConditionNode node,
			StringBuilder sql, List<Object> params, Dialect dialect, boolean isJoinOn) {
		LambadaColumn<?> field = node.getField();
		Class<?> entityClass = LambadaCache.getLambadaMeta(field).getEntityClass();
		String tableAlias = wrapper.getAliasMap().get(entityClass);
		if (tableAlias == null) {
			throw new PureOrmException("实体 " + entityClass.getSimpleName() + " 未在 FROM 或 JOIN 中声明");
		}
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		Op op = node.getOp();
		Object value = node.getValue();

		String leftExpr = tableAlias + "." + dialect.wrap(column);

		switch (op) {
		case EQ:
		case NE:
		case GT:
		case GE:
		case LT:
		case LE:
		case LIKE:
			if (isJoinOn && value instanceof LambadaColumn) {
				LambadaColumn<?> rightField = (LambadaColumn<?>) value;
				Class<?> rightEntityClass = LambadaCache.getLambadaMeta(rightField).getEntityClass();
				String rightAlias = wrapper.getAliasMap().get(rightEntityClass);
				if (rightAlias == null) {
					throw new PureOrmException(
							"实体 " + rightEntityClass.getSimpleName() + " 未在 FROM 或 JOIN 中声明");
				}
				String rightColumn = LambadaCache.getLambadaMeta(rightField).getColumnName();
				sql.append(leftExpr).append(" ").append(opToSql(op)).append(" ").append(rightAlias).append(".")
						.append(dialect.wrap(rightColumn));
			} else {
				sql.append(leftExpr).append(" ").append(opToSql(op)).append(" ?");
				params.add(value);
			}
			break;
		case IN:
			Object[] values = (Object[]) value;
			sql.append(leftExpr).append(" IN (");
			for (int i = 0; i < values.length; i++) {
				if (i > 0) {
					sql.append(", ");
				}
				sql.append("?");
				params.add(values[i]);
			}
			sql.append(")");
			break;
		case IS_NULL:
			sql.append(leftExpr).append(" IS NULL");
			break;
		case IS_NOT_NULL:
			sql.append(leftExpr).append(" IS NOT NULL");
			break;
		default:
			throw new PureOrmException("不支持的操作符：" + op);
		}
	}

	private static String opToSql(Op op) {
		switch (op) {
		case EQ:
			return "=";
		case NE:
			return "!=";
		case GT:
			return ">";
		case GE:
			return ">=";
		case LT:
			return "<";
		case LE:
			return "<=";
		case LIKE:
			return "LIKE";
		default:
			return op.name();
		}
	}

	private static void buildGroupBy(HighLevelSelectWrapper wrapper, StringBuilder sql, Dialect dialect) {
		List<GroupColumn> groupBys = wrapper.getGroupBys();
		if (groupBys.isEmpty()) {
			return;
		}
		sql.append(" GROUP BY ");
		for (int i = 0; i < groupBys.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			GroupColumn g = groupBys.get(i);
			sql.append(g.getTableName()).append(".").append(dialect.wrap(g.getColumnName()));
		}
	}

	private static void buildOrderBy(HighLevelSelectWrapper wrapper, StringBuilder sql, Dialect dialect) {
		List<OrderColumn> orderBys = wrapper.getOrderBys();
		if (orderBys.isEmpty()) {
			return;
		}
		sql.append(" ORDER BY ");
		for (int i = 0; i < orderBys.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			OrderColumn o = orderBys.get(i);
			sql.append(o.getTableName()).append(".").append(dialect.wrap(o.getColumnName()));
			sql.append(" ").append(o.getOrderByType() == OrderByType.ASC ? "ASC" : "DESC");
		}
	}
}
