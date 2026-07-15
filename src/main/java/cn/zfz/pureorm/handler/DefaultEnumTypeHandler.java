package cn.zfz.pureorm.handler;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import cn.zfz.pureorm.annotations.EnumValue;
import cn.zfz.pureorm.core.PureOrmException;

public class DefaultEnumTypeHandler implements EnumTypeHandler {

	private static final Map<Class<? extends Enum<?>>, Field> ENUM_VALUE_FIELD_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<? extends Enum<?>>, Map<Object, Enum<?>>> ENUM_VALUE_TO_CONSTANT_CACHE = new ConcurrentHashMap<>();

	@Override
	public Enum<?> toJava(Class<? extends Enum<?>> enumClass, Object dbValue) {
		if (enumClass == null || dbValue == null)
			return null;
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException(enumClass.getName() + " 不是枚举类");
		}

		Map<Object, Enum<?>> valueMap = ENUM_VALUE_TO_CONSTANT_CACHE.computeIfAbsent(enumClass, this::buildValueMap);
		Enum<?> result = valueMap.get(normalizeKey(dbValue));
		if (result != null) {
			return result;
		}
		if (dbValue instanceof Number) {
			for (Map.Entry<Object, Enum<?>> entry : valueMap.entrySet()) {
				if (entry.getKey() instanceof Number
						&& ((Number) entry.getKey()).longValue() == ((Number) dbValue).longValue()) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	private Map<Object, Enum<?>> buildValueMap(Class<? extends Enum<?>> enumClass) {
		Map<Object, Enum<?>> map = new ConcurrentHashMap<>();
		Field annoField = getEnumValueField(enumClass);
		for (Enum<?> enumConst : enumClass.getEnumConstants()) {
			Object key;
			if (annoField != null) {
				try {
					key = annoField.get(enumConst);
				} catch (IllegalAccessException e) {
					throw new PureOrmException("枚举值映射失败", e);
				}
			} else {
				key = enumConst.name();
			}
			map.put(normalizeKey(key), enumConst);
		}
		return map;
	}

	private Object normalizeKey(Object key) {
		if (key == null) {
			return null;
		}
		return key;
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
}
