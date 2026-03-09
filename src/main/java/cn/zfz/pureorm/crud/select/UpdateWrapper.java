package cn.zfz.pureorm.crud.select;

import java.util.List;

import cn.zfz.pureorm.core.AbstractWrapper;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.crud.update.UpdateNode;

public abstract class UpdateWrapper<W, E> extends AbstractWrapper<W, E> {
	
	public abstract W set(LambadaColumn<E> field, Object value);

	public	abstract W setIncr(LambadaColumn<E> field, Long value);

	public	abstract W setDecr(LambadaColumn<E> field, Long value);

	public W setIncr(LambadaColumn<E> field) {
		return setIncr(field, 1L);
	}

	public W setDecr(LambadaColumn<E> field) {
		return setDecr(field, 1L);
	}

	public abstract List<UpdateNode> getUpdateNodes();

	public abstract W setNative(String nativeSql);
}
