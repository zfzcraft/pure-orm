package cn.zfz.pureorm.crud.select.single;

import cn.zfz.pureorm.enums.Order;

public class OrderBy {
	
	private String name;
	
	private Order order;

	public OrderBy(String name, Order order) {
		this.name = name;
		this.order = order;
	}

	public static OrderBy of(String name, Order order) {
		return new OrderBy(name, order);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}
