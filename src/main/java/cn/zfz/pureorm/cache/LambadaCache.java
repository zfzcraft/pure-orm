package cn.zfz.pureorm.cache;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.utils.StringUtils;

public class LambadaCache {

	private static final Map<Class<?>, LambadaMeta> CACHE_MAP = new ConcurrentHashMap<>();

	public static LambadaMeta getLambadaMeta(LambadaColumn<?> lambadaColumn) {
		Class<?> lambdaClass = lambadaColumn.getClass();
		LambadaMeta meta = CACHE_MAP.get(lambdaClass);
		if (meta != null) {
			return meta;
		}
		return CACHE_MAP.computeIfAbsent(lambdaClass, k -> parseAndCache(lambadaColumn));
	}

	private static LambadaMeta parseAndCache(LambadaColumn<?> lambadaColumn) {
		SerializedLambda lambda = getSerializedLambda(lambadaColumn);
		Class<?> entityClass = getImplClass(lambda);
		String fieldName = resolveFieldName(lambda);

		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(entityClass);
		FieldMeta fieldMeta = entityMeta.getFieldNameMap().get(fieldName);

		LambadaMeta meta = new LambadaMeta();
		meta.setEntityClass(entityClass);
		meta.setClassName(entityClass.getSimpleName());
		meta.setTableName(entityMeta.getTableName());
		meta.setFieldName(fieldName);
		meta.setColumnName(fieldMeta != null ? fieldMeta.getColumnName() : StringUtils.camelToSnake(fieldName));
		return meta;
	}

	private static String resolveFieldName(SerializedLambda lambda) {
		String methodName = lambda.getImplMethodName();
		if (methodName.startsWith("get") && methodName.length() > 3) {
			return StringUtils.lowerFirst(methodName.substring(3));
		}
		if (methodName.startsWith("is") && methodName.length() > 2) {
			return StringUtils.lowerFirst(methodName.substring(2));
		}
		return methodName;
	}

	private static Class<?> getImplClass(SerializedLambda lambda) {
		String implClass = lambda.getImplClass().replace("/", ".");
		int dollarIndex = implClass.indexOf("$");
		if (dollarIndex > 0) {
			implClass = implClass.substring(0, dollarIndex);
		}
		try {
			return Class.forName(implClass);
		} catch (Exception e) {
			throw new RuntimeException("解析Lambda表达式失败", e);
		}
	}

	private static SerializedLambda getSerializedLambda(Serializable getter) {
		try {
			Method method = getter.getClass().getDeclaredMethod("writeReplace");
			method.setAccessible(Boolean.TRUE);
			return (SerializedLambda) method.invoke(getter);
		} catch (Exception e) {
			throw new RuntimeException("解析Lambda表达式失败", e);
		}
	}
}