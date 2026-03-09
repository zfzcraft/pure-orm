// com/simpleorm/factory/MapperFactory.java
package cn.zfz.pureorm.core;

import javax.sql.DataSource;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SimpleORM 的 Mapper 工厂：通过动态代理创建 BaseMapper 接口的实现。
 * <p>
 * 特性：
 * - 无状态、线程安全
 * - 不依赖 SqlSession / SqlSessionFactory
 * - 仅需传入 DataSource 和 Mapper 接口
 * - 自动解析泛型实体类型 T
 */
public final class MapperFactory {

    private MapperFactory() {
        // 工具类，禁止实例化
    }

    private static final ConcurrentHashMap<Class<?>, Class<?>> ENTITY_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 创建 Mapper 接口的代理实例
     *
     * @param mapperInterface 继承 BaseMapper<T> 的接口（如 UserMapper）
     * @param dataSource      数据源（如 HikariCP）
     * @param <T>             Mapper 接口类型
     * @return 代理实现
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> mapperInterface, DataSource dataSource) {
        if (mapperInterface == null) {
            throw new IllegalArgumentException("Mapper interface cannot be null");
        }
        if (!mapperInterface.isInterface()) {
            throw new IllegalArgumentException("Mapper must be an interface, but got: " + mapperInterface.getName());
        }

        // 检查是否继承 BaseMapper
        if (!BaseMapper.class.isAssignableFrom(mapperInterface)) {
            throw new IllegalArgumentException(
                "Mapper interface must extend BaseMapper, but " + mapperInterface.getName() + " does not."
            );
        }

        // 解析泛型实体类型 T
        Class<?> entityType = resolveEntityType((Class<? extends BaseMapper<?>>) mapperInterface);

        // 创建 InvocationHandler
        MapperInvocationHandler handler = new MapperInvocationHandler(dataSource, entityType);

        // 创建 JDK 动态代理
        return (T) Proxy.newProxyInstance(
            mapperInterface.getClassLoader(),
            new Class[]{mapperInterface},
            handler
        );
    }

    /**
     * 从 Mapper 接口解析泛型实体类型 T
     * 例如：UserMapper extends BaseMapper<User> → 返回 User.class
     */
    private static Class<?> resolveEntityType(Class<? extends BaseMapper<?>> mapperInterface) {
        return ENTITY_TYPE_CACHE.computeIfAbsent(mapperInterface, key -> {
            // 遍历所有泛型接口，找到 BaseMapper<T>
            for (Type genericInterface : key.getGenericInterfaces()) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericInterface;
                    if (pt.getRawType() == BaseMapper.class) {
                        Type entityTypeArg = pt.getActualTypeArguments()[0];
                        if (entityTypeArg instanceof Class) {
                            return (Class<?>) entityTypeArg;
                        } else {
                            throw new IllegalStateException(
                                "Unsupported generic type: " + entityTypeArg +
                                ". Only concrete classes are supported as entity type in BaseMapper<T>."
                            );
                        }
                    }
                }
            }
            throw new IllegalStateException(
                "Cannot resolve entity type T from " + key.getName() +
                ". Make sure it extends BaseMapper<T> with a concrete class (e.g., BaseMapper<User>)."
            );
        });
    }
}
