package cn.zfz.pureorm.core;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import cn.zfz.pureorm.annotations.OneToMany;
import cn.zfz.pureorm.annotations.OneToOne;

public final class AutoEntityMapper {
    // 反射缓存
    private static final Map<Class<?>, EntityStruct> STRUCT_CACHE = new HashMap<>();

    private static class EntityStruct {
        Class<?> type;
        String prefix;
        Map<String, Field> plainFields = new HashMap<>();
        Map<String, EntityStruct> oneToOne = new HashMap<>();
        Map<String, EntityStruct> oneToMany = new HashMap<>();
    }

    // 对外唯一入口
    public static <T> List<T> map(ResultSet rs, Class<T> rootClass) throws SQLException {
        if (rs == null || rootClass == null) return Collections.emptyList();

        EntityStruct rootStruct = getEntityStruct(rootClass);
        List<Map<String, Map<String, Object>>> rows = new ArrayList<>();
        
        // 读取所有行
        while (rs.next()) {
            rows.add(splitRowByPrefix(rs));
        }

        // 构建全局树（全层级去重）
        List<EntityNode> rootTree = buildGlobalTree(rows, rootStruct);
        // 转实体
        return toEntityList(rootTree, rootStruct);
    }

    // 带缓存解析实体结构
    private static EntityStruct getEntityStruct(Class<?> clazz) {
        if (STRUCT_CACHE.containsKey(clazz)) {
            return STRUCT_CACHE.get(clazz);
        }

        EntityStruct struct = new EntityStruct();
        struct.type = clazz;
        struct.prefix = lowerFirst(clazz.getSimpleName());

        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(OneToOne.class)) {
                Class<?> target = field.getType();
                EntityStruct child = getEntityStruct(target);
                struct.oneToOne.put(child.prefix, child);
                continue;
            }

            if (field.isAnnotationPresent(OneToMany.class)) {
                Type genType = field.getGenericType();
                Class<?> genericClazz = (Class<?>) ((ParameterizedType) genType).getActualTypeArguments()[0];
                EntityStruct child = getEntityStruct(genericClazz);
                struct.oneToMany.put(child.prefix, child);
                continue;
            }

