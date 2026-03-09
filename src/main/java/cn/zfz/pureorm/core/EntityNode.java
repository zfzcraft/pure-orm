package cn.zfz.pureorm.core;

import java.util.*;

public class EntityNode {
    private final String prefix;
    private final Map<String, Object> data;
    // 一对一子节点（加缓存去重）
    private final Map<String, EntityNode> oneToOneNodes = new HashMap<>();
    // 一对多子节点（加缓存去重）
    private final Map<String, EntityNode> oneToManyCache = new HashMap<>();
    private final List<EntityNode> oneToManyNodes = new ArrayList<>();

    public EntityNode(String prefix, Map<String, Object> data) {
        this.prefix = prefix;
        this.data = new HashMap<>(data);
    }

    // 核心：去重比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityNode that = (EntityNode) o;
        return Objects.equals(prefix, that.prefix) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, data);
    }

    // 一对一挂载（自带去重）
    public void putOneToOne(String prefix, EntityNode node) {
        oneToOneNodes.putIfAbsent(prefix, node);
    }

    // 一对多挂载（核心修复：先查缓存，再追加）
    public void addOneToMany(EntityNode node) {
        String key = node.hashCode() + ""; // 用唯一hash做key
        if (!oneToManyCache.containsKey(key)) {
            oneToManyCache.put(key, node);
            oneToManyNodes.add(node);
        }
    }

    // getter
    public String getPrefix() { return prefix; }
    public Map<String, Object> getData() { return data; }
    public EntityNode getOneToOne(String prefix) { return oneToOneNodes.get(prefix); }
    public List<EntityNode> getOneToManyNodes() { return oneToManyNodes; }
}