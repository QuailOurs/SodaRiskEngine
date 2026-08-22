import axios from '@/libs/api.request'

const request = (url, method = 'get', data) => axios.request({ url, method, data })

export const createCrudApi = config => ({
  list: filters => request(config.list, config.listMethod || 'post', filters || {}),
  detail: config.detail ? id => request(config.detail.replace('{id}', id)) : null,
  add: data => request(config.add, config.addMethod || 'post', data),
  update: data => request(config.update, config.updateMethod || 'put', data),
  remove: config.remove ? id => request(config.remove.replace('{id}', id), 'delete') : null
})

export const businessSideApi = createCrudApi({
  list: 'strategy-engine-config-center/businessside/list',
  detail: 'strategy-engine-config-center/businessside/{id}',
  add: 'strategy-engine-config-center/businessside/add',
  update: 'strategy-engine-config-center/businessside/update',
  remove: 'strategy-engine-config-center/businessside/{id}'
})

export const sceneApi = createCrudApi({
  list: 'strategy-engine-config-center/scene/list',
  detail: 'strategy-engine-config-center/scene/{id}',
  add: 'strategy-engine-config-center/scene/add',
  update: 'strategy-engine-config-center/scene/update',
  remove: 'strategy-engine-config-center/scene/{id}'
})

export const strategyApi = createCrudApi({
  list: 'strategy-engine-config-center/strategy/list',
  detail: 'strategy-engine-config-center/strategy/id/{id}',
  add: 'strategy-engine-config-center/strategy/add',
  update: 'strategy-engine-config-center/strategy/update',
  updateMethod: 'post',
  remove: 'strategy-engine-config-center/strategy/{id}'
})

export const ruleApi = createCrudApi({
  list: 'strategy-engine-config-center/rule/list',
  detail: 'strategy-engine-config-center/rule/detail/{id}',
  add: 'strategy-engine-config-center/rule/add',
  update: 'strategy-engine-config-center/rule/update',
  updateMethod: 'post',
  remove: 'strategy-engine-config-center/rule/delete/{id}'
})

export const featureApi = createCrudApi({
  list: 'feature-operation-center/feature/list',
  detail: 'feature-operation-center/feature/{id}',
  add: 'feature-operation-center/feature/add',
  update: 'feature-operation-center/feature/update',
  updateMethod: 'post',
  remove: 'feature-operation-center/feature/{id}'
})

export const riskConfigApi = createCrudApi({
  list: 'risk-decision-config-center/risk/list',
  detail: 'risk-decision-config-center/risk/{id}',
  add: 'risk-decision-config-center/risk/add',
  update: 'risk-decision-config-center/risk/update',
  remove: 'risk-decision-config-center/risk/{id}'
})

export const blackWhiteListApi = createCrudApi({
  list: 'risk-decision-config-center/blackWhiteList/list',
  detail: 'risk-decision-config-center/blackWhiteList/{id}',
  add: 'risk-decision-config-center/blackWhiteList/add',
  update: 'risk-decision-config-center/blackWhiteList/update',
  remove: 'risk-decision-config-center/blackWhiteList/{id}'
})

export const returnCodeApi = createCrudApi({
  list: 'risk-decision-config-center/returnCode/list',
  detail: 'risk-decision-config-center/returnCode/{id}',
  add: 'risk-decision-config-center/returnCode/add',
  update: 'risk-decision-config-center/returnCode/update',
  remove: 'risk-decision-config-center/returnCode/{id}'
})

export const disposerConfigApi = createCrudApi({
  list: 'disposer-config-center/disposerConfig/list',
  detail: 'disposer-config-center/disposerConfig/{id}',
  add: 'disposer-config-center/disposerConfig',
  update: 'disposer-config-center/disposerConfig',
  remove: 'disposer-config-center/disposerConfig/{id}'
})

export const getSceneOptions = () => request('strategy-engine-config-center/scene/sceneName/list')
export const getBusinessSideOptions = () => request('strategy-engine-config-center/businessside/listAll')
export const getRuleOptions = filters => request('strategy-engine-config-center/rule/list', 'post', filters || {})
export const getDisposerOptions = () => request('disposer-config-center/disposerConfig/list')

export const syncConfig = async type => {
  const response = await request(`v1/config/sync/${type}`, 'post')
  if (['all', 'scene', 'strategy', 'rule', 'feature'].includes(type)) {
    return request('v1/engine/config/reload', 'post')
  }
  return response
}

export const computeStrategy = data => axios.request({
  url: 'v1/engine/evaluate',
  method: 'post',
  data
})

export const identifyRisk = params => axios.request({
  url: 'v1/risk/identification',
  method: 'post',
  params
})

export const executeDisposer = (params, data) => axios.request({
  url: 'v1/disposer/execute',
  method: 'post',
  params,
  data
})

export const queryDisposerStatus = userId => request(`v1/disposer/status/${encodeURIComponent(userId)}`)
