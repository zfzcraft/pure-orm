package cn.zfz.pureorm.test;

import java.util.List;

import cn.zfz.pureorm.annotations.OneToMany;
import lombok.Data;
@Data
public class Role {
    private Long id;
    private String name;
    private String code;
    @OneToMany
    private List<Permission> permissions;

}
