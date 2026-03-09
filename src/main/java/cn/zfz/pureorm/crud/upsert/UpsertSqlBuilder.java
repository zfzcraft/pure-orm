package cn.zfz.pureorm.crud.upsert;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.dialect.Dialect;

// Upsert SQL 生成器（兼容所有现代数据库）
public class UpsertSqlBuilder {

    /**
     * 构建 Upsert SQL
     * @param <W>
     * @param wrapper UpsertWrapper
     * @param dialect 方言
     * @return SQL + 参数
     */
    public static <E, W extends UpsertWrapper<W, E>> SqlAndParams buildSql(UpsertWrapper<W,E> wrapper, Dialect dialect) {
    	return dialect.buildUpsertSql(wrapper);
        
    }

    
}
