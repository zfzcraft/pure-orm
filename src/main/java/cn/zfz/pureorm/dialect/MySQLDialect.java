package cn.zfz.pureorm.dialect;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.upsert.UpsertWrapper;

public class MySQLDialect implements Dialect {
    @Override
    public String wrap(String name) {
        return "`" + name + "`";
    }

    @Override
    public String upsertSql(String tableName, String[] insertColumns, String[] updateColumns) {
        StringBuilder sql = new StringBuilder();
        // INSERT 基础
        sql.append("INSERT INTO ").append(wrap(tableName)).append(" (");
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append(wrap(insertColumns[i]));
        }
        sql.append(") VALUES (");
        // 占位符
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        // ON DUPLICATE KEY UPDATE
        sql.append(") ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < updateColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append(wrap(updateColumns[i])).append(" = ?");
        }
        return sql.toString();
    }

    @Override
    public String buildPageSql(String sql, long offset, int limit) {
        return sql + " LIMIT ?, ?";
    }
    @Override
    public <W extends UpsertWrapper<W, E>,E> SqlAndParams buildUpsertSql(UpsertWrapper<W,E> wrapper) {
    	
    	StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(wrapper.getTableName()).append(" (");
        appendColumns(sql, wrapper.getInsertColumnNames());
        sql.append(") VALUES (");
        appendPlaceholders(sql, wrapper.getInsertColumnNames().size());
        sql.append(") ON DUPLICATE KEY UPDATE ");
        appendUpdateSet(sql, wrapper.getUpdateColumnNames());

        List<Object> params = new ArrayList<>();
        params.addAll(wrapper.getInsertColumnValues());
        params.addAll(wrapper.getUpdateColumnValues());

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