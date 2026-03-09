package cn.zfz.pureorm.crud.select.natives;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.cache.LambadaCache;
import cn.zfz.pureorm.core.LambadaColumn;

public class LambadaNativeSelectWrapper<E> implements NativeSelectWrapper<LambadaNativeSelectWrapper<E>, E> {

	private String nativeSQL;

	private Object[] params;

	private List<ColumnMapping> columnMappings = new ArrayList<>();

	@Override
	public LambadaNativeSelectWrapper<E> nativeSQL(String sql) {
		this.nativeSQL = sql;
		return this;
	}

	@Override
	public LambadaNativeSelectWrapper<E> nativeSQL(String sql, Object[] params) {
		this.nativeSQL = sql;
		this.params = params;
		return this;
	}

	@Override
	public LambadaNativeSelectWrapper<E> mapping(String column, LambadaColumn<E> field) {
		String className = LambadaCache.getLambadaMeta(field).getClassName();
		String fieldName = LambadaCache.getLambadaMeta(field).getFieldName();
		String entityField = className + "." + fieldName;
		ColumnMapping columnMapping = ColumnMapping.of(column, entityField);
		columnMappings.add(columnMapping);
		return this;
	}

	@Override
	public String getNativeSQL() {
		return nativeSQL;
	}

	@Override
	public Object[] getParams() {
		return params;
	}

	@Override
	public List<ColumnMapping> getColumnMappings() {
		return columnMappings;
	}

}
