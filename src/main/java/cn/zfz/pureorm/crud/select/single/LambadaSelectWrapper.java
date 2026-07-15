package cn.zfz.pureorm.crud.select.single;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.enums.LockMode;
import cn.zfz.pureorm.enums.Order;
import cn.zfz.pureorm.utils.StringUtils;

public class LambadaSelectWrapper<E> extends SelectWrapper<LambadaSelectWrapper<E>, E> {

	private Integer limit;
	private Integer offset;
	private LockMode lockMode;
	private final List<String> selectFields = new ArrayList<>();
	private final List<OrderBy> orderBys = new ArrayList<>();
	private final List<String> groupBys = new ArrayList<>();
	private SqlAndParams having;

	public LambadaSelectWrapper() {
	}

	@SuppressWarnings("unchecked")
	public LambadaSelectWrapper<E> select(LambadaColumn<E>... columns) {
		for (LambadaColumn<E> column : columns) {
			if (StringUtils.isEmpty(tableName)) {
				tableName = LambadaCache.getLambadaMeta(column).getTableName();
			}
			String col = LambadaCache.getLambadaMeta(column).getColumnName();
			selectFields.add(col);
		}
		return this;
	}

	@Override
	public LambadaSelectWrapper<E> selectAll(Class<E> entityClass) {
		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		if (StringUtils.isEmpty(tableName)) {
			tableName = entityMeta.getTableName();
		}
		for (FieldMeta fieldMeta : entityMeta.getFieldList()) {
			selectFields.add(fieldMeta.getColumnName());
		}
		return this;
	}

	public LambadaSelectWrapper<E> orderByAsc(LambadaColumn<E> column) {
		return orderBy(column, Order.ASC);
	}

	public LambadaSelectWrapper<E> orderByDesc(LambadaColumn<E> column) {
		return orderBy(column, Order.DESC);
	}

	private LambadaSelectWrapper<E> orderBy(LambadaColumn<E> column, Order order) {
		if (StringUtils.isEmpty(tableName)) {
			tableName = LambadaCache.getLambadaMeta(column).getTableName();
		}
		String col = LambadaCache.getLambadaMeta(column).getColumnName();
		orderBys.add(OrderBy.of(col, order));
		return this;
	}

	public LambadaSelectWrapper<E> groupBy(LambadaColumn<E> column) {
		if (StringUtils.isEmpty(tableName)) {
			tableName = LambadaCache.getLambadaMeta(column).getTableName();
		}
		String col = LambadaCache.getLambadaMeta(column).getColumnName();
		groupBys.add(col);
		return this;
	}

	public LambadaSelectWrapper<E> having(String nativeSql, Object... params) {
		this.having = new SqlAndParams(nativeSql, Arrays.asList(params));
		return this;
	}

	@Override
	public LambadaSelectWrapper<E> limit(Integer limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public LambadaSelectWrapper<E> offset(Integer offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public List<String> getSelectFields() {
		return Collections.unmodifiableList(selectFields);
	}

	@Override
	public List<OrderBy> getOrderBys() {
		return Collections.unmodifiableList(orderBys);
	}

	@Override
	public Integer getLimit() {
		return limit;
	}

	@Override
	public Integer getOffset() {
		return offset;
	}

	@Override
	public LambadaSelectWrapper<E> forUpdateShare() {
		this.lockMode = LockMode.FOR_SHARE;
		return this;
	}

	@Override
	public LambadaSelectWrapper<E> forUpdateLock() {
		this.lockMode = LockMode.FOR_UPDATE;
		return this;
	}

	@Override
	public LockMode getLockMode() {
		return lockMode;
	}

	@Override
	public List<String> getGroupBys() {
		return Collections.unmodifiableList(groupBys);
	}

	@Override
	public SqlAndParams getHaving() {
		return having;
	}

	public static <T> LambadaSelectWrapper<T> of() {
		return new LambadaSelectWrapper<>();
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
