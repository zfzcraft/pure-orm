package cn.zfz.pureorm.crud.select.natives;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnMapping {
	private String column;
	private String entityField;
	
	public static ColumnMapping of(String column,String entityField) {
		return new ColumnMapping(column,entityField);
	}
}
