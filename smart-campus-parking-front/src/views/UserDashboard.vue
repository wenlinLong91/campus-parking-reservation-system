<template>
  <div class="app-wrapper">
    <header class="portal-header">
      <div class="header-left">
        <el-icon class="header-icon"><Compass /></el-icon>
        <span class="header-title">Campus Parking - Personal Portal</span>
      </div>
      <div class="header-right">
        <el-tag type="warning" effect="dark" size="large" class="welcome-tag">
          Welcome, Campus Staff: {{ currentUser }}
        </el-tag>
        <el-button type="primary" @click="passwordDialogVisible = true">Change Password</el-button>
        <el-button type="danger" @click="handleLogout">Sign Out</el-button>
      </div>
    </header>

    <div class="user-dashboard">
      <el-row style="margin-bottom: 20px;">
        <el-col :span="24">
          <el-card class="status-card" shadow="never">
            <div class="status-content" style="display: flex; align-items: center; font-size: 16px;">
              <span class="status-label" style="font-weight: bold; color: #606266; margin-right: 15px;">My Parking Status:</span>
              <template v-if="myActiveBooking">
                <el-tag type="success" size="large" effect="light" class="status-value">
                  Bay Code: <strong>{{ myActiveBooking.bayCode }}</strong> 
                  <span class="split-line" style="margin: 0 10px; color: #bbb;">|</span> 
                  Period: <strong>{{ myActiveBooking.expiryDate }}</strong>
                  <span class="split-line" style="margin: 0 10px; color: #bbb;">|</span>
                  Status: <strong style="color: #e6a23c;">{{ myActiveBooking.displayStatus }}</strong>
                </el-tag>
              </template>
              <template v-else>
                <span class="no-status" style="color: #909399; font-style: italic;">No active parking subscription</span>
              </template>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="24">
          <el-card class="box-card" shadow="hover">
            <template #header>
              <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                <span class="title-text" style="font-size: 18px; font-weight: bold;">
                  <el-icon><Grid /></el-icon> Available Parking Bays for Booking
                </span>
                <el-tag type="info">Select a vacant or shared bay to apply</el-tag>
              </div>
            </template>
            
            <div v-if="filteredParkingBays.length > 0" class="bays-grid">
              <div
                v-for="bay in filteredParkingBays"
                :key="bay.id"
                :class="['bay-item', bay.status.toLowerCase()]"
                @click="openBookingDialog(bay)"
              >
                <div class="bay-header">
                  Bay {{ bay.bayCode || bay.id }}
                </div>
                <div class="bay-body">
                  <el-tag
                    :type="bay.status === 'Vacant' ? 'success' : 'warning'"
                    effect="dark"
                    size="large"
                  >
                    {{ bay.status === 'Vacant' ? 'Available (New)' : 'Shared (Temporary)' }}
                  </el-tag>
                  <div v-if="bay.status === 'Shared' || bay.status === 'shared'" class="shared-dates">
                 <el-icon><Calendar /></el-icon> Free: {{ bay.shared_dates || bay.sharedDates || '' }}
              </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state-container">
              <el-empty description="Oops! All parking bays are currently occupied.">
                <el-button type="primary" @click="waitlistDialogVisible = true">
                  Join Waiting List Manually
                </el-button>
              </el-empty>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row style="margin-top: 20px;">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                <span class="title-text" style="font-size: 18px; font-weight: bold; color: #e6a23c;">
                  <el-icon><Timer /></el-icon> Real-time Waiting List
                </span>
                <el-tag type="warning" effect="plain">Queue for Any Available Bay</el-tag>
              </div>
            </template>

            <el-table :data="queueList" style="width: 100%" empty-text="The waiting list is currently empty.">
              <el-table-column type="index" label="Rank" width="80" align="center" />
              <el-table-column prop="plate" label="Car Number" min-width="120" />
              <el-table-column prop="applyTime" label="Application Time" min-width="180" />
              <el-table-column prop="status" label="Status" width="120">
                <template #default="{ row }">
                  <el-tag type="warning" effect="light">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="Action" width="120" align="center">
                <template #default="{ row }">
                  <el-button
                    v-if="isCurrentUser(row.staffId)"
                    type="danger"
                    size="small"
                    @click="cancelMyQueue(row)"
                  >
                    Cancel
                  </el-button>
                  <span v-else style="color: #c0c4cc; font-size: 12px;">-</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog
      v-model="bookingDialogVisible"
      :title="`Submit Application for Bay ${selectedBay?.bayCode || selectedBay?.id}`"
      width="500px"
    >
      <div v-if="selectedBay?.status === 'Shared'" class="dialog-notice">
        <strong>Notice:</strong> This bay is only available for: <br/>
        <span style="color: #e6a23c;">{{ selectedBay.sharedDates }}</span>
      </div>
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="120px" style="margin-top: 20px;">
        <el-form-item label="Staff ID" prop="staffId">
          <el-input v-model="bookingForm.staffId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Car Number" prop="plate">
          <el-input v-model="bookingForm.plate" placeholder="e.g. WXB 8842"></el-input>
        </el-form-item>
        <el-form-item label="Usage Period" prop="dateRange">
          <el-date-picker
            v-model="bookingForm.dateRange"
            type="daterange"
            range-separator="To"
            start-placeholder="Start Date"
            end-placeholder="End Date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledSharedDates"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookingDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitBooking">Submit for Approval</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="waitlistDialogVisible"
      title="Join the Waiting List"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="Campus Zone A is fully occupied."
        type="warning"
        description="Please register your details below. The administrator will allocate the next available bay to you based on the queue."
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />
      <el-form :model="waitlistForm" :rules="waitlistRules" ref="waitlistFormRef" label-width="120px">
        <el-form-item label="Staff ID" prop="staffId">
          <el-input v-model="waitlistForm.staffId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Car Number" prop="plate">
          <el-input v-model="waitlistForm.plate" placeholder="e.g. VAA 1923"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="waitlistDialogVisible = false">Maybe Later</el-button>
          <el-button type="warning" @click="submitWaitlist">Queue Now</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="Change Password" width="400px">
      <el-form label-width="140px">
        <el-form-item label="Old Password">
          <el-input type="password" show-password placeholder="Enter old password"></el-input>
        </el-form-item>
        <el-form-item label="New Password">
          <el-input type="password" show-password placeholder="Enter new password"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="passwordDialogVisible = false; ElMessage.success('Password changed successfully!')">Confirm</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const router = useRouter()

