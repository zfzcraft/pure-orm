package cn.zfz.pureorm.crud.update;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;
import cn.zfz.pureorm.crud.condition.ConditionSqlBuilder;
import cn.zfz.pureorm.crud.select.UpdateWrapper;
import cn.zfz.pureorm.dialect.Dialect;

public class UpdateSqlGenerator {

    public static SqlAndParams buildSql(
            UpdateWrapper<?,?> wrapper,
            Dialect dialect
    ) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(dialect.wrap(wrapper.getTableName())).append(" SET ");
        List<Object> params = new ArrayList<>();

        List<UpdateNode> nodes = wrapper.getUpdateNodes();

        for (int i = 0; i < nodes.size(); i++) {
            UpdateNode node = nodes.get(i);
            if (i > 0) sql.append(", ");

            switch (node.getType()) {
                case SET:
                    sql.append(dialect.wrap(node.getField())).append(" = ?");
                    params.add(node.getValue());
                    break;
                case INCR:
                    sql.append(dialect.wrap(node.getField())).append(" = ").append(dialect.wrap(node.getField())).append(" + ?");
                    params.add(node.getValue());
                    break;
                case DECR:
                    sql.append(dialect.wrap(node.getField())).append(" = ").append(dialect.wrap(node.getField())).append(" - ?");
                    params.add(node.getValue());
                    break;
                case NATIVE:
                    sql.append(node.getNativeSql());
                    break;
            }
        }

        // WHERE
        SqlAndParams where = ConditionSqlBuilder.buildWhere(wrapper.getConditionNodes());
        if (!where.getSql().isEmpty()) {
            sql.append(" WHERE ").append(where.getSql());
            params.addAll(where.getParams());
        }

        return new SqlAndParams(sql.toString(), params);
    }
}