            struct.plainFields.put(field.getName(), field);
        }

        STRUCT_CACHE.put(clazz, struct);
        return struct;
    }

    // 拆分每行数据
    private static Map<String, Map<String, Object>> splitRowByPrefix(ResultSet rs) throws SQLException {
        Map<String, Map<String, Object>> row = new HashMap<>();
        ResultSetMetaData  meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        for (int i = 1; i <= colCount; i++) {
            String label = meta.getColumnLabel(i);
            Object value = rs.getObject(i);

            String[] parts = label.split("\\.", 2);
            if (parts.length != 2) continue;

            String prefix = parts[0];
            String field = parts[1];
            row.computeIfAbsent(prefix, k -> new HashMap<>()).put(field, value);
        }
        return row;
    }

    // 构建全局树（核心：全层级去重）
    private static List<EntityNode> buildGlobalTree(List<Map<String, Map<String, Object>>> rows,
                                                    EntityStruct rootStruct) {
        List<EntityNode> globalRoots = new ArrayList<>();

        for (Map<String, Map<String, Object>> row : rows) {
            // 1. 构建本行所有节点
            Map<String, EntityNode> nodeMap = new HashMap<>();
            row.forEach((prefix, data) -> nodeMap.put(prefix, new EntityNode(prefix, data)));

            // 2. 根节点去重
            EntityNode rootNode = nodeMap.get(rootStruct.prefix);
            if (rootNode == null) continue;

            int idx = globalRoots.indexOf(rootNode);
            if (idx != -1) {
                rootNode = globalRoots.get(idx);
            } else {
                globalRoots.add(rootNode);
            }

            // 3. 递归挂载子节点（全层级去重）
            mountChildren(rootNode, rootStruct, nodeMap);
        }
        return globalRoots;
    }

    // 递归挂载子节点（核心修复：每一层都做去重）
    private static void mountChildren(EntityNode parent,
                                     EntityStruct parentStruct,
                                     Map<String, EntityNode> rowNodes) {
        // 处理一对一
        for (Entry<String, EntityStruct> entry : parentStruct.oneToOne.entrySet()) {
            String childPrefix = entry.getKey();
            EntityStruct childStruct = entry.getValue();
            EntityNode childNode = rowNodes.get(childPrefix);
            
            if (childNode == null) continue;

            // 父节点下的一对一节点去重
            EntityNode existChild = parent.getOneToOne(childPrefix);
            if (existChild == null) {
                parent.putOneToOne(childPrefix, childNode);
                existChild = childNode;
            }

            // 递归处理子节点的子节点
            mountChildren(existChild, childStruct, rowNodes);
        }

        // 处理一对多（核心修复：调用带缓存的addOneToMany）
        for (Entry<String, EntityStruct> entry : parentStruct.oneToMany.entrySet()) {
            String childPrefix = entry.getKey();
            EntityStruct childStruct = entry.getValue();
            EntityNode childNode = rowNodes.get(childPrefix);
            
            if (childNode == null) continue;

            // 调用带缓存的add方法，自动去重
            parent.addOneToMany(childNode);
            // 递归处理子节点的子节点
            mountChildren(childNode, childStruct, rowNodes);
        }
    }

    // 节点转实体
    private static <T> List<T> toEntityList(List<EntityNode> roots, EntityStruct struct) {
        List<T> list = new ArrayList<>();
        for (EntityNode root : roots) {
            list.add(toEntity(root, struct));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
	private static <T> T toEntity(EntityNode node, EntityStruct struct) {
        try {
            T inst = (T) struct.type.getDeclaredConstructor().newInstance();
            Map<String, Object> data = node.getData();

            // 普通字段赋值
            for (Field f : struct.plainFields.values()) {
                f.set(inst, data.get(f.getName()));
            }

            // 一对一赋值
            for (Entry<String, EntityStruct> entry : struct.oneToOne.entrySet()) {
                String prefix = entry.getKey();
                EntityNode childNode = node.getOneToOne(prefix);
                if (childNode == null) continue;

                Field f = findOneToOneField(struct.type, prefix);
                f.set(inst, toEntity(childNode, entry.getValue()));
            }

            // 一对多赋值
            for (Entry<String, EntityStruct> entry : struct.oneToMany.entrySet()) {
                String prefix = entry.getKey();
                List<Object> children = new ArrayList<>();
                
                for (EntityNode n : node.getOneToManyNodes()) {
                    if (prefix.equals(n.getPrefix())) {
                        children.add(toEntity(n, entry.getValue()));
                    }
                }

                Field f = findOneToManyField(struct.type, prefix);
                f.set(inst, children);
            }

            return inst;
        } catch (Exception e) {
            throw new RuntimeException("映射实体失败：" + struct.type, e);
        }
    }

    // 工具方法
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static String lowerFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        char[] c = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private static Field findOneToOneField(Class<?> owner, String targetPrefix) {
        for (Field f : owner.getDeclaredFields()) {
            if (f.isAnnotationPresent(OneToOne.class)) {
                if (targetPrefix.equals(lowerFirst(f.getType().getSimpleName()))) {
                    f.setAccessible(true);
                    return f;
                }
            }
        }
        throw new IllegalArgumentException("未匹配到 @OneToOne 字段：" + targetPrefix);
    }

    private static Field findOneToManyField(Class<?> owner, String targetPrefix) {
        for (Field f : owner.getDeclaredFields()) {
            if (f.isAnnotationPresent(OneToMany.class)) {
                Type type = f.getGenericType();
                Class<?> genericClazz = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
                if (targetPrefix.equals(lowerFirst(genericClazz.getSimpleName()))) {
                    f.setAccessible(true);
                    return f;
                }
            }
        }
        throw new IllegalArgumentException("未匹配到 @OneToMany 字段：" + targetPrefix);
    }

    private AutoEntityMapper() {}
}