package cn.zfz.pureorm.crud.update;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.core.TypeHandlerHelper;
import cn.zfz.pureorm.enums.UpdateType;
import cn.zfz.pureorm.utils.StringUtils;

public class LambadaUpdateWrapper<E> extends UpdateWrapper<LambadaUpdateWrapper<E>, E> {

	private final List<UpdateNode> updateNodes = new ArrayList<>();

	public LambadaUpdateWrapper() {
		super();
	}

	@Override
	public LambadaUpdateWrapper<E> setNative(String nativeSql) {
		updateNodes.add(new UpdateNode(UpdateType.NATIVE, nativeSql));
		return this;
	}

	public LambadaUpdateWrapper<E> setNative(String nativeSql, List<Object> params) {
		updateNodes.add(new UpdateNode(UpdateType.NATIVE, nativeSql, params));
		return this;
	}

	public LambadaUpdateWrapper<E> setColumn(String column, Object value) {
		updateNodes.add(new UpdateNode(UpdateType.SET, column, value));
		return this;
	}

	@Override
	public List<UpdateNode> getUpdateNodes() {
		return updateNodes;
	}

	public static <T> LambadaUpdateWrapper<T> of() {
		return new LambadaUpdateWrapper<>();
	}

	@Override
	public LambadaUpdateWrapper<E> set(LambadaColumn<E> field, Object value) {
		if (StringUtils.isEmpty(tableName)) {
			String tableName = LambadaCache.getLambadaMeta(field).getTableName();
			this.tableName = tableName;
		}
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		Class<?> entityClass = LambadaCache.getLambadaMeta(field).getEntityClass();
		Object realValue = TypeHandlerHelper.getRealValue(entityClass, column, value);
		updateNodes.add(new UpdateNode(UpdateType.SET, column, realValue));
		return this;
	}

	@Override
	public LambadaUpdateWrapper<E> setIncr(LambadaColumn<E> field, Long value) {
		if (StringUtils.isEmpty(tableName)) {
			String tableName = LambadaCache.getLambadaMeta(field).getTableName();
			this.tableName = tableName;
		}
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		updateNodes.add(new UpdateNode(UpdateType.INCR, column, value));
		return this;
	}

	@Override
	public LambadaUpdateWrapper<E> setDecr(LambadaColumn<E> field, Long value) {
		if (StringUtils.isEmpty(tableName)) {
			String tableName = LambadaCache.getLambadaMeta(field).getTableName();
			this.tableName = tableName;
		}
		String column = LambadaCache.getLambadaMeta(field).getColumnName();
		updateNodes.add(new UpdateNode(UpdateType.DECR, column, value));
		return this;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
