package cn.zfz.pureorm.crud.select.highlevel;

public class SelectColumn {

	private SelectType selectType;
	private String tableName;
	private String columnName;
	private String alias;

	public SelectColumn(SelectType selectType, String tableName, String columnName) {
		this.selectType = selectType;
		this.tableName = tableName;
		this.columnName = columnName;
	}

	public SelectColumn(SelectType selectType, String tableName, String columnName, String alias) {
		this.selectType = selectType;
		this.tableName = tableName;
		this.columnName = columnName;
		this.alias = alias;
	}

	public SelectType getSelectType() {
		return selectType;
	}

	public void setSelectType(SelectType selectType) {
		this.selectType = selectType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
