//package cn.pureorm.crud.select;
//
//import java.util.*;
//import java.util.function.Consumer;
//
//import cn.pureorm.crud.LambadaColumn;
//import cn.pureorm.enums.JoinType;
//import cn.pureorm.enums.Op;
//import cn.pureorm.utils.LambadaUtils;
//import lombok.Data;
//@Data
//public class HighLevelSelectWrapper {
//	
//	private Class<?> fromClass;
//
//    // JOIN 列表
//    private final List<JoinNode> joins = new ArrayList<>();
//
//    // 查询字段
//    private final List<SelectColumn> selectFields = new ArrayList<>();
//
//    // WHERE 条件树
//    private final HighLevelConditionNode where = new HighLevelConditionNode(HighLevelConditionNode.Logic.AND);
//
//    // ORDER BY
//    private final List<OrderColumn> orderBys = new ArrayList<>();
//
//    // 分组 / 分页
//    private List<GroupColumn> groupBy = new ArrayList<>();
//    private List<HavingColumn> having = new ArrayList<>();
//    private long offset = -1;
//    private long limit = -1;
//
//    public List<GroupColumn> getGroupBy() {
//		return groupBy;
//	}
//
//	public List<HavingColumn> getHaving() {
//		return having;
//	}
//
//	// -------------------------------------------------------------------------
//    // FROM
//    // -------------------------------------------------------------------------
//    public  HighLevelSelectWrapper from(Class<?> entityClass) {
//    	HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
//    	wrap.fromClass = entityClass;
//        return wrap;
//    }
//
//    
//    // -------------------------------------------------------------------------
//    // JOIN 完整版：LEFT / INNER / RIGHT 全支持
//    // -------------------------------------------------------------------------
//    
//    public HighLevelSelectWrapper leftJoin(Class<?> target, Consumer<HighLevelSelectWrapper> on) {
//        HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
//        on.accept(wrap);
//        joins.add(new JoinNode(JoinType.LEFT, target, wrap.where));
//        return this; 
//    }
//
//    public HighLevelSelectWrapper innerJoin(Class<?> target, Consumer<HighLevelSelectWrapper> on) {
//       
//        HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
//        on.accept(wrap);
//        joins.add(new JoinNode(JoinType.INNER, target, wrap.where));
//        return this;
//    }
//
//    public HighLevelSelectWrapper rightJoin(Class<?> target, Consumer<HighLevelSelectWrapper> on) {
//        
//        HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
//        on.accept(wrap);
//        joins.add(new JoinNode(JoinType.RIGHT, target, wrap.where));
//        return this;
//    }
//
//    // -------------------------------------------------------------------------
//    // SELECT
//    // -------------------------------------------------------------------------
//    @SafeVarargs
//    public final HighLevelSelectWrapper select(LambadaColumn<?, ?>... fields) {
//    	
//        for (LambadaColumn<?, ?> f : fields) {
//        String tableName =	LambadaUtils.getClassSimpleName(f);
//       String columnName =	LambadaUtils.getFieldName(f);
//            selectFields.add(new SelectColumn(SelectType.COLUMN,tableName, columnName));
//        }
//        return this;
//    }
//
//    public HighLevelSelectWrapper count(LambadaColumn<?, ?> field, LambadaColumn<?, ?> asName) {
//    	 String tableName =	LambadaUtils.getClassSimpleName(field);
//         String columnName =	LambadaUtils.getFieldName(asName);
//    	selectFields.add(new SelectColumn(SelectType.COUNT,tableName,  columnName));
//        return this;
//    }
//
//    public HighLevelSelectWrapper sum(LambadaColumn<?, ?> field, LambadaColumn<?, ?> asName) {
//    	String tableName =	LambadaUtils.getClassSimpleName(field);
//        String columnName =	LambadaUtils.getFieldName(asName);
//   	selectFields.add(new SelectColumn(SelectType.SUM,tableName,  columnName));
//        return this;
//    }
//
//    public HighLevelSelectWrapper max(LambadaColumn<?, ?> field, LambadaColumn<?, ?> asName) {
//    	String tableName =	LambadaUtils.getClassSimpleName(field);
//        String columnName =	LambadaUtils.getFieldName(asName);
//   	selectFields.add(new SelectColumn(SelectType.MAX,tableName,  columnName));
//        return this;
//    }
//
//    public HighLevelSelectWrapper min(LambadaColumn<?, ?> field, LambadaColumn<?, ?> asName) {
//    	String tableName =	LambadaUtils.getClassSimpleName(field);
//        String columnName =	LambadaUtils.getFieldName(asName);
//   	selectFields.add(new SelectColumn(SelectType.MIN,tableName,  columnName));
//        return this;
//    }
//
//    // -------------------------------------------------------------------------
//    // WHERE 条件（完整嵌套）
//    // -------------------------------------------------------------------------
//    public HighLevelSelectWrapper eq(LambadaColumn<?, ?> field, Object val) {
//        where.addLeaf(field, Op.EQ, val);
//        return this;
//    }
//
//    public HighLevelSelectWrapper ne(LambadaColumn<?, ?> field, Object val) {
//        where.addLeaf(field, Op.NE, val);
//        return this;
//    }
//
//    public HighLevelSelectWrapper gt(LambadaColumn<?, ?> field, Object val) {
//        where.addLeaf(field, Op.GT, val);
//        return this;
//    }
//
//    public HighLevelSelectWrapper ge(LambadaColumn<?, ?> field, Object val) {
//        where.addLeaf(field, Op.GE, val);
//        return this;
//    }
//
//    public HighLevelSelectWrapper lt(LambadaColumn<?, ?> field, Object val) {
//        where.addLeaf(field, Op.LT, val);
//        return this;
//    }
//
//    public HighLevelSelectWrapper le(LambadaColumn<?, ?> field, Object val) {
//        where.addLeaf(field, Op.LE, val);
//        return this;
//    }
//
//    public HighLevelSelectWrapper like(LambadaColumn<?, ?> field, Object val) {
//        where.addLeaf(field, Op.LIKE, val);
//        return this;
//    }
//
//    public HighLevelSelectWrapper isNull(LambadaColumn<?, ?> field) {
//        where.addLeaf(field, Op.IS_NULL, null);
//        return this;
//    }
//
//    public HighLevelSelectWrapper and(Consumer<HighLevelSelectWrapper> consumer) {
//        HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
//        consumer.accept(wrap);
//        where.getChildren().add(wrap.where);
//        return this;
//    }
//
//    public HighLevelSelectWrapper or(Consumer<HighLevelSelectWrapper> consumer) {
//        HighLevelSelectWrapper wrap = new HighLevelSelectWrapper();
//        consumer.accept(wrap);
//        HighLevelConditionNode orNode = new HighLevelConditionNode(HighLevelConditionNode.Logic.OR);
//        orNode.getChildren().add(wrap.where);
//        where.getChildren().add(orNode);
//        return this;
//    }
//
//    // -------------------------------------------------------------------------
//    // ORDER BY
//    // -------------------------------------------------------------------------
//    public HighLevelSelectWrapper orderByAsc(LambadaColumn<?, ?> field) {
//    	String tableName =	LambadaUtils.getClassSimpleName(field);
//        String columnName =	LambadaUtils.getFieldName(field);
//        orderBys.add(new OrderColumn(tableName,columnName, OrderByType.ASC));
//        return this;
//    }
//
//    public HighLevelSelectWrapper orderByDesc(LambadaColumn<?, ?> field) {
//    	String tableName =	LambadaUtils.getClassSimpleName(field);
//        String columnName =	LambadaUtils.getFieldName(field);
//        orderBys.add(new OrderColumn(tableName,columnName, OrderByType.DESC));
//        return this;
//    }
//
//    // -------------------------------------------------------------------------
//    // 分页
//    // -------------------------------------------------------------------------
//    public HighLevelSelectWrapper limit(long offset, long limit) {
//        this.offset = offset;
//        this.limit = limit;
//        return this;
//    }
//
//   
//}





