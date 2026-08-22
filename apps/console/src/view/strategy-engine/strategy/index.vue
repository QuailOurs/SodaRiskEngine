<template>
  <div>
    <Card shadow style="margin-bottom: 10px;">
      <Form ref="form_id" :model="formData">
        <Row :gutter="10">
          <i-col span="2">
            <FormItem>
              <Input v-model="formData.id" placeholder="ID" clearable/>
            </FormItem>
          </i-col>
          <i-col span="2">
            <FormItem>
              <Input v-model="formData.name" placeholder="规则名称" clearable/>
            </FormItem>
          </i-col>
          <i-col span="2">
            <FormItem>
              <Select v-model="formData.type" placeholder="类型">
                <i-option v-for="item in formDataInit.type" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
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
              <Select v-model="formData.sceneKey" placeholder="场景" clearable @on-change="onChangeScene">
                <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="4">
            <FormItem>
              <Input v-model="formData.returnCode" placeholder="返回码" clearable/>
            </FormItem>
          </i-col>
          <i-col span="4">
            <FormItem>
              <Select v-model="formData.state" placeholder="状态" clearable>
                <i-option v-for="item in formDataInit.state" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="4">
            <FormItem>
              <Input v-model="formData.operator" placeholder="操作人" clearable/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <Button :loading="queryConfig.btnLoading" type="primary"
                    @click="handleSubmit(queryConfig.formId, '查询成功', '查询失败')">查询
            </Button>
            <Button style="margin-left: 10px;" type="primary" @click="exportExcel">导出</Button>
            <Button style="margin-left: 10px;" type="primary" @click="onAddStrategy">新建规则</Button>
          </i-col>
        </Row>
      </Form>
    </Card>
    <Card shadow>
      <Row>
        <i-col span="24">
          <Tables ref="tables" size="small" v-model="tableConfig.data.records" :columns="tableConfig.columns"
                      :pageTotal="tableConfig.data.total" :pageSize="tableConfig.data.pageSize" :pageCurrent="tableConfig.data.current" :loading="tableConfig.loading"
                      @on-page-change="changePage"
                      @on-page-size-change="pageSizeChange"/>
        </i-col>
      </Row>
    </Card>
    <AddOrEdit v-model="strategyAddOrEditModalConfig.show" :id="strategyAddOrEditModalConfig.curId"
               :parentData="strategyAddOrEditModalConfig.data" @onAfterCommit="onAddOrEditAfterCommit"/>
  </div>
</template>

<script>
import Common from '../common'
import Message from '@/libs/message'
import Tables from '_c/tables-common/tables-v2'
import AddOrEdit from '_v/strategy-engine/strategy/add-edit.vue'
import StrategyService from '@/api/strategy-engine/strategy'
import BusinessSideService from '@/api/strategy-engine/businessside'
import SceneService from '@/api/strategy-engine/scene'