const passwordDialogVisible = ref(false)
const waitingList = ref([])
const parkingBays = ref([])

// 🌟 1. 从 Session 或 Local 中安全动态拉取当前登录的用户账号
const currentUser = computed(() => {
  return sessionStorage.getItem('username') || localStorage.getItem('username') || 'staff01'
})

// 🌟 完美兼容新列的数据拉取与清洗函数
const fetchBaysFromBackend = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/parking/bays')
    if (res.data.code === 200) {
      parkingBays.value = res.data.data.map(bay => ({
        ...bay,
        sharedDates: bay.shared_dates || bay.sharedDates || ''
      }))
      localStorage.setItem('parkingBays', JSON.stringify(parkingBays.value))
    }
  } catch (error) {
    const saved = localStorage.getItem('parkingBays')
    if (saved) parkingBays.value = JSON.parse(saved)
    console.error("用户端同步后端车位状态失败:", error)
  }
}

// 保留可用车位原本的全部计算逻辑
const availableBays = computed(() => {
  return parkingBays.value.filter(bay => bay.status === 'Vacant' || bay.status === 'Shared')
})

// 🌟 2. 核心过滤：在下方申请矩阵里彻底隐藏当前用户自己的车位
const filteredParkingBays = computed(() => {
  const user = currentUser.value.trim().toLowerCase()
  return availableBays.value.filter(bay => {
    if (!bay.staffId) return true
    return bay.staffId.trim().toLowerCase() !== user
  })
})

// 获取排队队列（只显示 Pending 状态的）
const queueList = computed(() => {
  return waitingList.value.filter(item => item.status === 'Pending')
})

// 判断是否是当前用户的申请（用于控制 Cancel 按钮的显示）
const isCurrentUser = (staffId) => {
  return staffId && staffId.trim().toLowerCase() === currentUser.value.trim().toLowerCase()
}

