package cn.zfz.pureorm.test;

import lombok.Data;

@Data
public class User11 {
    private Long id;
    private String name;
    private Integer age;
    private Integer status;
    private Integer visitCount;
    private String createTime;
    private String updateTime;
    // getter/setter 省略
}
