package cn.zfz.pureorm.crud.select.highlevel;

public class HavingColumn {
	private String tableName;
	
	private String columnName;
	
	private SelectType selectType;

	public HavingColumn(String tableName, String columnName, SelectType selectType) {
		this.tableName = tableName;
		this.columnName = columnName;
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

	public SelectType getSelectType() {
		return selectType;
	}

	public void setSelectType(SelectType selectType) {
		this.selectType = selectType;
	}
}
