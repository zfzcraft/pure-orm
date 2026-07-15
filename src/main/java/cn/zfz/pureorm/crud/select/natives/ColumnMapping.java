package cn.zfz.pureorm.crud.select.natives;

public class ColumnMapping {
	private String column;
	private String entityField;

	public ColumnMapping(String column, String entityField) {
		this.column = column;
		this.entityField = entityField;
	}

	public static ColumnMapping of(String column, String entityField) {
		return new ColumnMapping(column, entityField);
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getEntityField() {
		return entityField;
	}

	public void setEntityField(String entityField) {
		this.entityField = entityField;
	}
}
