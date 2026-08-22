<template>
  <div>
    <Modal :title="(config.pageType==='edit' ? '修改条件' : '新建条件') + formData.id" :width="1100"
           :styles="{top: '20px'}"
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="form_modal_add_edit_rule_id" :model="formData" :rules="ruleValidate" :label-width="100">
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="名称" prop="name">
              <Input v-model="formData.name" placeholder="名称" clearable v-if="config.pageType==='add'"/>
              <Input v-model="formData.name" placeholder="名称" readonly v-if="config.pageType==='edit'"/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="左操作数类型" prop="ruleType">
              <Select v-model="formData.ruleType" placeholder="左操作数类型" clearable @on-change="onChangeRuleType">
                <i-option v-for="item in formDataInit.ruleType" :value="item.value" :key="item.value" >{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="工具" prop="tool" v-if="hasArrValue([2,4,6,14,15,16], formData.ruleType)">
              <Select v-model="formData.tool" placeholder="工具" clearable @on-change="onChangeTool">
                <i-option v-for="item in formDataInit.tool" :value="item.value" :key="item.value" >{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="查询参数" prop="srcParam" v-if="hasArrValue([2,4,6,15], formData.ruleType)">
              <Cascader v-model="formData.srcParam" :data="formDataInit.srcParam" placeholder="查询参数" trigger="hover"
                        filterable
                        @on-change="onChangeSrcParam"/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">

        </Row>
        <Row type="flex">
          <i-col span="24">
            <FormItem label="表达式" prop="express">
              <Row>
                <i-col span="9" v-if="!hasArrValue([2,4,6,14,15,16], formData.ruleType)">
                  <FormItem prop="left">
                    <Cascader v-model="formData.left" :data="formDataInit.left" :value="formData.left"
                              placeholder="左操作数" trigger="hover" filterable
                              @on-change="onChangeLeftParam"/>
                  </FormItem>
                </i-col>
                <i-col span="9" v-if="hasArrValue([4,6], formData.ruleType)">
                  <FormItem prop="destParams">
                    <i-select v-model="formData.destParams" :value="formData.destParams" @on-change="onChangeDestParams"
                              placeholder="需要查询的标签或维度" trigger="hover" filterable clearable>
                      <i-option v-for="item in formDataInit.destParams" :value="item.value" :key="item.value">{{item.label}}</i-option>
                    </i-select>
                  </FormItem>
                </i-col>
                <i-col span="9" v-if="hasArrValue([16], formData.ruleType)&&!hasArrValue([25,26], formData.tool)">
                  <FormItem prop="quotaId">
                    <Input v-model="formData.quotaId" placeholder="指标ID" clearable/>
                  </FormItem>
                </i-col>
                <i-col span="9" v-if="hasArrValue([25], formData.tool)">
                  <FormItem prop="longitude">
                    <Cascader v-model="distance.longitude" :data="formDataInit.left" :value="formData.left"
                              placeholder="经度" trigger="hover" filterable style="margin-bottom: 10px"
                              @on-change="onChangedistanceLeftParam"/>
                  </FormItem>
                  <FormItem prop="latitude">
                    <Cascader v-model="distance.latitude" :data="formDataInit.left" :value="formData.left"
                              placeholder="纬度" trigger="hover" filterable style="margin-bottom: 10px"
                              @on-change="onChangedistanceLeftParam"/>
                  </FormItem>
                  <FormItem prop="userId">
                    <Cascader v-model="distance.userId" :data="formDataInit.left" :value="formData.left"
                              placeholder="userId" trigger="hover" filterable
                              @on-change="onChangedistanceLeftParam"/>
                  </FormItem>
                </i-col>
                <i-col span="9" v-if="hasArrValue([26], formData.tool)">
                  <FormItem prop="userId">
                    <Cascader v-model="distance.userId" :data="formDataInit.left" :value="formData.left"
                              placeholder="userId" trigger="hover" filterable style="margin-bottom: 10px"
                              @on-change="onChangedistanceLeftParam"/>
                  </FormItem>
                </i-col>
                <i-col span="1" style="text-align:center" v-if="!hasArrValue([2,14,15], formData.ruleType)&&!(hasArrValue([16], formData.ruleType)&&hasArrValue([23], formData.tool))">-</i-col>
                <i-col span="3">
                  <FormItem prop="op" v-if="!hasArrValue([15], formData.ruleType)&&!(hasArrValue([16], formData.ruleType)&&hasArrValue([23], formData.tool))">
                    <i-select v-model="formData.op" placeholder="操作符">
                      <i-option v-for="item in formDataInit.op" :value="item.value" :key="item.value">{{item.label}}</i-option>
                    </i-select>
                  </FormItem>
                  <FormItem prop="op" v-if="hasArrValue([15], formData.ruleType)">
                    <i-select v-model="formData.op" placeholder="认证方式">
                      <i-option v-for="item in formDataInit.authType" :value="item.value" :key="item.value">{{item.label}}</i-option>
                    </i-select>
                  </FormItem>
                </i-col>
                <i-col span="1" style="text-align:center" v-if="!hasArrValue(['FIELD_EXISTS','FIELD_NOT_EXISTS'], formData.op)&&!(hasArrValue([16], formData.ruleType)&&hasArrValue([23], formData.tool))">-</i-col>
                <i-col span="8">
                  <FormItem prop="right" v-if="!hasArrValue([2,15], formData.ruleType) && !hasArrValue(['FIELD_EQUAL','FIELD_NOT_EQUAL','FIELD_EXISTS','FIELD_NOT_EXISTS','FIELD_INCLUDE','FIELD_NOT_INCLUDE'], formData.op)
                  &&!(hasArrValue([16], formData.ruleType)&&hasArrValue([23], formData.tool))">
                    <Input v-model="formData.right" placeholder="右操作数" type="textarea" clearable/>
                  </FormItem>
                  <FormItem prop="right" v-if="hasArrValue([15], formData.ruleType)">
                    <i-select v-model="formData.right" placeholder="认证状态">
                      <i-option v-for="item in formDataInit.authState" :value="item.value" :key="item.value">{{item.label}}</i-option>
                    </i-select>
                  </FormItem>
                  <FormItem prop="right" v-if="hasArrValue([2], formData.ruleType)">
                    <i-select v-model="formData.right" placeholder="右操作数">
                      <i-option v-for="item in formDataInit.booleanValue" :value="item.value" :key="item.value">{{item.label}}</i-option>
                    </i-select>
                  </FormItem>
                  <FormItem prop="right" v-if="hasArrValue(['FIELD_EQUAL','FIELD_NOT_EQUAL','FIELD_INCLUDE','FIELD_NOT_INCLUDE'], formData.op)">
                    <Cascader v-model="formData.rightParam" :data="formDataInit.rightParam"
                              placeholder="右操作数" trigger="hover" filterable
                              @on-change="onChangeRightParam"/>
                  </FormItem>
                </i-col>
                <i-col span="1">
                  <Poptip word-wrap width="800" trigger="hover" title="操作符说明" content="* 示例格式: 数据 -> 右操作数 = 结果
=
{a:abcXYZ} -> 'abcXYZ' = true
{a:}       -> ''       = true
{a:3}        -> 3        = true
>(支持相同日期格式比较, >=功能相同)
{a:5} -> 6 = false
{a:2010-01-02 00:00:00:00} -> '2010-01-01 00:00:00:00' = true
<(支持相同日期格式比较, <=功能相同)
{a:5} -> 6 = true
{a:2010-01-01 00:00:00:00} -> '2010-01-01 23:59:00:00' = true
以开头                                              以结尾
{a:abcXYZ} -> def,XYZ = true           {a:abcXYZ} -> def,XYZ = true
{a:abcXYZ} -> def,xyz = false           {a:abcXYZ} -> def,xyz = false
等于空对象
{a:{a1:xxx}} -> {}  = false
{a:{}}           -> {}  = true
{a:{a1:xxx}} -> !{} = true
{a:{}}           -> !{} = false
等于字符串
{a:abc} -> abc = true
{a:abc} -> ABC = false
字段不相等                           字段相等
{a:abc,b:abd} -> b = true       {a:abc,b:abc} -> b = true
{a:abc,b:abc} -> b = false      {a:abc,b:abd} -> b = false
字段存在                               字段不存在
{a:abcXYZ} -> a = true          {a:abcXYZ} -> a = false
{b:abcXYZ} -> a = false         {b:abcXYZ} -> a = true
包含                                       不包含
{a:abcd} -> ab,cd  = true        {a:abcd} -> ab,cd  = false
{a:abc}  -> d,abc  = true         {a:abc}  -> d,abc  = false
{a:abc}  -> d,ef   = false          {a:abc}  -> d,ef   = true
数组交集
{a:abc,def} -> abc,hij = true
{a:abc,def} -> opq,hij = false
字段长度大于等于                  字段长度小于等于
{a:abc} -> 2 = true                   {a:abc} -> 2 = false
{a:abc} -> 3 = true                   {a:abc} -> 3 = true
{a:abc} -> 4 = false                  {a:abc} -> 4 = true
">
                    <Icon type="md-help-circle" size="50"/>
                  </Poptip>
                </i-col>
              </Row>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10" v-if="hasArrValue([15], formData.ruleType)">
          <i-col span="24">
            <FormItem>
              <Checkbox v-model="formData.authDatetimeSwitch">校验认证时间</Checkbox>
            </FormItem>
            <FormItem prop="authDatetimeRange" v-if="formData.authDatetimeSwitch">
              <DatePicker v-model="formData.authDatetimeRange" :options="dateOpts" type="datetimerange" format="yyyy-MM-dd HH:mm:ss" @on-change="changeDate" style="width:100%" placeholder="认证时间范围"></DatePicker>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="24">
            <FormItem label="描述" prop="description">
              <Input v-model="formData.description" type="textarea" />
            </FormItem>
          </i-col>
        </Row>
      </Form>
      <div slot="footer">
        <Button type="primary" size="large" @click="onClickCheckExpression">表达式校验工具</Button>
        <Button type="primary" size="large" @click="handleSubmit('form_modal_add_edit_rule_id')">提交</Button>
      </div>
    </Modal>
    <CheckExpression v-model="checkExpressionModalConfig.show" @onAfterCommit="onCheckExpressionAfterCommit"/>
  </div>
</template>

<script>
import Common from '_v/strategy-engine/common'
import CommonTools from '@/libs/common-tools'
import LogUtil from '@/libs/log'
import Message from '@/libs/message'
import RuleService from '@/api/strategy-engine/rule'
import ParamService from '@/api/strategy-engine/param'
import ToolService from '@/api/strategy-engine/tool'

import CheckExpression from '_v/strategy-engine/common/check-expression'

export default {
  components: { CheckExpression },
  name: 'RuleAddOrEdit',
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
      checkExpressionModalConfig: {
        show: false
      },
      formDataInit: {
        left: [],
        op: Common.getOp(),
        rightParam: [],
        state: Common.getState(),
        ruleType: [],
        tool: [],
        srcParam: [],
        destParams: [],
        authType: Common.getAuthType(),
        authState: Common.getAuthState(),
        booleanValue: Common.getBooleanValue()
      },
      formData: {
        id: this.id,
        sceneKey: '',
        name: '',
        left: [],
        op: '',
        right: '',
        rightParam: '',
        ruleType: 1,
        description: '',
        state: true,
        tool: '',
        srcParam: [],
        destParams: [],
        authDatetimeSwitch: false,
        authDatetimeRange: '',
        authStartTime: '',
        authEndTime: '',
        quotaId: ''
      },
      distance: {
        longitude: '', // 经度
        latitude: '', // 纬度
        userId: ''
      },
      extParamMap: new Map(),
      ruleValidate: {
        name: [
          { required: true, message: '名称为空', trigger: 'blur' },
          { type: 'string', message: '不能超过100个字符', max: 100, trigger: 'change' },
          { type: 'string', message: '不能包含~!@#$%^&*()+=\\/?,.\',;\\"等符号', pattern: /^[^\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\+\\=\\\\\\/\\?\\,\\.]*$/, trigger: 'blur' },
          { type: 'string', message: '不能以数字开头且不能包含空格', pattern: /^[a-zA-Z_\u4e00-\u9fa5][a-zA-Z_0-9\u4e00-\u9fa5]*$/, trigger: 'blur' },
          { validator: this.validNameExist, trigger: 'blur' }
        ],
        ruleType: [{
          required: true, message: '左操作数类型为空'
        }],
        tool: [{
          required: true, message: '数据源为空'
        }],
        srcParam: [{
          required: true, message: '查询参数为空'
        }],
        destParams: [{
          required: true, message: '标签为空'
        }],
        express: [{
          required: true, message: ' ', validator: this.valid
        }],
        left: [{
          required: true, message: '左操作数为空'
        }],
        op: [{
          required: true, message: '操作符为空'
        }],
        right: [{
          required: true, message: '右操作数为空'
        }],
        description: [{
          required: true, message: '描述为空'
        }],
        authDatetimeRange: [{
          validator: this.validDateNull, trigger: 'blur'
        }],
        quotaId: [{
          required: true, message: '指标ID为空'
        }]
      },
      dateOpts: Common.getDateOpts()
    }
  },
  created: function () {},
  mounted () {},
  watch: {
    id (id) {
      this.formData.id = id
    }
  },
  methods: {
    validDateNull (rule, value, callback) {
      if (value[0] + value[1] === '') {
        let msg = '认证时间范围不能为空'
        callback(msg)
      } else {
        callback()
      }
    },
    onChangeSrcParam (value, selectedData) {
      this.formData.srcParam = [this.parentData.sceneKey, parseInt(value[1])]
    },
    onChangeDestParams (value, selectedData) {
      this.formData.destParams = [value]
    },
    onChangeLeftParam (value, selectedData) {
      console.log(this.parentData)
      console.log(value)
      this.formData.left = [this.parentData.sceneKey, parseInt(value[1])]
    },
    onChangedistanceLeftParam (value, selectedData) {
      var key = selectedData[1].value
      var label = selectedData[1].label.split('(')[0]
      this.extParamMap.set(key, label)
    },
    onChangeRightParam (value, selectedData) {
      console.log('onChangeRightParam', value[1])
      this.formData.right = value[1]
    },
    onVisibleChange (isVisible) {
      if (!isVisible) {
        this.clearForm()
        return
      }

      this.formDataInit.ruleType = Common.getRuleType(this.parentData.type)

      this.config.pageType = this.parentData.pageType
      this.formData.sceneKey = this.parentData.sceneKey

      this.initSrcParam(this.formData.sceneKey)
      this.initRightParam(this.formData.sceneKey)

      if (CommonTools.isEditPage(this.config.pageType)) {
        RuleService.getById(this.formData.id).then(res => {
          let data = res.data.data
          console.log('getById', data)
          this.formData.id = data.id
          this.formData.name = data.name
          this.formData.ruleType = data.type
          this.initLeft(this.formData.sceneKey, this.formData.ruleType)
          this.formData.tool = data.toolId
          this.formData.description = data.description
          this.formData.left = [this.formData.sceneKey, data.ruleExpressLeft]
          if (data.toolId === 25) {
            const extParam = JSON.parse(data.extParam)
            this.distance.longitude = [this.formData.sceneKey, extParam.longitude_id]
            this.distance.latitude = [this.formData.sceneKey, extParam.latitude_id]
            this.distance.userId = [this.formData.sceneKey, extParam.userId_id]
          }
          if (data.toolId === 26) {
            const extParam = JSON.parse(data.extParam)
            this.distance.userId = [this.formData.sceneKey, extParam.userId_id]
          }

          this.initTool(this.formData.ruleType)
          if (!CommonTools.isNull(this.formData.ruleType)) {
            this.initDestParam(this.formData.tool)
          }
          if (Common.isFeature(this.formData.ruleType)) {
            this.formData.srcParam = [this.formData.sceneKey, data.srcParamId]
            this.formData.destParams = [CommonTools.isNull(data.destParamIds) ? 1 : data.destParamIds[0]]
            if (!CommonTools.isNull(data.extParam)) {
              this.formData.authDatetimeSwitch = true
              let dataTime = data.extParam.split('_')
              this.formData.authDatetimeRange = [dataTime[0], dataTime[1]]
            }
          }

          if (this.hasArrValue([16], this.formData.ruleType)) {
            this.formData.quotaId = data.extParam
          }

          this.formData.op = data.ruleExpressOp
          if (this.formData.op === 'FIELD_EQUAL' || this.formData.op === 'FIELD_NOT_EQUAL' || this.formData.op === 'FIELD_INCLUDE') {
            this.formData.rightParam = [this.formData.sceneKey, data.ruleExpressRight]
            this.formData.right = data.ruleExpressRight
          } else {
            this.formData.right = data.ruleExpressRight
          }
          this.formData.description = data.description
          this.formData.state = data.state !== 0

          LogUtil.json2str('formData', this.formData)
        })
      } else {
        this.initLeft(this.formData.sceneKey, this.parentData.type)
        this.formData.ruleType = this.parentData.type
      }
    },
    validNameExist (rule, value, callback) {
      if (CommonTools.isEditPage(this.config.pageType)) {
        callback()
      } else {
        RuleService.validExist(this.formData.sceneKey, value).then(res => {
          let backendData = res.data.data
          if (backendData) {
            let error = '名称重复'
            callback(error)
          } else {
            callback()
          }
        })
      }
    },
    onChangeRuleType (ruleType) {
      // 清空已填配置
      this.formData.srcParam = []
      this.formData.destParams = []
      this.formData.tool = ''
      this.formData.left = []
      this.formData.right = ''
      this.distance = {
        longitude: '', // 经度
        latitude: '', // 纬度
        dateTime: '',
        userId: ''
      }

      if (Common.isCalculationFeature(ruleType)) {
        this.initTool(ruleType)
      }

      if (Common.isFeature(ruleType)) {
        this.initTool(ruleType)
        this.initDestParam(this.formData.tool)
      } else if (Common.isBaseFieldOrStatistics(ruleType)) {
        this.initLeft(this.formData.sceneKey, ruleType)
      }
    },
    onChangeTool (tool) {
      this.initDestParam(tool)
    },
    initTool (ruleType) {
      let reqData = {
        type: ruleType
      }
      ToolService.getSelectData(reqData).then(res => {
        this.formDataInit.tool = res
      })
    },
    initLeft (sceneKey, ruleType) {
      RuleService.getCascaderDataGroupByRuleType(sceneKey, ruleType).then(res => {
        this.formDataInit.left = res.data.data
      })
    },
    initSrcParam (sceneKey) {
      ParamService.getCascaderDataGroupParamIdBySceneKey(this.formData.sceneKey, true).then(res => {
        this.formDataInit.srcParam = res.data.data
      })
    },
    initRightParam (sceneKey) {
      ParamService.getCascaderDataGroupParamKeyBySceneKey(this.formData.sceneKey, true).then(res => {
        this.formDataInit.rightParam = res.data.data
      })
    },
    initDestParam (tool) {
      this.formData.destParams = []
      if (!CommonTools.isNull(this.formData.tool)) {
        ToolService.getToolFieldCascaderDataGroupByTool(tool, false).then(res => {
          this.formDataInit.destParams = res.data.data
        })
      }
    },
    valid (rule, value, callback) {
      callback()
    },
    clearForm () {
      this.$refs['form_modal_add_edit_rule_id'].resetFields()
    },
    onClickCheckExpression () {
      this.checkExpressionModalConfig.show = true
    },
    onCheckExpressionAfterCommit () {
      this.checkExpressionModalConfig.show = false
    },
    handleSubmit (name) {
      LogUtil.log('handleSubmit(): ' + name)
      this.$refs[name].validate((valid) => {
        if (valid) {
          let param = []
          for (let i = 0; i < this.formData.destParams.length; i++) {
            param.push(this.formData.destParams[0])
          }

          let reqData = {
            id: this.formData.id,
            name: this.formData.name,
            type: this.formData.ruleType,
            sceneKey: this.formData.sceneKey,
            toolId: this.formData.tool,
            srcParamId: this.formData.srcParam[1],
            destParamIds: param,
            featureId: this.formData.left[1],
            ruleExpressLeft: this.formData.left[1],
            ruleExpressOp: this.formData.op,
            ruleExpressRight: this.formData.right,
            description: this.formData.description,
            extParam: this.formData.authDatetimeSwitch ? this.formData.authStartTime + '_' + this.formData.authEndTime : '-1_-1'
          }

          if (Common.isCalculationFeature(this.formData.ruleType)) {
            if (this.formData.tool === 23) {
              reqData.ruleExpressOp = '=='
              reqData.ruleExpressRight = '1'
            }
            reqData.extParam = this.formData.quotaId
            if (this.formData.tool === 25) {
              var extParam = {
                longitude: this.extParamMap.get(this.distance.longitude[1]), // 经度
                latitude: this.extParamMap.get(this.distance.longitude[1]), // 纬度
                userId: this.extParamMap.get(this.distance.longitude[1]),
                longitude_id: this.distance.longitude[1],
                latitude_id: this.distance.latitude[1],
                userId_id: this.distance.userId[1]
              }
              reqData.extParam = JSON.stringify(extParam)
            }
            if (this.formData.tool === 26) {
              var extParam1 = {
                userId: this.extParamMap.get(this.distance.userId[1]),
                userId_id: this.distance.userId[1]
              }
              reqData.extParam = JSON.stringify(extParam1)
            }
          }

          if (CommonTools.isEditPage(this.config.pageType)) {
            reqData.id = this.formData.id
            reqData.state = 2
            RuleService.update(reqData).then(res => {
              if (res.data.code === 200) {
                Message.message('更新成功', 'success', this)
                this.$emit('onAfterCommit', res)
              } else {
                Message.message('更新失败', 'error', this, res.data.data)
              }
            })
          } else {
            reqData.state = 2
            RuleService.add(reqData).then(res => {
              Message.message('新建成功', 'success', this)
              this.$emit('onAfterCommit', res)
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
    hasArrValue (arr, key) {
      return arr.indexOf(key) >= 0
    },
    changeDate (date) {
      this.formData.authStartTime = date[0]
      this.formData.authEndTime = date[1]
    }
  }
}
</script>

<style scoped>

</style>
