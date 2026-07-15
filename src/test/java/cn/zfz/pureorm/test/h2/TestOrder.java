package cn.zfz.pureorm.test.h2;

import cn.zfz.pureorm.annotations.PrimaryKey;
import cn.zfz.pureorm.annotations.Table;

@Table(name = "t_order")
public class TestOrder {

    @PrimaryKey(autoIncrement = true)
    private Long id;

    private Long userId;

    private String orderNo;

    private Integer amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}
