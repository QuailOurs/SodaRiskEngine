<template>
  <div>
    <Modal title="新增补全信息"
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
              <Select v-model="formData.scene" placeholder="请选择" @on-change="getParamByScene()">
                <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="字段key" prop="param">
               <Select v-model="formData.param" placeholder="请选择" filterable remote
                      :remote-method="remoteMethod"
                      :loading="paramSelectConfig.loading">>
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
import SceneService from '@/api/strategy-engine/scene'
import ComplementService from '@/api/strategy-engine/complement'
export default {
  name: 'AddParam',
  props: {
    businessSide: {
      type: Array
    },
    tool: {
      type: Array
    }
  },
  data () {
    return {
      formDataInit: {
        businessSide: [],
        type: [],
        scene: [],
        tool: [],
        complementKey: [],
        param: []
      },
      formData: {
        currentPage: 1,
        pageSize: 10,
        param: '',
        name: '',
        businessSideKey: '',
        scene: '',
        type: '',
        complementKey: '',
        description: '',
        tool: ''
      },
      paramSelectConfig: {
        loading: false,
        options: []
      },
      finishButtonDisabled: false,
      ruleValidate: {
        businessSideKey: [{
          required: true, message: '请选择业务方', trigger: 'blur'
        }],
        scene: [{
          required: true, message: '请选择场景', trigger: 'blur'
        }],
        param: [{
          required: true, type: 'number', message: '请输入字段key', trigger: 'blur'
        }],
        complementKey: [{
          required: true, type: 'number', message: '请选择补全key', trigger: 'blur'
        }],
        tool: [{
          required: true, type: 'number', message: '请选择补全工具', trigger: 'blur'
        }]
      }
    }
  },
  created: function () {
  },
  methods: {
    onVisibleChange (isVisible) {
      if (isVisible) {
        this.clearForm()
        this.formData.businessSideKey = this.formDataInit.businessSide[0].value
        this.getSceneByBusinessSideKey(this.formData.businessSideKey)
        this.formData.tool = this.formDataInit.tool[0].value
        this.getComplementKeyByToolId(this.formData.tool)
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
            businessSideKey: this.formData.businessSideKey,
            paramId: this.formData.param,
            complementKeyId: this.formData.complementKey,
            toolId: this.formData.tool
          }
          ComplementService.addParam(reqData).then(res => {
            if (res.data.code !== null && res.data.code === 200) {
              Message.message('新增成功', 'success', this)
              this.$emit('onAfterCommit')
            } else {
              Message.message('新增失败,数据存在或其他错误', 'error', this)
              this.$emit('onAfterCommit')
            }
          })
        } else {
          Message.message('新增失败', 'error', this)
        }
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
        if (respData !== null) {
          respData.forEach(item => {
            let temp = {}
            temp.label = item.name + '(' + item.sceneKey + ')'
            temp.value = item.sceneKey
            this.formDataInit.scene.push(temp)
          })
          this.formData.scene = this.formDataInit.scene[0].value
          this.getParamByScene()
        }
      })
    },
    updateSceneList (sceneKey) {
      if (this.formData.businessSideKey !== undefined && this.formData.businessSideKey !== '') {
        this.getSceneByBusinessSideKey(this.formData.businessSideKey)
      } else {
        this.getScene()
      }
    },
    getParamByScene () {
      this.formDataInit.param = []
      let reqData = {
        sceneKey: this.formData.scene,
        currentPage: this.formData.currentPage,
        pageSize: this.formData.pageSize
      }
      ParamService.getParamList(reqData).then(res => {
        let respData = res.data.data.records
        this.formDataInit.param = []
        if (respData !== null) {
          respData.forEach(item => {
            let temp = {}
            temp.label = item.paramKey + '(' + item.name + ')'
            temp.value = item.id
            this.formDataInit.param.push(temp)
          })
        }
      })
    },
    getComplementKeyByToolId (tool) {
      let reqData = {
        toolId: tool
      }
      ComplementService.getComplementKeyByToolId(reqData).then(res => {
        let respData = res.data.data
        this.formDataInit.complementKey = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.complementKey + '(' + item.description + ')'
          temp.value = item.id
          this.formDataInit.complementKey.push(temp)
        })
        this.formData.complementKey = this.formDataInit.complementKey[0].value
      })
    },
    remoteMethod (query) {
      if (query !== '') {
        this.paramSelectConfig.loading = true
        setTimeout(() => {
          this.paramSelectConfig.loading = false
          let reqData = {
            sceneKey: this.formData.scene,
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
    businessSide (businessSide) {
      this.formDataInit.businessSide = businessSide
    },
    tool (tool) {
      this.formDataInit.tool = tool
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
