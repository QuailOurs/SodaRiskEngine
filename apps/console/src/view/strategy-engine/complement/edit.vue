<template>
  <div>
    <Modal :title="'编辑补全信息' + formData.id"
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="add" :model="formData" :rules="ruleValidate" :label-width="80">
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="业务方">
              <Input v-model="formData.businessSideName" readonly/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="场景" prop="scene">
              <Input v-model="formData.scene" :value="formData.sceneKey" readonly/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段key" prop="param">
               <Select v-model="formData.param" placeholder="请选择" filterable remote
                      :remote-method="remoteMethod"
                      :loading="paramSelectConfig.loading">
                <i-option v-for="item in formDataInit.param" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="补全工具" prop="tool">
              <Select v-model="formData.tool" placeholder="请选择">
                <i-option v-for="item in formDataInit.tool" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
         <Row :gutter="10">
          <i-col span="24">
            <FormItem label="补全key" prop="complementKey">
              <Select v-model="formData.complementKey" placeholder="请选择">
                <i-option v-for="item in formDataInit.complementKey" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
      </Form>
      <div slot="footer">
        <Poptip
          confirm
          transfer
          title="确认提交吗?"
          @on-ok="finishSubmit('add')">
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
    complement: {
      type: Object
    }
  },
  data () {
    return {
      formDataInit: {
        type: []
      },
      formData: {
        currentPage: 1,
        pageSize: 10,
        businessSideName: '',
        scene: '',
        param: '',
        tool: '',
        complementKey: '',
        sceneKey: ''
      },
      finishButtonDisabled: false,
      ruleValidate: {
        description: [{
          required: true, message: '请输入备注', trigger: 'blur'
        }]
      },
      paramSelectConfig: {
        loading: false,
        options: []
      }
    }
  },
  created: function () {
  },
  mounted: function () {
    this.getParamType()
    this.getParamList()
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
    getParamList () {
      let reqData = {
        sceneKey: this.formData.sceneKey,

        currentPage: this.formData.currentPage,
        pageSize: this.formData.pageSize
      }
      ParamService.getParamList(reqData).then(res => {
        if (res.data.code === 200) {
          let respData = res.data.data.records
          this.formDataInit.param = []
          let paramOpt = []
          if (respData !== null) {
            respData.forEach(item => {
              paramOpt.push({
                value: item.id,
                label: item.paramKey + '(' + item.name + ')'
              })
            })
            this.formDataInit.param = paramOpt
          }
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
    },
    remoteMethod (query) {
      if (query !== '') {
        this.paramSelectConfig.loading = true
        setTimeout(() => {
          this.paramSelectConfig.loading = false
          let reqData = {
            sceneKey: this.formData.sceneKey,
            fuzzyParamKey: query,
            currentPage: this.formData.currentPage,
            pageSize: this.formData.pageSize
          }
          ParamService.getParamList(reqData).then(res => {
            if (res.data.code === 200) {
              let respData = res.data.data.records
              this.formDataInit.param = []
              let paramOpt = []
              if (respData !== null) {
                respData.forEach(item => {
                  paramOpt.push({
                    value: item.id,
                    label: item.paramKey + '(' + item.name + ')'
                  })
                })
                this.formDataInit.param = paramOpt
              }
            }
          })
        }, 200)
      } else {
        this.paramSelectConfig.options = []
      }
    }
  },
  watch: {
    complement (complement) {
      this.formData.businessSideName = complement.businessSideName
      this.formData.scene = complement.sceneName + '(' + complement.sceneKey + ')'
      this.formData.sceneKey = complement.sceneKey
      this.formData.param = complement.paramKey
      this.formData.tool = complement.tool
      this.formData.complementKey = complement.complementKey
      this.formData.id = complement.id
    }
  }
}
</script>
