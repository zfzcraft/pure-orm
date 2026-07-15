package cn.zfz.pureorm.test.h2;

import java.util.List;

import cn.zfz.pureorm.annotations.NotColumn;
import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;

@Table(name = "t_user")
public class TestUserWithOrders {

    @PrimaryKey(autoIncrement = true)
    private Long id;

    private String name;

    private Integer age;

    @NotColumn
    private List<TestOrder> orders;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public List<TestOrder> getOrders() { return orders; }
    public void setOrders(List<TestOrder> orders) { this.orders = orders; }
}
