<template>
<div>
  <Modal :title="'修改场景' + scene.id"
         @on-visible-change="onVisibleChange"
         v-bind="$attrs"
         v-on="$listeners">
    <Form ref="edit" :model="formData" :rules="ruleValidate" :label-width="100">
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="场景标志" prop="sceneKey">
            <Input v-model="formData.sceneKey" placeholder="无需输入,自动生成" readonly/>
          </FormItem>
        </i-col>
      </Row>
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
          <FormItem label="场景名称" prop="name" >
            <Input v-model="formData.name" placeholder="请输入,同一业务方的场景名不能相同" readonly/>
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
            <Input v-model="formData.description" type="textarea"  placeholder="请填写场景特点与使用方式" clearable/>
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
import LogUtil from '@/libs/log.js'
import Message from '@/libs/message.js'
import SceneService from '@/api/strategy-engine/scene'
import BusinessSideService from '@/api/strategy-engine/businessside'

export default {
  name: 'EditScene',
  props: {
    scene: {
      type: Object
    },
    businessSide: {
      type: Array
    }
  },
  data () {
    const validNameExist = (rule, value, callback) => {
      if (this.formData.name === value) {
        callback()
      }

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
        id: '',
        businessSideKey: '',
        sceneKey: '',
        name: '',
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
  mounted: function () {
    this.initBusinessSide()
  },
  watch: {},
  methods: {
    onVisibleChange (isVisible) {
      if (!isVisible) {
        return
      }
      SceneService.getById(this.scene.id).then(res => {
        let data = res.data.data
        let accountOpt = []

        data.accountList.forEach(item => {
          accountOpt.push({
            value: item.account.toString(),
            label: item.realName + '/' + item.department + '/' + item.location
          })
        })

        this.formData.id = data.id
        this.formData.name = data.name
        this.formData.businessSideKey = data.businessSideKey
        this.formData.sceneKey = data.sceneKey
        this.accountSelectConfig.options = accountOpt
        this.formData.pmAccount = data.pmAccountArr
        this.formData.rdAccount = data.rdAccountArr
        this.formData.description = data.description
      })
    },
    finishSubmit (name) {
      LogUtil.log('handleSubmit(): ' + name)

      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            id: this.formData.id,
            businessSideKey: this.formData.businessSideKey,
            name: this.formData.name,
            pmAccountArr: this.formData.pmAccount,
            rdAccountArr: this.formData.rdAccount,
            description: this.formData.description
          }
          SceneService.updateScene(reqData).then(res => {
            Message.message('修改成功', 'success', this)
            this.$emit('onAfterCommit')
          })
        } else {
          Message.message('修改失败', 'error', this)
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
    },
    initBusinessSide () {
      BusinessSideService.getSelectData({}).then(res => {
        this.formDataInit.businessSide = res
      })
    }
  }
}
</script>

<style module>
  /*动态必填项className*/
  .requireStar .ivu-form-item-label:before{
    content: '*';
    display: inline-block;
    margin-right: 4px;
    line-height: 1;
    font-family: SimSun;
    font-size: 12px;
    color: #ed4014;
  }
</style>
