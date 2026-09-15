<template>

  <div class="manage-container">

    <el-menu mode="horizontal" background-color="#4caf50" text-color="#fff" active-text-color="#ffffff" class="top-nav">

      <div class="nav-title">

        <el-icon><Compass /></el-icon>

        <span>Campus Parking Control Center</span>

      </div>

      <div class="user-info">

        <el-tag type="info" effect="dark">Role: System Administrator</el-tag>

       

        <el-button type="primary" size="small" @click="passwordDialogVisible = true">Change Password</el-button>

       

        <el-button type="danger" size="small" @click="handleLogout" class="logout-btn">Sign Out</el-button>

      </div>

    </el-menu>



    <div class="workspace-layout">

      <el-row :gutter="20">

       

       

            <el-col :span="9">
  
  <el-card class="box-card" shadow="hover" style="margin-bottom: 20px;">
    <template #header>
      <div class="card-header">
        <span class="title-text"><el-icon><DocumentChecked /></el-icon> Real-time System Approvals</span>
      </div>
    </template>
    
    <el-table :data="specificApprovals" style="width: 100%" height="260" empty-text="No pending applications">
      <el-table-column prop="staffId" label="Staff ID" width="130" />
      <el-table-column prop="plate" label="Car No." width="100" />
      <el-table-column prop="targetBayCode" label="Bay" width="60" align="center" />
      <el-table-column prop="dateRange" label="Usage Period" min-width="180" />
      <el-table-column label="Action" width="180" align="center">
  <template #default="{ row }">
    <div style="display: flex; justify-content: center; gap: 8px;">
      <el-button 
        type="success" 
        size="small" 
        @click="approveRequest(row)">
        Approve
      </el-button>
      
      <el-button 
        type="danger" 
        size="small" 
        plain 
        @click="rejectRequest(row)">
        Reject
      </el-button>
    </div>
  </template>
</el-table-column>
    </el-table>
  </el-card>

  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="title-text"><el-icon><List /></el-icon> Waiting List Management</span>
        <el-tag type="warning" size="small">Any Available</el-tag>
      </div>
    </template>
    
    <el-table :data="queueList" style="width: 100%" height="240" empty-text="No one is waiting">
      <el-table-column type="index" label="Rank" width="70" align="center" />
      <el-table-column prop="plate" label="Car No." min-width="100" />
      <el-table-column prop="staffId" label="Staff ID" min-width="120" />
      <el-table-column label="Action" width="100" align="center">
        <template #default="{ row }">
            <el-button type="primary" size="small" @click="openAllocateDialog(row)">Allocate</el-button>
            <el-button type="danger" size="small" plain @click="removeQueue(row)">Remove</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

