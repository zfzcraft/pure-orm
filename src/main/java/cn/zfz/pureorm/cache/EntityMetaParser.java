package cn.zfz.pureorm.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.zfz.pureorm.annotations.Column;
import cn.zfz.pureorm.annotations.EnumValue;
import cn.zfz.pureorm.annotations.NotColumn;
import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.utils.StringUtils;

public class EntityMetaParser {

	private static final Map<Class<? extends Enum<?>>, Field> ENUM_VALUE_FIELD_CACHE = new ConcurrentHashMap<>();

	public static EntityMeta parse(Class<?> clazz) {
		String tableName = resolveTableName(clazz);

		List<FieldMeta> fieldList = new ArrayList<>();
		Map<String, FieldMeta> fieldNameMap = new HashMap<>();
		Map<String, FieldMeta> columnNameMap = new HashMap<>();
		Map<String, FieldMeta> columnNameLowerMap = new HashMap<>();
		FieldMeta pkField = null;

		for (Field field : getAllDeclaredFields(clazz)) {
			int mod = field.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || field.isAnnotationPresent(NotColumn.class)) {
				continue;
			}

			FieldMeta meta = parseField(field);
			fieldList.add(meta);
			fieldNameMap.put(meta.getField().getName(), meta);
			columnNameMap.put(meta.getColumnName(), meta);
			columnNameLowerMap.put(meta.getColumnName().toLowerCase(), meta);

			if (meta.getFieldKind() == FieldKind.PRIMARY_KEY_AUTO_INCREMENT) {
				if (pkField != null) {
					throw new IllegalStateException("实体类只能有一个自增主键: " + clazz);
				}
				pkField = meta;
			}
		}

		return new EntityMeta(clazz, tableName, pkField,
				Collections.unmodifiableList(fieldList),
				Collections.unmodifiableMap(fieldNameMap),
				Collections.unmodifiableMap(columnNameMap),
				Collections.unmodifiableMap(columnNameLowerMap),
				Collections.unmodifiableList(getAllDeclaredFields(clazz)));
	}

	private static List<Field> getAllDeclaredFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			fields.addAll(Arrays.asList(current.getDeclaredFields()));
			current = current.getSuperclass();
		}
		return fields;
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
		Column col = field.getAnnotation(Column.class);
		String columnName = col != null && !col.name().isEmpty() ? col.name() : StringUtils.camelToSnake(fieldName);
		boolean pk = field.isAnnotationPresent(PrimaryKey.class);
		if (pk) {
			boolean autoIncrement = field.getAnnotation(PrimaryKey.class).autoIncrement();
			if (autoIncrement) {
				fieldKind = FieldKind.PRIMARY_KEY_AUTO_INCREMENT;
			}
		}

		boolean isEnum = field.getType().isEnum();
		Field enumValueField = null;
		if (isEnum) {
			fieldKind = FieldKind.ENUM;
			enumValueField = findEnumValueField((Class<? extends Enum<?>>) field.getType());
		}

		return new FieldMeta(field, fieldKind, field.getGenericType(), columnName, enumValueField);
	}

	private static Field findEnumValueField(Class<? extends Enum<?>> enumClass) {
		return ENUM_VALUE_FIELD_CACHE.computeIfAbsent(enumClass, cls -> {
			for (Field f : cls.getDeclaredFields()) {
				if (f.isAnnotationPresent(EnumValue.class)) {
					f.setAccessible(true);
					return f;
				}
			}
			return null;
		});
	}
}
