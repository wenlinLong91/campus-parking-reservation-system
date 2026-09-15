package com.segi.parking.entity; // 🌟 你的真实包名

public class WaitList {
    private Long id;
    private String staffId;    // 排队教职工的工号
    private String plate;      // 排队教职工的车牌号
    private String status;     // 状态：Pending（等待分配）, Allocated（已完成分配）
    private String createTime; // 加入排队的时间戳字符串

    // 无参构造函数
    public WaitList() {
    }

    // 全参构造函数
    public WaitList(Long id, String staffId, String plate, String status, String createTime) {
        this.id = id;
        this.staffId = staffId;
        this.plate = plate;
        this.status = status;
        this.createTime = createTime;
    }

    // --- 标准 Getter 和 Setter 方法 ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}