// 用户自己取消排队（调用后端接口，不发通知）
const cancelMyQueue = async (row) => {
  try {
    const res = await axios.post('http://localhost:8080/api/parking/waiting-list/remove', {
      id: row.id,
      staffId: row.staffId,
      skipNotification: true  // ✅ 用户自己取消，跳过通知
    })
    
    if (res.data.code === 200) {
      ElMessage.success('You have successfully left the waiting list.')
      sessionStorage.removeItem('hasJoinedQueue')
      await loadWaitingList()  // 重新从后端拉取最新列表
    } else {
      ElMessage.error('Cancel failed')
    }
  } catch (error) {
    console.error("取消排队失败:", error)
    ElMessage.error('Cancel failed. Please try again.')
  }
}

// 无车位自动弹窗控制状态
const waitlistDialogVisible = ref(false)

// 🌟 3. 漏洞拦截修复：只有当【对当前用户而言真正可见的可用车位】为 0 时，才允许跳排队弹窗！
const checkAndTriggerWaitlist = () => {
  if (sessionStorage.getItem('hasJoinedQueue') === 'true') return
  const actualAvailableCount = filteredParkingBays.value.length
  if (actualAvailableCount === 0) {
    setTimeout(() => {
      if (filteredParkingBays.value.length === 0) {
        waitlistDialogVisible.value = true
      }
    }, 500)
  } else {
    waitlistDialogVisible.value = false
  }
}

// 监控真正过滤后的车位网格数量变化
watch(() => filteredParkingBays.value.length, () => {
  checkAndTriggerWaitlist()
})

// 🌟 4. 动态解析当前登录用户的专属车位订阅与共享租赁状态
const myActiveBooking = computed(() => {
  const user = currentUser.value.trim().toLowerCase()
  const myBay = parkingBays.value.find(b =>
    b.staffId && b.staffId.trim().toLowerCase() === user
  )

  if (myBay) {
    if (myBay.status === 'Shared' || myBay.status === 'shared') {
      return {
        bayCode: myBay.bayCode,
        expiryDate: myBay.sharedDates || '2026-07-15 to 2026-07-22',
        displayStatus: 'Shared (Subletting)'
      }
    } else {
      return {
        bayCode: myBay.bayCode,
        expiryDate: myBay.expiryDate || 'Long-term',
        displayStatus: 'Occupied (Active)'
      }
    }
  }
  return null
})

// ✅ 移到外面：加载等待列表
const loadWaitingList = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/parking/waiting-list')
    // 后端直接返回数组
    if (res.data && Array.isArray(res.data)) {
      waitingList.value = res.data
      localStorage.setItem('waitingList', JSON.stringify(res.data))
    }
  } catch (error) {
    // 降级读本地
    const list = localStorage.getItem('waitingList')
    if (list) waitingList.value = JSON.parse(list)
    console.error("加载等待列表失败:", error)
  }
}

// 🌟 核心升级：生命挂载周期内启动点对点后端专属通知监听大闸
onMounted(async () => {
  fetchBaysFromBackend().then(() => {
    checkAndTriggerWaitlist()
  })
  
  await loadWaitingList() 

  // 🚀 核心启动：拉起专属点对点通知侦听器
  startNotificationPolling()

  // 跨标签页监听核心保持原样
  window.addEventListener('storage', (e) => {
    if (e.key === 'parkingBays') {
      parkingBays.value = JSON.parse(e.newValue)
    }
    if (e.key === 'waitingList') {
      loadWaitingList()
    }
  })
})

watch(() => localStorage.getItem('waitingList'), (newVal) => {
  if (newVal) waitingList.value = JSON.parse(newVal)
})

// 弹窗表单状态
const bookingDialogVisible = ref(false)
const selectedBay = ref(null)
const bookingFormRef = ref(null)
const bookingForm = reactive({ staffId: '', plate: '', dateRange: [] })
const bookingRules = {
  staffId: [{ required: true, message: 'Required', trigger: 'blur' }],
  plate: [{ required: true, message: 'Required', trigger: 'blur' }],
  dateRange: [{ type: 'array', required: true, message: 'Required', trigger: 'change' }]
}

