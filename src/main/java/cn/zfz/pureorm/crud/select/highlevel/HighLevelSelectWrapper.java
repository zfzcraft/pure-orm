package cn.zfz.pureorm.crud.select.highlevel;

import java.util.*;
import java.util.function.Consumer;

import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.PureOrmException;
import cn.zfz.pureorm.crud.select.single.OrderByType;
import cn.zfz.pureorm.enums.JoinType;
import cn.zfz.pureorm.enums.Op;

public class HighLevelSelectWrapper {

	private Class<?> rootClass;
	private String rootAlias;
	private final Map<Class<?>, String> aliasMap = new HashMap<>();
	private final List<JoinNode> joins = new ArrayList<>();
	private final List<SelectColumn> selectFields = new ArrayList<>();
	private final HighLevelConditionNode where = new HighLevelConditionNode(HighLevelConditionNode.Logic.AND);
	private final List<OrderColumn> orderBys = new ArrayList<>();
	private final List<GroupColumn> groupBys = new ArrayList<>();
	private final List<HavingColumn> havingColumns = new ArrayList<>();
	private Integer offset;
	private Integer limit;

	public HighLevelSelectWrapper from(Class<?> entityClass) {
		this.rootClass = entityClass;
		this.rootAlias = entityClass.getSimpleName().toLowerCase();
		this.aliasMap.put(entityClass, rootAlias);
		return this;
	}

	public static HighLevelSelectWrapper of(Class<?> entityClass) {
		HighLevelSelectWrapper wrapper = new HighLevelSelectWrapper();
		return wrapper.from(entityClass);
	}

	// ===================== JOIN =====================

	public HighLevelSelectWrapper leftJoin(Class<?> target, Consumer<JoinOnBuilder> on) {
		return join(JoinType.LEFT, target, on);
	}

	public HighLevelSelectWrapper innerJoin(Class<?> target, Consumer<JoinOnBuilder> on) {
		return join(JoinType.INNER, target, on);
	}

	public HighLevelSelectWrapper rightJoin(Class<?> target, Consumer<JoinOnBuilder> on) {
		return join(JoinType.RIGHT, target, on);
	}

	private HighLevelSelectWrapper join(JoinType type, Class<?> target, Consumer<JoinOnBuilder> on) {
		String alias = target.getSimpleName().toLowerCase();
		if (aliasMap.containsKey(target)) {
			alias = alias + "_" + joins.size();
		}
		aliasMap.put(target, alias);

		JoinOnBuilder builder = new JoinOnBuilder();
		on.accept(builder);
		joins.add(new JoinNode(type, target, alias, builder.build()));
		return this;
	}

	// ===================== SELECT =====================

	@SafeVarargs
	public final <T> HighLevelSelectWrapper select(LambadaColumn<T>... fields) {
		for (LambadaColumn<T> f : fields) {
			Class<?> entityClass = LambadaCache.getLambadaMeta(f).getEntityClass();
			String tableAlias = aliasMap.get(entityClass);
			if (tableAlias == null) {
				throw new PureOrmException("实体 " + entityClass.getSimpleName() + " 未在 FROM 或 JOIN 中声明");
			}
			String columnName = LambadaCache.getLambadaMeta(f).getColumnName();
			selectFields.add(new SelectColumn(SelectType.COLUMN, tableAlias, columnName));
		}
		return this;
	}

	public HighLevelSelectWrapper selectAll(Class<?> entityClass) {
		String alias = aliasMap.get(entityClass);
		if (alias == null) {
			throw new PureOrmException("实体 " + entityClass.getSimpleName() + " 未在 FROM 或 JOIN 中声明");
		}
		selectFields.add(new SelectColumn(SelectType.COLUMN, alias, "*"));
		return this;
	}

	public <T> HighLevelSelectWrapper count(LambadaColumn<T> field, String alias) {
		return aggregate(field, SelectType.COUNT, alias);
	}

	public <T> HighLevelSelectWrapper sum(LambadaColumn<T> field, String alias) {
		return aggregate(field, SelectType.SUM, alias);
	}

	public <T> HighLevelSelectWrapper max(LambadaColumn<T> field, String alias) {
		return aggregate(field, SelectType.MAX, alias);
	}

	public <T> HighLevelSelectWrapper min(LambadaColumn<T> field, String alias) {
		return aggregate(field, SelectType.MIN, alias);
	}

	public <T> HighLevelSelectWrapper avg(LambadaColumn<T> field, String alias) {
		return aggregate(field, SelectType.AVG, alias);
	}

	private <T> HighLevelSelectWrapper aggregate(LambadaColumn<T> field, SelectType type, String alias) {
		Class<?> entityClass = LambadaCache.getLambadaMeta(field).getEntityClass();
		String tableAlias = aliasMap.get(entityClass);
		if (tableAlias == null) {
			throw new PureOrmException("实体 " + entityClass.getSimpleName() + " 未在 FROM 或 JOIN 中声明");
		}
		String columnName = LambadaCache.getLambadaMeta(field).getColumnName();
		selectFields.add(new SelectColumn(type, tableAlias, columnName, alias));
		return this;
	}

	// ===================== WHERE =====================

	public <T> HighLevelSelectWrapper eq(LambadaColumn<T> field, Object val) {
		where.addLeaf(field, Op.EQ, val);
		return this;
	}

	public <T> HighLevelSelectWrapper ne(LambadaColumn<T> field, Object val) {
		where.addLeaf(field, Op.NE, val);
		return this;
	}

