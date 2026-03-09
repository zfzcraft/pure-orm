//package cn.pureorm.crud.select;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import cn.pureorm.crud.LambadaColumn;
//import cn.pureorm.enums.Op;
//
//public class HighLevelConditionNode {
//    public enum Logic { AND, OR }
//    private final Logic logic;
//    private final List<HighLevelConditionNode> children = new ArrayList<>();
//    private LambadaColumn<?, ?> field;
//    private Op op;
//    private Object value;
//
//    public HighLevelConditionNode(Logic logic) {
//        this.logic = logic;
//    }
//
//    public void addLeaf(LambadaColumn<?, ?> field, Op op, Object value) {
//        HighLevelConditionNode leaf = new HighLevelConditionNode(Logic.AND);
//        leaf.field = field;
//        leaf.op = op;
//        leaf.value = value;
//        children.add(leaf);
//    }
//
//    public Logic getLogic() { return logic; }
//    public List<HighLevelConditionNode> getChildren() { return children; }
//    public LambadaColumn<?, ?> getField() { return field; }
//    public Op getOp() { return op; }
//    public Object getValue() { return value; }
//}





