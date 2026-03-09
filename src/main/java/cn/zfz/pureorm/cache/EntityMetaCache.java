package cn.zfz.pureorm.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityMetaCache {

	private static Map<Class<?>, EntityMeta> cacheMap = new ConcurrentHashMap<>();

	public static EntityMeta getEntityMeta(Class<?> clazz) {
		return cacheMap.computeIfAbsent(clazz, action -> {
			return EntityMetaParser.parse(clazz);
		});
	}


}