	public <T> HighLevelSelectWrapper gt(LambadaColumn<T> field, Object val) {
		where.addLeaf(field, Op.GT, val);
		return this;
	}

	public <T> HighLevelSelectWrapper ge(LambadaColumn<T> field, Object val) {
		where.addLeaf(field, Op.GE, val);
		return this;
	}

	public <T> HighLevelSelectWrapper lt(LambadaColumn<T> field, Object val) {
		where.addLeaf(field, Op.LT, val);
		return this;
	}

	public <T> HighLevelSelectWrapper le(LambadaColumn<T> field, Object val) {
		where.addLeaf(field, Op.LE, val);
		return this;
	}

	public <T> HighLevelSelectWrapper like(LambadaColumn<T> field, Object val) {
		where.addLeaf(field, Op.LIKE, val);
		return this;
	}

	@SafeVarargs
	public final <T> HighLevelSelectWrapper in(LambadaColumn<T> field, Object... values) {
		where.addLeaf(field, Op.IN, values);
		return this;
	}

	public <T> HighLevelSelectWrapper isNull(LambadaColumn<T> field) {
		where.addLeaf(field, Op.IS_NULL, null);
		return this;
	}

	public <T> HighLevelSelectWrapper isNotNull(LambadaColumn<T> field) {
		where.addLeaf(field, Op.IS_NOT_NULL, null);
		return this;
	}

	public HighLevelSelectWrapper and(Consumer<HighLevelSelectWrapper> consumer) {
		HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
		wrap.aliasMap.putAll(this.aliasMap);
		consumer.accept(wrap);
		where.getChildren().add(wrap.where);
		return this;
	}

	public HighLevelSelectWrapper or(Consumer<HighLevelSelectWrapper> consumer) {
		HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
		wrap.aliasMap.putAll(this.aliasMap);
		consumer.accept(wrap);
		HighLevelConditionNode orNode = new HighLevelConditionNode(HighLevelConditionNode.Logic.OR);
		orNode.getChildren().add(wrap.where);
		where.getChildren().add(orNode);
		return this;
	}

	// ===================== ORDER BY =====================

	public <T> HighLevelSelectWrapper orderByAsc(LambadaColumn<T> field) {
		return orderBy(field, OrderByType.ASC);
	}

	public <T> HighLevelSelectWrapper orderByDesc(LambadaColumn<T> field) {
		return orderBy(field, OrderByType.DESC);
	}

	private <T> HighLevelSelectWrapper orderBy(LambadaColumn<T> field, OrderByType type) {
		Class<?> entityClass = LambadaCache.getLambadaMeta(field).getEntityClass();
		String tableAlias = aliasMap.get(entityClass);
		if (tableAlias == null) {
			throw new PureOrmException("实体 " + entityClass.getSimpleName() + " 未在 FROM 或 JOIN 中声明");
		}
		String columnName = LambadaCache.getLambadaMeta(field).getColumnName();
		orderBys.add(new OrderColumn(tableAlias, columnName, type));
		return this;
	}

	// ===================== GROUP BY / HAVING =====================

	public <T> HighLevelSelectWrapper groupBy(LambadaColumn<T> field) {
		Class<?> entityClass = LambadaCache.getLambadaMeta(field).getEntityClass();
		String tableAlias = aliasMap.get(entityClass);
		if (tableAlias == null) {
			throw new PureOrmException("实体 " + entityClass.getSimpleName() + " 未在 FROM 或 JOIN 中声明");
		}
		String columnName = LambadaCache.getLambadaMeta(field).getColumnName();
		groupBys.add(new GroupColumn(tableAlias, columnName));
		return this;
	}

	// ===================== 分页 =====================

	public HighLevelSelectWrapper limit(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
		return this;
	}

	// ===================== JoinOnBuilder =====================

	public class JoinOnBuilder {
		private final HighLevelConditionNode onNode = new HighLevelConditionNode(HighLevelConditionNode.Logic.AND);

		public <L, R> JoinOnBuilder eq(LambadaColumn<L> left, LambadaColumn<R> right) {
			return addCondition(left, Op.EQ, right);
		}

		public JoinOnBuilder and(Consumer<JoinOnBuilder> consumer) {
			JoinOnBuilder builder = new JoinOnBuilder();
			consumer.accept(builder);
			onNode.getChildren().add(builder.onNode);
			return this;
		}

		private <L, R> JoinOnBuilder addCondition(LambadaColumn<L> left, Op op, LambadaColumn<R> right) {
			HighLevelConditionNode node = new HighLevelConditionNode(HighLevelConditionNode.Logic.AND);
			node.setField(left);
			node.setOp(op);
			node.setValue(right);
			onNode.getChildren().add(node);
			return this;
		}

		HighLevelConditionNode build() {
			return onNode;
		}
	}

	// ===================== Getter =====================

	public Class<?> getRootClass() {
		return rootClass;
	}

	public String getRootAlias() {
		return rootAlias;
	}

	public Map<Class<?>, String> getAliasMap() {
		return aliasMap;
	}

	public List<JoinNode> getJoins() {
		return joins;
	}

	public List<SelectColumn> getSelectFields() {
		return selectFields;
	}

	public HighLevelConditionNode getWhere() {
		return where;
	}

	public List<OrderColumn> getOrderBys() {
		return orderBys;
	}

	public List<GroupColumn> getGroupBys() {
		return groupBys;
	}

	public List<HavingColumn> getHavingColumns() {
		return havingColumns;
	}

	public Integer getOffset() {
		return offset;
	}

	public Integer getLimit() {
		return limit;
	}
}
