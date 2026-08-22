import { expect } from 'chai'
import api from '@/libs/api.request'
import { syncConfig } from '@/api/config-admin'
import EnginePlayground from '@/view/operations/playground.vue'

describe('EnginePlayground', () => {
  let originalRequest

  beforeEach(() => {
    originalRequest = api.request
  })

  afterEach(() => {
    api.request = originalRequest
  })

  it('submits the debug form to the JSON engine API', async () => {
    const calls = []
    api.request = options => {
      calls.push(options)
      return Promise.resolve({ data: { code: 0 } })
    }
    let action
    const context = {
      requestId: 'debug-test-001',
      openKey: 'demo_business',
      sceneKey: 'login_protection',
      needDetail: true,
      requestJson: '{"blacklisted":true}',
      execute: value => { action = value }
    }

    EnginePlayground.methods.compute.call(context)
    await action()

    expect(calls).to.have.length(1)
    expect(calls[0]).to.deep.include({ url: 'v1/engine/evaluate', method: 'post' })
    expect(calls[0].data).to.deep.equal({
      requestId: 'debug-test-001',
      businessKey: 'demo_business',
      sceneKey: 'login_protection',
      needDetail: true,
      data: { blacklisted: true }
    })
  })

  it('selects the business key belonging to the scene', () => {
    const context = {
      openKey: '',
      sceneOptions: [
        { value: 'login_protection', businessKey: 'demo_business' },
        { value: 'account_security', businessKey: 'account_business' }
      ]
    }
    EnginePlayground.methods.selectScene.call(context, 'account_security')
    expect(context.openKey).to.equal('account_business')
  })

  it('reloads the runtime snapshot after synchronizing engine configuration', async () => {
    const calls = []
    api.request = options => {
      calls.push(options)
      return Promise.resolve({ data: { code: 0 } })
    }

    await syncConfig('rule')

    expect(calls.map(item => item.url)).to.deep.equal([
      'v1/config/sync/rule',
      'v1/engine/config/reload'
    ])
  })
})
