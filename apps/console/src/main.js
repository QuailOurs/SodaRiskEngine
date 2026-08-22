// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import store from './store'
import iView from 'view-design'
import i18n from '@/locale'
import config from '@/config'
import services from '@/config/services'
import importDirective from '@/directive'
import { directive as clickOutside } from 'v-click-outside-x'
import installPlugin from '@/plugin'
import './index.less'
import '@/assets/icons/iconfont.css'
import TreeTable from 'tree-table-vue'
import VOrgTree from 'v-org-tree'
import cascaderMulti from 'cascader-multi'
import 'v-org-tree/dist/v-org-tree.css'
import { initRouter } from '@/libs/router-util' // 新增 引入动态菜单渲染
import 'view-design/dist/styles/iview.css'
import { ResError } from '@/libs/error/ResError'

// 实际打包时应该不引入mock
/* eslint-disable */
// if (process.env.NODE_ENV !== 'production') require('@/mock')

Vue.use(iView, {
  i18n: (key, value) => i18n.t(key, value),
  size: 'default',
})
Vue.use(TreeTable)
Vue.use(VOrgTree)
Vue.use(cascaderMulti)
/**
 * @description 注册admin内置插件
 */
installPlugin(Vue)
/**
 * @description 生产环境关掉提示
 */
Vue.config.productionTip = false
/**
 * @description 全局注册应用配置
 */
Vue.prototype.$config = config
/**
 * @description 服务列表配置
 */
Vue.prototype.$services = services
/**
 * 注册指令
 */
importDirective(Vue)
Vue.directive('clickOutside', clickOutside)
Vue.prototype.$throw = (error, vm, mes) => {
  iView.Message.destroy()
  if(!(error instanceof ResError)){
    //iView.Message.error("系统出了点小差，请联系管理员修复一下~");
    console.error(error)
  }else{
    iView.Message.error(error.message);
  }
}
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  i18n,
  store,
  render: h => h(App),
  mounted() {
    initRouter() // 新增 调用方法,动态生成路由
  }
})
