package cn.zfz.pureorm.test.insert;

import java.util.List;

import cn.zfz.pureorm.annotations.NotColumn;
import cn.zfz.pureorm.annotations.OneToMany;
import cn.zfz.pureorm.test.Role;
import lombok.Data;

@Data
public class User {
    private Long id;
    private String name;
    private int age;
    @OneToMany
    @NotColumn
    private List<Role> roles;

   
}