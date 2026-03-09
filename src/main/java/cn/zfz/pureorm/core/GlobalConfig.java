package cn.zfz.pureorm.core;

import lombok.Data;

@Data
public class GlobalConfig {
	
	private String tablePrefix;
	
	private boolean printSql;
	
	private boolean printParams;

	private static final  GlobalConfig CONFIG = new GlobalConfig();
	
	public static  GlobalConfig instance() {
		return CONFIG;
	}

}