</el-col>


        <el-col :span="15">
  <el-card class="box-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="title-text"><el-icon><Grid /></el-icon> Campus Zone A - Digital Bay Allocation Matrix</span>
      </div>
    </template>

   <div class="parking-grid">
  <div 
    v-for="bay in parkingBays" 
    :key="bay.id" 
    :class="['bay-box', bay.status.toLowerCase()]"
  >
 
    <div class="bay-header-info">
      <span class="bay-id">Bay {{ bay.bayCode || bay.id }}</span>
      
      <span class="slot-tag" v-if="bay.status === 'Vacant'">Available</span>
      <span class="slot-tag" v-else-if="bay.status === 'Occupied'" style="color: #f56c6c; font-weight: bold;">
        Occupied
      </span>
      <span class="slot-tag" v-else-if="bay.status === 'Shared'" style="color: #e6a23c; font-weight: bold;">
        Shared
      </span>
    </div>

    <div class="bay-body">
      
      <span v-if="bay.status === 'Vacant'" class="status-txt vacant">Available (Vacant)</span>
      
      <div v-else-if="bay.status === 'Occupied'" class="occupied-details">
        <span class="plate-txt" style="display: block; font-weight: bold; color: #2c3e50;">{{ bay.plate }}</span>
        <div style="margin-top: 5px;">
          <el-tag size="small" type="info" plain>
            Staff ID: {{ bay.staffId || 'Staff05' }}
          </el-tag>
        </div>
        
        <div v-if="bay.expiryDate" style="margin-top: 8px; font-size: 12px; color: #f56c6c; font-weight: bold;">
          Period: {{ bay.expiryDate }}
        </div>
      </div> <div v-else-if="bay.status === 'Shared' || bay.status === 'shared'" class="occupied-details">
        <span class="status-txt" style="color: #e6a23c; font-weight: bold; display: block; margin-bottom: 5px;">Shared (Sublet)</span>
        <div class="info-item">
          <span class="info-label" style="font-size: 13px; color: #606266;">Owner: </span>
          <span class="info-val" style="font-weight: bold; color: #2c3e50;">{{ bay.staffId }}</span>
        </div>
        <div class="info-item" style="margin-top: 4px;">
          <span class="info-label" style="font-size: 13px; color: #606266;">Free Period: </span>
          <span class="info-val" style="color: #e6a23c; font-weight: bold;">{{ bay.sharedDates || bay.plate }}</span>
        </div>
      </div> </div> <div class="bay-action" style="margin-top: 12px;">
      <div style="display: flex; gap: 8px; justify-content: center; flex-wrap: wrap;">
        
        <template v-if="bay.status === 'Vacant' || bay.status === 'vacant'">
          <el-button 
            type="primary" 
            size="small" 
            @click="matrixAssign(bay)">
            Assign
          </el-button>
        </template>
        
        <template v-else>
          <el-button 
            v-if="bay.status === 'Occupied'" 
            type="warning" 
            size="small" 
            plain 
            @click="openReleaseDialog(bay)">
            Release
          </el-button>

          <el-button 
            type="danger" 
            size="small" 
            plain 
            @click="handleCancel(bay)">
            Cancel
          </el-button>
        </template>

  </div>
</div>
      </div>
    </div>
  </el-card>

  <el-dialog v-model="releaseDialogVisible" title="Temporary Bay Release" width="450px">
    <p style="margin-bottom: 15px;">
      Select the date range to release <strong>Bay {{ currentReleaseBay?.bayCode || currentReleaseBay?.id }}</strong> for others:
    </p>
    
    <el-date-picker
      v-model="releaseDateRange"
      type="daterange"
      range-separator="To"
      start-placeholder="Start Date"
      end-placeholder="End Date"
      format="YYYY-MM-DD"
      value-format="YYYY-MM-DD"
      :disabled-date="disableReleaseDates"
      style="width: 100%"
    />

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="releaseDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmTemporaryRelease">Confirm Release</el-button>
      </span>
    </template>
  </el-dialog>

</el-col>



      </el-row>

    </div>



    <el-dialog title="Security Settings - Change Admin Password" v-model="passwordDialogVisible" width="400px" @close="resetPasswordForm">

      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">

        <el-form-item label="Old Password" prop="oldPassword">

          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="Enter current admin password" />

        </el-form-item>

        <el-form-item label="New Password" prop="newPassword">

          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="At least 6 characters" />

        </el-form-item>

        <el-form-item label="Confirm New Password" prop="confirmPassword">

          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="Repeat your new password" />

        </el-form-item>

      </el-form>

      <template #footer>

        <span class="dialog-footer">

          <el-button @click="passwordDialogVisible = false">Cancel</el-button>

          <el-button type="primary" @click="handleAdminChangePassword" :loading="passwordLoading">Update Password</el-button>

        </span>

      </template>

    </el-dialog>

  </div>

  <el-dialog title="Allocate Exclusive Bay" v-model="allocateDialogVisible" width="400px">
  <el-form :model="allocateForm" label-position="top">
    
    <el-form-item label="Staff ID">
      <el-input 
        v-model="allocateForm.staffId" 
        :disabled="!!allocateForm.id && !allocateForm.bayCode"
      ></el-input>
    </el-form-item>
    
    <el-form-item label="Car Number">
      <el-input 
        v-model="allocateForm.plate" 
        :disabled="!!allocateForm.id && !allocateForm.bayCode"
      ></el-input>
    </el-form-item>

    <el-form-item label="Select Bay">
      <el-select 
        v-model="allocateForm.bayCode" 
        placeholder="Choose an available bay" 
        :disabled="allocateForm.isMatrixMode" 
        style="width: 100%"
      >
        <el-option
          v-for="bay in availableBays"
          :key="bay.id"
          :label="`Bay ${bay.bayCode} (${bay.status})`"
          :value="bay.bayCode"
        />
      </el-select>
    </el-form-item>
    
    <el-form-item label="Usage Period">
      <el-date-picker
        v-model="allocateForm.dateRange"
        type="daterange"
        range-separator="To"
        start-placeholder="Start Date"
        end-placeholder="End Date"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="width: 100%"
      />
    </el-form-item>
  </el-form>
  
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="allocateDialogVisible = false">Cancel</el-button>
      <el-button type="primary" @click="submitAllocation">Confirm & Notify User</el-button>
    </span>
  </template>
