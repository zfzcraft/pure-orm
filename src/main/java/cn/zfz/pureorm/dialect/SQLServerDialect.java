package cn.zfz.pureorm.dialect;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.upsert.LambadaUpsertWrapper;
import cn.zfz.pureorm.crud.upsert.UpsertWrapper;

public class SQLServerDialect implements Dialect {

    // 1. 字段包裹符：SQL Server 用 []
    @Override
    public String wrap(String name) {
        return "[" + name + "]";
    }

    // 2. FOR UPDATE 特殊处理：SQL Server 用 WITH (UPDLOCK, HOLDLOCK) 等效 FOR UPDATE
    @Override
    public String forUpdate() {
        // SQL Server 没有原生 FOR UPDATE，用 UPDLOCK+HOLDLOCK 实现行级排他锁
        return " WITH (UPDLOCK, HOLDLOCK)";
    }

    // 3. UPSERT 语法：SQL Server 用 MERGE 或 INSERT ... ON CONFLICT（2022+支持）
    @Override
    public String upsertSql(String tableName, String[] insertColumns, String[] updateColumns) {
        // 兼容 SQL Server 2012+ 主流版本：用 MERGE 实现 UPSERT
        StringBuilder sql = new StringBuilder();
        
        // 1. MERGE 基础模板
        sql.append("MERGE INTO ").append(wrap(tableName)).append(" AS TARGET ");
        sql.append("USING (SELECT ");
        
        // 2. 拼接插入值占位符
        for (int i = 0; i < insertColumns.length; i++) {
            if (i > 0) sql.append(", ");
            sql.append("? AS ").append(wrap(insertColumns[i]));
        }
        sql.append(") AS SOURCE ");
        
        // 3. 关联条件（主键匹配，这里默认取第一个列作为主键，你也可以扩展传主键名）
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

    // 4. 分页语法：SQL Server 2012+ 用 OFFSET/FETCH
    @Override
    public String buildPageSql(String sql, long offset, int limit) {
        // 注意：SQL Server 的 OFFSET 从 0 开始，和 MySQL 一致
        return sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    }

	@Override
	public <W extends UpsertWrapper<W, E>,E> SqlAndParams buildUpsertSql(UpsertWrapper<W,E> wrapper) {
		 StringBuilder sql = new StringBuilder();
	        sql.append("MERGE INTO ").append(wrapper.getTableName()).append(" t USING (VALUES ()) AS src ON (t.")
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