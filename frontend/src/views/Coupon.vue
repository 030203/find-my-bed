<template>
  <div class="coupon">
    <el-page-header content="优惠券管理" icon="ArrowLeft" @back="$router.push('/')" />

    <el-card shadow="hover" class="mt-16">
      <template #header>
        <div class="card-header">
          <div>优惠券列表</div>
          <div class="actions">
            <el-select v-model="filters.status" placeholder="按状态" clearable size="small" style="width: 140px">
              <el-option label="启用" value="ACTIVE" />
              <el-option label="停用" value="INACTIVE" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
            <el-input
              v-model="filters.merchantId"
              size="small"
              placeholder="商户ID（可选）"
              style="width: 150px"
              clearable
            />
            <el-button type="primary" size="small" @click="loadCoupons">筛选</el-button>
            <el-button size="small" @click="openDialog()">新增优惠券</el-button>
          </div>
        </div>
      </template>

      <el-table :data="coupons" stripe v-loading="loading">
        <el-table-column prop="couponName" label="名称" width="180" />
        <el-table-column prop="couponType" label="类型" width="100">
          <template #default="{ row }">{{ row.couponType === 'PERCENTAGE' ? '折扣' : '立减' }}</template>
        </el-table-column>
        <el-table-column prop="discountValue" label="面值" width="120">
          <template #default="{ row }">
            {{ row.couponType === 'PERCENTAGE' ? row.discountValue + '%' : '¥' + row.discountValue }}
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="门槛" width="120">
          <template #default="{ row }">¥{{ Number(row.minAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="发放/已用" width="140">
          <template #default="{ row }">{{ row.totalQuantity }} / {{ row.usedQuantity || 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="起止时间" width="220">
          <template #default="{ row }">
            <div>{{ row.startTime }}</div>
            <div>{{ row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除？" @confirm="remove(row)">
              <template #reference>
                <el-button size="small" type="danger" plain>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑优惠券' : '新增优惠券'" width="680px">
      <el-form :model="form" label-width="130px" ref="formRef" :rules="rules">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="couponName">
              <el-input v-model="form.couponName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="couponType">
              <el-select v-model="form.couponType" style="width: 100%">
                <el-option label="立减" value="FIXED" />
                <el-option label="折扣" value="PERCENTAGE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面值/折扣" prop="discountValue">
              <el-input v-model.number="form.discountValue" type="number" placeholder="立减金额或折扣百分比" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大优惠(可选)">
              <el-input v-model.number="form.maxDiscount" type="number" placeholder="仅折扣券使用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低消费">
              <el-input v-model.number="form.minAmount" type="number" placeholder="默认为0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总数量" prop="totalQuantity">
              <el-input v-model.number="form.totalQuantity" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用商户ID">
              <el-input v-model="form.merchantId" placeholder="为空则为平台券" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" value="ACTIVE" />
                <el-option label="停用" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适用民宿ID列表">
          <el-input
            v-model="form.applicableProperties"
            type="textarea"
            :rows="2"
            placeholder='JSON 数组，如 [1,2]'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { couponApi } from '../api'

const coupons = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const filters = reactive({
  status: '',
  merchantId: ''
})

const defaultForm = () => ({
  id: null,
  couponName: '',
  couponType: 'FIXED',
  discountValue: 0,
  minAmount: 0,
  maxDiscount: null,
  totalQuantity: 100,
  usedQuantity: 0,
  startTime: '',
  endTime: '',
  status: 'ACTIVE',
  merchantId: '',
  applicableProperties: ''
})

const form = reactive(defaultForm())

const rules = {
  couponName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  couponType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入面值', trigger: 'blur' }],
  totalQuantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const statusText = (val) => {
  const map = { ACTIVE: '启用', INACTIVE: '停用', EXPIRED: '已过期' }
  return map[val] || val || '-'
}
const statusType = (val) => {
  const map = { ACTIVE: 'success', INACTIVE: 'info', EXPIRED: 'warning' }
  return map[val] || 'info'
}

const loadCoupons = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.status) params.status = filters.status
    if (filters.merchantId) params.merchantId = filters.merchantId
    const { data } = await couponApi.getList(params)
    coupons.value = data || []
  } catch (err) {
    ElMessage.error('加载优惠券失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, defaultForm(), row || {})
  form.merchantId = row?.merchant?.id || ''
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    payload.merchant = form.merchantId ? { id: form.merchantId } : null
    delete payload.merchantId
    if (form.id) {
      await couponApi.update(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await couponApi.create(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCoupons()
  } catch (err) {
    ElMessage.error(err?.response?.data?.error || '保存失败')
  } finally {
    submitting.value = false
  }
}

const remove = async (row) => {
  try {
    await couponApi.delete(row.id)
    ElMessage.success('已删除')
    loadCoupons()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

onMounted(loadCoupons)
</script>

<style scoped>
.coupon {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.mt-16 {
  margin-top: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
