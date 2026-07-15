package cn.zfz.pureorm.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.crud.condition.ConditionNode;
import cn.zfz.pureorm.crud.condition.ConditionType;
import cn.zfz.pureorm.utils.StringUtils;

public abstract class AbstractWrapper<W, E> implements Wrapper<AbstractWrapper<W, E>, E> {
	
	protected String tableName;

	protected final List<ConditionNode> conditions = new ArrayList<>();

	protected AbstractWrapper<W, E> eqPrimaryKey(String tableName, String column, Serializable value) {
		this.tableName = tableName;
		conditions.add(new ConditionNode(ConditionType.EQ, column, value));
		return this;
	}

	protected AbstractWrapper<W, E> inPrimaryKeys(String tableName, String column, Object... values) {
		this.tableName = tableName;
		conditions.add(new ConditionNode(ConditionType.IN, column, values));
		return this;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public AbstractWrapper<W, E> or() {
		conditions.add(new ConditionNode(ConditionType.OR));
		return this;
	}

	private void setTableName(LambadaColumn<E> field) {
		if (StringUtils.isEmpty(tableName)) {
			String tableName = LambadaCache.getLambadaMeta(field).getTableName();
			this.tableName = tableName;
		}

	}

	@Override
	public AbstractWrapper<W, E> beginGroup() {
		conditions.add(new ConditionNode(ConditionType.LEFT_PAREN));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> endGroup() {
		conditions.add(new ConditionNode(ConditionType.RIGHT_PAREN));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> conditionNative(String nativeSql) {
		and();
		conditions.add(new ConditionNode(nativeSql));
		return this;
	}

	private void and() {
		if (!conditions.isEmpty()) {
			ConditionNode lastConditionNode = conditions.get(conditions.size() - 1);
			if (lastConditionNode.getType() != ConditionType.LEFT_PAREN
					&& lastConditionNode.getType() != ConditionType.OR) {
				conditions.add(new ConditionNode(ConditionType.AND));
			}

		}

	}

	@Override
	public AbstractWrapper<W, E> eq(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.EQ, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> ne(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.NE, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> gt(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.GT, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> ge(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.GE, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> lt(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.LT, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> le(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.LE, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> like(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.LIKE, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> likeLeft(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.LIKE_LEFT, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> likeRight(LambadaColumn<E> field, Object value) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.LIKE_RIGHT, column, value));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> in(LambadaColumn<E> field, Object... values) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.IN, column, values));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> notIn(LambadaColumn<E> field, Object... values) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.NOT_IN, column, values));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> isNull(LambadaColumn<E> field) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.IS_NULL, column));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> isNotNull(LambadaColumn<E> field) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.IS_NOT_NULL, column));
		return this;
	}

	@Override
	public AbstractWrapper<W, E> between(LambadaColumn<E> field, Object start, Object end) {
		and();
		setTableName(field);
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		conditions.add(new ConditionNode(ConditionType.BETWEEN, column, start, end));
		return this;
	}

	@Override
	public List<ConditionNode> getConditionNodes() {
		return conditions;
	}

	@Override
	public boolean hasCondition() {
		return !conditions.isEmpty();
	}
}
