package cn.zfz.pureorm.annotations;
import java.lang.annotation.*;
//枚举值注解（标记枚举类中对应数据库的字段）
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {
}
