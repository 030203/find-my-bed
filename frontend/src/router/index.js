import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Booking from '../views/Booking.vue'
import Property from '../views/Property.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import SimpleBooking from '../views/SimpleBooking.vue'
import Stats from '../views/Stats.vue'
import Settings from '../views/Settings.vue'
import Payment from '../views/Payment.vue'
import Coupon from '../views/Coupon.vue'
import PropertyDetail from '../views/PropertyDetail.vue'
import RoomType from '../views/RoomType.vue'
import Admin from '../views/Admin.vue'
import UserProfile from '../views/UserProfile.vue'
import MerchantOrders from '../views/MerchantOrders.vue'
import PendingPayment from '../views/PendingPayment.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/booking',
    name: 'Booking',
    component: Booking
  },
  {
    path: '/simple-booking',
    name: 'SimpleBooking',
    component: SimpleBooking
  },
  {
    path: '/stats',
    name: 'Stats',
    component: Stats
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings
  },
  {
    path: '/property',
    name: 'Property',
    component: Property
  },
  {
    path: '/property/:id',
    name: 'PropertyDetail',
    component: PropertyDetail
  },
  {
    path: '/room-type',
    name: 'RoomType',
    component: RoomType
  },
  {
    path: '/payments',
    name: 'Payment',
    component: Payment
  },
  {
    path: '/pending-payment',
    name: 'PendingPayment',
    component: PendingPayment
  },
  {
    path: '/merchant-orders',
    name: 'MerchantOrders',
    component: MerchantOrders
  },
  {
    path: '/coupons',
    name: 'Coupon',
    component: Coupon
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
