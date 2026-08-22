<template>
  <div>
    <Modal title="新增字段"
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="add" :model="formData" :rules="ruleValidate" :label-width="80">
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="业务方" prop="businessSideKey">
              <Select v-model="formData.businessSideKey" placeholder="请选择" @on-change="updateSceneList()">
                <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="场景" prop="scene">
              <Select v-model="formData.scene" placeholder="请选择">
                <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段标志" prop="paramKey">
              <Input v-model="formData.paramKey" placeholder="字段key，不能输入中文"/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段名称" prop="name">
              <Input v-model="formData.name" placeholder="请输入字段中文名"/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段类型" prop="type">
              <Select v-model="formData.type" placeholder="请选择" clearable>
                <i-option v-for="item in formDataInit.type" :value="item.value" :key="item.value">{{item.label}}</i-option>
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
import SceneService from '@/api/strategy-engine/scene'

export default {
  name: 'AddParam',
  props: {
    businessSide: {
      type: Array
    }
  },
  data () {
    const validKeyExist = (rule, value, callback) => {
      ParamService.existedKeyParam(this.formData.scene, value).then(res => {
        if (res.data.code === 200 && res.data.data === 'existed') {
          callback(new Error('字段标志不能重复'))
        } else {
          callback()
        }
      })
    }
    const validNameExist = (rule, value, callback) => {
      ParamService.existedNameParam(this.formData.scene, value).then(res => {
        if (res.data.code === 200 && res.data.data === 'existed') {
          callback(new Error('字段名称不能重复'))
        } else {
          callback()
        }
      })
    }
    return {
      formDataInit: {
        businessSide: [],
        type: [],
        scene: []
      },
      formData: {
        paramKey: '',
        name: '',
        businessSideKey: '',
        scene: '',
        type: '',
        description: ''
      },
      finishButtonDisabled: false,
      ruleValidate: {
        businessSideKey: [{
          required: true, message: '请选择业务方', trigger: 'blur'
        }],
        scene: [{
          required: true, message: '请选择场景', trigger: 'blur'
        }],
        name: [
          { required: true, message: '请输入字段名称', trigger: 'blur' },
          { type: 'string', message: '不能超过100个字符', max: 100, trigger: 'change' },
          { type: 'string', message: '不能包含~!@#$%^&*()+=\\/?,.\',;\\"等符号', pattern: /^[^\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\+\\=\\\\\\/\\?\\,\\.]*$/, trigger: 'blur' },
          { type: 'string', message: '不能以数字开头且不能包含空格', pattern: /^[a-zA-Z_\u4e00-\u9fa5][a-zA-Z_0-9\u4e00-\u9fa5]*$/, trigger: 'blur' },
          { validator: validNameExist, trigger: 'change' }
        ],
        paramKey: [
          { required: true, message: '请输入字段标志', trigger: 'blur' },
          { validator: validKeyExist, trigger: 'change' }
        ],
        type: [
          { required: true, message: '请选择字段类型', trigger: 'blur' }
        ],
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
      if (isVisible) {
        this.clearForm()
        this.formData.businessSideKey = this.formDataInit.businessSide[0].value
        this.getSceneByBusinessSideKey(this.formData.businessSideKey)
      }
    },
    clearForm () {
      this.$refs['add'].resetFields()
    },
    finishSubmit (name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            sceneKey: this.formData.scene,
            typeId: this.formData.type,
            businessSideKey: this.formData.businessSideKey,
            description: this.formData.description,
            name: this.formData.name,
            paramKey: this.formData.paramKey
          }
          ParamService.addParam(reqData).then(res => {
            Message.message('新增成功', 'success', this)
            this.$emit('onAfterCommit')
          })
        } else {
          Message.message('新增失败', 'error', this)
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
        this.formData.scene = this.formDataInit.scene[0].value
      })
    },
    updateSceneList () {
      if (this.formData.businessSideKey !== undefined && this.formData.businessSideKey !== '') {
        this.getSceneByBusinessSideKey(this.formData.businessSideKey)
      } else {
        this.getScene()
      }
    }
  },
  watch: {
    businessSide (businessSide) {
      this.formDataInit.businessSide = businessSide
    }
  }
}
</script>

<style module>
  /*动态必填项className*/
  .requireStar .ivu-form-item-label:before {
    content: '*';
    display: inline-block;
    margin-right: 4px;
    line-height: 1;
    font-family: SimSun;
    font-size: 12px;
    color: #ed4014;
  }
</style>
