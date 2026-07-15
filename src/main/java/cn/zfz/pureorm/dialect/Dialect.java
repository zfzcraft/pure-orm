package cn.zfz.pureorm.dialect;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.upsert.UpsertWrapper;

public interface Dialect {

    // 1. 字段/表名包裹符（适配 SQL Server 的 []）
    String wrap(String name);

    // 2. FOR UPDATE 锁语法（SQL Server 特殊处理）
    default String forUpdate() {
        return " FOR UPDATE";
    }
    
    default String forShare() {
        return " FOR SHARE";
    }

    // 3. UPSERT 语法（各库差异核心）
    String upsertSql(String tableName, String[] insertColumns, String[] updateColumns);

    // 4. 分页语法（各库参数顺序不同，由方言返回 SqlAndParams）
    default SqlAndParams buildPageSql(String sql, long offset, int limit) {
        return new SqlAndParams(sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                java.util.Arrays.asList(offset, limit));
    }
    
    <W extends UpsertWrapper<W, E>,E> SqlAndParams buildUpsertSql(UpsertWrapper<W,E> wrapper);

}