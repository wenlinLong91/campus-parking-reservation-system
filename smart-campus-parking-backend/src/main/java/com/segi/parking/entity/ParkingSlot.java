package com.segi.parking.entity; // 🌟 你的真实包名

public class ParkingSlot {
    private Long id;
    private String slotId;     // 车位号（如 A01, A02）
    private String status;     // 状态：Vacant（空闲）, Occupied（固定占用）, Shared（共享中）
    private String staffId;    // 占用或共享此车位的教职员工工号（如 staff01）
    private String plate;      // 车牌号
    private String expiryDate; // 使用截止时间

    // 无参构造函数（Spring Boot 反序列化及后续整合数据库时必需）
    public ParkingSlot() {
    }

    // 全参构造函数（方便我们在后面 Controller 里不连数据库也能直接给内存赋模拟数据）
    public ParkingSlot(Long id, String slotId, String status, String staffId, String plate, String expiryDate) {
        this.id = id;
        this.slotId = slotId;
        this.status = status;
        this.staffId = staffId;
        this.plate = plate;
        this.expiryDate = expiryDate;
    }

    // --- 标准 Getter 和 Setter 方法 ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}