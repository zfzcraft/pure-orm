package cn.zfz.pureorm.cache;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class EntityMeta {

    private final Class<?> entityClass;
    private final String tableName;
    private final FieldMeta primaryKeyField;
    private final List<FieldMeta> fieldList;
    private final Map<String, FieldMeta> fieldNameMap;
    private final Map<String, FieldMeta> columnNameMap;
    private final Map<String, FieldMeta> columnNameLowerMap;
    private final List<Field> allDeclaredFields;

    public EntityMeta(Class<?> entityClass, String tableName, FieldMeta primaryKeyField, List<FieldMeta> fieldList, Map<String, FieldMeta> fieldNameMap, Map<String, FieldMeta> columnNameMap, Map<String, FieldMeta> columnNameLowerMap, List<Field> allDeclaredFields) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.primaryKeyField = primaryKeyField;
        this.fieldList = fieldList;
        this.fieldNameMap = fieldNameMap;
        this.columnNameMap = columnNameMap;
        this.columnNameLowerMap = columnNameLowerMap;
        this.allDeclaredFields = allDeclaredFields;
    }

    public Class<?> getEntityClass() { return entityClass; }
    public String getTableName() { return tableName; }
    public FieldMeta getPrimaryKeyField() { return primaryKeyField; }
    public List<FieldMeta> getFieldList() { return fieldList; }
    public Map<String, FieldMeta> getFieldNameMap() { return fieldNameMap; }
    public Map<String, FieldMeta> getColumnNameMap() { return columnNameMap; }
    public Map<String, FieldMeta> getColumnNameLowerMap() { return columnNameLowerMap; }
    public List<Field> getAllDeclaredFields() { return allDeclaredFields; }
}

