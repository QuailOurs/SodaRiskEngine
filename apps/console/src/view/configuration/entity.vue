<template>
  <div>
    <Spin v-if="!ready" fix />
    <config-crud
      v-else
      :key="$route.name"
      :title="current.title"
      :entity-name="current.entityName"
      :api="current.api"
      :fields="current.fields"
      :columns="current.columns"
      :before-save="current.beforeSave || identity"
    />
  </div>
</template>

<script>
import ConfigCrud from '@/components/config-crud/config-crud.vue'
import {
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
  getDisposerOptions
} from '@/api/config-admin'

const stateOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]
const strategyStateOptions = [...stateOptions, { value: 2, label: '预上线' }]

export default {
  name: 'EntityConfigPage',
  components: { ConfigCrud },
  data () {
    return {
      ready: false,
      sceneOptions: [],
      businessOptions: [],
      ruleOptions: [],
      disposerOptions: []
    }
  },
  computed: {
    configs () {
      return {
        'business-side': {
          title: '业务方配置',
          entityName: '业务方',
          api: businessSideApi,
          fields: [
            { key: 'name', label: '业务方名称', required: true },
            { key: 'businessSideKey', label: '业务方标识', required: true, immutable: true },
            { key: 'systemKey', label: '系统标识', required: true },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 },
            { title: '业务方名称', key: 'name', minWidth: 140 },
            { title: '业务方标识', key: 'businessSideKey', minWidth: 160 },
            { title: '系统标识', key: 'systemKey', minWidth: 150 },
            { title: '状态', key: 'state', type: 'state', width: 90 },
            { title: '描述', key: 'description', minWidth: 180 }
          ]
        },
        scene: {
          title: '场景配置',
          entityName: '场景',
          api: sceneApi,
          fields: [
            { key: 'name', label: '场景名称', required: true },
            { key: 'sceneKey', label: '场景标识', required: true, immutable: true },
            { key: 'businessSideKey', label: '业务方', type: 'select', options: this.businessOptions, required: true },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'pmAccount', label: '产品联系人' },
            { key: 'rdAccount', label: '研发联系人' },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 },
            { title: '场景名称', key: 'name', minWidth: 140 },
            { title: '场景标识', key: 'sceneKey', minWidth: 180 },
            { title: '业务方', key: 'businessSideKey', minWidth: 150 },
            { title: '状态', key: 'state', type: 'state', width: 90 },
            { title: '描述', key: 'description', minWidth: 180 }
          ]
        },
        rule: {
          title: '规则配置',
          entityName: '规则',
          api: ruleApi,
          fields: [
            { key: 'name', label: '规则名称', required: true },
            { key: 'ruleKey', label: '规则标识', required: true, immutable: true },
            { key: 'sceneKey', label: '所属场景', type: 'select', options: this.sceneOptions, required: true },
            { key: 'ruleType',
              label: '规则类型',
              type: 'select',
              required: true,
              default: 'EXPRESSION',
              options: [
                { value: 'EXPRESSION', label: '表达式' },
                { value: 'THRESHOLD', label: '阈值' },
                { value: 'LIST', label: '名单' }
              ] },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'expression', label: 'Aviator 表达式', type: 'textarea', required: true, span: 24, rows: 4 },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 },
            { title: '规则名称', key: 'name', minWidth: 150 },
            { title: '规则标识', key: 'ruleKey', minWidth: 160 },
            { title: '场景', key: 'sceneKey', minWidth: 160 },
            { title: '类型', key: 'ruleType', minWidth: 110 },
            { title: '表达式', key: 'expression', minWidth: 220 },
            { title: '状态', key: 'state', type: 'state', width: 90 }
          ]
        },
        strategy: {
          title: '策略配置',
          entityName: '策略',
          api: strategyApi,
          fields: [
            { key: 'name', label: '策略名称', required: true },
            { key: 'strategyKey', label: '策略标识', required: true, immutable: true },
            { key: 'sceneKey', label: '所属场景', type: 'select', options: this.sceneOptions, required: true },
            { key: 'ruleIds', label: '关联规则', type: 'select', multiple: true, options: this.ruleOptions, required: true, span: 24 },
            { key: 'type',
              label: '策略类型',
              type: 'select',
              default: 1,
              options: [
                { value: 0, label: '默认' }, { value: 1, label: '实时' }, { value: 2, label: '准实时' }
              ] },
            { key: 'expressionRelation',
              label: '规则关系',
              type: 'select',
              default: '||',
              options: [
                { value: '||', label: '任一命中' }, { value: '&&', label: '全部命中' }
              ] },
            { key: 'priority', label: '优先级', type: 'number', min: 1, max: 100, default: 50 },
            { key: 'threshold', label: '命中阈值', type: 'number', min: 1, default: 1 },
            { key: 'score', label: '策略分值', type: 'number', min: 0, default: 0 },
            { key: 'returnCode', label: '返回码' },
            { key: 'abilitySource', label: '能力来源', default: 'RULE_ENGINE' },
            { key: 'state', label: '状态', type: 'select', options: strategyStateOptions, default: 1 },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 },
            { title: '策略名称', key: 'name', minWidth: 160 },
            { title: '策略标识', key: 'strategyKey', minWidth: 170 },
            { title: '场景', key: 'sceneKey', minWidth: 160 },
            { title: '规则数', key: 'ruleIds', minWidth: 90, render: (h, p) => h('span', (p.row.ruleIds || []).length) },
            { title: '优先级', key: 'priority', width: 90 },
            { title: '返回码', key: 'returnCode', width: 100 },
            { title: '状态', key: 'state', type: 'state', width: 90 }
          ],
          beforeSave: data => ({ ...data, strategyType: data.type })
        },
        feature: {
          title: '特征配置',
          entityName: '特征',
          api: featureApi,
          fields: [
            { key: 'name', label: '特征名称', required: true },
            { key: 'featureKey', label: '特征标识', required: true, immutable: true },
            { key: 'sceneKey', label: '所属场景', type: 'select', options: this.sceneOptions, required: true },
            { key: 'featureType',
              label: '特征类型',
              type: 'select',
              required: true,
              default: 'base',
              options: [
                { value: 'base', label: '基础特征' }, { value: 'calculation', label: '累计特征' },
                { value: 'algorithm', label: '算法特征' }, { value: 'list', label: '名单特征' }
              ] },
            { key: 'dataType',
              label: '数据类型',
              type: 'select',
              required: true,
              default: 'STRING',
              options: [
                { value: 'STRING', label: '字符串' }, { value: 'INT', label: '整数' },
                { value: 'DOUBLE', label: '小数' }, { value: 'BOOLEAN', label: '布尔值' }
              ] },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 }, { title: '特征名称', key: 'name', minWidth: 150 },
            { title: '特征标识', key: 'featureKey', minWidth: 160 }, { title: '场景', key: 'sceneKey', minWidth: 160 },
            { title: '类型', key: 'featureType', width: 110 }, { title: '数据类型', key: 'dataType', width: 110 },
            { title: '状态', key: 'state', type: 'state', width: 90 }
          ]
        },
        'risk-config': {
          title: '风险决策配置',
          entityName: '风险配置',
          api: riskConfigApi,
          fields: [
            { key: 'name', label: '配置名称', required: true }, { key: 'riskKey', label: '风险标识', required: true, immutable: true },
            { key: 'businessType', label: '业务类型', required: true },
            { key: 'riskLevel',
              label: '风险等级',
              type: 'select',
              default: 1,
              options: [
                { value: 1, label: '低风险' }, { value: 2, label: '中风险' }, { value: 3, label: '高风险' }
              ] },
            { key: 'scoreThreshold', label: '分数阈值', type: 'number', min: 0, max: 100, default: 80 },
            { key: 'disposition', label: '处置方式', type: 'select', options: this.disposerOptions },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 }, { title: '配置名称', key: 'name', minWidth: 160 },
            { title: '风险标识', key: 'riskKey', minWidth: 150 }, { title: '业务类型', key: 'businessType', width: 110 },
            { title: '等级', key: 'riskLevel', width: 80 }, { title: '阈值', key: 'scoreThreshold', width: 80 },
            { title: '处置方式', key: 'disposition', minWidth: 140 }, { title: '状态', key: 'state', type: 'state', width: 90 }
          ]
        },
        'black-white-list': {
          title: '黑白名单',
          entityName: '名单记录',
          api: blackWhiteListApi,
          fields: [
            { key: 'listType',
              label: '名单类型',
              type: 'select',
              required: true,
              default: 'BLACK',
              options: [
                { value: 'BLACK', label: '黑名单' }, { value: 'WHITE', label: '白名单' }
              ] },
            { key: 'listKey', label: '名单维度', required: true }, { key: 'listValue', label: '名单值', required: true },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 }, { title: '类型', key: 'listType', width: 100 },
            { title: '维度', key: 'listKey', minWidth: 120 }, { title: '值', key: 'listValue', minWidth: 220 },
            { title: '状态', key: 'state', type: 'state', width: 90 }, { title: '描述', key: 'description', minWidth: 180 }
          ]
        },
        'return-code': {
          title: '返回码配置',
          entityName: '返回码',
          api: returnCodeApi,
          fields: [
            { key: 'returnCode', label: '返回码', required: true }, { key: 'name', label: '名称', required: true },
            { key: 'sceneKey', label: '所属场景', type: 'select', options: this.sceneOptions, required: true },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 }, { title: '返回码', key: 'returnCode', minWidth: 120 },
            { title: '名称', key: 'name', minWidth: 120 }, { title: '场景', key: 'sceneKey', minWidth: 170 },
            { title: '状态', key: 'state', type: 'state', width: 90 }, { title: '描述', key: 'description', minWidth: 200 }
          ]
        },
        disposer: {
          title: '处置方式配置',
          entityName: '处置方式',
          api: disposerConfigApi,
          fields: [
            { key: 'name', label: '处置名称', required: true }, { key: 'disposerKey', label: '处置标识', required: true, immutable: true },
            { key: 'disposerType',
              label: '处置类型',
              type: 'select',
              required: true,
              options: [
                { value: 'LOCK', label: '锁定' }, { value: 'BAN', label: '封禁' },
                { value: 'ALERT', label: '告警' }, { value: 'VERIFY', label: '二次验证' }
              ] },
            { key: 'state', label: '状态', type: 'select', options: stateOptions, default: 1 },
            { key: 'operator', label: '操作人', default: 'admin' },
            { key: 'description', label: '描述', type: 'textarea', span: 24 }
          ],
          columns: [
            { title: 'ID', key: 'id', width: 70 }, { title: '处置名称', key: 'name', minWidth: 150 },
            { title: '处置标识', key: 'disposerKey', minWidth: 160 }, { title: '处置类型', key: 'disposerType', width: 110 },
            { title: '状态', key: 'state', type: 'state', width: 90 }, { title: '描述', key: 'description', minWidth: 200 }
          ]
        }
      }
    },
    current () {
      return this.configs[this.$route.name] || this.configs.scene
    }
  },
  watch: {
    '$route.name' () {
      this.loadOptions()
    }
  },
  mounted () {
    this.loadOptions()
  },
  methods: {
    identity (data) { return data },
    unwrap (response) {
      return response && response.data && response.data.data ? response.data.data : []
    },
    async loadOptions () {
      this.ready = false
      try {
        const [scenes, businesses, rules, disposers] = await Promise.all([
          getSceneOptions(), getBusinessSideOptions(), getRuleOptions({}), getDisposerOptions()
        ])
        this.sceneOptions = this.unwrap(scenes).map(item => ({ value: item.sceneKey, label: item.sceneName }))
        this.businessOptions = this.unwrap(businesses).map(item => ({ value: item.businessSideKey, label: item.name }))
        this.ruleOptions = this.unwrap(rules).map(item => ({ value: item.id, label: `${item.name}（${item.ruleKey}）` }))
        this.disposerOptions = this.unwrap(disposers).map(item => ({ value: item.disposerKey, label: item.name }))
      } catch (error) {
        this.$Message.error('选项数据加载失败')
      } finally {
        this.ready = true
      }
    }
  }
}
</script>
