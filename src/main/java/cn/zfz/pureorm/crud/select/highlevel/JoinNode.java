package cn.zfz.pureorm.crud.select.highlevel;

import cn.zfz.pureorm.enums.JoinType;

public class JoinNode {

	private final JoinType type;
	private final Class<?> entityClass;
	private final String alias;
	private final HighLevelConditionNode on;

	public JoinNode(JoinType type, Class<?> entityClass, String alias, HighLevelConditionNode on) {
		this.type = type;
		this.entityClass = entityClass;
		this.alias = alias;
		this.on = on;
	}

	public JoinType getType() {
		return type;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public String getAlias() {
		return alias;
	}

	public HighLevelConditionNode getOn() {
		return on;
	}
}
