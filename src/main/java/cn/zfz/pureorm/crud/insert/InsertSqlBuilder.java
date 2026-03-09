package cn.zfz.pureorm.crud.insert;

import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.dialect.Dialect;

import java.util.ArrayList;

// 插入 SQL 生成器（单条 + 批量）
public class InsertSqlBuilder {

    // ====================== 单条插入 ======================
    public static <W extends InsertWrapper<W, T>, T> SqlAndParams buildSql(InsertWrapper<W,T> wrapper, Dialect dialect) {
        // 校验必填
        if (wrapper.getTableName() == null || wrapper.getTableName().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空");
        }
        if (wrapper.getColumnNames().isEmpty()) {
            throw new IllegalArgumentException("插入字段不能为空");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        // 表名（方言包裹）
        sql.append(dialect.wrap(wrapper.getTableName())).append(" (");

        // 拼接字段名
        List<String> columns = wrapper.getColumnNames();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(dialect.wrap(columns.get(i)));
        }
        sql.append(") VALUES (");

        // 拼接占位符
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");

        // 返回 SQL + 参数
        return new SqlAndParams(
                sql.toString(),
                new ArrayList<>(wrapper.getColumnValues())
        );
    }

    // ====================== 批量插入 ======================
    // 参数：tableName + 字段列表 + 多组值列表
    public static SqlAndParams buildBatchInsertSql(
            String tableName,
            List<String> columnNames,
            List<List<Object>> batchValues,
            Dialect dialect
    ) {
        // 校验必填
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("表名不能为空");
        }
        if (columnNames.isEmpty()) {
            throw new IllegalArgumentException("插入字段不能为空");
        }
        if (batchValues.isEmpty()) {
            throw new IllegalArgumentException("批量插入值不能为空");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        // 表名（方言包裹）
        sql.append(dialect.wrap(tableName)).append(" (");

        // 拼接字段名
        for (int i = 0; i < columnNames.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(dialect.wrap(columnNames.get(i)));
        }
        sql.append(") VALUES ");

        // 拼接多组值占位符
        List<Object> allParams = new ArrayList<>();
        for (int i = 0; i < batchValues.size(); i++) {
            List<Object> rowValues = batchValues.get(i);
            // 校验字段数和值数一致
            if (rowValues.size() != columnNames.size()) {
                throw new IllegalArgumentException("第" + (i + 1) + "行值数量和字段数量不匹配");
            }

            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(");
            for (int j = 0; j < rowValues.size(); j++) {
                if (j > 0) {
                    sql.append(", ");
                }
                sql.append("?");
                allParams.add(rowValues.get(j));
            }
            sql.append(")");
        }

        // 返回 SQL + 所有参数
        return new SqlAndParams(sql.toString(), allParams);
    }
}
