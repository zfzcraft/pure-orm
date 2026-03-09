package cn.zfz.pureorm.crud.select.highlevel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data@AllArgsConstructor
public class HavingColumn {
private String tableName;
	
	private String columnName;
	
	private SelectType selectType;
}
