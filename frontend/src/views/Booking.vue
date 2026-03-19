<template>
  <component :is="currentView" />
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import UserBooking from './UserBooking.vue'
import StaffBooking from './StaffBooking.vue'

const currentUser = ref(null)

const syncUser = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    currentUser.value = null
    return
  }
  try {
    currentUser.value = JSON.parse(userStr)
  } catch (error) {
    currentUser.value = null
  }
}

const currentView = computed(() => {
  const role = currentUser.value?.role
  return role === 'MERCHANT' || role === 'ADMIN' ? StaffBooking : UserBooking
})

onMounted(() => {
  syncUser()
  window.addEventListener('user-changed', syncUser)
  window.addEventListener('storage', syncUser)
})

onBeforeUnmount(() => {
  window.removeEventListener('user-changed', syncUser)
  window.removeEventListener('storage', syncUser)
})
</script>
