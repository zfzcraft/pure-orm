package cn.zfz.pureorm.cache;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class EntityMeta {

    // 实体类
    private final Class<?> entityClass;
    // 表名
    private final String tableName;
    // 主键字段
    private final FieldMeta primaryKeyField;
    // 全部映射字段
    private final List<FieldMeta> fieldList;
    // 快速查找：实体字段名 → FieldMeta
    private final Map<String, FieldMeta> fieldNameMap;
    // 快速查找：数据库列名 → FieldMeta
    private final Map<String, FieldMeta> columnNameMap;
	

    
}
