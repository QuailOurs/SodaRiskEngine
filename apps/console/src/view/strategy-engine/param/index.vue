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
            <FormItem prop="name">
              <Input v-model="formData.name" placeholder="字段名称" clearable/>
            </FormItem>
          </i-col>
        </Row>
        <Row>
          <i-col span="24">
            <Button :loading="queryConfig.btnLoading" type="primary" @click="handleSubmit('点击查询', '查询成功', '查询失败')">查询</Button>
            <Button style="margin-left: 10px;" @click="onClearForm">重置</Button>
            <Button style="margin-left: 10px;" type="primary" @click="onAddParam">新建</Button>
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
    <AddParam v-model="addParamModalConfig.show" :businessSide="formDataInit.businessSide"
              @onAfterCommit="onAddParamAfterCommit"/>
    <EditParam v-model="editParamModalConfig.show" :parameter="curParam" @onAfterCommit="onEditParamAfterCommit"/>

  </div>
</template>

<script>
import Message from '@/libs/message.js'
import Tables from '_c/tables-common/tables-v2'
import ParamService from '@/api/strategy-engine/param'
import BusinessSideService from '@/api/strategy-engine/businessside'
import SceneService from '@/api/strategy-engine/scene'
import AddParam from '_v/strategy-engine/param/add.vue'
import EditParam from '_v/strategy-engine/param/./edit'

export default {
  name: '',
  components: {
    EditParam, Tables, AddParam
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
      addParamModalConfig: {
        show: false
      },
      editParamModalConfig: {
        show: false
      },
      formDataInit: {
        businessSide: [],
        scene: []
      },
      queryConfig: {
        btnLoading: false
      },
      formData: {
        currentPage: 1,
        pageSize: 5,
        businessSideKey: '',
        name: '',
        sceneKey: ''
      }
    }
  },
  created: function () {
  },
  mounted: function () {
    // 首次打开界面初始化显示数据
    this.getBusinessSide()
    this.getScene()
    this.handleSubmit('初始化', '默认加载数据成功', '默认加载数据失败')
  },
  methods: {
    onAddParam () {
      this.addParamModalConfig.show = true
    },
    onClearForm () {
      this.clearForm()
    },
    clearForm () {
      this.$refs['form'].resetFields()
    },
    onAddParamAfterCommit () {
      this.addParamModalConfig.show = false
      this.handleSubmit('添加字段', '列表数据已更新', '列表数据更新失败')
    },
    onEditParamAfterCommit () {
      this.editParamModalConfig.show = false
      this.handleSubmit('编辑字段', '列表数据已更新', '列表数据更新失败')
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
      this.updateTableData()
    },
    updateTableData () {
      this.queryConfig.btnLoading = true
      this.tableConfig.loading = true
      let reqData = {
        currentPage: this.tableConfig.data.current,
        pageSize: this.tableConfig.data.pageSize,

        name: this.formData.name,
        businessSideKey: this.formData.businessSideKey,
        sceneKey: this.formData.sceneKey
      }
      this.getTableData(reqData)
    },
    getTableData (reqData) {
      ParamService.getParamList(reqData).then(res => {
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
        { title: '字段标识', key: 'paramKey', sortable: true, minWidth: 150 },
        { title: '字段名称', key: 'name', minWidth: 150, fixed: 'left', tooltip: true },
        { title: '类型', key: 'typeName', minWidth: 100, tooltip: true },
        { title: '业务方', key: 'businessSideName', minWidth: 100, tooltip: true },
        { title: '场景', key: 'sceneName', minWidth: 150, tooltip: true },
        { title: '场景Key', key: 'sceneKey', minWidth: 150, tooltip: true },
        { title: '操作人', key: 'operator', minWidth: 100, tooltip: true },
        { title: '更新时间', key: 'updateTime', minWidth: 150, sortable: true, tooltip: true },
        { title: '备注', key: 'description', minWidth: 100, tooltip: true },
        {
          title: '操作',
          key: 'handle',
          options: [],
          width: 130,
          fixed: 'right',
          button: [
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.curParam = params.row
                    this.editParamModalConfig.show = true
                  }
                }
              }, '编辑')
            },
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确认删除?',
                  confirm: true,
                  transfer: true
                },
                on: {
                  'on-ok': () => {
                    let id = params.row.id
                    this.deleteParamById(id)
                  }
                }
              }, [
                h('Button', {
                  props: {
                    size: 'small'
                  },
                  style: {
                    marginRight: '1px'
                  }
                }, '删除')
              ])
            }
          ]
        }
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
    }
  }
}
</script>

<style>

</style>
