<template>
  <div>
    <Modal title="新增场景"
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="add" :model="formData" :rules="ruleValidate" :label-width="100">
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="业务方" prop="businessSideKey">
              <Select v-model="formData.businessSideKey" placeholder="请选择">
                <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value">{{item.label}}
                </i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="场景名称" prop="name">
              <Input v-model="formData.name" placeholder="请输入,同一业务方的场景名不能相同" clearable/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="产品联系人" prop="pmAccount">
              <Select v-model="formData.pmAccount" multiple filterable remote
                      :placeholder="formData.pmAccount.join(';')"
                      :remote-method="remoteMethod"
                      :loading="accountSelectConfig.loading">
                <Option v-for="(option, index) in accountSelectConfig.options" :value="option.value" :key="index">{{option.label}}</Option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="技术联系人" prop="rdAccount">
              <Select v-model="formData.rdAccount" multiple filterable remote
                      :placeholder="formData.rdAccount.join(';')"
                      :remote-method="remoteMethod"
                      :loading="accountSelectConfig.loading">
                <Option v-for="(option, index) in accountSelectConfig.options" :value="option.value" :key="index">{{option.label}}</Option>
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
import LogUtil from '@/libs/log.js'
import Message from '@/libs/message.js'
import SceneService from '@/api/strategy-engine/scene'

export default {
  name: 'AddScene',
  props: {
    businessSide: {
      type: Array
    }
  },
  data () {
    const validNameExist = (rule, value, callback) => {
      SceneService.existedScene(this.formData.businessSideKey, value).then(res => {
        if (res.data.code === 200 && res.data.data === 'existed') {
          callback(new Error('场景名称不能重复'))
        } else {
          callback()
        }
      })
    }
    return {
      formDataInit: {
        businessSide: []
      },
      formData: {
        name: '',
        businessSideKey: '',
        pmAccount: [],
        rdAccount: [],
        description: ''
      },
      accountSelectConfig: {
        loading: false,
        options: []
      },
      finishButtonDisabled: false,
      ruleValidate: {
        businessSideKey: [{
          required: true, message: '请选择业务方', trigger: 'blur'
        }],
        name: [
          { required: true, message: '请输入场景名称', trigger: 'blur' },
          { type: 'string', message: '不能超过100个字符', max: 100, trigger: 'change' },
          { type: 'string', message: '不能包含~!@#$%^&*()+=\\/?,.\',;\\"等符号', pattern: /^[^\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\+\\=\\\\\\/\\?\\,\\.]*$/, trigger: 'blur' },
          { validator: validNameExist, trigger: 'change' }
        ],
        pmAccount: [{
          required: true, message: '产品/运营联系人OA账号为空'
        }],
        rdAccount: [{
          required: true, message: '技术联系人OA账号为空'
        }],
        description: [{
          required: true, message: '请输入备注', trigger: 'blur'
        }]
      }
    }
  },
  created: function () {},
  mounted: function () {},
  methods: {
    onVisibleChange (isVisible) {
      if (isVisible) {
        this.clearForm()
      }
      this.formData.businessSideKey = this.formDataInit.businessSide[0].value
    },
    clearForm () {
      this.$refs['add'].resetFields()
    },
    finishSubmit (name) {
      LogUtil.log('handleSubmit(): ' + name)

      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            businessSideKey: this.formData.businessSideKey,
            sceneKey: this.formData.sceneKey,
            name: this.formData.name,
            pmAccountArr: this.formData.pmAccount,
            rdAccountArr: this.formData.rdAccount,
            description: this.formData.description
          }
          SceneService.addScene(reqData).then(res => {
            Message.message('新增成功', 'success', this)
            this.$emit('onAfterCommit')
          })
        } else {
          Message.message('新增失败', 'error', this)
        }
      })
    },
    remoteMethod (query) {
      if (query !== '') {
        this.accountSelectConfig.loading = true
        setTimeout(() => {
          this.accountSelectConfig.loading = false
          SceneService.getByOaAccount(query).then(res => {
            if (res.data.code === 200) {
              this.accountSelectConfig.options = [{
                value: res.data.data.username.toString(),
                label: res.data.data.realname + '/' + res.data.data.orgName + '/' + (res.data.data.location === null ? '未知' : res.data.data.location)
              }]
            }
          })
        }, 200)
      } else {
        this.accountSelectConfig.options = []
      }
    }
  },
  watch: {
    businessSide (businessSide) {
      this.formDataInit.businessSide = businessSide
      this.formData.businessSideKey = businessSide[0].value
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
