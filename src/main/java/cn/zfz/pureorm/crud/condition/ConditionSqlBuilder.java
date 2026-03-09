package cn.zfz.pureorm.crud.condition;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.SqlAndParams;

// 核心：拼接 WHERE 条件的 SQL + 参数
public class ConditionSqlBuilder {

    // 构建 WHERE 条件（返回 SQL 片段 + 参数列表）
    public static SqlAndParams buildWhere(List<ConditionNode> nodes) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // 遍历所有条件节点，按顺序拼接
        for (ConditionNode node : nodes) {
            ConditionType type = node.getType();
            switch (type) {
                // ========== 逻辑/括号 ==========
                case AND:
                    sql.append(" AND ");
                    break;
                case OR:
                    sql.append(" OR ");
                    break;
                case LEFT_PAREN:
                    sql.append(" ( ");
                    break;
                case RIGHT_PAREN:
                    sql.append(" ) ");
                    break;

                // ========== 原生 SQL ==========
                case NATIVE:
                    sql.append(node.getNativeSql());
                    break;

                // ========== 基础条件 ==========
                case EQ:
                    sql.append(node.getField()).append(" = ?");
                    params.add(node.getValues()[0]);
                    break;
                case NE:
                    sql.append(node.getField()).append(" != ?");
                    params.add(node.getValues()[0]);
                    break;
                case GT:
                    sql.append(node.getField()).append(" > ?");
                    params.add(node.getValues()[0]);
                    break;
                case GE:
                    sql.append(node.getField()).append(" >= ?");
                    params.add(node.getValues()[0]);
                    break;
                case LT:
                    sql.append(node.getField()).append(" < ?");
                    params.add(node.getValues()[0]);
                    break;
                case LE:
                    sql.append(node.getField()).append(" <= ?");
                    params.add(node.getValues()[0]);
                    break;

                // ========== 模糊查询 ==========
                case LIKE:
                    sql.append(node.getField()).append(" LIKE ?");
                    params.add("%" + node.getValues()[0] + "%");
                    break;
                case LIKE_LEFT:
                    sql.append(node.getField()).append(" LIKE ?");
                    params.add("%" + node.getValues()[0]);
                    break;
                case LIKE_RIGHT:
                    sql.append(node.getField()).append(" LIKE ?");
                    params.add(node.getValues()[0] + "%");
                    break;

                // ========== IN/NOT IN ==========
                case IN:
                    sql.append(node.getField()).append(" IN (");
                    Object[] inValues = node.getValues();
                    for (int i = 0; i < inValues.length; i++) {
                        if (i > 0) sql.append(", ");
                        sql.append("?");
                        params.add(inValues[i]);
                    }
                    sql.append(")");
                    break;
                case NOT_IN:
                    sql.append(node.getField()).append(" NOT IN (");
                    Object[] notInValues = node.getValues();
                    for (int i = 0; i < notInValues.length; i++) {
                        if (i > 0) sql.append(", ");
                        sql.append("?");
                        params.add(notInValues[i]);
                    }
                    sql.append(")");
                    break;

                // ========== 空值判断 ==========
                case IS_NULL:
                    sql.append(node.getField()).append(" IS NULL");
                    break;
                case IS_NOT_NULL:
                    sql.append(node.getField()).append(" IS NOT NULL");
                    break;

                // ========== 区间 ==========
                case BETWEEN:
                    sql.append(node.getField()).append(" BETWEEN ? AND ?");
                    params.add(node.getValues()[0]);
                    params.add(node.getValues()[1]);
                    break;

                // 兜底（防止漏枚举）
                default:
                    break;
            }
        }

        // 清理首尾多余的 AND/OR（可选，增强健壮性）
        String finalSql = sql.toString().trim();
        if (finalSql.startsWith("AND ")) finalSql = finalSql.substring(4);
        if (finalSql.startsWith("OR ")) finalSql = finalSql.substring(3);

        return new SqlAndParams(finalSql, params);
    }

}
