package cn.zfz.pureorm.handler;

/**
 * 枚举类型处理器核心接口 核心职责： 1. toDb：从枚举对象中提取@EnumValue注解标记的数据库存储值 2.
 * toJava：将数据库值转换为对应的枚举对象
 */
public interface EnumTypeHandler {

	/**
	 * 数据库值 → 枚举对象（值转枚举）
	 * 
	 * @param enumClass 目标枚举类
	 * @param dbValue   数据库中存储的原始值（与@EnumValue注解字段类型一致）
	 * @return 匹配的枚举对象，无匹配抛IllegalArgumentException，null入参返回null
	 * @param <E> 枚举类型，遵循Java枚举标准泛型约束
	 */
	Enum<?> toJava(Class<? extends Enum<?>> enumClass, Object dbValue);

	/**
	 * 枚举对象 → 数据库存储值（提取注解值）
	 * 
	 * @param enumObj 枚举实例
	 * @return 枚举中@EnumValue注解标记的字段值，null入参返回null
	 * @param <E> 枚举类型，遵循Java枚举标准泛型约束
	 */
	Object toDatabase(Enum<?> enumObject);

}