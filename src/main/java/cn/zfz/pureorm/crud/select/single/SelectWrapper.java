package cn.zfz.pureorm.crud.select.single;

import java.util.List;

import cn.zfz.pureorm.core.AbstractWrapper;
import cn.zfz.pureorm.enums.LockMode;

//单表查询 Wrapper，继承 BaseWrapper，扩展查询专属方法
public abstract class SelectWrapper<W, E> extends AbstractWrapper<W, E> {
	

	public abstract W limit(Integer limit);

	public abstract W offset(Integer offset);

	public abstract List<String> getSelectFields();

	public abstract List<OrderBy> getOrderBys();

	public abstract Integer getLimit();

	public abstract Integer getOffset();

	public abstract W forUpdateShare();

	public abstract W forUpdateLock();

	public abstract W selectAll(Class<E> entityClass);

	public abstract LockMode getLockMode();
	
	

}
