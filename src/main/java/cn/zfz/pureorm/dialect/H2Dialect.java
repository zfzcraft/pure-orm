package cn.zfz.pureorm.dialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.upsert.UpsertWrapper;

public class H2Dialect implements Dialect {

    @Override
    public String wrap(String name) {
        return "`" + name + "`";
    }

    @Override
    public String upsertSql(String tableName, String[] insertColumns, String[] updateColumns) {
        StringBuilder sql = new StringBuilder();
        sql.append("MERGE INTO ").append(wrap(tableName)).append(" (");
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append(wrap(insertColumns[i]));
        }
        sql.append(") KEY (id) VALUES (");
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        if (updateColumns.length > 0) {
            sql.append(") WHEN MATCHED THEN UPDATE SET ");
            for (int i = 0; i < updateColumns.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(wrap(updateColumns[i])).append(" = ?");
            }
        }
        return sql.toString();
    }

    @Override
    public SqlAndParams buildPageSql(String sql, long offset, int limit) {
        return new SqlAndParams(sql + " LIMIT ? OFFSET ?", Arrays.asList(limit, offset));
    }

    @Override
    public <W extends UpsertWrapper<W, E>, E> SqlAndParams buildUpsertSql(UpsertWrapper<W, E> wrapper) {
        StringBuilder sql = new StringBuilder();
        sql.append("MERGE INTO ").append(wrapper.getTableName()).append(" (");
        appendColumns(sql, wrapper.getInsertColumnNames());
        sql.append(") KEY (").append(wrapper.getConflictKey()).append(") VALUES (");
        appendPlaceholders(sql, wrapper.getInsertColumnNames().size());
        sql.append(")");

        List<Object> params = new ArrayList<>();
        params.addAll(wrapper.getInsertColumnValues());

        return new SqlAndParams(sql.toString(), params);
    }

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
}
