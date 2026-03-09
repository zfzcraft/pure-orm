//package cn.pureorm.crud;
//
//import java.util.*;
//
//import cn.pureorm.enums.Op;
//
//public class HighLevelSqlGenerator {
//
//    public static SqlResult generate(HighLevelSelectWrapper<?> wrapper) {
//        StringBuilder sql = new StringBuilder();
//        List<Object> params = new ArrayList<>();
//
//        buildSelect(wrapper, sql, params);
//        buildFrom(wrapper, sql);
//        buildJoin(wrapper, sql, params);
//        buildWhere(wrapper, sql, params);
//        buildOrderBy(wrapper, sql);
//        buildLimit(wrapper, sql);
//
//        return new SqlResult(sql.toString(), params);
//    }
//
//    private static void buildSelect(HighLevelSelectWrapper<?> w, StringBuilder sql, List<Object> params) {
//        sql.append("SELECT ");
//        List<SelectField> fields = w.getSelectFields();
//        for (int i = 0; i < fields.size(); i++) {
//            SelectField sf = fields.get(i);
//            LambdaResolver.LambdaInfo info = LambdaResolver.resolve(sf.getField());
//            String alias = w.getAliasMap().get(info.getEntityClass());
//
//            if (sf.isAggregate()) {
//                sql.append(sf.getFunc()).append("(").append(alias).append(".").append(info.getColumnName()).append(")");
//                sql.append(" AS ").append(sf.getAsName());
//            } else {
//                String uniqueAs = alias + "_" + info.getColumnName();
//                sql.append(alias).append(".").append(info.getColumnName()).append(" AS ").append(uniqueAs);
//            }
//            if (i < fields.size() - 1) sql.append(", ");
//        }
//    }
//
//    private static void buildFrom(HighLevelSelectWrapper<?> w, StringBuilder sql) {
//        sql.append(" FROM ").append(toTableName(w.getRootClass())).append(" ").append(w.getRootAlias());
//    }
//
//    private static void buildJoin(HighLevelSelectWrapper<?> w, StringBuilder sql, List<Object> params) {
//        for (JoinNode join : w.getJoins()) {
//            sql.append(" ").append(join.getType().name()).append(" JOIN ");
//            sql.append(toTableName(join.getEntityClass())).append(" ").append(join.getAlias());
//            sql.append(" ON ");
//            buildCondition(w, sql, params, join.getOn(), true);
//        }
//    }
//
//    private static void buildWhere(HighLevelSelectWrapper<?> w, StringBuilder sql, List<Object> params) {
//        if (!w.getWhere().getChildren().isEmpty()) {
//            sql.append(" WHERE ");
//            buildCondition(w, sql, params, w.getWhere(), false);
//        }
//    }
//
//    private static void buildCondition(HighLevelSelectWrapper<?> w, StringBuilder sql, List<Object> params,
//                                       HighLevelSelectWrapper.ConditionNode node, boolean isJoinOn) {
//        if (node.getChildren().isEmpty()) return;
//
//        boolean first = true;
//        for (HighLevelSelectWrapper.ConditionNode child : node.getChildren()) {
//            if (!first) sql.append(" ").append(node.getLogic().name()).append(" ");
//            first = false;
//
//            if (!child.getChildren().isEmpty()) {
//                sql.append("(");
//                buildCondition(w, sql, params, child, isJoinOn);
//                sql.append(")");
//                continue;
//            }
//
//            LambdaResolver.LambdaInfo info = LambdaResolver.resolve(child.getField());
//            String alias = w.getAliasMap().get(info.getEntityClass());
//            String col = info.getColumnName();
//            Op op = child.getOp();
//            Object val = child.getValue();
//
//            switch (op) {
//                case EQ -> sql.append(alias).append(".").append(col).append(" = ?");
//                case NE -> sql.append(alias).append(".").append(col).append(" != ?");
//                case GT -> sql.append(alias).append(".").append(col).append(" > ?");
//                case GE -> sql.append(alias).append(".").append(col).append(" >= ?");
//                case LT -> sql.append(alias).append(".").append(col).append(" < ?");
//                case LE -> sql.append(alias).append(".").append(col).append(" <= ?");
//                case LIKE -> sql.append(alias).append(".").append(col).append(" LIKE ?");
//                case IS_NULL -> sql.append(alias).append(".").append(col).append(" IS NULL");
//                case IS_NOT_NULL -> sql.append(alias).append(".").append(col).append(" IS NOT NULL");
//            }
//
//            if (val != null && op != Op.IS_NULL && op != Op.IS_NOT_NULL) {
//                params.add(val);
//            }
//        }
//    }
//
//    private static void buildOrderBy(HighLevelSelectWrapper<?> w, StringBuilder sql) {
//        if (w.getOrderBys().isEmpty()) return;
//        sql.append(" ORDER BY ");
//        List<OrderField> orders = w.getOrderBys();
//        for (int i = 0; i < orders.size(); i++) {
//            OrderField of = orders.get(i);
//            LambdaResolver.LambdaInfo info = LambdaResolver.resolve(of.getField());
//            String alias = w.getAliasMap().get(info.getEntityClass());
//            sql.append(alias).append(".").append(info.getColumnName());
//            sql.append(of.isAsc() ? " ASC" : " DESC");
//            if (i < orders.size() - 1) sql.append(", ");
//        }
//    }
//
//    private static void buildLimit(HighLevelSelectWrapper<?> w, StringBuilder sql) {
//        if (w.getLimit() > 0) {
//            sql.append(" LIMIT ?, ?");
//        }
//    }
//
//    private static String toTableName(Class<?> clazz) {
//        return LambdaResolver.toUnderline(clazz.getSimpleName());
//    }
//}









