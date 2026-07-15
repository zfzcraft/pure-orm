package cn.zfz.pureorm.crud.select.single;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.core.PureOrmException;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;

public class ResultSetMapper {

	@SuppressWarnings("unchecked")
	public static <T> List<T> map(ResultSet rs, Class<T> clazz) {
		if (rs == null || clazz == null) {
			return new ArrayList<>();
		}

		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(clazz);
		List<T> list = new ArrayList<>();

		try {
			ResultSetMetaData meta = rs.getMetaData();
			int columnCount = meta.getColumnCount();

			List<FieldMeta> mappedFields = new ArrayList<>();
			for (int i = 1; i <= columnCount; i++) {
				String columnName = meta.getColumnLabel(i).toLowerCase();
				FieldMeta fieldMeta = entityMeta.getColumnNameLowerMap().get(columnName);
				mappedFields.add(fieldMeta);
				if (fieldMeta != null) {
					fieldMeta.getField().setAccessible(true);
				}
			}

			while (rs.next()) {
				T instance;
				try {
					instance = clazz.getDeclaredConstructor().newInstance();
				} catch (Exception e) {
					throw new PureOrmException("创建实体实例失败：" + clazz.getName(), e);
				}

				for (int i = 1; i <= columnCount; i++) {
					FieldMeta fieldMeta = mappedFields.get(i - 1);
					if (fieldMeta == null) {
						continue;
					}

					Object dbValue = rs.getObject(i);
					if (dbValue == null) {
						continue;
					}

					Field field = fieldMeta.getField();
					try {
						if (fieldMeta.getFieldKind() == FieldKind.ENUM) {
							EnumTypeHandler enumTypeHandler = fieldMeta.getEnumTypeHandler();
							Object javaValue = enumTypeHandler.toJava((Class<? extends Enum<?>>) field.getType(), dbValue);
							field.set(instance, javaValue);
						} else {
							Object convertedValue = convertValue(dbValue, field.getType());
							field.set(instance, convertedValue);
						}
					} catch (Exception e) {
						throw new PureOrmException("映射字段失败：" + field.getName() + " = " + dbValue, e);
					}
				}
				list.add(instance);
			}
		} catch (SQLException e) {
			throw new PureOrmException("结果集映射失败", e);
		}
		return list;
	}

	private static Object convertValue(Object value, Class<?> targetType) {
		if (value == null) {
			return null;
		}
		if (targetType.isInstance(value)) {
			return value;
		}

		if (targetType == String.class) {
			return value.toString();
		}

		if (value instanceof Number) {
			Number num = (Number) value;
			if (targetType == int.class || targetType == Integer.class) {
				return num.intValue();
			}
			if (targetType == long.class || targetType == Long.class) {
				return num.longValue();
			}
			if (targetType == short.class || targetType == Short.class) {
				return num.shortValue();
			}
			if (targetType == byte.class || targetType == Byte.class) {
				return num.byteValue();
			}
			if (targetType == double.class || targetType == Double.class) {
				return num.doubleValue();
			}
			if (targetType == float.class || targetType == Float.class) {
				return num.floatValue();
			}
		}

		if (targetType == boolean.class || targetType == Boolean.class) {
			if (value instanceof Boolean) {
				return value;
			}
			if (value instanceof Number) {
				return ((Number) value).intValue() != 0;
			}
			if (value instanceof String) {
				return Boolean.parseBoolean((String) value);
			}
		}

		return value;
	}

}
