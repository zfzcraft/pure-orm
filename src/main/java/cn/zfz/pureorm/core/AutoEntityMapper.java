package cn.zfz.pureorm.core;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import cn.zfz.pureorm.annotations.NotColumn;
import cn.zfz.pureorm.annotations.OneToMany;
import cn.zfz.pureorm.annotations.OneToOne;
import cn.zfz.pureorm.cache.EntityMeta;
import cn.zfz.pureorm.cache.EntityMetaCache;
import cn.zfz.pureorm.cache.FieldMeta;
import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.EnumTypeHandler;

public final class AutoEntityMapper {

	private static final Map<StructCacheKey, EntityStruct> STRUCT_CACHE = new ConcurrentHashMap<>();

	private static class StructCacheKey {
		final Class<?> clazz;
		final String prefix;

		StructCacheKey(Class<?> clazz, String prefix) {
			this.clazz = clazz;
			this.prefix = prefix;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof StructCacheKey)) return false;
			StructCacheKey that = (StructCacheKey) o;
			return Objects.equals(clazz, that.clazz) && Objects.equals(prefix, that.prefix);
		}

		@Override
		public int hashCode() {
			return Objects.hash(clazz, prefix);
		}
	}

	private static class EntityStruct {
		Class<?> type;
		String prefix;
		EntityMeta entityMeta;
		Map<String, FieldMeta> columnToFieldMap;
		Map<String, Field> nestedOneToOne = new HashMap<>();
		Map<String, Field> nestedOneToMany = new HashMap<>();
		Map<String, EntityStruct> oneToOneStructs = new HashMap<>();
		Map<String, EntityStruct> oneToManyStructs = new HashMap<>();
		String primaryKeyColumn;
	}

	public static <T> List<T> map(ResultSet rs, Class<T> rootClass) throws SQLException {
		return map(rs, rootClass, rootClass);
	}

	public static <T> List<T> map(ResultSet rs, Class<T> resultType, Class<?> rootEntityClass) throws SQLException {
		if (rs == null || resultType == null) {
			return Collections.emptyList();
		}

		String rootPrefix = rootEntityClass.getSimpleName().toLowerCase();
		EntityStruct rootStruct = getEntityStruct(resultType, rootPrefix);
		List<Map<String, Map<String, Object>>> rows = readAllRows(rs);

		List<EntityNode> rootTree = buildGlobalTree(rows, rootStruct);
		return toEntityList(rootTree, rootStruct);
	}

	private static List<Map<String, Map<String, Object>>> readAllRows(ResultSet rs) throws SQLException {
		List<Map<String, Map<String, Object>>> rows = new ArrayList<>();
		ResultSetMetaData meta = rs.getMetaData();
		int colCount = meta.getColumnCount();

		List<String> columnLabels = new ArrayList<>(colCount);
		for (int i = 1; i <= colCount; i++) {
			columnLabels.add(meta.getColumnLabel(i));
		}

		while (rs.next()) {
			Map<String, Map<String, Object>> row = new HashMap<>();
			for (int i = 1; i <= colCount; i++) {
				String label = columnLabels.get(i - 1);
				Object value = rs.getObject(i);

				int dotIdx = label.indexOf('.');
				if (dotIdx <= 0 || dotIdx == label.length() - 1) {
					continue;
				}

				String prefix = label.substring(0, dotIdx).toLowerCase();
				String column = label.substring(dotIdx + 1).toLowerCase();
				row.computeIfAbsent(prefix, k -> new HashMap<>()).put(column, value);
			}
			rows.add(row);
		}
		return rows;
	}

	private static EntityStruct getEntityStruct(Class<?> clazz, String prefix) {
		StructCacheKey key = new StructCacheKey(clazz, prefix);
		EntityStruct cached = STRUCT_CACHE.get(key);
		if (cached != null) {
			return cached;
		}
		EntityStruct struct = buildEntityStruct(clazz, prefix);
		STRUCT_CACHE.put(key, struct);
		return struct;
	}

	private static EntityStruct buildEntityStruct(Class<?> clazz, String prefix) {
		EntityStruct struct = new EntityStruct();
		struct.type = clazz;
		struct.prefix = prefix;

		EntityMeta entityMeta = EntityMetaCache.getEntityMeta(clazz);
		struct.entityMeta = entityMeta;
		struct.columnToFieldMap = entityMeta.getColumnNameLowerMap();

		FieldMeta pkField = entityMeta.getPrimaryKeyField();
		if (pkField != null) {
			struct.primaryKeyColumn = pkField.getColumnName().toLowerCase();
		}

		for (Field field : entityMeta.getAllDeclaredFields()) {
			field.setAccessible(true);

			if (isOneToOne(field)) {
				Class<?> target = field.getType();
				String childPrefix = getNestedPrefix(target);
				EntityStruct child = getEntityStruct(target, childPrefix);
				struct.oneToOneStructs.put(child.prefix, child);
				struct.nestedOneToOne.put(child.prefix, field);
				continue;
			}

			if (isOneToMany(field)) {
				Type genType = field.getGenericType();
				Class<?> genericClazz = (Class<?>) ((ParameterizedType) genType).getActualTypeArguments()[0];
				String childPrefix = getNestedPrefix(genericClazz);
				EntityStruct child = getEntityStruct(genericClazz, childPrefix);
				struct.oneToManyStructs.put(child.prefix, child);
				struct.nestedOneToMany.put(child.prefix, field);
				continue;
			}

			if (field.getAnnotation(NotColumn.class) != null) {
				continue;
			}
		}

		return struct;
	}

	private static boolean isOneToOne(Field field) {
		if (field.getAnnotation(OneToOne.class) != null) {
			return true;
		}
		Class<?> type = field.getType();
		return isEntityLike(type);
	}

	private static boolean isOneToMany(Field field) {
		if (field.getAnnotation(OneToMany.class) != null) {
			return true;
		}
		if (!Collection.class.isAssignableFrom(field.getType())) {
			return false;
		}
		Type genType = field.getGenericType();
		if (!(genType instanceof ParameterizedType)) {
			return false;
		}
		Type[] typeArgs = ((ParameterizedType) genType).getActualTypeArguments();
		if (typeArgs.length == 0 || !(typeArgs[0] instanceof Class)) {
			return false;
		}
		Class<?> genericClazz = (Class<?>) typeArgs[0];
		return isEntityLike(genericClazz);
	}

	private static boolean isEntityLike(Class<?> type) {
		if (type.isPrimitive() || type.isEnum() || type.isArray()) {
			return false;
		}
		if (type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class
				|| type == Character.class || type == Date.class) {
			return false;
		}
		if (type.getName().startsWith("java.") || type.getName().startsWith("javax.")) {
			return false;
		}
		try {
			EntityMetaCache.getEntityMeta(type);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static String getNestedPrefix(Class<?> targetType) {
		return targetType.getSimpleName().toLowerCase();
	}

	private static List<EntityNode> buildGlobalTree(List<Map<String, Map<String, Object>>> rows,
			EntityStruct rootStruct) {
		List<EntityNode> globalRoots = new ArrayList<>();
		Map<String, EntityNode> rootPkCache = new HashMap<>();

		for (Map<String, Map<String, Object>> row : rows) {
			Map<String, EntityNode> nodeMap = new HashMap<>();
			row.forEach((prefix, data) -> nodeMap.put(prefix, new EntityNode(prefix, data)));

			EntityNode rootNode = nodeMap.get(rootStruct.prefix);
			if (rootNode == null) {
				continue;
			}

			String rootPk = getPkValue(rootNode, rootStruct);
			EntityNode existingRoot = null;
			if (rootPk != null) {
				existingRoot = rootPkCache.get(rootPk);
			} else {
				int idx = globalRoots.indexOf(rootNode);
				if (idx != -1) {
					existingRoot = globalRoots.get(idx);
				}
			}

			if (existingRoot != null) {
				rootNode = existingRoot;
			} else {
				globalRoots.add(rootNode);
				if (rootPk != null) {
					rootPkCache.put(rootPk, rootNode);
				}
			}

			mountChildren(rootNode, rootStruct, nodeMap);
		}
		return globalRoots;
	}

	private static String getPkValue(EntityNode node, EntityStruct struct) {
		if (struct.primaryKeyColumn == null) {
			return null;
		}
		Object pk = node.getData().get(struct.primaryKeyColumn);
		return pk == null ? null : pk.toString();
	}

	private static void mountChildren(EntityNode parent, EntityStruct parentStruct,
			Map<String, EntityNode> rowNodes) {
		Map<String, EntityNode> oneToManyPkCache = new HashMap<>();
		for (EntityNode existing : parent.getOneToManyNodes()) {
			String prefix = existing.getPrefix();
			EntityStruct childStruct = parentStruct.oneToManyStructs.get(prefix);
			if (childStruct != null) {
				String pk = getPkValue(existing, childStruct);
				if (pk != null) {
					oneToManyPkCache.put(prefix + ":" + pk, existing);
				}
			}
		}

		for (Entry<String, EntityStruct> entry : parentStruct.oneToOneStructs.entrySet()) {
			String childPrefix = entry.getKey();
			EntityStruct childStruct = entry.getValue();
			EntityNode childNode = rowNodes.get(childPrefix);

			if (childNode == null || isAllNull(childNode.getData())) {
				continue;
			}

			EntityNode existChild = parent.getOneToOne(childPrefix);
			if (existChild == null) {
				parent.putOneToOne(childPrefix, childNode);
				existChild = childNode;
			}

			mountChildren(existChild, childStruct, rowNodes);
		}

		for (Entry<String, EntityStruct> entry : parentStruct.oneToManyStructs.entrySet()) {
			String childPrefix = entry.getKey();
			EntityStruct childStruct = entry.getValue();
			EntityNode childNode = rowNodes.get(childPrefix);

			if (childNode == null || isAllNull(childNode.getData())) {
				continue;
			}

			String childPk = getPkValue(childNode, childStruct);
			EntityNode existingChild = null;
			if (childPk != null) {
				existingChild = oneToManyPkCache.get(childPrefix + ":" + childPk);
			}

			if (existingChild == null) {
				parent.addOneToMany(childNode);
				if (childPk != null) {
					oneToManyPkCache.put(childPrefix + ":" + childPk, childNode);
				}
				existingChild = childNode;
			}

			mountChildren(existingChild, childStruct, rowNodes);
		}
	}

	private static boolean isAllNull(Map<String, Object> data) {
		if (data == null || data.isEmpty()) {
			return true;
		}
		for (Object v : data.values()) {
			if (v != null) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> toEntityList(List<EntityNode> roots, EntityStruct struct) {
		List<T> list = new ArrayList<>(roots.size());
		for (EntityNode root : roots) {
			list.add((T) toEntity(root, struct));
		}
		return list;
	}

	private static Object toEntity(EntityNode node, EntityStruct struct) {
		try {
			Object inst = struct.type.getDeclaredConstructor().newInstance();
			Map<String, Object> data = node.getData();

			for (Entry<String, FieldMeta> entry : struct.columnToFieldMap.entrySet()) {
				String column = entry.getKey();
				FieldMeta fieldMeta = entry.getValue();
				if (!data.containsKey(column)) {
					continue;
				}
				Object value = data.get(column);
				if (value == null) {
					continue;
				}

				Field field = fieldMeta.getField();
				field.setAccessible(true);

				try {
					if (fieldMeta.getFieldKind() == FieldKind.ENUM) {
						EnumTypeHandler enumTypeHandler = fieldMeta.getEnumTypeHandler();
						Object javaValue = enumTypeHandler.toJava((Class<? extends Enum<?>>) field.getType(), value);
						field.set(inst, javaValue);
					} else {
						field.set(inst, convertValue(value, field.getType()));
					}
				} catch (Exception e) {
					throw new PureOrmException("映射字段失败：" + field.getName(), e);
				}
			}

			for (Entry<String, EntityStruct> entry : struct.oneToOneStructs.entrySet()) {
				String prefix = entry.getKey();
				EntityNode childNode = node.getOneToOne(prefix);
				if (childNode == null) {
					continue;
				}

				Field f = struct.nestedOneToOne.get(prefix);
				f.setAccessible(true);
				f.set(inst, toEntity(childNode, entry.getValue()));
			}

			for (Entry<String, EntityStruct> entry : struct.oneToManyStructs.entrySet()) {
				String prefix = entry.getKey();
				List<Object> children = new ArrayList<>();

				for (EntityNode n : node.getOneToManyNodes()) {
					if (prefix.equals(n.getPrefix())) {
						children.add(toEntity(n, entry.getValue()));
					}
				}

				Field f = struct.nestedOneToMany.get(prefix);
				f.setAccessible(true);
				f.set(inst, children);
			}

			return inst;
		} catch (Exception e) {
			throw new PureOrmException("映射实体失败：" + struct.type.getName(), e);
		}
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

	private AutoEntityMapper() {
	}
}
