<template>
  <div>
    <Card shadow style="margin-bottom: 10px;">
      <Form ref="form" :model="formData">
        <Row :gutter="10">
          <i-col span="6">
            <FormItem prop="businessSideKey">
              <Select v-model="formData.businessSideKey" placeholder="业务方" clearable @on-change="updateSceneList()">
                <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value">
                  {{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="sceneKey">
              <Select v-model="formData.sceneKey" placeholder="场景" clearable>
                <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="sceneKey">
              <Input v-model="formData.sceneKey" placeholder="场景Key" clearable/>
            </FormItem>
          </i-col>
            <i-col span="6">
            <FormItem prop="toolName">
              <Input v-model="formData.toolName" placeholder="工具名称" clearable/>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="paramkey">
              <Input v-model="formData.paramKey" placeholder="字段key" clearable/>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="complementKey">
              <Input v-model="formData.complementKey" placeholder="补全参数key" clearable/>
            </FormItem>
          </i-col>
           <i-col span="6">
            <FormItem prop="state">
              <Select v-model="formData.state" placeholder="状态" clearable >
                <i-option v-for="item in formDataInit.state" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row>
          <i-col span="24">
            <Button :loading="queryConfig.btnLoading" type="primary" @click="handleSubmit('点击查询', '查询成功', '查询失败')">查询</Button>
            <Button style="margin-left: 10px;" @click="onClearForm">重置</Button>
            <Button style="margin-left: 10px;" type="primary" @click="onAddParam">新建字段映射补全信息</Button>
            <Button style="margin-left: 10px;" type="primary" @click="onAddToolComplement">新增工具补全信息</Button>
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
    <AddParam v-model="addComplementModalConfig.show" :businessSide="formDataInit.businessSide" :tool="formDataInit.tool"
              @onAfterCommit="onAddParamAfterCommit"/>
    <AddToolComplement v-model="addToolComplementModalConfig.show" :tool="formDataInit.tool"
              @onAfterCommit="onAddToolComplementAfterCommit"/>
    <EditParam v-model="editComplementModalConfig.show" :complement="curComplement" @onAfterCommit="onEditParamAfterCommit"/>

  </div>
</template>

<script>
import Message from '@/libs/message.js'
import Tables from '_c/tables-common/tables-v2'
import ComplementService from '@/api/strategy-engine/complement'
import BusinessSideService from '@/api/strategy-engine/businessside'
import SceneService from '@/api/strategy-engine/scene'
import ToolService from '@/api/strategy-engine/tool'
import AddParam from '_v/strategy-engine/complement/add.vue'
import EditParam from '_v/strategy-engine/complement/edit.vue'
import AddToolComplement from '_v/strategy-engine/complement/addToolComplement.vue'

export default {
  name: '',
  components: {
    EditParam, Tables, AddParam, AddToolComplement
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
      curComplement: {},
      addComplementModalConfig: {
        show: false
      },
      addToolComplementModalConfig: {
        show: false
      },
      editComplementModalConfig: {
        show: false
      },
      formDataInit: {
        businessSide: [],
        scene: [],
        tool: []
      },
      queryConfig: {
        btnLoading: false
      },
      formData: {
        currentPage: 1,
        pageSize: 5,
        businessSideKey: '',
        sceneKey: '',
        sceneName: '',
        toolName: '',
        paramkey: '',
        complementKey: '',
        state: '',
        id: ''
      }
    }
  },
  created: function () {
  },
  mounted: function () {
    // 首次打开界面初始化显示数据
    this.getBusinessSide()
    this.getScene()
    this.getState()
    this.getComplementTool()
    this.handleSubmit('初始化', '默认加载数据成功', '默认加载数据失败')
  },
  methods: {
    onAddParam () {
      this.addComplementModalConfig.show = true
    },
    onAddToolComplement () {
      this.addToolComplementModalConfig.show = true
    },
    onClearForm () {
      this.clearForm()
    },
    clearForm () {
      this.$refs['form'].resetFields()
    },
    onAddParamAfterCommit () {
      this.addComplementModalConfig.show = false
      this.handleSubmit('添加字段补全信息', '列表数据已更新', '列表数据更新失败')
    },
    onEditParamAfterCommit () {
      this.editComplementModalConfig.show = false
      this.handleSubmit('编辑字段补全信息', '列表数据已更新', '列表数据更新失败')
    },
    onAddToolComplementAfterCommit () {
      this.addToolComplementModalConfig.show = false
      this.handleSubmit('添加工具字段', '列表数据已更新', '列表数据更新失败')
    },
    deleteParamById (id) {
      ParamService.deleteParam(id).then(res => {
        if (res.data.code === 200) {
          Message.message('删除成功', 'success', this)
          this.handleSubmit('删除字段' + id, '列表数据已更新', '列表数据更新失败')
        } else {
          Message.message('删除失败', 'error', this)
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
      this.queryConfig.btnLoading = true
      this.tableConfig.loading = true
      this.updateTableData()
    },
    updateTableData () {
      let reqData = {
        currentPage: this.tableConfig.data.current,
        pageSize: this.tableConfig.data.pageSize,
        businessSideKey: this.formData.businessSideKey,
        sceneKey: this.formData.sceneKey,
        toolName: this.formData.toolName,
        paramKey: this.formData.paramKey,
        complementKey: this.formData.complementKey,
        state: this.formData.state
      }
      this.getTableData(reqData)
    },
    getTableData (reqData) {
      ComplementService.getComplementList(reqData).then(res => {
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
        { title: '业务方', key: 'businessSideName', minWidth: 110, width: 110, tooltip: true },
        { title: '场景名称', key: 'sceneName', minWidth: 150, tooltip: true },
        { title: '场景key', key: 'sceneKey', minWidth: 150, tooltip: true },
        { title: '字段key', key: 'paramKey', sortable: true, minWidth: 150 },
        { title: '补全参数key', key: 'complementKey', minWidth: 150, tooltip: true },
        { title: '状态', key: 'state', minWidth: 100, tooltip: true },
        { title: '操作人', key: 'operator', minWidth: 100, tooltip: true },
        { title: '更新时间', key: 'updateTime', minWidth: 150, sortable: true, tooltip: true },
        {
          title: '操作',
          key: 'handle',
          options: [],
          width: 130,
          fixed: 'right',
          button: [
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确认禁用?',
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
                    display: (params.row.state === '禁用') ? 'none' : 'inline-block',
                    color: (params.row.state === '启用') ? 'red' : 'green'
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
                    display: (params.row.state === '启用') ? 'none' : 'inline-block',
                    color: (params.row.state === '禁用') ? 'green' : 'red'
                  }
                }, '启用')
              ])
            }
          ]
        }
      ]
    },
    getState () {
      this.formDataInit.state = [
        { value: '禁用', label: '禁用' },
        { value: '启用', label: '启用' }
      ]
    },
    getBusinessSide () {
      BusinessSideService.getList({}).then(res => {
        let respData = res.data.data
        this.formDataInit.businessSide = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.name
          temp.value = item.businessSideKey
          this.formDataInit.businessSide.push(temp)
        })
      })
    },
    getScene () {
      SceneService.getSceneList({}).then(res => {
        let respData = res.data.data
        this.formDataInit.scene = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.name
          temp.value = item.sceneKey
          this.formDataInit.scene.push(temp)
        })
      })
    },
    getSceneByBusinessSideKey (businessSideKey) {
      let reqData = {
        businessSideKey: businessSideKey
      }
      SceneService.getSceneList(reqData).then(res => {
        let respData = res.data.data
        this.formDataInit.scene = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.name
          temp.value = item.sceneKey
          this.formDataInit.scene.push(temp)
        })
      })
    },
    updateSceneList () {
      if (this.formData.businessSideKey !== undefined && this.formData.businessSideKey !== '') {
        this.getSceneByBusinessSideKey(this.formData.businessSideKey)
      } else {
        this.getScene()
      }
    },
    getComplementTool () {
      let reqData = {
        type: 20
      }
      ToolService.getToolList(reqData).then(res => {
        let respData = res.data.data
        this.formDataInit.tool = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.name + '(' + item.description + ')'
          temp.value = item.id
          this.formDataInit.tool.push(temp)
        })
      })
    },
    forbidden (id) {
      ComplementService.forbiddenComplement(id).then(res => {
        if (res.data.code === 200) {
          Message.message('禁用成功', 'success', this)
          this.handleSubmit('禁用参数补全' + id, '参数补全列表数据已更新', '参数补全列表数据更新失败')
        } else {
          Message.message(res.data.data, 'error', this)
        }
      })
    },
    enable (id) {
      ComplementService.enableComplement(id).then(res => {
        if (res.data.code === 200) {
          Message.message('启用成功', 'success', this)
          this.handleSubmit('启用参数补全' + id, '参数补全列表数据已更新', '参数补全列表数据更新失败')
        } else {
          Message.message(res.data.data, 'error', this)
        }
      })
    }
  }
}
</script>

<style>

</style>