export default {
  name: '',
  components: {
    Tables, AddOrEdit
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
      formDataInit: {
        type: Common.getStrategyType(1),
        state: Common.getStrategyState(),
        businessSide: [],
        scene: []
      },
      formData: {
        id: '',
        name: '',
        type: '',
        state: '',
        operator: '',
        businessSideKey: '',
        sceneKey: ''
      }
    }
  },
  created: function () {
    // 初始化过滤项
    this.initBusinessSide()
  },
  mounted: function () {
    // 首次打开界面初始化显示数据
    this.handleSubmit(this.queryConfig.formId, '加载数据成功', '加载数据失败')
  },
  methods: {
    onChangeScene (sceneKey) {
      this.strategyAddOrEditModalConfig.data.sceneKey = sceneKey
    },
    onAddStrategy () {
      this.strategyAddOrEditModalConfig.show = true
      this.strategyAddOrEditModalConfig.data.pageType = 'add'
      this.strategyAddOrEditModalConfig.curId = ''
    },
    onAddOrEditAfterCommit (newId) {
      this.strategyAddOrEditModalConfig.show = false
      this.handleSubmit(this.queryConfig.formId, '列表数据已更新', '列表数据更新失败')
    },
    updateState (id, state) {
      let reqData = {
        id: id,
        state: state
      }
      StrategyService.updateState(reqData).then(res => {
        this.handleSubmit(this.queryConfig.formId, '规则状态更新成功', '规则状态更新失败')
      })
    },
    exportExcel () {
      this.$refs.tables.exportCsv({
        filename: `table-case-${(new Date()).valueOf()}.csv`,
        columns: this.getExportColumns(),
        data: this.getExportData()
      })
      this.updateTableData()
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

        id: this.formData.id,
        name: this.formData.name,
        type: this.formData.type,
        state: this.formData.state,
        returnCode: this.formData.returnCode,
        operator: this.formData.operator,
        businessSideKey: this.formData.businessSideKey,
        sceneKey: this.formData.sceneKey
      }

      this.getTableData(reqData)
    },
    getTableData (reqData) {
      StrategyService.getList(reqData).then(res => {
        let data = res.data.data
        let records = Array.isArray(data) ? data : data.records

        if (Array.isArray(data)) {
          this.tableConfig.data.total = records.length
        } else {
          this.tableConfig.data.current = data.current || 1
          this.tableConfig.data.pageSize = data.size || this.tableConfig.data.pageSize
          this.tableConfig.data.total = data.total || 0
        }
        this.tableConfig.data.records = records === null ? [] : records

        this.queryConfig.btnLoading = false
        this.tableConfig.loading = false
      })
    },
    getColumns () {
      return [
        { title: 'ID', key: 'id', minWidth: 70, width: 70, sortable: true, fixed: 'left' },
        { title: '规则名称', key: 'name', width: 180, tooltip: true, fixed: 'left' },
        { title: '场景', key: 'sceneName', minWidth: 80, width: 100, tooltip: true },
        { title: '业务方', key: 'businessSideKey', minWidth: 80, width: 100, tooltip: true },
        { title: '类型', key: 'typeName', minWidth: 80, width: 100, tooltip: true },
        { title: '表达式', key: 'expressionView', minWidth: 100, resizable: true, tooltip: true },
        { title: '分值', key: 'score', minWidth: 80, width: 80, tooltip: true },
        { title: '返回码', key: 'returnCode', minWidth: 80, width: 80, tooltip: true },
        { title: '优先级', key: 'priority', minWidth: 80, width: 80, tooltip: true },
        { title: '命中阀值', key: 'threshold', minWidth: 100, width: 100, tooltip: true },
        { title: '状态', key: 'stateName', width: 80 },
        { title: '操作人', key: 'operator', width: 120, tooltip: true },
        { title: '更新时间', key: 'updateTime', width: 180, sortable: true },
        { title: '创建时间', key: 'createTime', width: 180, sortable: true },
        { title: '能力来源', key: 'abilitySource', width: 100, tooltip: true },
        { title: '备注', key: 'description', width: 180, tooltip: true },
        {
          title: '操作',
          key: 'handle',
          options: [],
          width: 140,
          fixed: 'right',
          button: [
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确认下线?',
                  confirm: true,
                  transfer: true
                },
                on: {
                  'on-ok': () => {
                    let id = params.row.id
                    let state = '0'
                    this.updateState(id, state)
                  }
                }
              }, [
                h('Button', {
                  props: {
                    size: 'small',
                    disabled: (params.row.stateName === '下线')
                  },
                  style: {
                    marginRight: '1px',
                    display: (params.row.stateName === '下线') ? 'none' : 'inline-block'
                  }
                }, '下线')
              ])
            },
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确认预上线?',
                  confirm: true,
                  transfer: true
                },
                on: {
                  'on-ok': () => {
                    let id = params.row.id
                    let state = '1'
                    this.updateState(id, state)
                  }
                }
              }, [
                h('Button', {
                  props: {
                    size: 'small',
                    disabled: (params.row.stateName === '预上线') || (params.row.typeName === '累计过滤')
                  },
                  style: {
                    marginRight: '1px',
                    display: (params.row.stateName === '预上线') || (params.row.typeName === '累计过滤') ? 'none' : 'inline-block'
                  }
                }, '预上线')
              ])
            },
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确认上线?',
                  confirm: true,
                  transfer: true
                },
                on: {
                  'on-ok': () => {
                    let id = params.row.id
                    let state = '2'
                    this.updateState(id, state)
                  }
                }
              }, [
                h('Button', {
                  props: {
                    size: 'small',
                    disabled: (params.row.stateName === '上线' || (params.row.stateName === '下线' && params.row.typeName !== '累计过滤'))
                  },
                  style: {
                    marginRight: '1px',
                    display: (params.row.stateName === '上线' || (params.row.stateName === '下线' && params.row.typeName !== '累计过滤')) ? 'none' : 'inline-block'
                  }
                }, '上线')
              ])
            },
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small',
                  disabled: false
                },
                style: {
                  marginRight: '1px'
                },
                on: {
                  click: () => {
                    this.strategyAddOrEditModalConfig.curId = params.row.id
                    this.strategyAddOrEditModalConfig.data.type = params.row.type
                    this.strategyAddOrEditModalConfig.data.pageType = 'edit'
                    this.strategyAddOrEditModalConfig.show = true
                  }
                }
              }, '修改')
            }
          ]
        }
      ]
    },
    getExportColumns () {
      return [
        { title: 'ID', key: 'id' },
        { title: '规则名称', key: 'name' },
        { title: '场景', key: 'sceneName' },
        { title: '业务方', key: 'businessSideKey' },
        { title: '类型', key: 'typeName' },
        { title: '表达式', key: 'expressionView' },
        { title: '分值', key: 'score' },
        { title: '返回码', key: 'returnCode' },
        { title: '优先级', key: 'priority' },
        { title: '命中阀值', key: 'threshold' },
        { title: '状态', key: 'stateName' },
        { title: '操作人', key: 'operator' },
        { title: '更新时间', key: 'updateTime' },
        { title: '创建时间', key: 'createTime' },
        { title: '能力来源', key: 'abilitySource' },
        { title: '备注', key: 'description' }
      ]
    },
    getExportData () {
      // 包含换行的列，特殊处理
      this.tableConfig.data.records.forEach(item => {
        // let description = item.description
        // if (description !== null) {
        //   description.replace('"', '""')
        //   item.description = '"' + description + '"'
        // }
        // let result = item.result
        // if (result !== null) {
        //   result.replace('"', '""')
        //   item.result = '"' + result + '"'
        // }
      })
      return this.tableConfig.data.records
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
