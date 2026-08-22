<template>
<div>
  <Modal :title="config.pageType==='edit' ? '修改累计特征' + formData.id : '新增累计特征'" :width="800"
         @on-visible-change="onVisibleChange"
         v-bind="$attrs"
         v-on="$listeners">
    <Form ref="form_modal_add_edit_statistics_id" :model="formData" :rules="ruleValidate" :label-width="110">
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="名称" prop="name">
            <Input v-model="formData.name" placeholder="不可以数字开头,不可包含除下划线之外的其它符号" v-if="config.pageType==='add'"/>
            <Input v-model="formData.name" placeholder="不可以数字开头,不可包含除下划线之外的其它符号" readonly v-if="config.pageType==='edit'"/>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10" v-if="config.pageType==='add'">
        <i-col span="12">
          <FormItem label="业务方" prop="businessSideKey">
            <Select v-model="formData.businessSideKey" placeholder="业务方" @on-change="initScene">
              <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
        <i-col span="12">
          <FormItem label="场景" prop="sceneKey">
            <Select v-model="formData.sceneKey" placeholder="场景" @on-change="onChangeScene">
              <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10" v-if="config.pageType==='edit'">
        <i-col span="12">
          <FormItem label="业务方" prop="businessSideKey">
            <Select v-model="formData.businessSideKey" placeholder="业务方" @on-change="initScene" disabled>
              <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
        <i-col span="12">
          <FormItem label="场景" prop="sceneKey">
            <Select v-model="formData.sceneKey" placeholder="场景" @on-change="onChangeScene" disabled>
              <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="特征id" prop="featureId">
            <Input v-model="formData.featureId" placeholder="请输入" />
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="累计写入" prop="writeState">
            <RadioGroup v-model="formData.writeState" @on-change="onChangeWriteState">
              <Radio label="启用"></Radio>
              <Radio label="禁用"></Radio>
            </RadioGroup>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10" v-if="formData.writeState === '启用'">
        <i-col span="24">
          <FormItem label="写入过滤规则" prop="writeStrategyId">
            <Select v-model="formData.writeStrategyId" placeholder="写入过滤规则">
              <i-option v-for="item in formDataInit.strategy" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="查询过滤规则" prop="queryStrategyId">
            <Select v-model="formData.queryStrategyId" placeholder="查询过滤规则">
              <i-option v-for="item in formDataInit.strategy" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="维度字段" prop="identificationParas" >
            <Select v-model="formData.identificationParas" placeholder="请选择" multiple filterable>
              <i-option v-for="item in formDataInit.params" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="累计字段" prop="valId">
            <Select v-model="formData.valId" placeholder="请选择" filterable>
              <i-option v-for="item in formDataInit.params" :value="item.value" :key="item.value" >{{item.label}}</i-option>
            </Select>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="时间窗口" prop="beforeMinute">
            <Input v-model="formData.beforeMinute" placeholder="请输入距离时间" @on-keypress="onKeypressBeforeMinute">
              <i-select v-model="formData.beforeMinuteSelect" slot="prepend" style="width: 120px"  @on-change="onChangeBeforeMinute">
                <i-option v-for="item in formDataInit.beforeMinute" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </i-select>
              <span slot="append">分钟</span>
            </Input>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="备注" prop="description">
            <Input v-model="formData.description" type="textarea"  placeholder="请输入备注" clearable/>
          </FormItem>
        </i-col>
      </Row>
      <Row :gutter="10">
        <i-col span="24">
          <FormItem label="状态" prop="state">
            <RadioGroup v-model="formData.state">
              <Radio label="启用"></Radio>
              <Radio label="禁用"></Radio>
            </RadioGroup>
          </FormItem>
        </i-col>
      </Row>
    </Form>
    <div slot="footer">
      <Poptip
        confirm
        transfer
        title="确认提交吗?"
        @on-ok="finishSubmit('form_modal_add_edit_statistics_id')">
        <Button style="margin-left: 10px;" type="primary" size="large" :disabled="finishButtonDisabled">确定</Button>
      </Poptip>
    </div>
  </Modal>
</div>
</template>

