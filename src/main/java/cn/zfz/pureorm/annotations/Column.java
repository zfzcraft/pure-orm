package cn.zfz.pureorm.annotations;

import java.lang.annotation.*;

//普通列注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	String name();
}
