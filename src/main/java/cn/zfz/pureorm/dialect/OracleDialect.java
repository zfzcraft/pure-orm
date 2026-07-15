package cn.zfz.pureorm.dialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.upsert.UpsertWrapper;

public class OracleDialect implements Dialect {

    // 1. 字段/表名包裹符：Oracle 用双引号（区分大小写，默认大写）
    @Override
    public String wrap(String name) {
        // 建议统一转大写，符合 Oracle 命名规范
        return "\"" + name.toUpperCase() + "\"";
    }

    // 2. FOR UPDATE 语法：Oracle 原生支持，可追加 WAIT/NOWAIT（这里只保留基础版）
    @Override
    public String forUpdate() {
        // Oracle 12c+ 原生支持 FOR UPDATE，和 MySQL/Pg 一致
        // 高级特性（WAIT 5/NOWAIT）可让用户通过 conditionNative 自行拼接
        return " FOR UPDATE";
    }

    // 3. UPSERT 语法：Oracle 用 MERGE（12c+ 也支持 INSERT ... ON DUPLICATE KEY，但 MERGE 更通用）
    @Override
    public String upsertSql(String tableName, String[] insertColumns, String[] updateColumns) {
        StringBuilder sql = new StringBuilder();
        
        // 1. MERGE 基础模板（Oracle 标准 UPSERT 实现）
        sql.append("MERGE INTO ").append(wrap(tableName)).append(" TARGET ");
        sql.append("USING (SELECT ");
        
        // 2. 拼接插入值占位符（绑定变量）
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            // Oracle 占位符建议用 :n 格式，这里兼容 ?（JDBC 驱动会自动转换）
            sql.append("? AS ").append(wrap(insertColumns[i]));
        }
        sql.append(") SOURCE ");
        
        // 3. 关联条件（主键匹配，取第一个列作为主键）
        sql.append("ON (TARGET.").append(wrap(insertColumns[0])).append(" = SOURCE.").append(wrap(insertColumns[0])).append(") ");
        
        // 4. 匹配则更新
        sql.append("WHEN MATCHED THEN UPDATE SET ");
        for (int i = 0; i < updateColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append("TARGET.").append(wrap(updateColumns[i])).append(" = SOURCE.").append(wrap(updateColumns[i]));
        }
        
        // 5. 不匹配则插入
        sql.append(" WHEN NOT MATCHED THEN INSERT (");
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append(wrap(insertColumns[i]));
        }
        sql.append(") VALUES (");
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append("SOURCE.").append(wrap(insertColumns[i]));
        }
        sql.append(");");
        
        return sql.toString();
    }

    // 4. 分页语法：Oracle 12c+ 支持标准 OFFSET/FETCH（和 SQL Server 一致）
    @Override
    public SqlAndParams buildPageSql(String sql, long offset, int limit) {
        return new SqlAndParams(sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                Arrays.asList(offset, limit));
    }
    @Override
    public <W extends UpsertWrapper<W, E>,E> SqlAndParams buildUpsertSql(UpsertWrapper<W,E> wrapper) {
    	StringBuilder sql = new StringBuilder();
        sql.append("MERGE INTO ").append(wrapper.getTableName()).append(" t USING DUAL ON (t.")
           .append(wrapper.getConflictKey()).append(" = ?) ");

        sql.append("WHEN MATCHED THEN UPDATE SET ");
        appendUpdateSet(sql, wrapper.getUpdateColumnNames());

        sql.append(" WHEN NOT MATCHED THEN INSERT (");
        appendColumns(sql, wrapper.getInsertColumnNames());
        sql.append(") VALUES (");
        appendPlaceholders(sql, wrapper.getInsertColumnNames().size());
        sql.append(")");

        List<Object> params = new ArrayList<>();
        int keyIndex = wrapper.getInsertColumnNames().indexOf(wrapper.getConflictKey());
        params.add(wrapper.getInsertColumnValues().get(keyIndex));
        params.addAll(wrapper.getUpdateColumnValues());
        params.addAll(wrapper.getInsertColumnValues());

        return new SqlAndParams(sql.toString(), params);
    }

 // ====================== 工具方法 ======================
    private void appendColumns(StringBuilder sql, List<String> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(columns.get(i));
        }
    }

    private void appendPlaceholders(StringBuilder sql, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
    }

    private void appendUpdateSet(StringBuilder sql, List<String> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(columns.get(i)).append(" = ?");
        }
    }
}