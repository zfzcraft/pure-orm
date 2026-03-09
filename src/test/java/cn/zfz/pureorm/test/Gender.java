package cn.zfz.pureorm.test;

import cn.zfz.pureorm.annotations.EnumValue;

public enum Gender {
MALE(1),FEMALE(0);
	
	@EnumValue
	private int code;

	private Gender(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
