package cn.zfz.pureorm.crud.condition;

public class ConditionNode {

    private final ConditionType type;
    private String        field;    // 字段名：如 username
    private Object[]      values;   // 参数
    private String        nativeSql;// 原生 SQL（仅 NATIVE 使用）

    // 普通条件
    public ConditionNode(ConditionType type, String field, Object... values) {
        this.type = type;
        this.field = field;
        this.values = values;
    }

    // AND / OR / BEGIN / END
    public ConditionNode(ConditionType type) {
        this(type, null, (Object[]) null);
    }

    // NATIVE 专用
    public ConditionNode(String nativeSql) {
        this(ConditionType.NATIVE);
        this.nativeSql = nativeSql;
    }

    // getter
    public ConditionType getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public Object[] getValues() {
        return values;
    }

    public String getNativeSql() {
        return nativeSql;
    }
}