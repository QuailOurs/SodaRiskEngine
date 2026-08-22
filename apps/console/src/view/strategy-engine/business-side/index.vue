<template>
  <div>
    <Card shadow style="margin-bottom: 10px;">
      <Form ref="form" :model="formData" >
        <Row :gutter="10">
          <i-col span="6">
          <FormItem prop="name">
            <Input v-model="formData.name" placeholder="名称" clearable/>
          </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="systemKey">
              <Select v-model="formData.systemKey" placeholder="接入系统" clearable>
                <i-option v-for="item in formDataInit.system" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <Button :loading="queryConfig.btnLoading" type="primary" @click="handleSubmit('点击查询', '查询成功', '查询失败')">查询</Button>
            <Button style="margin-left: 10px;" @click="onClearForm">重置</Button>
            <Button style="margin-left: 10px;" type="primary" @click="onAdd">新建</Button>
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
    <AddOrEdit v-model="addOrEditModalConfig.show" :parentData="addOrEditModalConfig.data" :id="addOrEditModalConfig.curId"
               @onAfterCommit="onAddOrEditAfterCommit"/>
  </div>
</template>

<script>
import Message from '@/libs/message.js'
import Tables from '_c/tables-common/tables-v2'
import BusinessSideService from '@/api/strategy-engine/businessside'
import AddOrEdit from '_v/strategy-engine/business-side/add-edit.vue'

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
      addOrEditModalConfig: {
        curId: '',
        show: false,
        data: {
          type: 1,
          row: {},
          pageType: 'edit'
        }
      },
      formDataInit: {
        system: []
      },
      queryConfig: {
        btnLoading: false
      },
      formData: {
        name: '',
        systemKey: ''
      }
    }
  },
  created: function () {},
  mounted: function () {
    this.getSystemKey()
    this.handleSubmit('初始化', '默认加载数据成功', '默认加载数据失败')
  },
  methods: {
    onClearForm () {
      this.clearForm()
    },
    clearForm () {
      this.$refs['form'].resetFields()
    },
    onAdd () {
      this.addOrEditModalConfig.data.pageType = 'add'
      this.addOrEditModalConfig.show = true
      this.addOrEditModalConfig.curId = ''
    },
    onAddOrEditAfterCommit () {
      this.addOrEditModalConfig.show = false
      this.handleSubmit('添加', '列表数据已更新', '列表数据更新失败')
    },
    onEditAfterCommit () {
      this.addOrEditModalConfig.show = false
      this.handleSubmit('更新', '列表数据已更新', '列表数据更新失败')
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
      }
    },
    updateTableData () {
      this.queryConfig.btnLoading = true
      this.tableConfig.loading = true
      let reqData = {
        currentPage: this.tableConfig.data.current,
        pageSize: this.tableConfig.data.pageSize,
        name: this.formData.name,
        systemKey: this.formData.systemKey
      }
      this.getTableData(reqData)
    },
    getTableData (reqData) {
      BusinessSideService.getList(reqData).then(res => {
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
        { title: 'ID', key: 'id', minWidth: 30 },
        { title: '名称', key: 'name', minWidth: 100, tooltip: true },
        { title: '标识', key: 'businessSideKey', minWidth: 100, sortable: true, tooltip: true },
        { title: '接入系统', key: 'systemKey', minWidth: 100, sortable: true, tooltip: true },
        { title: '描述', key: 'description', minWidth: 200, tooltip: true },
        { title: '操作人', key: 'operator', minWidth: 100, tooltip: true },
        { title: '创建时间', key: 'createTime', minWidth: 150, tooltip: true, sortable: true },
        { title: '更新时间', key: 'updateTime', minWidth: 150, tooltip: true, sortable: true },
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
                    this.addOrEditModalConfig.curId = params.row.id
                    this.addOrEditModalConfig.data.row = params.row
                    this.addOrEditModalConfig.data.pageType = 'edit'
                    this.addOrEditModalConfig.show = true
                  }
                }
              }, '编辑')
            }
          ]
        }
      ]
    },
    getSystemKey () {
      BusinessSideService.getSystemKeySelectData().then(res => {
        this.formDataInit.system = res
      })
    }
  }
}
</script>

<style>

</style>
