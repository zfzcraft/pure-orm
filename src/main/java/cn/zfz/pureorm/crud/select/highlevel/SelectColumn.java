package cn.zfz.pureorm.crud.select.highlevel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SelectColumn {

	private SelectType selectType;
	
	private String tableName;
	
	private String columnName;
}
