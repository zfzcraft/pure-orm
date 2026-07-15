package cn.zfz.pureorm.test.h2;

import cn.zfz.pureorm.annotations.Column;
import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;

@Table(name = "t_product")
public class TestProduct {

    @PrimaryKey(autoIncrement = true)
    private Long id;

    private String name;

    @Column(name = "price")
    private Integer price;

    private String category;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
