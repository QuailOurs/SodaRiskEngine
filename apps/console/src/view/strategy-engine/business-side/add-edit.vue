<template>
  <div>
    <Modal :title="(config.pageType==='edit' ? '修改 ' : '新建') + formData.id"
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="modal_add_edit_form_id" :model="formData" :rules="ruleValidate" :label-width="80">
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="名称" prop="name">
              <Input v-model="formData.name" placeholder="名称" clearable/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem v-if="config.pageType==='add'" label="标识" prop="businessSideKey">
              <Input v-model="formData.businessSideKey" placeholder="仅使用字母" clearable/>
            </FormItem>
            <FormItem v-if="config.pageType==='edit'" label="标识" prop="businessSideKey">
              <Input v-model="formData.businessSideKey" placeholder="仅使用字母" disabled />
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="接入系统" prop="systemKey">
              <Input v-model="formData.systemKey" placeholder="仅使用字母" clearable/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="描述" prop="description">
              <Input v-model="formData.description" type="textarea"/>
            </FormItem>
          </i-col>
        </Row>
      </Form>
      <div slot="footer">
        <Button type="primary" size="large" @click="handleSubmit('modal_add_edit_form_id')">提交</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import CommonTools from '@/libs/common-tools'
import Message from '@/libs/message'
import BusinessSideService from '@/api/strategy-engine/businessside'

export default {
  components: {},
  name: 'AddOrEdit',
  props: {
    id: {},
    parentData: {
      type: Object
    }
  },
  data () {
    return {
      config: {
        pageType: 'edit'
      },
      formDataInit: {
      },
      formData: {
        id: this.id,
        name: '',
        businessSideKey: '',
        systemKey: '',
        description: ''
      },
      ruleValidate: {
        name: [
          { required: true, message: '名称为空' },
          { validator: this.validNameExist, trigger: 'blur' }
        ],
        businessSideKey: [
          { required: true, message: '标识为空' },
          { validator: this.validKeyExist, trigger: 'blur' }
        ],
        description: [{
          required: true, message: '描述为空', trigger: 'blur'
        }]
      }
    }
  },
  created: function () {},
  mounted () {},
  computed: {},
  watch: {
    id (id) {
      this.formData.id = id
    }
  },
  methods: {
    onVisibleChange (isVisible) {
      if (!isVisible) {
        this.clearForm()
        return
      }

      this.config.pageType = this.parentData.pageType
      if (CommonTools.isEditPage(this.config.pageType)) {
        BusinessSideService.getById(this.formData.id).then(res => {
          let data = res.data.data
          this.formData.id = data.id
          this.formData.name = data.name
          this.formData.businessSideKey = data.businessSideKey
          this.formData.systemKey = data.systemKey
          this.formData.description = data.description
        })
      } else {
        this.clearForm()
      }
    },
    clearForm () {
      this.$refs['modal_add_edit_form_id'].resetFields()
    },
    handleSubmit (name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            name: this.formData.name,
            businessSideKey: this.formData.businessSideKey,
            systemKey: this.formData.systemKey,
            description: this.formData.description
          }
          if (CommonTools.isEditPage(this.config.pageType)) {
            reqData.id = this.formData.id
            BusinessSideService.update(reqData).then(res => {
              Message.message('更新成功', 'success', this)
              this.$emit('onAfterCommit')
            })
          } else {
            BusinessSideService.add(reqData).then(res => {
              Message.message('新建成功', 'success', this)
              this.$emit('onAfterCommit')
            })
          }
        } else {
          if (CommonTools.isEditPage(this.config.pageType)) {
            Message.message('更新失败', 'error', this)
          } else {
            Message.message('新建失败', 'error', this)
          }
        }
      })
    },
    validNameExist (rule, value, callback) {
      const excludeId = CommonTools.isEditPage(this.config.pageType) ? this.formData.id : null
      BusinessSideService.validNameExist(value, excludeId).then(res => {
        let backendData = res.data.data
        if (backendData === 'existed') {
          let error = '名称重复'
          callback(error)
        } else {
          callback()
        }
      })
    },
    validKeyExist (rule, value, callback) {
      const excludeId = CommonTools.isEditPage(this.config.pageType) ? this.formData.id : null
      BusinessSideService.validKeyExist(value, excludeId).then(res => {
        let backendData = res.data.data
        if (backendData === 'existed') {
          let error = '标识重复'
          callback(error)
        } else {
          callback()
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
