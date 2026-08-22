<template>
  <div>
    <Card shadow style="margin-bottom: 10px;">
      <Form ref="form_id" :model="formData">
        <Row :gutter="10">
          <i-col span="3">
            <FormItem>
              <Select v-model="formData.businessSideKey" placeholder="业务方" clearable @on-change="initScene">
                <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value">
                  {{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="3">
            <FormItem>
              <Select v-model="formData.sceneKey" placeholder="场景" clearable>
                <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem>
              <Input v-model="formData.name" placeholder="特征名称" clearable/>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem>
              <Input v-model="formData.featureId" placeholder="泰坦特征ID" clearable/>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem>
              <Select v-model="formData.state" placeholder="状态" clearable >
                <i-option v-for="item in formDataInit.state" :value="item.value" :key="item.value" >{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row>
          <i-col span="24">
            <Button :loading="queryConfig.btnLoading" type="primary" @click="handleSubmit('点击查询', '查询成功', '查询失败')">查询</Button>
            <Button style="margin-left: 10px;" @click="onClearForm">重置</Button>
            <Button style="margin-left: 10px;" type="primary" @click="onAddFeatureStatistics">新建</Button>
          </i-col>
        </Row>
      </Form>
    </Card>
    <Card shadow>
      <Row>
        <i-col span="24">
          <Tables ref="tables" size="small" v-model="tableConfig.data.records" :columns="tableConfig.columns"
                  :pageTotal="tableConfig.data.total" :pageSize="tableConfig.data.pageSize" :pageCurrent="tableConfig.data.current" :loading="tableConfig.loading"
                  @on-page-change="changePage" @on-page-size-change="pageSizeChange" />
        </i-col>
      </Row>
    </Card>

    <AddOrEditFeatureStatistics v-model="featureStatisticsAddOrEditModalConfig.show" :id="featureStatisticsAddOrEditModalConfig.curId"
                                :parentData="featureStatisticsAddOrEditModalConfig.data" @onAfterCommit="onAddOrEditFeatureStatisticsAfterCommit"/>
    <AddOrEditStrategy v-model="strategyAddOrEditModalConfig.show" :id="strategyAddOrEditModalConfig.curId"
               :parentData="strategyAddOrEditModalConfig.data" @onAfterCommit="onAddOrEditStrategyAfterCommit"/>
  </div>
</template>

<script>
import Common from '_v/strategy-engine/common'
import CommonTools from '@/libs/common-tools'
import ComponentsTool from '@/libs/components-tools.js'
import Message from '@/libs/message.js'
import Tables from '_c/tables-common/tables-v2'
import BusinessSideService from '@/api/strategy-engine/businessside'
import SceneService from '@/api/strategy-engine/scene'
import { getFeatureStatisticsList, enableFeatureStatistics, forbiddenFeatureStatistics } from '@/api/strategy-engine/feature-statistics.js'
import AddOrEditFeatureStatistics from '_v/strategy-engine/feature-statistics/add-edit.vue'
import AddOrEditStrategy from '_v/strategy-engine/strategy/add-edit.vue'

export default {
  name: '',
  components: {
    Tables, AddOrEditFeatureStatistics, AddOrEditStrategy
  },
  data () {
    return {
      tableConfig: {
        loading: false,
        columns: this.getColumns(),
        data: {
          current: 1,
          pageSize: 10,
          total: 1,
          records: []
        }
      },
      curParam: {},
      featureStatisticsAddOrEditModalConfig: {
        curId: '',
        show: false,
        data: {
          row: {},
          sceneKey: '',
          pageType: 'edit'
        }
      },
      strategyAddOrEditModalConfig: {
        curId: '',
        show: false,
        data: {
          type: 1,
          row: {},
          sceneKey: '',
          pageType: 'edit'
        }
      },
      queryConfig: {
        formId: 'form_id',
        btnLoading: false
      },
      detailParamModalConfig: {
        show: false
      },
      formDataInit: {
        businessSide: [],
        scene: [],
        state: Common.getState()
      },
      formData: {
        currentPage: 1,
        pageSize: 5,
        name: '',
        businessSideKey: '',
        sceneKey: '',
        featureId: '',
        state: ''
      }
    }
  },
  created: function () {},
  mounted: function () {
    this.initBusinessSide()
    // 首次打开界面初始化显示数据
    this.handleSubmit(this.queryConfig.formId, '默认加载数据成功', '默认加载数据失败')
  },
  methods: {
    onClearForm () {
      this.clearForm()
    },
    clearForm () {
      this.$refs['form_id'].resetFields()
    },
    onAddFeatureStatistics () {
      this.featureStatisticsAddOrEditModalConfig.show = true
      this.featureStatisticsAddOrEditModalConfig.data.pageType = 'add'
      this.featureStatisticsAddOrEditModalConfig.curId = ''
    },
    onAddOrEditFeatureStatisticsAfterCommit () {
      this.featureStatisticsAddOrEditModalConfig.show = false
      this.handleSubmit(this.queryConfig.formId, '累计特征列表数据已更新', '累计特征列表数据更新失败')
    },
    onAddOrEditStrategyAfterCommit () {
      this.strategyAddOrEditModalConfig.show = false
    },
    forbidden (id) {
      forbiddenFeatureStatistics(id).then(res => {
        if (res.data.code === 200) {
          Message.message('禁用成功', 'success', this)
          this.handleSubmit(this.queryConfig.formId, '累计特征列表数据已更新', '累计特征列表数据更新失败')
        } else {
          Message.message(res.data.data, 'error', this)
        }
      })
    },
    enable (id) {
      enableFeatureStatistics(id).then(res => {
        if (res.data.code === 200) {
          Message.message('启用成功', 'success', this)
          this.handleSubmit(this.queryConfig.formId, '累计特征列表数据已更新', '累计特征列表数据更新失败')
        } else {
          Message.message(res.data.data, 'error', this)
        }
      })
    },
    changePage (value) {
      this.tableConfig.data.current = value
      this.updateTableData()
    },
    pageSizeChange (value) {
      this.tableConfig.data.pageSize = value
      this.updateTableData()
    },
    handleSubmit (name, successMsg, errorMsg) {
      this.query()
      Message.message(successMsg, 'success', this)
    },
    query () {
      this.updateTableData()
    },
    updateTableData () {
      this.queryConfig.btnLoading = true
      this.tableConfig.loading = true
      let reqData = {
        currentPage: this.tableConfig.data.current,
        pageSize: this.tableConfig.data.pageSize,
        businessSideKey: this.formData.businessSideKey,
        sceneKey: this.formData.sceneKey,
        name: this.formData.name,
        featureId: this.formData.featureId,
        state: this.formData.state
      }
      this.getTableData(reqData)
    },
    getTableData (reqData) {
      getFeatureStatisticsList(reqData).then(res => {
        let data = res.data.data
        let records = data.records

        this.tableConfig.data.current = data.current
        this.tableConfig.data.pageSize = data.size
        this.tableConfig.data.total = data.total
        this.tableConfig.data.records = records === null ? [] : records

        this.queryConfig.btnLoading = false
        this.tableConfig.loading = false
      })
    },
    getColumns () {
      return [
        { title: 'ID', key: 'id', minWidth: 70, width: 70, sortable: true, fixed: 'left' },
        { title: '特征名称', key: 'name', minWidth: 100, width: 150, tooltip: true },
        { title: '业务方', key: 'businessSideName', minWidth: 100, width: 100 },
        { title: '场景', key: 'sceneName', minWidth: 100, width: 100 },
        { title: '泰坦ID', key: 'featureId', minWidth: 100, width: 100 },
        { title: '累计写入状态', key: 'writeState', minWidth: 110, width: 110 },
        { title: '写入过滤规则',
          key: 'writeStrategy',
          minWidth: 110,
          tooltip: true,
          render: (h, params) => {
            if (CommonTools.isNull(params.row.writeStrategy)) {
              return h('div', null, '-')
            }
            let text = '[' + params.row.writeStrategy.id + ']' + '[' + params.row.writeStrategy.stateName + ']' + params.row.writeStrategy.expression
            return ComponentsTool.customTooltip(h, params, text)
          }
        },
        { title: '查询过滤规则',
          key: 'queryStrategy',
          minWidth: 110,
          tooltip: true,
          render: (h, params) => {
            if (CommonTools.isNull(params.row.queryStrategy)) {
              return h('div', null, '-')
            }
            let text = '[' + params.row.queryStrategy.id + ']' + '[' + params.row.queryStrategy.stateName + ']' + params.row.queryStrategy.expression
            return ComponentsTool.customTooltip(h, params, text)
          }
        },
        { title: '状态', key: 'state', minWidth: 70, width: 70 },
        { title: '操作人', key: 'operator', minWidth: 100, width: 100, tooltip: true },
        { title: '更新时间', key: 'updateTime', minWidth: 150, width: 150, sortable: true },
        { title: '描述', key: 'description', width: 200, tooltip: true },
        { title: '操作',
          key: 'handle',
          options: [],
          width: 140,
          fixed: 'right',
          button: [
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确定禁用?',
                  confirm: true,
                  transfer: true
                },
                on: {
                  'on-ok': () => {
                    let id = params.row.id
                    this.forbidden(id)
                  }
                }
              }, [
                h('Button', {
                  props: {
                    size: 'small'
                  },
                  style: {
                    marginRight: '1px',
                    display: (params.row.state === '禁用') ? 'none' : 'inline-block'
                  }
                }, '禁用')
              ])
            },
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确定启用?',
                  confirm: true,
                  transfer: true
                },
                on: {
                  'on-ok': () => {
                    let id = params.row.id
                    this.enable(id)
                  }
                }
              }, [
                h('Button', {
                  props: {
                    size: 'small'
                  },
                  style: {
                    marginRight: '1px',
                    display: (params.row.state === '启用') ? 'none' : 'inline-block'
                  }
                }, '启用')
              ])
            },
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.featureStatisticsAddOrEditModalConfig.curId = params.row.id
                    this.featureStatisticsAddOrEditModalConfig.data.sceneKey = params.row.sceneKey
                    this.featureStatisticsAddOrEditModalConfig.data.pageType = 'edit'
                    this.featureStatisticsAddOrEditModalConfig.show = true
                  }
                }
              }, '编辑')
            },
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.strategyAddOrEditModalConfig.curId = params.row.writeStrategyId
                    this.strategyAddOrEditModalConfig.data.type = 50
                    this.strategyAddOrEditModalConfig.data.pageType = 'edit'
                    this.strategyAddOrEditModalConfig.show = true
                  }
                }
              }, '编辑写入过滤规则')
            },
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.strategyAddOrEditModalConfig.curId = params.row.queryStrategyId
                    this.strategyAddOrEditModalConfig.data.type = 50
                    this.strategyAddOrEditModalConfig.data.pageType = 'edit'
                    this.strategyAddOrEditModalConfig.show = true
                  }
                }
              }, '编辑查询过滤规则')
            }
          ]
        }
      ]
    },
    initBusinessSide () {
      BusinessSideService.getSelectData({}).then(res => {
        this.formDataInit.businessSide = res
      })
    },
    initScene (businessSideKey) {
      let reqData = {
        businessSideKey: businessSideKey
      }
      SceneService.getSelectData(reqData).then(res => {
        this.formDataInit.scene = res
      })
    }
  }
}
</script>

<style>

</style>
