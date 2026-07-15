package cn.zfz.pureorm.test.h2.rbac;

import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;

@Table(name = "sys_permission")
public class SysPermission {

    @PrimaryKey(autoIncrement = true)
    private Long id;

    private String permName;

    private String permCode;

    private String resource;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }
    public String getPermCode() { return permCode; }
    public void setPermCode(String permCode) { this.permCode = permCode; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
}
