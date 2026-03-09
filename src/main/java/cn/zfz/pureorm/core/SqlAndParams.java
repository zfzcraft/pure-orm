package cn.zfz.pureorm.core;

import java.util.List;

import lombok.Data;

//复用你已有的
@Data
public class SqlAndParams {
    private final String sql;
    private final List<Object> params;
    public SqlAndParams(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params;
    }
}
