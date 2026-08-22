<template>
  <div>
    <Card shadow style="margin-bottom: 10px;">
      <Form ref="form" :model="formData" >
        <Row :gutter="10">
          <i-col span="6">
            <FormItem prop="name">
              <Input v-model="formData.name" placeholder="工具名称" clearable/>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem prop="state">
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
  </div>
</template>

<script>
import Message from '@/libs/message.js'
import Tables from '_c/tables-common/tables-v2'
import ToolService from '@/api/strategy-engine/tool'

export default {
  name: '',
  components: {
    Tables
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
      detailParamModalConfig: {
        show: false
      },
      formDataInit: {
        state: []
      },
      queryConfig: {
        btnLoading: false
      },
      formData: {
        currentPage: 1,
        pageSize: 5,
        name: '',
        state: ''
      }
    }
  },
  created: function () {
  },
  mounted: function () {
    // 首次打开界面初始化显示数据
    this.getState()
    this.handleSubmit('初始化', '默认加载数据成功', '默认加载数据失败')
  },
  methods: {
    onClearForm () {
      this.clearForm()
    },
    clearForm () {
      this.$refs['form'].resetFields()
    },
    forbidden (id) {
      ToolService.forbiddenTool(id).then(res => {
        if (res.data.code === 200) {
          Message.message('禁用成功', 'success', this)
          this.handleSubmit('禁用工具' + id, '工具列表数据已更新', '工具列表数据更新失败')
        } else {
          Message.message(res.data.data, 'error', this)
        }
      })
    },
    enable (id) {
      ToolService.enableTool(id).then(res => {
        if (res.data.code === 200) {
          Message.message('启用成功', 'success', this)
          this.handleSubmit('启用工具' + id, '工具列表数据已更新', '工具列表数据更新失败')
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
        name: this.formData.name,
        state: this.formData.state
      }
      this.getTableData(reqData)
    },
    getTableData (reqData) {
      ToolService.getToolList(reqData).then(res => {
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
        { title: '工具名称', key: 'name', minWidth: 100, tooltip: true },
        { title: '工具描述', key: 'description', minWidth: 150, tooltip: true },
        { title: '操作人', key: 'operator', minWidth: 100, tooltip: true },
        { title: '更新时间', key: 'updateTime', minWidth: 150, sortable: true },
        { title: '状态', key: 'state', minWidth: 100 },
        {
          title: '操作',
          key: 'handle',
          fixed: 'right',
          width: 70,
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
    }
  }
}
</script>

<style>

</style>
