import Main from '@/components/main'
import { dynamicRouterAdd } from '@/libs/router-util'

export const otherRouter = [
  {
    path: '/401',
    name: 'error_401',
    meta: { hideInMenu: true },
    component: () => import('@/view/error-page/401.vue')
  },
  {
    path: '/500',
    name: 'error_500',
    meta: { hideInMenu: true, title: '服务端错误' },
    component: () => import('@/view/error-page/500.vue')
  },
  {
    path: '*',
    name: 'error_404',
    meta: { hideInMenu: true },
    component: () => import('@/view/error-page/404.vue')
  }
]

export const mainRouter = [
  {
    path: '/',
    name: '_home',
    redirect: '/home',
    component: Main,
    meta: { hideInMenu: true, notCache: true },
    children: [
      {
        path: '/home',
        name: 'home',
        meta: {
          hideInMenu: true,
          title: '首页',
          notCache: true,
          icon: 'md-home'
        },
        component: () => import('@/view/single-page/home')
      }
    ]
  }
]

export const appRouter = [...dynamicRouterAdd()]

export const routes = [
  ...otherRouter,
  ...mainRouter,
  ...appRouter
]

export default routes
