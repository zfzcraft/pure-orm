package cn.zfz.pureorm.crud.insert;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.dialect.Dialect;

public class InsertSqlBuilder {

    public static <W extends InsertWrapper<W, T>, T> SqlAndParams buildSql(InsertWrapper<W,T> wrapper, Dialect dialect) {
        validateTableAndColumns(wrapper.getTableName(), wrapper.getColumnNames());
        return buildSingleInsertSql(wrapper.getTableName(), wrapper.getColumnNames(), wrapper.getColumnValues(), dialect);
    }

    public static SqlAndParams buildBatchInsertSql(
            String tableName,
            List<String> columnNames,
            List<List<Object>> batchValues,
            Dialect dialect
    ) {
        validateTableAndColumns(tableName, columnNames);
        if (batchValues.isEmpty()) {
            throw new IllegalArgumentException("批量插入值不能为空");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(dialect.wrap(tableName)).append(" (");
        appendColumnNames(sql, columnNames, dialect);
        sql.append(") VALUES ");

        List<Object> allParams = new ArrayList<>();
        for (int i = 0; i < batchValues.size(); i++) {
            List<Object> rowValues = batchValues.get(i);
            if (rowValues.size() != columnNames.size()) {
                throw new IllegalArgumentException("第" + (i + 1) + "行值数量和字段数量不匹配");
            }
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(");
            appendPlaceholders(sql, rowValues.size());
            sql.append(")");
            allParams.addAll(rowValues);
        }

        return new SqlAndParams(sql.toString(), allParams);
    }

    private static SqlAndParams buildSingleInsertSql(
            String tableName,
            List<String> columnNames,
            List<Object> columnValues,
            Dialect dialect
    ) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(dialect.wrap(tableName)).append(" (");
        appendColumnNames(sql, columnNames, dialect);
        sql.append(") VALUES (");
        appendPlaceholders(sql, columnNames.size());
        sql.append(")");

        return new SqlAndParams(sql.toString(), new ArrayList<>(columnValues));
    }

    private static void validateTableAndColumns(String tableName, List<String> columnNames) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("表名不能为空");
        }
        if (columnNames.isEmpty()) {
            throw new IllegalArgumentException("插入字段不能为空");
        }
    }

    private static void appendColumnNames(StringBuilder sql, List<String> columnNames, Dialect dialect) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(dialect.wrap(columnNames.get(i)));
        }
    }

    private static void appendPlaceholders(StringBuilder sql, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
    }
}
