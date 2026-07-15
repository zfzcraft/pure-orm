package cn.zfz.pureorm.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import cn.zfz.pureorm.enums.FieldKind;
import cn.zfz.pureorm.handler.DefaultEnumTypeHandler;
import cn.zfz.pureorm.handler.EnumTypeHandler;

public class FieldMeta {
	
	private static final EnumTypeHandler ENUM_TYPE_HANLDER = new DefaultEnumTypeHandler();

    // 反射基础
    private final Field field;
    
    private final FieldKind fieldKind;
    
    private final Type genericType; // 用于泛型如 List<XXX> JSON

    // @Column
    private final String columnName;
    
    private final Field enumValueField; // 缓存 @EnumValue 字段

    // 转换器
    private final EnumTypeHandler enumTypeHandler = ENUM_TYPE_HANLDER;

	public FieldMeta(Field field, FieldKind fieldKind, Type genericType, String columnName, Field enumValueField) {
		super();
		this.field = field;
		this.fieldKind = fieldKind;
		this.genericType = genericType;
		this.columnName = columnName;
		this.enumValueField = enumValueField;
	}

	public Field getField() {
		return field;
	}

	public FieldKind getFieldKind() {
		return fieldKind;
	}

	public Type getGenericType() {
		return genericType;
	}

	public String getColumnName() {
		return columnName;
	}

	public Field getEnumValueField() {
		return enumValueField;
	}

	public EnumTypeHandler getEnumTypeHandler() {
		return enumTypeHandler;
	}

}
