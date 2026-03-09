package cn.zfz.pureorm.cache;

import lombok.Data;

@Data
public class LambadaMeta {
	
	private String className;
	
	private String tableName;
	
	private String fieldName;
	
	private String columnName;
	
	private Class<?> entityClass;
}
