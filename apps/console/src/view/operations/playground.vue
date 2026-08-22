<template>
  <div>
    <Row :gutter="16">
      <Col :span="15">
        <Card dis-hover>
          <p slot="title">引擎调试台</p>
          <Tabs v-model="tab">
            <TabPane label="策略计算" name="strategy">
              <Form :label-width="100">
                <FormItem label="场景">
                  <Select v-model="sceneKey" filterable @on-change="selectScene">
                    <Option v-for="item in sceneOptions" :key="item.value" :value="item.value">{{ item.label }}</Option>
                  </Select>
                </FormItem>
                <FormItem label="业务方标识"><Input v-model="openKey" /></FormItem>
                <FormItem label="请求标识"><Input v-model="requestId" placeholder="可选，用于调用链追踪" /></FormItem>
                <FormItem label="返回规则明细"><i-switch v-model="needDetail" /></FormItem>
                <FormItem label="请求 JSON"><Input v-model="requestJson" type="textarea" :rows="9" /></FormItem>
                <FormItem><Button type="primary" :loading="loading" @click="compute">执行策略计算</Button></FormItem>
              </Form>
            </TabPane>
            <TabPane label="风险识别" name="risk">
              <Form :label-width="100">
                <FormItem label="调用方标识"><Input v-model="openKey" /></FormItem>
                <FormItem label="请求 JSON"><Input v-model="requestJson" type="textarea" :rows="9" /></FormItem>
                <FormItem><Button type="primary" :loading="loading" @click="risk">执行风险识别</Button></FormItem>
              </Form>
            </TabPane>
            <TabPane label="处置查询" name="disposer">
              <Form :label-width="100">
                <FormItem label="用户ID"><Input v-model="userId" /></FormItem>
                <FormItem><Button type="primary" :loading="loading" @click="queryStatus">查询处置状态</Button></FormItem>
              </Form>
            </TabPane>
          </Tabs>
        </Card>
      </Col>
      <Col :span="9">
        <Card dis-hover>
          <p slot="title">响应结果</p>
          <pre class="response-view">{{ responseText }}</pre>
        </Card>
      </Col>
    </Row>
    <Card dis-hover class="sync-card">
      <p slot="title">配置同步</p>
      <Button
        v-for="item in syncTypes"
        :key="item.value"
        class="sync-button"
        :loading="syncing === item.value"
        @click="sync(item.value)"
      >{{ item.label }}</Button>
    </Card>
  </div>
</template>

<script>
import {
  computeStrategy,
  identifyRisk,
  queryDisposerStatus,
  syncConfig,
  getSceneOptions
} from '@/api/config-admin'

export default {
  name: 'EnginePlayground',
  data () {
    return {
      tab: 'strategy',
      sceneKey: 'login_protection',
      sceneOptions: [],
      openKey: 'demo_business',
      requestId: 'debug-console-001',
      needDetail: true,
      userId: 'demo-user',
      requestJson: '{\n  "userId": "demo-user",\n  "ip_address": "192.0.2.100",\n  "blacklisted": true,\n  "login_count": 8,\n  "device_risk_score": 90\n}',
      responseText: '请选择操作并执行。',
      loading: false,
      syncing: '',
      syncTypes: [
        { value: 'all', label: '同步全部' }, { value: 'scene', label: '同步场景' },
        { value: 'strategy', label: '同步策略' }, { value: 'rule', label: '同步规则' },
        { value: 'feature', label: '同步特征' }, { value: 'risk', label: '同步风险配置' },
        { value: 'disposer', label: '同步处置配置' }
      ]
    }
  },
  mounted () {
    this.loadScenes()
  },
  methods: {
    async loadScenes () {
      try {
        const response = await getSceneOptions()
        this.sceneOptions = (response.data.data || []).map(item => ({
          value: item.sceneKey,
          label: item.sceneName,
          businessKey: item.businessKey
        }))
        this.selectScene(this.sceneKey)
      } catch (error) {
        this.$Message.error('场景加载失败')
      }
    },
    showResponse (response) {
      this.responseText = JSON.stringify(response.data, null, 2)
    },
    selectScene (sceneKey) {
      const scene = this.sceneOptions.find(item => item.value === sceneKey)
      if (scene && scene.businessKey) this.openKey = scene.businessKey
    },
    async execute (action) {
      this.loading = true
      try {
        const response = await action()
        if (response.data && ![0, 200].includes(response.data.code)) {
          throw new Error(response.data.msg || '引擎返回业务错误')
        }
        this.showResponse(response)
        this.$Message.success('执行成功')
      } catch (error) {
        this.responseText = JSON.stringify((error.response && error.response.data) || { message: error.message }, null, 2)
        this.$Message.error('执行失败')
      } finally {
        this.loading = false
      }
    },
    compute () {
      this.execute(() => computeStrategy({
        requestId: this.requestId || undefined,
        businessKey: this.openKey,
        sceneKey: this.sceneKey,
        needDetail: this.needDetail,
        data: JSON.parse(this.requestJson)
      }))
    },
    risk () {
      this.execute(() => identifyRisk({ data: this.requestJson, openKey: this.openKey }))
    },
    queryStatus () {
      this.execute(() => queryDisposerStatus(this.userId))
    },
    async sync (type) {
      this.syncing = type
      try {
        const response = await syncConfig(type)
        this.showResponse(response)
        this.$Message.success('同步成功')
      } catch (error) {
        this.$Message.error('同步失败')
      } finally {
        this.syncing = ''
      }
    }
  }
}
</script>

<style scoped>
.response-view {
  min-height: 360px;
  max-height: 560px;
  margin: 0;
  padding: 12px;
  overflow: auto;
  color: #d6e4ff;
  background: #17233d;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.sync-card {
  margin-top: 16px;
}

.sync-button {
  margin-right: 10px;
  margin-bottom: 8px;
}
</style>