// 🔒 核心控制：当申请共享车位时，死死限制用户只能选择车位指定的共享时间段
const disabledSharedDates = (time) => {
  if (!selectedBay.value || (selectedBay.value.status !== 'Shared' && selectedBay.value.status !== 'shared')) {
    return time.getTime() < Date.now() - 8.64e7
  }
  const datesStr = selectedBay.value.sharedDates || selectedBay.value.expiryDate
  if (!datesStr) return false

  const parts = datesStr.split(' to ')
  if (parts.length !== 2) return false

  const startTimestamp = new Date(parts[0] + ' 00:00:00').getTime()
  const endTimestamp = new Date(parts[1] + ' 23:59:59').getTime()

  return time.getTime() < startTimestamp || time.getTime() > endTimestamp
}

const openBookingDialog = (bay) => {
  selectedBay.value = bay
  bookingForm.staffId = currentUser.value
  bookingForm.plate = ''

  if (bay.status === 'Shared' && bay.sharedDates) {
    const dates = bay.sharedDates.split(' to ')
    if (dates.length === 2) bookingForm.dateRange = [dates[0], dates[1]]
  } else {
    bookingForm.dateRange = []
  }
  bookingDialogVisible.value = true
}

// 🌟 核心修复：补齐向后端投递的 status 状态参数与安全合并后的时间段
const submitBooking = () => {
  bookingFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await axios.post('http://localhost:8080/api/parking/book', {
          bayCode: selectedBay.value.bayCode,
          staffId: bookingForm.staffId,
          plate: bookingForm.plate,
          dateRange: bookingForm.dateRange && bookingForm.dateRange.length === 2 
            ? `${bookingForm.dateRange[0]} to ${bookingForm.dateRange[1]}` 
            : 'Long-term',
          status: selectedBay.value.status,
          isUserApply: true // 🌟 核心新增：告诉后端这是普通员工在前台发起的申请，必须走审批流！
        })
        
        if (res.data.code === 200) {
          ElMessage.success(`Application for Bay ${selectedBay.value.bayCode} submitted!`)
          await fetchBaysFromBackend()
          bookingDialogVisible.value = false
        }
      } catch (err) {
        // 本地 Catch 降级逻辑维持你原有代码不变
        const newApplication = {
          id: Date.now(),
          targetBayCode: selectedBay.value.bayCode,
          staffId: bookingForm.staffId,
          plate: bookingForm.plate,
          dateRange: `${bookingForm.dateRange[0]} to ${bookingForm.dateRange[1]}`,
          applyTime: new Date().toLocaleString(),
          status: 'Pending',
          type: selectedBay.value.status === 'Shared' ? 'Sublet Request' : 'New Subscription'
        }
        const list = JSON.parse(localStorage.getItem('waitingList') || '[]')
        list.push(newApplication)
        localStorage.setItem('waitingList', JSON.stringify(list))
        waitingList.value = list
        ElMessage.success(`Application for Bay ${selectedBay.value.bayCode} committed locally!`)
        bookingDialogVisible.value = false
      }
    }
  })
}

const waitlistFormRef = ref(null)
const waitlistForm = reactive({ staffId: '', plate: '' })
const waitlistRules = {
  staffId: [{ required: true, message: 'Staff ID is required', trigger: 'blur' }],
  plate: [{ required: true, message: 'Car number is required', trigger: 'blur' }]
}

// ✅ 修复：等待列表弹窗打开时自动填充当前用户 Staff ID
watch(() => waitlistDialogVisible.value, (newVal) => {
  if (newVal) {
    waitlistForm.staffId = currentUser.value
  }
})


const submitWaitlist = () => {
  waitlistForm.staffId = currentUser.value  // 双重保险，提交前再确认一次
  waitlistFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // ✅ 新增：调用后端 API 存入数据库
        const res = await axios.post('http://localhost:8080/api/parking/waiting-list/join', {
          staffId: waitlistForm.staffId,
          plate: waitlistForm.plate,
          targetBayCode: 'Any Available',
          status: 'Pending',
          type: 'Queue Registration'
        })
        
      if (res.data.code === 200) {
          ElMessage.success('Successfully joined the waiting list!')
          waitlistDialogVisible.value = false
          waitlistForm.plate = ''
          sessionStorage.setItem('hasJoinedQueue', 'true')
  
         // ✅ 重新从后端拉取最新列表
         await loadWaitingList()
       }
     else {
          ElMessage.error(res.data.message || 'Failed to join waiting list')
        }
      } catch (err) {
        // ❌ 降级逻辑：后端连不上时，先存本地（保留你原来的逻辑）
        console.error("加入等待列表失败:", err)
        const queueRequest = {
          id: Date.now(),
          targetBayCode: 'Any Available',
          staffId: waitlistForm.staffId,
          plate: waitlistForm.plate,
          applyTime: new Date().toLocaleString(),
          status: 'Pending',
          type: 'Queue Registration'
        }
        const list = JSON.parse(localStorage.getItem('waitingList') || '[]')
        list.push(queueRequest)
        waitingList.value = list
        localStorage.setItem('waitingList', JSON.stringify(list))
        ElMessage.success('Joined waiting list (local mode)!')
        waitlistDialogVisible.value = false
        waitlistForm.plate = ''
        sessionStorage.setItem('hasJoinedQueue', 'true')
      }
    }
  })
}

