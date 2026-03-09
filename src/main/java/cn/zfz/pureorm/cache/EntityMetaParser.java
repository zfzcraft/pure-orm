package cn.zfz.pureorm.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import cn.zfz.pureorm.annotations.Column;
import cn.zfz.pureorm.annotations.EnumValue;
import cn.zfz.pureorm.annotations.NotColumn;
import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.utils.StringUtils;

public class EntityMetaParser {

	public static EntityMeta parse(Class<?> clazz) {
		// 1. 表名（你可以加 @Table 注解，这里先用类名）
		String tableName = resolveTableName(clazz);

		List<FieldMeta> fieldList = new ArrayList<>();
		Map<String, FieldMeta> fieldNameMap = new HashMap<>();
		Map<String, FieldMeta> columnNameMap = new HashMap<>();
		FieldMeta pkField = null;

		// 遍历所有字段（排除静态、transient）
		for (Field field : clazz.getDeclaredFields()) {
			int mod = field.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || field.isAnnotationPresent(NotColumn.class)) {
				continue;
			}

			FieldMeta meta = parseField(field);
			fieldList.add(meta);
			fieldNameMap.put(meta.getField().getName(), meta);
			columnNameMap.put(meta.getColumnName(), meta);

			if (meta.getFieldKind() == FieldKind.PRIMARY_KEY_AUTO_INCREMENT) {
				if (pkField != null) {
					throw new IllegalStateException("实体类只能有一个自增主键: " + clazz);
				}
				pkField = meta;
			}
		}

		return new EntityMeta(clazz, tableName, pkField, Collections.unmodifiableList(fieldList),
				Collections.unmodifiableMap(fieldNameMap), Collections.unmodifiableMap(columnNameMap));
	}

	private static String resolveTableName(Class<?> clazz) {
		Table tableAnnotation = clazz.getAnnotation(Table.class);
		if (tableAnnotation != null && !tableAnnotation.name().trim().isEmpty()) {
			return tableAnnotation.name().trim();
		}
		return StringUtils.camelToSnake(clazz.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private static FieldMeta parseField(Field field) {
		field.setAccessible(true);

		FieldKind fieldKind = FieldKind.COMMON;
		String fieldName = field.getName();
		// ====================== @Column ======================
		Column col = field.getAnnotation(Column.class);
		String columnName = col != null && !col.name().isEmpty() ? col.name() : StringUtils.camelToSnake(fieldName);
		boolean pk = field.isAnnotationPresent(PrimaryKey.class);
		boolean autoIncrement = false;
		if (pk) {
			autoIncrement = field.getAnnotation(PrimaryKey.class).autoIncrement();
			if (autoIncrement) {
				fieldKind = FieldKind.PRIMARY_KEY_AUTO_INCREMENT;
			}
		}
		
		// ====================== 枚举 & @EnumValue ======================
		boolean isEnum = field.getType().isEnum();
		Field enumValueField = null;
		if (isEnum) {
			fieldKind = FieldKind.ENUM;
			enumValueField = findEnumValueField((Class<? extends Enum<?>>) field.getType());
		}

		return new FieldMeta(field, fieldKind, field.getGenericType(), columnName, enumValueField);

	}

	/**
	 * 一次性找到 @EnumValue 字段，缓存起来，不再循环
	 */
	private static Field findEnumValueField(Class<? extends Enum<?>> enumClass) {
		for (Field f : enumClass.getDeclaredFields()) {
			if (f.isAnnotationPresent(EnumValue.class)) {
				return f;
			}
		}
		return null;
	}

}