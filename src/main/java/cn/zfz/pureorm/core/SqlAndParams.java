package cn.zfz.pureorm.core;

import java.util.List;

//复用你已有的
public class SqlAndParams {
    private final String sql;
    private final List<Object> params;
    public SqlAndParams(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParams() {
        return params;
    }
}
