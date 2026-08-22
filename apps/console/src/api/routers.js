import axios from '@/libs/api.request'
import globalConfig from '@/config'

export const getRouterReq = access => axios.request({
  url: 'get_router',
  params: { access },
  method: 'get'
})

const engineMenu = {
  code: 200,
  msg: 'success',
  data: [
    {
      path: '/engine',
      name: 'engine',
      meta: { icon: 'ios-options', title: '规则引擎' },
      component: 'Main',
      children: [
        {
          path: '/business-side/index',
          name: 'business-side',
          meta: { icon: 'ios-briefcase', title: '业务方配置' },
          component: 'strategy-engine/business-side/index'
        },
        {
          path: '/scene/index',
          name: 'scene',
          meta: { icon: 'md-git-branch', title: '场景管理' },
          component: 'strategy-engine/scene/index'
        },
        {
          path: '/strategy/index',
          name: 'strategy',
          meta: { icon: 'md-options', title: '规则管理' },
          component: 'strategy-engine/strategy/index'
        },
        {
          path: '/tool/index',
          name: 'tool',
          meta: { icon: 'md-build', title: '工具管理' },
          component: 'strategy-engine/tool/index'
        },
        {
          path: '/feature-statistics/index',
          name: 'feature-statistics',
          meta: { icon: 'md-trending-up', title: '累计特征管理' },
          component: 'strategy-engine/feature-statistics/index'
        },
        {
          path: '/param/index',
          name: 'param',
          meta: { icon: 'md-list-box', title: '字段管理' },
          component: 'strategy-engine/param/index'
        },
        {
          path: '/complement/index',
          name: 'complement',
          meta: { icon: 'md-git-merge', title: '参数补全管理' },
          component: 'strategy-engine/complement/index'
        },
        {
          path: '/feature/index',
          name: 'feature',
          meta: { icon: 'md-pulse', title: '基础特征管理' },
          component: 'configuration/entity'
        }
      ]
    },
    {
      path: '/risk-decision',
      name: 'risk-decision',
      meta: { icon: 'md-shield', title: '风险决策' },
      component: 'Main',
      children: [
        {
          path: '/risk-config/index',
          name: 'risk-config',
          meta: { icon: 'md-speedometer', title: '风险配置' },
          component: 'configuration/entity'
        },
        {
          path: '/black-white-list/index',
          name: 'black-white-list',
          meta: { icon: 'md-list-box', title: '黑白名单' },
          component: 'configuration/entity'
        },
        {
          path: '/return-code/index',
          name: 'return-code',
          meta: { icon: 'md-return-left', title: '返回码配置' },
          component: 'configuration/entity'
        }
      ]
    },
    {
      path: '/disposer-center',
      name: 'disposer-center',
      meta: { icon: 'md-hammer', title: '处置中心' },
      component: 'Main',
      children: [
        {
          path: '/disposer/index',
          name: 'disposer',
          meta: { icon: 'md-build', title: '处置方式' },
          component: 'configuration/entity'
        }
      ]
    },
    {
      path: '/operations',
      name: 'operations',
      meta: { icon: 'md-flask', title: '运行与调试' },
      component: 'Main',
      children: [
        {
          path: '/operations/playground',
          name: 'playground',
          meta: { icon: 'md-play', title: '引擎调试台' },
          component: 'operations/playground'
        }
      ]
    }
  ]
}

export const getMenu = () => {
  if (globalConfig.devMode) return engineMenu
  return axios.request({ url: '/right', method: 'get' })
}
