package cn.zfz.pureorm.handler;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import cn.zfz.pureorm.annotations.EnumValue;
import cn.zfz.pureorm.core.PureOrmException;

/**
 * 枚举处理器默认实现（整合原EnumUtils所有核心逻辑） 包含：注解解析、字段缓存、双向转换、类型兼容，无任何冗余
 */
public class DefaultEnumTypeHandler implements EnumTypeHandler {

	// 缓存：枚举类 → @EnumValue标记字段（复用原EnumUtils的缓存逻辑，避免重复反射）
	private static final Map<Class<? extends Enum<?>>, Field> ENUM_VALUE_FIELD_CACHE = new ConcurrentHashMap<>();

	@Override
	public Enum<?> toJava(Class<? extends Enum<?>> enumClass, Object dbValue) {
		if (enumClass == null || dbValue == null)
			return null;
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException(enumClass.getName() + " 不是枚举类");
		}

		try {
			Field annoField = getEnumValueField(enumClass);
			if (annoField!=null) {
				// 遍历枚举常量，匹配@EnumValue字段值（原EnumUtils核心匹配逻辑）
				for (Enum<?> enumConst : enumClass.getEnumConstants()) {
					Object annoValue = annoField.get(enumConst);
					if (isValueMatch(annoValue, dbValue)) {
						return enumConst;
					}
				}
			}else {
				for (Enum<?> enumConst : enumClass.getEnumConstants()) {
					if (enumConst.name().equals(dbValue)) {
						return enumConst;
					}
				}
			}
		} catch (IllegalAccessException e) {
			throw new PureOrmException("数据库值转枚举失败", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toDatabase(Enum<?> enumObject) {
		if (enumObject == null)
			return null;

		Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) enumObject.getClass();
		Field annoField = getEnumValueField(enumClass);
		if (annoField != null) {
			try {
				return annoField.get(enumObject);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new PureOrmException(e);
			}
		}
		return enumObject.name();

	}

	/**
	 * 获取@EnumValue标记字段（带缓存，复用原EnumUtils的反射解析逻辑）
	 */
	private Field getEnumValueField(Class<? extends Enum<?>> enumClass) {
		return ENUM_VALUE_FIELD_CACHE.computeIfAbsent(enumClass, cls -> {
			for (Field field : cls.getDeclaredFields()) {
				if (field.isAnnotationPresent(EnumValue.class)) {
					field.setAccessible(true);
					return field;
				}
			}
			return null;
		});
	}

	/**
	 * 值匹配（兼容数值类型互转，复用原EnumUtils的兼容逻辑）
	 */
	private boolean isValueMatch(Object annoValue, Object dbValue) {
		if (Objects.equals(annoValue, dbValue))
			return true;
		// 仅处理数值类型（@EnumValue最常用场景，保证轻量）
		if (annoValue instanceof Number && dbValue instanceof Number) {
			return ((Number) annoValue).longValue() == ((Number) dbValue).longValue();
		}
		return false;
	}
}