const handleLogout = () => {
  sessionStorage.clear()
  ElMessage.success('Signed out successfully.')
  router.push('/login')
}

// =========================================================================
// 🌟 专属新增一：专属通知弹出器（100% 严格还原你上传的效果图样式和标题展示）
// =========================================================================
const popApprovalNotification = (notif) => {
  const isApproved = notif.status === 'Approved'
  const displayTitle = isApproved ? 'Approval Success' : 'Approval Rejected'
  const statusLabel = isApproved ? 'Approved' : 'Rejected'
  const labelColor = isApproved ? '#67c23a' : '#f56c6c'

  ElMessageBox.alert(
    `<div style="font-size: 14px;">
      <div style="margin-bottom: 10px;"><strong>Status:</strong> <span style="color: ${labelColor}; font-weight: bold;">${statusLabel}</span></div>
      <p style="color: #606266; line-height: 1.6; margin: 0;">${notif.message}</p>
     </div>`,
    displayTitle,
    {
      dangerouslyUseHTMLString: true,
      type: isApproved ? 'success' : 'error',
      confirmButtonText: 'OK',
      callback: async () => {
        await fetchBaysFromBackend()
      }
    }
  )
}

// =========================================================================
// 🌟 专属新增二：点对点工号匹配轮询侦听器（阅后即焚大闸）
// =========================================================================
let notificationTimer = null

const startNotificationPolling = () => {
  const staffId = currentUser.value
  if (!staffId) return

  notificationTimer = setInterval(async () => {
    try {
      const res = await axios.get(`http://localhost:8080/api/parking/notifications?staffId=${staffId.trim()}`)
      if (res.data && res.data.id) {
        popApprovalNotification(res.data)
      }
    } catch (err) {
      console.error("专属通知拉取异常:", err)
    }
  }, 4000)
}
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  background-color: #f5f7fa;
}
.portal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #4caf50; 
  color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.header-left { display: flex; align-items: center; gap: 10px; }
.header-icon { font-size: 24px; }
.header-title { font-size: 20px; font-weight: 600; }
.header-right { display: flex; align-items: center; gap: 15px; }
.welcome-tag { background-color: #e6a23c; border-color: #e6a23c; color: white; font-weight: bold; }
.user-dashboard { padding: 20px; }
.status-card { background-color: #fff; border-left: 5px solid #e6a23c; border-radius: 4px; }
.bays-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; padding: 10px 0; }
.bay-item {
  border: 2px solid #ebeef5; border-radius: 8px; padding: 20px; text-align: center; cursor: pointer;
  transition: all 0.3s ease; background-color: #fff; display: flex; flex-direction: column;
  justify-content: center; align-items: center; min-height: 120px;
}
.bay-item:hover { transform: translateY(-5px); box-shadow: 0 8px 16px rgba(0,0,0,0.1); border-color: #409eff; }
.bay-header { font-size: 20px; font-weight: bold; color: #303133; margin-bottom: 15px; }
.bay-body { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.shared-dates { font-size: 13px; color: #e6a23c; background-color: #fdf6ec; padding: 4px 8px; border-radius: 4px; }
.vacant { border-left: 5px solid #67c23a; }
.shared { border-left: 5px solid #e6a23c; }
.dialog-notice { background-color: #fdf6ec; padding: 10px 15px; border-radius: 4px; color: #606266; font-size: 14px; line-height: 1.5; }
.empty-state-container { padding: 40px 0; }
</style>