package cn.zfz.pureorm.annotations;

import java.lang.annotation.*;

//表名注解
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	
	String name(); 
}
