import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useParkingStore = defineStore('parking', () => {
  // --- 1. 初始化等待队列 (Waiting List) ---
  const savedWaiting = localStorage.getItem('waitingList');
 const waitingList = ref(savedWaiting ? JSON.parse(savedWaiting) : [
    { id: 101, plate: 'SUK 2501', applyTime: '12/06/2026, 11:00', expiryDate: '2026-12-31', status: 'Pending' },
    { id: 102, plate: 'VAA 1923', applyTime: '12/06/2026, 11:01', expiryDate: '2026-10-15', status: 'Pending' }
  ]);

  // --- 2. 初始化历史月租记录 (Reservations) ---
  const savedReservations = localStorage.getItem('reservations');
  const reservations = ref(savedReservations ? JSON.parse(savedReservations) : [
    { id: 1, expiryDate: '2026-04-30', fee: 'RM 80.00', bayCode: 'A05', status: 'Completed' },
    { id: 2, expiryDate: '2026-05-30', fee: 'RM 80.00', bayCode: 'A02', status: 'Completed' }
  ]);

  // --- 操作方法：等待队列 ---
  const addWaiting = (newEntry) => {
    waitingList.value.push(newEntry);
    localStorage.setItem('waitingList', JSON.stringify(waitingList.value));
  };

const removeWaiting = (id) => {
    // 🌟 安全锁：如果没有 ID，直接拦截！
    if (!id) {
      console.error("警告：试图删除一条没有 ID 的排队记录！");
      return; 
    }
    
    // 正常过滤并保存
    waitingList.value = waitingList.value.filter(item => item.id !== id);
    localStorage.setItem('waitingList', JSON.stringify(waitingList.value));
  };


  
// --- 操作方法：添加成功分配的记录 (给 UserDashboard 看的) ---
  const addReservation = (newRes) => {
    // 🌟 核心修改：给每一条记录自动添加一个唯一的 ID
    // 这样后续点击 Complete 时，程序才知道你改的是哪一行
    const reservationWithId = { ...newRes, id: Date.now() }; 
    reservations.value.unshift(reservationWithId);
    localStorage.setItem('reservations', JSON.stringify(reservations.value));
  };

  // 🌟它们分别负责“状态切换”和“数据删除”，并且都会同步到 localStorage。
  const completeReservation = (id) => {
  const item = reservations.value.find(r => r.id === id);
  if (item) {
    item.status = 'Completed'; // 切换为 Completed
    localStorage.setItem('reservations', JSON.stringify(reservations.value));
  }
};

const deleteReservation = (id) => {
  // 🌟 安全锁：如果传进来的 id 是空的（undefined），直接拦截，不执行删除！
  if (!id) {
    console.error("警告：试图删除一条没有 ID 的无效数据！");
    return; 
  }
  
  // 正常的删除逻辑
  reservations.value = reservations.value.filter(r => r.id !== id);
  localStorage.setItem('reservations', JSON.stringify(reservations.value));
};

  // 导出所有方法，别忘了把 completeReservation 加上
  return { 
    waitingList, 
    reservations, 
    addWaiting, 
    removeWaiting, 
    addReservation,
    completeReservation, // 必须加这一行，否则页面调用不到
    deleteReservation
  };
});