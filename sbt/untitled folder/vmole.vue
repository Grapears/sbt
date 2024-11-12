import axios from 'axios'

const axiosInstance = axios.create({
	baseURL: import.meta.env.VITE_API_URL,
	withCredentials: true
})

export default axiosInstance
-----------------------------
VITE_API_URL=http://localhost:9000/api/
-----------------------------
import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useRouteStore = defineStore('route', () => {
	const currentLocationRef = ref()
	return {
		currentLocationRef
	}
})
----------------------------
import { createRouter, createWebHistory } from 'vue-router'
import { useCheckUserStore } from '@/stores/checkUser'
import { useRouteStore } from '@/stores/route'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/login',
      name: 'login',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/views/Login.vue'),
      meta: {
        requiresGuest: true
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: {
        requiresGuest: true
      }
    },
    {
      path: '/forgotPassword',
      name: 'forgot',
      component: () => import('@/views/ForgotPassword.vue'),
      meta: {
        requiresGuest: true
      }
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  const { checkUser, fetchUser } = useCheckUserStore()
  const routeStore = useRouteStore()
  const token = localStorage.getItem('userEmail')
  routeStore.currentLocationRef = to.path
  await fetchUser()
  if(to.meta.requiresAuth && !checkUser){
    next({name: 'login'})
  }else if(to.meta.requiresGuest && checkUser){
    next({name: 'dashboard'})
  }else {
    next()
  }
})

export default router
