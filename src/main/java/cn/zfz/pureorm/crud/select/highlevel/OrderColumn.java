package cn.zfz.pureorm.crud.select.highlevel;

import cn.zfz.pureorm.crud.select.single.OrderByType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderColumn {
private String tableName;
	
	private String columnName;
	
	private OrderByType orderByType;
}
