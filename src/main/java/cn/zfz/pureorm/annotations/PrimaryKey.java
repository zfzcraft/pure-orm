package cn.zfz.pureorm.annotations;

import java.lang.annotation.*;

//主键注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {
	boolean autoIncrement();
}
