// com/simpleorm/proxy/SimpleOrmInvocationHandler.java
package cn.zfz.pureorm.core;

import javax.sql.DataSource;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MapperInvocationHandler implements InvocationHandler {

	private final DataSource dataSource;
	private final Class<?> entityType;
	private volatile BaseMapperExecutor<?> executor; // lazy init, thread-safe

	public MapperInvocationHandler(DataSource dataSource, Class<?> entityType) {
		this.dataSource = dataSource;
		this.entityType = entityType;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 允许调用 Object 的方法（如 toString）
		if (method.getDeclaringClass() == Object.class) {
			return method.invoke(this, args);
		}
		if (method.isDefault()) {
			return invokeOriginalDefault(proxy, method, args);
		}
		// 懒加载 executor
		if (executor == null) {
			synchronized (this) {
				if (executor == null) {
					// 使用反射创建泛型实例（实际项目中可缓存或使用工厂）
					executor = new BaseMapperExecutor(dataSource, entityType);
				}
			}
		}
		return method.invoke(executor, args);
	}

	private Object invokeOriginalDefault(Object proxy, Method method, Object[] args)
			throws IllegalAccessException, Throwable {
		Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class,
				int.class);
		constructor.setAccessible(true);
		// 权限全开，才能调用接口的default
		int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
				| MethodHandles.Lookup.PACKAGE;
		MethodHandles.Lookup lookup = constructor.newInstance(method.getDeclaringClass(), allModes);
		// 这行是魔法：直接调用原始default，不进代理，不递归
		return lookup.unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxy).invokeWithArguments(args);
	}

}
