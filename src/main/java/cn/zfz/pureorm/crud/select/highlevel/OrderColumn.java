package cn.zfz.pureorm.crud.select.highlevel;

import cn.zfz.pureorm.crud.select.single.OrderByType;

public class OrderColumn {
	private String tableName;
	
	private String columnName;
	
	private OrderByType orderByType;

	public OrderColumn(String tableName, String columnName, OrderByType orderByType) {
		this.tableName = tableName;
		this.columnName = columnName;
		this.orderByType = orderByType;
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

	public OrderByType getOrderByType() {
		return orderByType;
	}

	public void setOrderByType(OrderByType orderByType) {
		this.orderByType = orderByType;
	}
}
