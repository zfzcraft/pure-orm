package cn.zfz.pureorm.dialect;

import java.util.HashMap;
import java.util.Map;

import cn.zfz.pureorm.enums.DbType;

public class DialectFactory {
	
	static Map<DbType, Dialect> dialectMap = new HashMap<>();
	
	static {
		dialectMap.put(DbType.MYSQL, new MySQLDialect());
	}

    public static Dialect detect(DbType dbType) {
        return dialectMap.getOrDefault(dbType, dialectMap.get(DbType.MYSQL));
    }

}