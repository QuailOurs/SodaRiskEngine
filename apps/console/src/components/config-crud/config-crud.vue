<template>
  <Card dis-hover class="config-crud">
    <div slot="title" class="config-crud__title">
      <span>{{ title }}</span>
      <Tag color="blue">{{ filteredRows.length }} 条</Tag>
    </div>
    <div class="config-crud__toolbar">
      <Input
        v-model="keyword"
        clearable
        search
        placeholder="搜索当前列表"
        style="width: 260px"
      />
      <div>
        <Button icon="md-refresh" class="toolbar-button" @click="load">刷新</Button>
        <Button type="primary" icon="md-add" @click="openCreate">新增{{ entityName }}</Button>
      </div>
    </div>

    <Table
      border
      stripe
      :loading="loading"
      :columns="tableColumns"
      :data="pagedRows"
      no-data-text="暂无数据"
    />
    <div v-if="filteredRows.length > pageSize" class="config-crud__pagination">
      <Page
        :current="currentPage"
        :total="filteredRows.length"
        :page-size="pageSize"
        :page-size-opts="[20, 50, 100]"
        show-total
        show-sizer
        @on-change="currentPage = $event"
        @on-page-size-change="changePageSize"
      />
    </div>

    <Modal
      v-model="modalVisible"
      :title="editing ? `编辑${entityName}` : `新增${entityName}`"
      :mask-closable="false"
      width="680"
    >
      <Form :label-width="110">
        <Row :gutter="16">
          <Col v-for="field in visibleFields" :key="field.key" :span="field.span || 12">
            <FormItem :label="field.label" :required="field.required">
              <Select
                v-if="field.type === 'select'"
                v-model="form[field.key]"
                :multiple="field.multiple"
                clearable
                filterable
                :placeholder="`请选择${field.label}`"
              >
                <Option
                  v-for="option in field.options || []"
                  :key="String(option.value)"
                  :value="option.value"
                >{{ option.label }}</Option>
              </Select>
              <InputNumber
                v-else-if="field.type === 'number'"
                v-model="form[field.key]"
                :min="field.min"
                :max="field.max"
                style="width: 100%"
              />
              <Input
                v-else
                v-model="form[field.key]"
                :type="field.type === 'textarea' ? 'textarea' : 'text'"
                :rows="field.rows || 3"
                :disabled="editing && field.immutable"
                :placeholder="`请输入${field.label}`"
              />
            </FormItem>
          </Col>
        </Row>
      </Form>
      <div slot="footer">
        <Button @click="modalVisible = false">取消</Button>
        <Button type="primary" :loading="saving" @click="save">保存</Button>
      </div>
    </Modal>
  </Card>
</template>

