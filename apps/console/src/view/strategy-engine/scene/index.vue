<template>
  <div>
    <Card shadow style="margin-bottom: 10px;">
      <Form ref="form" :model="formData" >
        <Row :gutter="10">
          <i-col span="6">
            <FormItem prop="businessSideKey">
              <Select v-model="formData.businessSideKey" placeholder="业务方">
                <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value" >{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="name">
              <Input v-model="formData.name" placeholder="场景名称" clearable/>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="sceneKey">
              <Input v-model="formData.sceneKey" placeholder="场景标识" clearable/>
            </FormItem>
          </i-col>
        </Row>
        <Row>
          <i-col span="24">
            <Button :loading="queryConfig.btnLoading" type="primary" @click="handleSubmit('点击查询', '查询成功', '查询失败')">查询</Button>
            <Button style="margin-left: 10px;" @click="onClearForm">重置</Button>
            <Button style="margin-left: 10px;" type="primary" @click="onAddScene">新建</Button>
            <Button v-if="businessSideMgr" style="margin-left: 10px;" type="primary" @click="onBusinessSideIndex">业务方管理</Button>
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
    <AddScene v-model="addSceneModalConfig.show" :businessSide="formDataInit.businessSide"  @onAfterCommit="onAddSceneAfterCommit"/>
    <EditScene v-model="editSceneModalConfig.show" :businessSide="formDataInit.businessSide" :scene="curScene"  @onAfterCommit="onEditSceneAfterCommit"/>
    <BusinessSideIndex v-model="businessSideIndexModalConfig.show"/>
  </div>
</template>

<script>
import CommonTools from '@/libs/common-tools'
import Message from '@/libs/message.js'
import Tables from '_c/tables-common/tables-v2'
import SceneService from '@/api/strategy-engine/scene'
import BusinessSideService from '@/api/strategy-engine/businessside'
import AddScene from '_v/strategy-engine/scene/add.vue'
import EditScene from '_v/strategy-engine/scene/edit.vue'
import BusinessSideIndex from '_v/strategy-engine/business-side/modal.vue'
export default {
  name: '',
  components: {
    Tables, AddScene, EditScene, BusinessSideIndex
  },
  props: {
    systemKey: { // 外部系统引用此页面时,指定系统标识, 控制显示业务方范围
      type: String,
      default: ''
    },
    businessSideKey: { // 外部系统引用此页面时,指定业务方标识, 控制显示业务方范围
      type: String,
      default: ''
    },
    businessSideMgr: { // 外部系统引用此页面时,是否显示业务方管理功能
      type: Boolean,
      default: true
    }
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
      curScene: {},
      addSceneModalConfig: {
        show: false
      },
      editSceneModalConfig: {
        show: false
      },
      businessSideIndexModalConfig: {
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
        businessSideKey: '',
        name: '',
        sceneKey: ''
      }
    }
  },
  created: function () {},
  mounted: function () {
    // 首次打开界面初始化显示数据
    this.getBusinessSide()
    this.handleSubmit('初始化', '默认加载数据成功', '默认加载数据失败')
  },
  methods: {
    onClearForm () {
      this.clearForm()
      this.initExternal()
    },
    clearForm () {
      this.$refs['form'].resetFields()
    },
    initExternal () {
      console.log('initExternal')
      if (CommonTools.nonNull(this.businessSideKey)) { // 外部系统引用,初始化业务方参数
        this.formData.businessSideKey = this.businessSideKey
      }
    },
    onAddScene () {
      this.addSceneModalConfig.show = true
    },
    onBusinessSideIndex () {
      this.businessSideIndexModalConfig.show = true
    },
    onAddSceneAfterCommit () {
      this.addSceneModalConfig.show = false
      this.handleSubmit('添加场景', '场景列表数据已更新', '场景列表数据更新失败')
    },
    onEditSceneAfterCommit () {
      this.editSceneModalConfig.show = false
      this.handleSubmit('更新场景', '场景列表数据已更新', '场景列表数据更新失败')
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
      try {
        this.updateTableData()
      } catch (e) {
        console.log(e)
        Message.message('查询失败', 'error', this)
      } finally {
        this.queryConfig.btnLoading = false
        this.tableConfig.loading = false
      }
    },
    updateTableData () {
      this.queryConfig.btnLoading = true
      this.tableConfig.loading = true
      let reqData = {
        currentPage: this.tableConfig.data.current,
        pageSize: this.tableConfig.data.pageSize,
        businessSideKey: this.formData.businessSideKey,
        name: this.formData.name,
        sceneKey: this.formData.sceneKey
      }
      this.getTableData(reqData)
    },
    getTableData (reqData) {
      SceneService.getSceneList(reqData).then(res => {
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
        { title: '场景标识', key: 'sceneKey', minWidth: 100, sortable: true, tooltip: true },
        { title: '场景名称', key: 'name', minWidth: 100, tooltip: true },
        { title: '所属业务方', key: 'businessSideName', minWidth: 100, tooltip: true },
        { title: '操作人', key: 'operator', minWidth: 100, tooltip: true },
        { title: '更新时间', key: 'updateTime', minWidth: 150, tooltip: true, sortable: true },
        { title: '备注', key: 'description', minWidth: 150, tooltip: true },
        { title: '操作',
          key: 'handle',
          options: [],
          width: 70,
          fixed: 'right',
          button: [
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.curScene = params.row
                    this.editSceneModalConfig.show = true
                  }
                }
              }, '编辑')
            }
          ]
        }
      ]
    },
    getBusinessSide () {
      let reqData = {}
      if (CommonTools.nonNull(this.systemKey)) { // 外部系统引用,默认首次不加载
        reqData = {
          systemKey: this.systemKey
        }
      }

      this.initExternal()

      BusinessSideService.getSelectData(reqData).then(res => {
        this.formDataInit.businessSide = res
      })
    }
  }
}
</script>

<style>

</style>
