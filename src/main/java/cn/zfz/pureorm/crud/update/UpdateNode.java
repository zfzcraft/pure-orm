package cn.zfz.pureorm.crud.update;

import java.util.Collections;
import java.util.List;

import cn.zfz.pureorm.enums.UpdateType;

public class UpdateNode {

	private final UpdateType type;
	private String field;
	private Object value;
	private String nativeSql;
	private List<Object> params;

	public UpdateNode(UpdateType type, String field, Object value) {
		this.type = type;
		this.field = field;
		this.value = value;
	}

	public UpdateNode(UpdateType type, String nativeSql) {
		this.type = type;
		this.nativeSql = nativeSql;
		this.params = Collections.emptyList();
	}

	public UpdateNode(UpdateType type, String nativeSql, List<Object> params) {
		this.type = type;
		this.nativeSql = nativeSql;
		this.params = params;
	}

	public UpdateType getType() {
		return type;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getNativeSql() {
		return nativeSql;
	}

	public void setNativeSql(String nativeSql) {
		this.nativeSql = nativeSql;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

}