<script>
export default {
  name: 'ConfigCrud',
  props: {
    title: { type: String, required: true },
    entityName: { type: String, required: true },
    api: { type: Object, required: true },
    fields: { type: Array, required: true },
    columns: { type: Array, required: true },
    beforeSave: { type: Function, default: data => data }
  },
  data () {
    return {
      rows: [],
      keyword: '',
      currentPage: 1,
      pageSize: 20,
      loading: false,
      saving: false,
      modalVisible: false,
      editing: false,
      form: {}
    }
  },
  computed: {
    filteredRows () {
      const keyword = this.keyword.trim().toLowerCase()
      if (!keyword) return this.rows
      return this.rows.filter(row => JSON.stringify(row).toLowerCase().includes(keyword))
    },
    pagedRows () {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredRows.slice(start, start + this.pageSize)
    },
    visibleFields () {
      return this.fields.filter(field => !(this.editing && field.hideOnEdit))
    },
    tableColumns () {
      const columns = this.columns.map(column => {
        if (column.type === 'state') {
          return {
            ...column,
            render: (h, params) => h('Tag', {
              props: { color: Number(params.row[column.key]) === 1 ? 'success' : 'default' }
            }, Number(params.row[column.key]) === 1 ? '启用' : Number(params.row[column.key]) === 2 ? '预上线' : '禁用')
          }
        }
        if (column.options) {
          return {
            ...column,
            render: (h, params) => {
              const option = column.options.find(item => String(item.value) === String(params.row[column.key]))
              return h('span', option ? option.label : String(params.row[column.key] === undefined ? '' : params.row[column.key]))
            }
          }
        }
        return column
      })
      columns.push({
        title: '操作',
        key: '_action',
        width: this.api.remove ? 150 : 90,
        fixed: 'right',
        render: (h, params) => h('div', [
          h('Button', {
            props: { size: 'small', type: 'primary', ghost: true },
            style: { marginRight: '8px' },
            on: { click: () => this.openEdit(params.row) }
          }, '编辑'),
          this.api.remove
            ? h('Button', {
              props: { size: 'small', type: 'error', ghost: true },
              on: { click: () => this.confirmRemove(params.row) }
            }, '删除')
            : null
        ])
      })
      return columns
    }
  },
  watch: {
    keyword () {
      this.currentPage = 1
    }
  },
  mounted () {
    this.load()
  },
  methods: {
    emptyForm () {
      const form = {}
      this.fields.forEach(field => {
        if (field.default !== undefined) form[field.key] = typeof field.default === 'function' ? field.default() : field.default
        else if (field.multiple) form[field.key] = []
        else form[field.key] = null
      })
      return form
    },
    unwrap (response) {
      const payload = response && response.data
      if (!payload || (payload.code !== 200 && payload.code !== 0)) {
        throw new Error((payload && (payload.msg || payload.message)) || '接口返回异常')
      }
      return payload.data
    },
    async load () {
      this.loading = true
      try {
        const data = this.unwrap(await this.api.list({}))
        this.rows = Array.isArray(data) ? data : ((data && data.records) || [])
        const lastPage = Math.max(1, Math.ceil(this.filteredRows.length / this.pageSize))
        if (this.currentPage > lastPage) this.currentPage = lastPage
      } catch (error) {
        this.$Message.error(error.message || '加载失败')
      } finally {
        this.loading = false
      }
    },
    changePageSize (size) {
      this.pageSize = size
      this.currentPage = 1
    },
    openCreate () {
      this.editing = false
      this.form = this.emptyForm()
      this.modalVisible = true
    },
    async openEdit (row) {
      this.editing = true
      try {
        const source = this.api.detail ? this.unwrap(await this.api.detail(row.id)) : row
        this.form = { ...this.emptyForm(), ...source }
        this.modalVisible = true
      } catch (error) {
        this.$Message.error(error.message || '加载详情失败')
      }
    },
    validate () {
      const missing = this.fields.find(field => {
        if (!field.required) return false
        const value = this.form[field.key]
        return value === null || value === undefined || value === '' || (Array.isArray(value) && value.length === 0)
      })
      if (missing) {
        this.$Message.warning(`请填写${missing.label}`)
        return false
      }
      return true
    },
    async save () {
      if (!this.validate()) return
      this.saving = true
      try {
        const data = this.beforeSave({ ...this.form }, this.editing)
        const response = this.editing ? await this.api.update(data) : await this.api.add(data)
        this.unwrap(response)
        this.$Message.success('保存成功')
        this.modalVisible = false
        await this.load()
      } catch (error) {
        this.$Message.error(error.message || '保存失败')
      } finally {
        this.saving = false
      }
    },
    confirmRemove (row) {
      this.$Modal.confirm({
        title: `删除${this.entityName}`,
        content: `确认删除“${row.name || row.ruleKey || row.id}”吗？`,
        onOk: async () => {
          try {
            this.unwrap(await this.api.remove(row.id))
            this.$Message.success('删除成功')
            await this.load()
          } catch (error) {
            this.$Message.error(error.message || '删除失败')
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.config-crud__title,
.config-crud__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.config-crud__toolbar {
  margin-bottom: 16px;
}

.config-crud__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.toolbar-button {
  margin-right: 8px;
}
</style>