<script>
import Common from '_v/strategy-engine/common'
import CommonTools from '@/libs/common-tools'
import log from '@/libs/log.js'
import Message from '@/libs/message.js'

import StrategyService from '@/api/strategy-engine/strategy'
import { addFeatureStatistics, existedFeatureStatistics, updateFeatureStatistics, getFeatureStatistics } from '@/api/strategy-engine/feature-statistics'
import BusinessSideService from '@/api/strategy-engine/businessside'
import SceneService from '@/api/strategy-engine/scene'
import ParamService from '@/api/strategy-engine/param'

export default {
  components: {},
  name: 'AddOrEditFeatureStatistics',
  props: {
    id: {},
    parentData: {
      type: Object
    }
  },
  data () {
    return {
      config: {
        parentData: {},
        pageType: 'edit'
      },
      formDataInit: {
        businessSide: [],
        strategy: [],
        params: [],
        scene: [],
        beforeMinute: Common.getBeforeMinute()
      },
      formData: {
        businessSideKey: '',
        sceneKey: '',
        writeState: '禁用',
        writeStrategyId: '',
        queryStrategyId: '',
        identificationParas: [],
        valId: [],
        name: '',
        featureId: '',
        beforeMinute: '',
        beforeMinuteSelect: '',
        state: '禁用',
        description: '',

        oldName: '',
        oldSceneKey: ''
      },
      finishButtonDisabled: false,
      ruleValidate: {
        name: [
          { required: true, message: '名称为空', trigger: 'blur' },
          { type: 'string', message: '不能超过100个字符', max: 100, trigger: 'change' },
          { type: 'string', message: '不能包含~!@#$%^&*()+=\\/?,.\',;\\"等符号', pattern: /^[^\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\+\\=\\\\\\/\\?\\,\\.]*$/, trigger: 'blur' },
          { type: 'string', message: '不能以数字开头且不能包含空格', pattern: /^[a-zA-Z_\u4e00-\u9fa5][a-zA-Z_0-9\u4e00-\u9fa5]*$/, trigger: 'blur' },

          { validator: this.validNameExist, trigger: 'blur' }
        ],
        businessSideKey: [{
          required: true, message: '请选择业务方', trigger: 'blur'
        }],
        writeState: [{
          required: true, message: '请选择写入状态', trigger: 'blur'
        }],
        writeStrategyId: [{
          required: true, message: '请选择写入过滤规则'
        }],
        queryStrategyId: [{
          required: true, message: '请选择查询过滤规则'
        }],
        sceneKey: [{
          required: true, message: '请选择场景', trigger: 'blur'
        }],
        identificationParas: [
          { type: 'array', required: true, message: '标志字段不能为空' }
        ],
        valId: [
          { required: true, message: '请选择累计字段', trigger: 'blur' }
        ],
        beforeMinute: [
          { required: true, message: '请输入距离时间' }
        ],
        featureId: [{
          required: true, message: '请输入特征id', trigger: 'blur'
        }],
        description: [{
          required: true, message: '请输入备注', trigger: 'blur'
        }],
        state: [{
          required: true, message: '请选择状态', trigger: 'blur'
        }]
      }
    }
  },
  created: function () {
  },
  mounted () {
    this.initBusinessSide()
  },
  watch: {
    id (id) {
      this.formData.id = id
    },
    pageType (pageType) {
      this.config.pageType = pageType
    }
  },
  methods: {
    onVisibleChange (isVisible) {
      if (!isVisible) {
        this.clearForm()
        return
      }

      this.config.pageType = this.parentData.pageType
      this.formData.sceneKey = this.parentData.sceneKey
      if (CommonTools.isEditPage(this.config.pageType)) {
        getFeatureStatistics(this.formData.id).then(res => {
          let data = res.data.data
          this.formData.id = data.id
          this.formData.businessSideKey = data.businessSideKey
          this.formData.oldSceneKey = data.sceneKey
          this.formData.writeState = data.writeState
          this.formData.writeStrategyId = data.writeStrategyId
          this.formData.queryStrategyId = data.queryStrategyId
          this.formData.identificationParas = []
          data.identificationParas.forEach(item => {
            this.formData.identificationParas.push(String(item))
          })
          this.formData.valId = String(data.valId)
          this.formData.name = data.name
          this.formData.oldName = data.name
          this.formData.featureId = String(data.featureId)
          this.formData.beforeMinute = String(data.beforeMinute)
          this.formData.state = data.state
          this.formData.description = data.description

          this.initScene(this.formData.businessSideKey)
          this.initParams(this.formData.sceneKey)
          this.initStrategy(this.formData.sceneKey)
        })
      } else {
        this.clearForm()
      }
    },
    clearForm () {
      this.$refs['form_modal_add_edit_statistics_id'].resetFields()
    },
    finishSubmit (name) {
      log.log('handleSubmit(): ' + name, this.formData)

      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            sceneKey: this.formData.sceneKey,
            strategyId: this.formData.writeStrategyId,
            writeState: this.formData.writeState,
            writeStrategyId: this.formData.writeStrategyId,
            queryStrategyId: this.formData.queryStrategyId,
            identificationParas: this.formData.identificationParas,
            valId: this.formData.valId,
            name: this.formData.name,
            featureId: this.formData.featureId,
            beforeMinute: this.formData.beforeMinute,
            state: this.formData.state,
            description: this.formData.description,
            ruleIds: this.formData.ruleIds
          }
          if (CommonTools.isEditPage(this.config.pageType)) {
            reqData.id = this.formData.id
            updateFeatureStatistics(reqData).then(res => {
              Message.message('更新成功', 'success', this)
              this.$emit('onAfterCommit')
            })
          } else {
            addFeatureStatistics(reqData).then(res => {
              Message.message('新建成功', 'success', this)
              this.$emit('onAfterCommit')
            })
          }
        } else {
          if (CommonTools.isEditPage(this.config.pageType)) {
            Message.message('更新累计特征失败', 'error', this)
          } else {
            Message.message('新建累计特征失败', 'error', this)
          }
        }
      })
    },
    validNameExist (rule, value, callback) {
      if (CommonTools.isEditPage(this.config.pageType)) {
        callback()
      } else {
        existedFeatureStatistics(this.formData.sceneKey, value).then(res => {
          if (res.data.code === 200 && res.data.data === 'existed') {
            callback(new Error('名称重复'))
          } else {
            callback()
          }
        })
      }
    },
    onChangeBeforeMinute (beforeMinute) {
      this.formData.beforeMinute = beforeMinute
    },
    onKeypressBeforeMinute () {
      this.formData.beforeMinuteSelect = ''
    },
    onChangeWriteState (writeState) {
    },
    onChangeScene (sceneKey) {
      this.formData.sceneKey = sceneKey

      // 加载过滤规则列表
      this.initStrategy(sceneKey)
      this.updateParams(sceneKey)
    },
    updateParams (sceneKey) {
      ParamService.getParamListBySceneKey(sceneKey).then(res => {
        let respData = CommonTools.isNull(res.data.data) ? [] : res.data.data
        this.formDataInit.params = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.name
          temp.value = String(item.id)
          this.formDataInit.params.push(temp)
        })
        this.formData.identificationParas = []
      })
    },
    initBusinessSide () {
      BusinessSideService.getSelectData({}).then(res => {
        this.formDataInit.businessSide = res
      })
    },
    initScene (businessSideKey) {
      let reqData = {
        businessSideKey: businessSideKey
      }
      SceneService.getSelectData(reqData).then(res => {
        this.formDataInit.scene = res
      })
    },
    initParams (sceneKey) {
      ParamService.getParamListBySceneKey(sceneKey).then(res => {
        let respData = CommonTools.isNull(res.data.data) ? [] : res.data.data
        this.formDataInit.params = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.name
          temp.value = String(item.id)
          this.formDataInit.params.push(temp)
        })
      })
    },
    initStrategy (sceneKey) {
      if (CommonTools.isNull(sceneKey)) {
        return
      }

      let reqData = {
        sceneKey: sceneKey,
        type: 50
      }
      StrategyService.getStatisticsFilterSelectData(reqData).then(res => {
        this.formDataInit.strategy = res
      })
    }
  }
}
</script>

<style module>

</style>
