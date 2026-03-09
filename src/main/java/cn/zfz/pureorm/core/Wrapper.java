package cn.zfz.pureorm.core;

import java.util.List;

import cn.zfz.pureorm.crud.condition.ConditionNode;

public interface Wrapper<W extends Wrapper<W,E>,E> {
	
	String getTableName();

	W conditionNative(String nativeSql);

	W endGroup();

	W beginGroup();

	W or();

	W eq(LambadaColumn<E> field, Object value);

	boolean hasCondition();

	List<ConditionNode> getConditionNodes();

	W between(LambadaColumn<E> field, Object start, Object end);

	W isNotNull(LambadaColumn<E> field);

	W isNull(LambadaColumn<E> field);

	W notIn(LambadaColumn<E> field, Object... values);

	W in(LambadaColumn<E> field, Object... values);

	W likeRight(LambadaColumn<E> field, Object value);

	W likeLeft(LambadaColumn<E> field, Object value);

	W like(LambadaColumn<E> field, Object value);

	W le(LambadaColumn<E> field, Object value);

	W lt(LambadaColumn<E> field, Object value);

	W gt(LambadaColumn<E> field, Object value);

	W ne(LambadaColumn<E> field, Object value);

	W ge(LambadaColumn<E> field, Object value);
}
