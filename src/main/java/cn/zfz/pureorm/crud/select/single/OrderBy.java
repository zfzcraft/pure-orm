package cn.zfz.pureorm.crud.select.single;

import cn.zfz.pureorm.enums.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class OrderBy {
	
	private String name;
	
	private Order order;
	
	public static OrderBy of(String name,Order order) {
		return new OrderBy(name, order);
	}

}
