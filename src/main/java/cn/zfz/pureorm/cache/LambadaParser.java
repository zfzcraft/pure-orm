package cn.zfz.pureorm.cache;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.utils.StringUtils;

public class LambadaParser {

	public static <T> LambadaMeta parse(LambadaColumn<T> lambadaColumn) {
		LambadaMeta lambadaMeta = new LambadaMeta();
		SerializedLambda lambda = getSerializedLambda(lambadaColumn);

		// 解析方法名（核心逻辑：从lambda中获取getter方法名）
		String methodName = lambda.getImplMethodName();
		if (methodName.startsWith("get") && methodName.length() > 3) {
			// 从getXXX解析出字段名（如getName -> name）
			String fieldName = lowFirst(methodName.substring(3));
			lambadaMeta.setFieldName(fieldName);
			// 驼峰转下划线作为列名
			lambadaMeta.setColumnName(StringUtils.camelToSnake(fieldName));
		}

		// 解析实体类名
		String className = getImplClassName(lambda);
		lambadaMeta.setClassName(className);
		// 类名驼峰转下划线作为表名
		lambadaMeta.setTableName(StringUtils.camelToSnake(className));
		Class<?> entityClass = getImplClass(lambda);
		lambadaMeta.setEntityClass(entityClass);
		return lambadaMeta;
	}

	private static Class<?> getImplClass(SerializedLambda lambda) {
		// 从lambda的实现类名解析（正确的类名获取方式）
		String implClass = lambda.getImplClass().replace("/", ".");
		// 去除内部类的后缀（如User$1 -> User）
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

	// 首字母小写
	public static String lowFirst(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		return Character.toLowerCase(s.charAt(0)) + s.substring(1);
	}

	// 获取序列化后的lambda对象
	private static SerializedLambda getSerializedLambda(Serializable getter) {
		try {
			Method method = getter.getClass().getDeclaredMethod("writeReplace");
			method.setAccessible(Boolean.TRUE);
			return (SerializedLambda) method.invoke(getter);
		} catch (Exception e) {
			throw new RuntimeException("解析Lambda表达式失败", e);
		}
	}

	// 解析lambda对应的实体类名
	private static String getImplClassName(SerializedLambda lambda) {
		// 从lambda的实现类名解析（正确的类名获取方式）
		String implClass = lambda.getImplClass().replace("/", ".");
		// 去除内部类的后缀（如User$1 -> User）
		int dollarIndex = implClass.indexOf("$");
		if (dollarIndex > 0) {
			implClass = implClass.substring(0, dollarIndex);
		}
		// 截取简单类名（如cn.pureorm.test.User -> User）
		int lastDotIndex = implClass.lastIndexOf(".");
		if (lastDotIndex > 0) {
			implClass = implClass.substring(lastDotIndex + 1);
		}
		return implClass;
	}


}