</el-dialog>


</template>



<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const router = useRouter()

const passwordDialogVisible = ref(false)
const releaseDialogVisible = ref(false)
const allocateDialogVisible = ref(false)

const currentReleaseBay = ref(null)
const releaseDateRange = ref([])

// 🌟 状态锁定：死死对齐 HTML 中的表格绑定变量
const specificApprovals = ref([])
const parkingBays = ref([])
const waitingList = ref([]) // 🌟 净化：改用 ref 本地响应式变量来装载排队队列，不再依赖 store

// 密码表单
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordFormRef = ref(null)

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('Passwords do not match!'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: 'Old password is required', trigger: 'blur' }],
  newPassword: [{ required: true, message: 'New password is required', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 分配表单
const allocateForm = reactive({
  id: null,
  staffId: '',
  plate: '',
  bayCode: '',
  dateRange: [],
  isMatrixMode: false
})

const availableBays = computed(() => {
  return parkingBays.value.filter(bay => bay.status === 'Vacant')
})

// 🌟 净化：让排队列表直接看本地响应式的 waitingList
const queueList = computed(() => {
  return waitingList.value  // ✅ 直接返回所有，不再过滤
})

// 同步车位大盘
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
    console.error("管理端获取大盘失败:", error)
  }
}

// 拉取后端 MySQL 审批流水
const fetchApprovalsFromBackend = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/parking/approvals')
    specificApprovals.value = res.data
  } catch (error) {
    console.error("管理端拉取审批流失败:", error)
  }
}



// ✅ 改为从后端接口加载等待列表
const loadWaitingList = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/parking/waiting-list')
    // ✅ 后端直接返回数组
    if (res.data && Array.isArray(res.data)) {
      waitingList.value = res.data
      localStorage.setItem('waitingList', JSON.stringify(res.data))
    }
  } catch (error) {
    console.error("获取等待列表失败:", error)
    // 降级：读本地
    const latestWaiting = localStorage.getItem('waitingList')
    if (latestWaiting) {
      waitingList.value = JSON.parse(latestWaiting)
    }
  }
}

let adminTimer = null

onMounted(() => {
  fetchBaysFromBackend()
  fetchApprovalsFromBackend()
  loadWaitingList()

  // ⏱️ 3 秒轮询：高光实时捕获大闸
  adminTimer = setInterval(() => {
    fetchApprovalsFromBackend()
    loadWaitingList()  // ✅ 加上这一行，实时刷新等待列表
  }, 3000)

  // 跨标签页互动监听
  window.addEventListener('storage', (e) => {
    if (e.key === 'waitingList') {
      waitingList.value = JSON.parse(e.newValue)
    }
    if (e.key === 'parkingBays') {
      fetchBaysFromBackend()
    }
  })
})


