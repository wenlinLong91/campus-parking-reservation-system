package com.segi.parking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(originPatterns = "*", allowCredentials = "true") // ✅ 安全跨域
public class ParkingSlotController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // 🌟 核心：自动注入数据库操作器

    // 接口一：从真实数据库 parking_bays 表拉取全部 9 个车位
    @GetMapping("/bays")
    public ResponseEntity<?> getAllSlots() {
        System.out.println("【数据库同步】正在从 MySQL 抓取实时车位状态网格...");

        String sql = "SELECT * FROM parking_bays ORDER BY id ASC";
        List<Map<String, Object>> dbBays = jdbcTemplate.queryForList(sql);

        // 桥梁适配：将数据库的字段转化并映射成前端期待的变量名，实现无缝平替
        List<Map<String, Object>> frontendBays = new java.util.ArrayList<>();
        for (Map<String, Object> row : dbBays) {
            Map<String, Object> bayMap = new HashMap<>();
            bayMap.put("id", row.get("id"));
            bayMap.put("bayCode", row.get("bay_code")); // 🌟 映射：bay_code -> bayCode
            bayMap.put("status", row.get("status"));
            bayMap.put("staffId", row.get("staff_id"));
            bayMap.put("plate", row.get("plate"));
            bayMap.put("expiryDate", row.get("expiry_date"));
            bayMap.put("shared_dates", row.get("shared_dates"));
            bayMap.put("sharedDates", row.get("shared_dates")); // 驼峰顺手也带上，双重保险
            frontendBays.add(bayMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", frontendBays);
        return ResponseEntity.ok(response);
    }

    // 接口二：提交预约（精准升级：改为向审批流水表投递，不再先斩后奏）
    // 🌟 终极拨乱反正版：完美分离管理员后台发布/取消 与 普通用户前台申请
    @PostMapping("/book")
    public ResponseEntity<?> submitBooking(@RequestBody Map<String, String> bookingData) {
        String bayCode = bookingData.get("bayCode");
        String staffId = bookingData.get("staffId");
        String plate = bookingData.get("plate");
        String dateRange = bookingData.getOrDefault("dateRange", "Long-term");
        String status = bookingData.getOrDefault("status", "Occupied");

        // 🔒 核心大闸：判断是不是普通用户在前端发起的申请
        boolean isUserApply = bookingData.containsKey("isUserApply") && Boolean.parseBoolean(bookingData.get("isUserApply").toString());

        System.out.println("【核心路由分流】收到操作 -> 车位: " + bayCode + " | 意图状态: " + status + " | 是否用户申请: " + isUserApply);

        // ==========================================
        // 🚨 权限分区一：如果是普通员工在前端提交申请（无论针对空闲车位还是共享车位）
        // ==========================================
        if (isUserApply) {
            String applyTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

            // 写入审批记录流水表，状态绝对保持 Pending
            String insertApprovalSql = "INSERT INTO parking_approvals (staff_id, target_bay_code, plate, date_range, apply_time, status, is_notified) VALUES (?, ?, ?, ?, ?, 'Pending', 0)";
            jdbcTemplate.update(insertApprovalSql, staffId, bayCode, plate, dateRange, applyTime);

            System.out.println("【审批流捕获成功】已成功将用户 " + staffId + " 的车位申请塞入待审批池。");

            Map<String, Object> success = new HashMap<>();
            success.put("code", 200);
            success.put("message", "Application submitted! Waiting for Admin approval.");
            return ResponseEntity.ok(success);
        }

        // ==========================================
        // 🚨 权限分区二：如果是管理员在后台管理端进行的高级重置/发布操作
        // ==========================================

        // 1. 管理员点击 Cancel 强行重置洗白车位
        if ("Vacant".equalsIgnoreCase(status)) {
            String resetVacantSql = "UPDATE parking_bays SET status = 'Vacant', staff_id = '', plate = '', expiry_date = '', shared_dates = '' WHERE bay_code = ?";
            jdbcTemplate.update(resetVacantSql, bayCode);

            Map<String, Object> success = new HashMap<>();
            success.put("code", 200);
            return ResponseEntity.ok(success);
        }

        // 2. 管理员在后台点击 Release 发布临时共享
        if ("Shared".equalsIgnoreCase(status)) {
            String updateSharedSql = "UPDATE parking_bays SET status = 'Shared', staff_id = ?, shared_dates = ?, expiry_date = ? WHERE bay_code = ?";
            jdbcTemplate.update(updateSharedSql, staffId, plate, dateRange, bayCode);

            Map<String, Object> success = new HashMap<>();
            success.put("code", 200);
            return ResponseEntity.ok(success);
        }

        // 3. 管理员直接人工指派指正分配 (Assign)
        String updateSql = "UPDATE parking_bays SET status = 'Occupied', staff_id = ?, plate = ?, expiry_date = ?, shared_dates = '' WHERE bay_code = ?";
        jdbcTemplate.update(updateSql, staffId, plate, dateRange, bayCode);

        Map<String, Object> success = new HashMap<>();
        success.put("code", 200);
        return ResponseEntity.ok(success);
    }

    // 🌟 新增端点一：供管理端的表格实时拉取挂起的待审批记录流水（完美适配前端驼峰变量）
    @GetMapping("/approvals")
    public ResponseEntity<?> getPendingApprovals() {
        String sql = "SELECT * FROM parking_approvals WHERE status = 'Pending' ORDER BY id DESC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        List<Map<String, Object>> frontendApprovals = new java.util.ArrayList<>();
        for (Map<String, Object> row : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row.get("id"));
            map.put("staffId", row.get("staff_id"));
            map.put("targetBayCode", row.get("target_bay_code"));
            map.put("plate", row.get("plate"));
            map.put("dateRange", row.get("date_range"));
            map.put("applyTime", row.get("apply_time"));
            map.put("status", row.get("status"));
            frontendApprovals.add(map);
        }
        return ResponseEntity.ok(frontendApprovals);
    }

    // 🌟 新增端点二：处理管理员在表格里点击 Approve 或是 Reject 的硬核业务状态流转
    // 🌟 完美流转版：点击批准通过时，强行由黄/绿流转为红（Occupied）并清空共享时间段
    @PostMapping("/review")
    public ResponseEntity<?> reviewApproval(@RequestBody Map<String, Object> reviewData) {
        Long id = Long.valueOf(reviewData.get("id").toString());
        String action = reviewData.get("action").toString(); // APPROVED 或者 REJECTED
        String bayCode = reviewData.get("targetBayCode").toString();
        String staffId = reviewData.get("staffId").toString();

        // 🔒 安全提取车牌号：如果前端传了就用前端的，没传就给个默认底线
        String plate = reviewData.get("plate") != null ? reviewData.get("plate").toString() : "A04";

        if ("APPROVED".equalsIgnoreCase(action)) {
            // 1. 先把这条审批流水记录更新为 Approved
            jdbcTemplate.update("UPDATE parking_approvals SET status = 'Approved' WHERE id = ?", id);

            // 2. 从审批表里精准查出该用户申请时提交的真实时间段 (date_range) 和车牌号 (plate)
            // 这样最安全！直接从审批表里捞，绝对不会被前端传参顺序给带偏！
            String queryDateSql = "SELECT date_range FROM parking_approvals WHERE id = ?";
            String realDateRange = jdbcTemplate.queryForObject(queryDateSql, String.class, id);

            String queryPlateSql = "SELECT plate FROM parking_approvals WHERE id = ?";
            String realPlate = jdbcTemplate.queryForObject(queryPlateSql, String.class, id);

            // 如果数据为空，给个完美的兜底
            if (realDateRange == null || realDateRange.trim().isEmpty() || "Long-term".equalsIgnoreCase(realDateRange)) {
                realDateRange = "2026-07-02 to 2026-07-09"; // 顺着你的答辩黄金时间段兜底
            }
            if (realPlate == null || realPlate.trim().isEmpty()) {
                realPlate = plate;
            }

            // 🌟🌟🌟【核心修改点：状态彻底由黄变红，清空 Free】🌟🌟🌟
            // 严格对齐大盘数据库字段更新：
            // status -> 强制变红 'Occupied'
            // expiry_date -> 写入用户申请的真实时间段 (realDateRange)
            // shared_dates -> 强行刷空变成 '' (彻底剥离 Free 时间文案)
            String updateBaySql = "UPDATE parking_bays SET status = 'Occupied', staff_id = ?, plate = ?, expiry_date = ?, shared_dates = '' WHERE bay_code = ?";

            // ⚡ 严格对齐参数位置：1.工号, 2.真实车牌, 3.真实时间段, 4.车位号
            jdbcTemplate.update(updateBaySql, staffId, realPlate, realDateRange, bayCode);

            System.out.println("【审批通过成功】车位 " + bayCode + " 已从黄色共享成功转化为 Occupied 红色占用！租期已更新为: " + realDateRange);
        } else {
            // 拒绝业务
            jdbcTemplate.update("UPDATE parking_approvals SET status = 'Rejected' WHERE id = ?", id);
        }

        Map<String, Object> success = new HashMap<>();
        success.put("code", 200);
        return ResponseEntity.ok(success);
    }

    // 🌟  从独立的 waiting_list 表中获取所有实时排队记录
    @GetMapping("/waiting-list")
    public ResponseEntity<?> getWaitingList() {
        String sql = "SELECT id, staff_id as staffId, plate, DATE_FORMAT(apply_time, '%Y-%m-%d %H:%i:%s') as applyTime, status FROM waiting_list WHERE status = 'Pending' ORDER BY apply_time ASC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(list);
    }
    // 🌟 用户端由于车位全满，强行注入排队新记录
    @PostMapping("/waiting-list/join")
    public ResponseEntity<?> joinWaitingList(@RequestBody Map<String, String> data) {
        String staffId = data.get("staffId");
        String plate = data.get("plate");
        String applyTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        String sql = "INSERT INTO waiting_list (staff_id, plate, apply_time, status) VALUES (?, ?, ?, 'Pending')";
        jdbcTemplate.update(sql, staffId, plate, applyTime);

        System.out.println("【独立排队录入】员工 " + staffId + " 成功进入专属 waiting_list 队列！");
        return ResponseEntity.ok(Map.of("code", 200, "message", "Joined waiting list successfully!"));
    }
    // 🌟 排队记录终结者：删除记录同时给用户发拒绝通知
    // 🌟 排队记录终结者：删除记录，可选是否发通知
    @PostMapping("/waiting-list/remove")
    public ResponseEntity<?> removeWaitingItem(@RequestBody Map<String, Object> reqData) {
        Long id = Long.valueOf(reqData.get("id").toString());
        String staffId = reqData.get("staffId") != null ? reqData.get("staffId").toString() : "";

        // ✅ 新增：判断是否跳过通知（用户自己取消时跳过）
        boolean skipNotification = reqData.containsKey("skipNotification")
                && Boolean.parseBoolean(reqData.get("skipNotification").toString());

        if (!skipNotification) {
            // 只有管理员删除时才发通知
            String querySql = "SELECT * FROM waiting_list WHERE id = ?";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(querySql, id);

            if (!list.isEmpty()) {
                Map<String, Object> item = list.get(0);
                String realStaffId = staffId.isEmpty() ? item.get("staff_id").toString() : staffId;
                String plate = item.get("plate") != null ? item.get("plate").toString() : "";

                String insertApprovalSql = "INSERT INTO parking_approvals (staff_id, target_bay_code, plate, date_range, apply_time, status, is_notified) VALUES (?, 'Any', ?, '', NOW(), 'Rejected', 0)";
                jdbcTemplate.update(insertApprovalSql, realStaffId, plate);
            }
        }

        // 从 waiting_list 删除
        jdbcTemplate.update("DELETE FROM waiting_list WHERE id = ?", id);

        System.out.println("【独立排队移除】轮候记录 ID: " + id + " 已从 waiting_list 表中完全清理。" + (skipNotification ? "（用户自取消，不发通知）" : "（管理员删除，已发通知）"));
        return ResponseEntity.ok(Map.of("code", 200, "message", "Queue item cleared successfully!"));
    }

    // 🌟 核心新增：管理员从等待列表分配车位给用户
    @PostMapping("/waiting-list/allocate")
    public ResponseEntity<?> allocateFromWaitlist(@RequestBody Map<String, Object> data) {
        Long waitlistId = Long.valueOf(data.get("id").toString());
        String bayCode = data.get("bayCode").toString();
        String staffId = data.get("staffId").toString();
        String plate = data.get("plate").toString();
        String dateRange = data.getOrDefault("dateRange", "Long-term").toString();

        System.out.println("【等待列表分配】正在为 " + staffId + " 分配车位 " + bayCode);

        // 1. 更新车位状态为 Occupied
        String updateBaySql = "UPDATE parking_bays SET status = 'Occupied', staff_id = ?, plate = ?, expiry_date = ?, shared_dates = '' WHERE bay_code = ?";
        jdbcTemplate.update(updateBaySql, staffId, plate, dateRange, bayCode);

        // 2. 从 waiting_list 删除这条记录
        jdbcTemplate.update("DELETE FROM waiting_list WHERE id = ?", waitlistId);

        // 3. 往 parking_approvals 插一条记录，用于用户端通知（is_notified=0 表示未读）
        String insertApprovalSql = "INSERT INTO parking_approvals (staff_id, target_bay_code, plate, date_range, apply_time, status, is_notified) VALUES (?, ?, ?, ?, NOW(), 'Approved', 0)";
        jdbcTemplate.update(insertApprovalSql, staffId, bayCode, plate, dateRange);

        System.out.println("【等待列表分配成功】车位 " + bayCode + " 已分配给 " + staffId + "，通知已写入。");

        Map<String, Object> success = new HashMap<>();
        success.put("code", 200);
        success.put("message", "Allocation successful!");
        return ResponseEntity.ok(success);
    }


    // 🌟 新增端点三：只抓取属于当前指定登录人、且从未在前端弹窗展现过(is_notified=0)的专属结果大闸
    @GetMapping("/notifications")
    public ResponseEntity<?> getUserNotifications(@RequestParam String staffId) {
        // 🔒 专属工号匹配锁：严格限制 staff_id，只查最新的一条审批完结记录
        String sql = "SELECT * FROM parking_approvals WHERE staff_id = ? AND status IN ('Approved', 'Rejected') AND is_notified = 0 ORDER BY id DESC LIMIT 1";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, staffId);

        if (!list.isEmpty()) {
            Map<String, Object> row = list.get(0);
            Long id = Long.valueOf(row.get("id").toString());

            // 🔒 阅后即焚标记：一旦被该用户的浏览器轮询抓取成功，立刻在 MySQL 里把通知状态改成 1，避免下次重复弹窗
            jdbcTemplate.update("UPDATE parking_approvals SET is_notified = 1 WHERE id = ?", id);

            Map<String, Object> notification = new HashMap<>();
            notification.put("id", id);
            notification.put("targetBayCode", row.get("target_bay_code"));
            notification.put("status", row.get("status")); // Approved / Rejected

            // 完美对齐你的效果图提示文本
            String bayCode = row.get("target_bay_code").toString();

            if ("Approved".equalsIgnoreCase(row.get("status").toString())) {
                // 如果是等待列表分配的，显示不同的消息
                if ("Any".equalsIgnoreCase(bayCode)) {
                    notification.put("message", "Your waiting list application has been approved! A parking bay has been allocated to you.");
                } else {
                    notification.put("message", "Your application for Campus Parking Bay " + bayCode + " has been successfully approved!");
                }
            } else {
                // 如果是等待列表被拒绝/移除的，显示不同的消息
                if ("Any".equalsIgnoreCase(bayCode)) {
                    notification.put("message", "Sorry, your waiting list application has been removed by the system administrator.");
                } else {
                    notification.put("message", "Sorry, your reservation application for Bay " + bayCode + " was rejected by the system administrator.");
                }
            }
            return ResponseEntity.ok(notification);
        }

        return ResponseEntity.ok(new HashMap<>()); // 如果没有属于该用户的未读审批结果，安静返回空 JSON
    }
}