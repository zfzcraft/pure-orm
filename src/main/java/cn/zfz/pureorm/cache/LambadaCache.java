package cn.zfz.pureorm.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.zfz.pureorm.core.LambadaColumn;

public class LambadaCache {
private static final Map<LambadaColumn<?>, LambadaMeta> CACHE_MAP = new ConcurrentHashMap<>();


public static LambadaMeta getLambadaMeta(LambadaColumn<?> lambadaColumn) {
	return CACHE_MAP.computeIfAbsent(lambadaColumn, action->{
		return LambadaParser.parse(lambadaColumn);
	});
}

}