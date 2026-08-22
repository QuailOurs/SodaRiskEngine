<template>
  <div>
    <Modal :title="'编辑字段' + formData.id"
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="edit" :model="formData" :rules="ruleValidate" :label-width="70">
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="业务方">
              <Input v-model="formData.businessSideName" readonly/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="场景">
              <Input v-model="formData.sceneName" readonly/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段标志">
              <Input v-model="formData.paramKey" readonly/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段名称" prop="name">
              <Input v-model="formData.name" preadonly/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段类型" prop="type">
              <Select v-model="formData.type" placeholder="请选择" clearable>
                <i-option v-for="item in formDataInit.type" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="备注" prop="description">
              <Input v-model="formData.description" type="textarea" placeholder="请输入备注" clearable/>
            </FormItem>
          </i-col>
        </Row>
      </Form>
      <div slot="footer">
        <Poptip
          confirm
          transfer
          title="确认提交吗?"
          @on-ok="finishSubmit('edit')">
          <Button style="margin-left: 10px;" type="primary" size="large" :disabled="finishButtonDisabled">确定</Button>
        </Poptip>
      </div>
    </Modal>
  </div>
</template>

<script>
import Message from '@/libs/message.js'
import ParamService from '@/api/strategy-engine/param'
import DataTypeService from '@/api/strategy-engine/datatype'

export default {
  name: 'EditParam',
  props: {
    parameter: {
      type: Object
    }
  },
  data () {
    return {
      formDataInit: {
        type: []
      },
      formData: {
        id: '',
        paramKey: '',
        name: '',
        businessSideName: '',
        sceneName: '',
        type: '',
        description: ''
      },
      finishButtonDisabled: false,
      ruleValidate: {
        description: [{
          required: true, message: '请输入备注', trigger: 'blur'
        }]
      }
    }
  },
  created: function () {
  },
  mounted: function () {
    this.getParamType()
  },
  methods: {
    onVisibleChange (isVisible) {
    },
    clearForm () {
      this.$refs['edit'].resetFields()
    },
    finishSubmit (name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            id: this.formData.id,
            name: this.formData.name,
            typeId: this.formData.type,
            description: this.formData.description
          }
          ParamService.updateParam(reqData).then(res => {
            Message.message('编辑成功', 'success', this)
            this.$emit('onAfterCommit')
          })
        } else {
          Message.message('编辑失败', 'error', this)
        }
      })
    },
    getParamType () {
      DataTypeService.getDataTypeList({}).then(res => {
        let respData = res.data.data
        this.formDataInit.type = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.typeName
          temp.value = String(item.id)
          this.formDataInit.type.push(temp)
        })
        this.formData.type = this.formDataInit.type[0].value
      })
    }
  },
  watch: {
    parameter (parameter) {
      this.formData.id = parameter.id
      this.formData.paramKey = parameter.paramKey
      this.formData.name = parameter.name
      this.formData.businessSideName = parameter.businessSideName
      this.formData.sceneName = parameter.sceneName
      this.formData.type = String(parameter.typeId)
      this.formData.description = parameter.description
    }
  }
}
</script>