// ✅ 移除等待列表记录（调用后端接口）
const removeQueue = async (row) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure to remove ${row.staffId} from waiting list?`,
      'Confirm Remove',
      { type: 'warning' }
    )
    
    const res = await axios.post('http://localhost:8080/api/parking/waiting-list/remove', {
      id: row.id,
      staffId: row.staffId  // ✅ 必须传 staffId，后端要用来发通知
    })
    
    if (res.data.code === 200) {
      ElMessage.success('Removed from waiting list. User will be notified.')
      await loadWaitingList()  // 刷新列表
    } else {
      ElMessage.error('Remove failed')
    }
  } catch (error) {
    // 用户点了取消或请求失败
    if (error?.response) {
      ElMessage.error('Remove failed')
    }
  }
}

// 审批通过枢纽
const approveRequest = async (row) => {
  try {
    const res = await axios.post('http://localhost:8080/api/parking/review', {
      id: row.id,
      action: 'APPROVED',
      targetBayCode: row.targetBayCode,
      staffId: row.staffId,
      plate: row.plate
    })
    if (res.data.code === 200) {
      ElMessage.success(`Application for Bay ${row.targetBayCode} approved.`)
      await fetchApprovalsFromBackend()
      await fetchBaysFromBackend()
    }
  } catch (error) {
    ElMessage.error("Operation failed.")
  }
}

// 审批拒绝枢纽
const rejectRequest = async (row) => {
  try {
    const res = await axios.post('http://localhost:8080/api/parking/review', {
      id: row.id,
      action: 'REJECTED',
      targetBayCode: row.targetBayCode,
      staffId: row.staffId,
      plate: row.plate
    })
    if (res.data.code === 200) {
      ElMessage.warning(`Application for Bay ${row.targetBayCode} rejected.`)
      await fetchApprovalsFromBackend()
    }
  } catch (error) {
    ElMessage.error("Operation failed.")
  }
}

// 🌟 核心状态：同时记录当前点击车位的【开始日期边界】和【结束日期边界】
const minReleaseDate = ref('')
const maxReleaseDate = ref('')

// 打开临时共享弹窗
const openReleaseDialog = (bay) => {
  currentReleaseBay.value = bay
  releaseDateRange.value = [] // 清空历史选择
  
  // 🔒 动态双向提取车位原本的 Period 范围
  if (bay.expiryDate && bay.expiryDate.includes('to')) {
    const parts = bay.expiryDate.split(' to ')
    minReleaseDate.value = parts[0] ? parts[0].trim() : ''
    maxReleaseDate.value = parts[1] ? parts[1].trim() : ''
  } else if (bay.expiryDate) {
    // 如果没有 'to' 只是单日期，则默认今天到截止日
    minReleaseDate.value = ''
    maxReleaseDate.value = bay.expiryDate.trim()
  } else {
    minReleaseDate.value = ''
    maxReleaseDate.value = '2026-08-31'
  }
  
  releaseDialogVisible.value = true
}

// 💥 终极双向拦截大闸：只允许在车位真正的 [开始日, 结束日] 之间选择！
const disableReleaseDates = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  // 1. 计算系统应该遵循的【实际允许最左侧时间点】
  // 如果车位本身的起租日（minReleaseDate）比今天还晚，就必须以车位起租日为准！
  let startLimit = today.getTime()
  if (minReleaseDate.value) {
    const minLimitObj = new Date(minReleaseDate.value)
    minLimitObj.setHours(0, 0, 0, 0)
    // 取今天和车位起租日的较大值，确保两头不穿帮
    startLimit = Math.max(today.getTime(), minLimitObj.getTime())
  }
  
  // 2. 计算【实际允许最右侧时间点】
  if (maxReleaseDate.value) {
    const maxLimitObj = new Date(maxReleaseDate.value)
    maxLimitObj.setHours(23, 59, 59, 999)
    
    // 🔒 核心死锁：小于允许开始日，或者大于允许结束日的，统统变成灰色不可选！
    return time.getTime() < startLimit || time.getTime() > maxLimitObj.getTime()
  }
  
  return time.getTime() < startLimit
}

const confirmTemporaryRelease = async () => {
  if (!releaseDateRange.value || releaseDateRange.value.length !== 2) {
    ElMessage.warning('Please select a valid date range.')
    return
  }
  const [startDate, endDate] = releaseDateRange.value
  const bay = currentReleaseBay.value
  const combinedPeriod = `${startDate} to ${endDate}`

  try {
    const res = await axios.post('http://localhost:8080/api/parking/book', {
      bayCode: bay.bayCode,
      staffId: bay.staffId,
      plate: combinedPeriod,
      status: 'Shared'
    })

    if (res.data.code === 200) {
      ElMessage.success(`Bay ${bay.bayCode} successfully shared!`)
      releaseDialogVisible.value = false
      await fetchBaysFromBackend()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error("Failed to save shared dates to database.")
  }
}

const matrixAssign = (bay) => {
  allocateForm.id = null
  allocateForm.staffId = ''
  allocateForm.plate = ''
  allocateForm.bayCode = bay.bayCode
  allocateForm.dateRange = []
  allocateForm.isMatrixMode = true
  allocateForm.status = bay.status
  allocateDialogVisible.value = true
}

const openAllocateDialog = (row) => {
  allocateForm.id = row.id
  allocateForm.staffId = row.staffId
  allocateForm.plate = row.plate
  allocateForm.bayCode = row.targetBayCode !== 'Any Available' ? row.targetBayCode : ''
  allocateForm.dateRange = row.dateRange ? row.dateRange.split(' to ') : []
  allocateForm.isMatrixMode = false
  allocateDialogVisible.value = true
}

const submitAllocation = async () => {
  if (!allocateForm.staffId || !allocateForm.plate || !allocateForm.bayCode || !allocateForm.dateRange || allocateForm.dateRange.length !== 2) {
    ElMessage.warning('Please complete all assignment fields.')
    return
  }

  const dateRangeStr = `${allocateForm.dateRange[0]} to ${allocateForm.dateRange[1]}`

  try {
    // 如果是从等待列表点 Allocate 进来的（有 id）
    if (allocateForm.id) {
      // ✅ 调用等待列表分配接口
      const res = await axios.post('http://localhost:8080/api/parking/waiting-list/allocate', {
        id: allocateForm.id,
        bayCode: allocateForm.bayCode,
        staffId: allocateForm.staffId,
        plate: allocateForm.plate,
        dateRange: dateRangeStr
      })
      
      if (res.data.code === 200) {
        ElMessage.success(`Bay ${allocateForm.bayCode} assigned successfully! User will be notified.`)
        allocateDialogVisible.value = false
        await loadWaitingList()     // 刷新等待列表
        await fetchBaysFromBackend() // 刷新车位大盘
      } else {
        ElMessage.error('Allocation failed')
      }
    } else {
      // 从车位矩阵点 Assign 进来的，走原来的逻辑
      const res = await axios.post('http://localhost:8080/api/parking/book', {
        bayCode: allocateForm.bayCode,
        staffId: allocateForm.staffId,
        plate: allocateForm.plate,
        dateRange: dateRangeStr,
        status: 'Occupied'
      })

      if (res.data.code === 200) {
        ElMessage.success(`Bay ${allocateForm.bayCode} assigned successfully!`)
        allocateDialogVisible.value = false
        await fetchBaysFromBackend()
      }
    }
  } catch (error) {
    console.error("Allocation failed:", error)
    ElMessage.error("Allocation failed.")
  }
}

// 🌟 核心升级：处理点击 Cancel 按钮，将红/黄车位一秒洗白复位为 Vacant
const handleCancel = async (bay) => {
  try {
    // 🚀 向后端投递清空大闸：状态改为 Vacant，工号、车牌、有效期、共享时间全部洗成空字符串
    const res = await axios.post('http://localhost:8080/api/parking/book', {
      bayCode: bay.bayCode,
      staffId: '',
      plate: '',
      dateRange: '', // 清空长租周期
      status: 'Vacant' // 强行拉回空闲状态
    })
    
    if (res.data.code === 200) {
      ElMessage.success(`Bay ${bay.bayCode} has been successfully reset to Available!`)
      
      // 🌟 核心联动：立刻重新拉取后端车位状态与大盘数据，让格子在两端瞬间全部变绿！
      await fetchBaysFromBackend()
    }
  } catch (error) {
    console.error("Failed to release bay:", error)
    ElMessage.error("Failed to release bay from database.")
  }
}

const resetPasswordForm = () => {
  if (passwordFormRef.value) passwordFormRef.value.resetFields()
}

const handleLogout = () => {
  ElMessage.success('Signed out successfully.')
  router.push('/login')
}
</script>


<style scoped>

.manage-container {

  min-height: 100vh;

  background-color: #f4f7f6;

}

.top-nav {

  display: flex;

  align-items: center;

  justify-content: space-between;

  padding: 0 20px;

  box-shadow: 0 2px 8px rgba(0,0,0,0.06);

}

.nav-title {

  color: #fff;

  font-size: 18px;

  font-weight: 600;

  display: flex;

  align-items: center;

  gap: 8px;

}

.user-info {

  display: flex;

  align-items: center;

  gap: 15px;

}

.workspace-layout {

  padding: 20px;

}

.box-card {

  margin-bottom: 20px;

  border-radius: 12px;

  border: none;

  box-shadow: 0 4px 12px rgba(0,0,0,0.03) !important;

}

.title-text {

  font-weight: 600;

  color: #2c3e50;

  display: flex;

  align-items: center;

  gap: 6px;

}

.action-btn {

  width: 100%;

  font-weight: 600;

}

.summary-header {

  display: flex;

  justify-content: space-between;

  align-items: center;

  width: 100%;

}

.log-timeline {

  max-height: 250px;

  overflow-y: auto;

  padding-top: 5px;

}

.parking-grid {

  display: grid;

  grid-template-columns: repeat(3, 1fr);

  gap: 15px;

}

.bay-box {

  border: 2px dashed #ccc;

  border-radius: 10px;

  padding: 15px;

  min-height: 130px;

  display: flex;

  flex-direction: column;

  justify-content: space-between;

  transition: all 0.3s;

  background-color: #fff;

}

.bay-box.vacant {

  border-color: #67c23a;

  background-color: #f0f9eb;

}

.bay-box.occupied {

  border-color: #f56c6c;

  background-color: #fef0f0;

}

.bay-header-info {

  display: flex;

  justify-content: space-between;

  align-items: center;

}

.bay-id {

  font-weight: bold;

  color: #34495e;

  font-size: 15px;

}

.slot-tag {

  font-size: 11px;

  color: #909399;

  background: rgba(0,0,0,0.04);

  padding: 2px 6px;

  border-radius: 4px;

}

.status-txt.vacant {

  color: #67c23a;

  font-size: 13px;

  font-weight: 500;

}

.occupied-details {

  display: flex;

  flex-direction: column;

  gap: 4px;

  margin: 8px 0;

}

.plate-txt {

  font-size: 16px;

  font-weight: bold;

  color: #c0392b;

}

.bay-action {

  text-align: right;

}


/* 1. 让最外层的容器启用 Flex 布局，使列高度自动拉伸对齐 */
.workspace-layout .el-row {
  display: flex;
  align-items: stretch; /* 核心：让所有 el-col 高度对齐 */
}

/* 2. 确保每一列也是 Flex 布局，以便让内部的卡片撑满高度 */
.workspace-layout .el-col {
  display: flex;
  flex-direction: column;
}

/* 3. 强制卡片撑满列的高度 */
.box-card {
  flex: 1; /* 核心：自动占据剩余空间 */
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03) !important;
  display: flex;
  flex-direction: column;
}

/* 4. 确保 Card 的内容区域能随卡片高度缩放 */
.box-card .el-card__body {
  flex: 1; /* 内容区自动伸缩 */
  display: flex;
  flex-direction: column;
}

/* 新增：让左侧表单垂直居中 */
.box-card .el-form {
  margin-top: auto;
  margin-bottom: auto;
}

</style> 