import { expect } from 'chai'
import api from '@/libs/api.request'
import {
  createCrudApi,
  businessSideApi,
  sceneApi,
  strategyApi,
  ruleApi,
  featureApi,
  riskConfigApi,
  blackWhiteListApi,
  returnCodeApi,
  disposerConfigApi,
  getSceneOptions,
  getBusinessSideOptions,
  getRuleOptions,
  getDisposerOptions,
  syncConfig,
  computeStrategy,
  identifyRisk,
  executeDisposer,
  queryDisposerStatus
} from '@/api/config-admin'

describe('configuration API contracts', () => {
  let originalRequest
  let calls

  beforeEach(() => {
    originalRequest = api.request
    calls = []
    api.request = options => {
      calls.push(options)
      return Promise.resolve({ data: { code: 0 } })
    }
  })

  afterEach(() => {
    api.request = originalRequest
  })

  it('builds list, detail, add, update and delete requests with explicit methods', async () => {
    const crud = createCrudApi({
      list: 'items/list',
      detail: 'items/{id}',
      add: 'items',
      update: 'items',
      remove: 'items/{id}'
    })
    await crud.list({ state: 1 })
    await crud.detail(7)
    await crud.add({ name: 'Soda' })
    await crud.update({ id: 7 })
    await crud.remove(7)

    expect(calls).to.deep.equal([
      { url: 'items/list', method: 'post', data: { state: 1 } },
      { url: 'items/7', method: 'get', data: undefined },
      { url: 'items', method: 'post', data: { name: 'Soda' } },
      { url: 'items', method: 'put', data: { id: 7 } },
      { url: 'items/7', method: 'delete', data: undefined }
    ])
  })

  it('keeps every matrix CRUD domain connected to its backend route', async () => {
    const domains = [
      [businessSideApi, 'strategy-engine-config-center/businessside'],
      [sceneApi, 'strategy-engine-config-center/scene'],
      [strategyApi, 'strategy-engine-config-center/strategy'],
      [ruleApi, 'strategy-engine-config-center/rule'],
      [featureApi, 'feature-operation-center/feature'],
      [riskConfigApi, 'risk-decision-config-center/risk'],
      [blackWhiteListApi, 'risk-decision-config-center/blackWhiteList'],
      [returnCodeApi, 'risk-decision-config-center/returnCode'],
      [disposerConfigApi, 'disposer-config-center/disposerConfig']
    ]
    for (const [domain] of domains) {
      await domain.list()
      await domain.detail(5)
      await domain.add({ name: 'test' })
      await domain.update({ id: 5 })
      await domain.remove(5)
    }
    domains.forEach(([, prefix]) => {
      expect(calls.some(call => call.url.startsWith(prefix))).to.equal(true)
    })
    expect(calls.find(call => call.url.endsWith('strategy/update')).method).to.equal('post')
    expect(calls.find(call => call.url.endsWith('rule/update')).method).to.equal('post')
    expect(calls.find(call => call.url.endsWith('feature/update')).method).to.equal('post')
  })

  it('loads editor options and encodes disposer status path data', async () => {
    await getSceneOptions()
    await getBusinessSideOptions()
    await getRuleOptions({ sceneKey: 'login' })
    await getDisposerOptions()
    await queryDisposerStatus('user/a b')

    expect(calls.map(call => call.url)).to.deep.equal([
      'strategy-engine-config-center/scene/sceneName/list',
      'strategy-engine-config-center/businessside/listAll',
      'strategy-engine-config-center/rule/list',
      'disposer-config-center/disposerConfig/list',
      'v1/disposer/status/user%2Fa%20b'
    ])
  })

  it('uses JSON, query and body contracts for runtime operations', async () => {
    await computeStrategy({ sceneKey: 'login' })
    await identifyRisk({ openKey: 'demo' })
    await executeDisposer({ userId: 'u-1' }, { disposerType: 'LOCK' })
    await syncConfig('rule')
    await syncConfig('risk')

    expect(calls.slice(0, 3)).to.deep.equal([
      { url: 'v1/engine/evaluate', method: 'post', data: { sceneKey: 'login' } },
      { url: 'v1/risk/identification', method: 'post', params: { openKey: 'demo' } },
      { url: 'v1/disposer/execute', method: 'post', params: { userId: 'u-1' }, data: { disposerType: 'LOCK' } }
    ])
    expect(calls.slice(3).map(call => call.url)).to.deep.equal([
      'v1/config/sync/rule',
      'v1/engine/config/reload',
      'v1/config/sync/risk'
    ])
  })
})
