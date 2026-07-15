package cn.zfz.pureorm.crud.select.highlevel;

import java.util.ArrayList;
import java.util.List;

import cn.zfz.pureorm.core.LambadaColumn;
import cn.zfz.pureorm.enums.Op;

public class HighLevelConditionNode {

	public enum Logic {
		AND, OR
	}

	private final Logic logic;
	private final List<HighLevelConditionNode> children = new ArrayList<>();
	private LambadaColumn<?> field;
	private Op op;
	private Object value;

	public HighLevelConditionNode(Logic logic) {
		this.logic = logic;
	}

	public void addLeaf(LambadaColumn<?> field, Op op, Object value) {
		HighLevelConditionNode leaf = new HighLevelConditionNode(Logic.AND);
		leaf.field = field;
		leaf.op = op;
		leaf.value = value;
		children.add(leaf);
	}

	public boolean isLeaf() {
		return children.isEmpty() && field != null;
	}

	public Logic getLogic() {
		return logic;
	}

	public List<HighLevelConditionNode> getChildren() {
		return children;
	}

	public LambadaColumn<?> getField() {
		return field;
	}

	public Op getOp() {
		return op;
	}

	public Object getValue() {
		return value;
	}

	public void setField(LambadaColumn<?> field) {
		this.field = field;
	}

	public void setOp(Op op) {
		this.op = op;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
