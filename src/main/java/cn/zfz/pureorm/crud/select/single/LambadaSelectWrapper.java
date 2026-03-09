package cn.zfz.pureorm.crud.select.single;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.enums.LockMode;
import cn.zfz.pureorm.enums.Order;

//单表查询 Wrapper，继承 BaseWrapper，扩展查询专属方法
public class LambadaSelectWrapper<E> extends SelectWrapper<LambadaSelectWrapper<E>, E> {

	// 要查询的字段（默认 *）
	private List<String> selectFields = new ArrayList<>();
	// 排序
	private List<OrderBy> orderBys = new ArrayList<>();
	// 分页
	private Integer limit;
	private Integer offset;

	private LockMode lockMode;

	public LambadaSelectWrapper<E> orderByAsc(String column) {
		orderBys.add(OrderBy.of(column, Order.ASC));
		return this;
	}

	public LambadaSelectWrapper<E> orderByDesc(String column) {
		orderBys.add(OrderBy.of(column, Order.DESC));
		return this;
	}

	public LambadaSelectWrapper(Class<E> entityClass) {
		super();

		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		this.tableName = entityMeta.getTableName();
	}

	public LambadaSelectWrapper() {
		super();
	}

	// ====================== 分页 ======================
	public LambadaSelectWrapper<E> limit(Integer count) {
		this.limit = count;
		return this;
	}

	public LambadaSelectWrapper<E> offset(Integer start) {
		this.offset = start;
		return this;
	}

	// ====================== 获取查询参数 ======================
	public List<String> getSelectFields() {
		return selectFields;
	}

	public Integer getLimit() {
		return limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public LambadaSelectWrapper<E> forUpdateShare() {
		this.lockMode = LockMode.FOR_SHARE;
		return this;
	}

	public LambadaSelectWrapper<E> forUpdateLock() {
		this.lockMode = LockMode.FOR_UPDATE;
		return this;
	}

	@Override
	public List<OrderBy> getOrderBys() {
		return orderBys;
	}

	@Override
	public LambadaSelectWrapper<E> selectAll(Class<E> entityClass) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public LockMode getLockMode() {
		return lockMode;
	}

}
