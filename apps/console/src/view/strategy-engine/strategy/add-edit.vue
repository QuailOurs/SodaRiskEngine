<template>
  <div>
    <Modal :title="(config.pageType==='edit' ? '修改规则' : '新建规则') + formData.id" fullscreen
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="modal_add_edit_strategy_form_id" :model="formData" :rules="ruleValidate" :label-width="80">
        <Row :gutter="10">
          <i-col>
            <FormItem label="名称" prop="name">
              <Input v-model="formData.name" placeholder="名称" clearable/>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="6">
            <FormItem v-if="config.pageType==='add'" label="业务方" prop="businessSideKey">
              <Select v-model="formData.businessSideKey" placeholder="业务方" @on-change="initScene">
                <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
            <FormItem v-if="config.pageType==='edit'" label="业务方" prop="businessSideKey">
              <Select v-model="formData.businessSideKey" placeholder="业务方" @on-change="initScene" disabled>
                <i-option v-for="item in formDataInit.businessSide" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem v-if="config.pageType==='add'" label="场景" prop="sceneKey">
              <Select v-model="formData.sceneKey" placeholder="场景" @on-change="onChangeScene">
                <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
            <FormItem v-if="config.pageType==='edit'" label="场景" prop="sceneKey">
              <Select v-model="formData.sceneKey" placeholder="场景" @on-change="onChangeScene" disabled>
                <i-option v-for="item in formDataInit.scene" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem v-if="config.pageType==='add'" label="类型" prop="type">
              <Select v-model="formData.type" placeholder="类型" @on-change="onChangeType">
                <i-option v-for="item in formDataInit.type" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
            <FormItem v-if="config.pageType==='edit'" label="类型" prop="type">
              <Select v-model="formData.type" placeholder="类型" @on-change="onChangeType" disabled>
                <i-option v-for="item in formDataInit.type" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem label="状态" prop="state">
              <Select v-model="formData.state" placeholder="状态">
                <i-option v-for="item in formDataInit.state" :value="item.value" :key="item.value">{{item.label}}</i-option>
              </Select>
            </FormItem>
          </i-col>
        </Row>
        <Row :gutter="10">
          <i-col span="3">
            <FormItem label="优先级" prop="priority">
              <InputNumber v-model="formData.priority" :max="100" :min="1" :step="10" :precision="0" placeholder="1-100"></InputNumber>
            </FormItem>
          </i-col>
          <i-col span="3">
            <FormItem label="命中阀值" prop="threshold">
              <InputNumber v-model="formData.threshold" :min="0" :step="1000" :precision="0" placeholder="0表示无上限"></InputNumber>
            </FormItem>
          </i-col>
          <i-col span="6">
            <FormItem label="分值" prop="score">
              <InputNumber v-model="formData.score" :max="100" :min="0" :step="10" :precision="2" placeholder="0-100"></InputNumber>
            </FormItem>
          </i-col>
          <i-col span="3">
            <FormItem label="返回码" prop="returnCode">
              <Input v-model="formData.returnCode"/>
            </FormItem>
          </i-col>
          <i-col span="3">
            <RadioGroup v-model="formData.returnCodeType" type="button" @on-change="onChangeReturnCodeType">
              <Radio label="0" border>自定义</Radio>
              <Radio label="1" border>规则ID</Radio>
            </RadioGroup>
          </i-col>
          <i-col span="6">
            <FormItem label="能力来源" prop="abilitySource">
              <Input v-model="formData.abilitySource"/>
            </FormItem>
          </i-col>
        </Row>
        <Row>
          <i-col span="8">
            <FormItem label="条件关系" prop="expressionRelation">
              <RadioGroup v-model="formData.expressionRelation" @on-change="onChangeExpressionRelation">
                <Radio label="&&">条件与</Radio>
                <Radio label="||">条件或</Radio>
                <Poptip trigger="hover" title="" content="&&代表与, ||代表或, (1&&2)||3, 代表条件1条件2同时满足, 或满足条件3时会命中策略">
                  <Radio label="@@">自定义</Radio>
                </Poptip>
              </RadioGroup>
            </FormItem>
          </i-col>
        </Row>
        <Row v-if="formData.expressionRelation === this.config.expressionRelationCustom">
          <i-col span="24">
            <FormItem prop="expression">
              <Input v-model="formData.expression" :rows="1" type="textarea"/>
            </FormItem>
          </i-col>
        </Row>
        <Row>
          <i-col span="24">
            <FormItem label="已选条件">
              <Tables ref="tables" v-model="formData.selected.rules" :columns="tableConfig.columnsSelected" size="small" :border="true" stripe :loading="tableConfig.loading"
                      @on-select="onSelectionSelectedRule"
                      @on-select-all-cancel="onSelectAllCancelSelectedRule"/>
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
        <Row>
          <i-col span="24">
            <FormItem label="条件" prop="rule">
              <Button type="primary" size="small" @click="onClickAddRule">新增条件</Button>
              <Row :gutter="10">
                <i-col span="1">
                  <Input v-model="tableConfig.filter.id" placeholder="ID" @on-enter="onEnterFilter" clearable/>
                </i-col>
                <i-col span="2">
                  <i-select v-model="tableConfig.filter.ruleType" placeholder="条件类型" @on-change="onEnterFilter" clearable>
                    <i-option v-for="item in formDataInit.ruleType" :value="item.value" :key="item.value">{{item.label}}</i-option>
                  </i-select>
                </i-col>
                <i-col span="6">
                  <Input v-model="tableConfig.filter.name" placeholder="条件名称" @on-enter="onEnterFilterName" clearable/>
                </i-col>
                <i-col span="6">
                  <Input v-model="tableConfig.filter.expression" placeholder="表达式" @on-enter="onEnterFilterExpression" clearable/>
                </i-col>
                <i-col span="6">
                  <Input v-model="tableConfig.filter.description" placeholder="描述" @on-enter="onEnterFilterDescription" clearable/>
                </i-col>
              </Row>
              <Row>
                <i-col span="24">
                  <RuleTables ref="tables" size="small" v-model="tableConfig.data.records" :columns="tableConfig.columns" stripe :border="true"
                              :pageTotal="tableConfig.data.total" :pageSize="tableConfig.data.pageSize" :pageCurrent="tableConfig.data.current" :loading="tableConfig.loading"
                              @on-page-change="changePage"
                              @on-page-size-change="pageSizeChange"
                              @on-select="onSelectionRule"
                              @on-select-cancel="onSelectionCancelRule"
                              @on-select-all="onSelectAllRule"
                              @on-select-all-cancel="onSelectAllCancelRule"
                              @on-selection-change="onSelectionChangeRule"/>
                </i-col>
              </Row>
            </FormItem>
          </i-col>
        </Row>
      </Form>
      <div slot="footer">
        <Button type="primary" size="large" @click="onClickCheckExpression">表达式校验工具</Button>
        <Button type="primary" size="large" @click="handleSubmit('modal_add_edit_strategy_form_id')">提交</Button>
      </div>
    </Modal>
    <RuleAddOrEdit v-model="ruleAddOrEditModalConfig.show" :id="ruleAddOrEditModalConfig.curId"
                   :parentData="ruleAddOrEditModalConfig.data" @onAfterCommit="onRuleAddOrEditAfterCommit"/>
    <CheckExpression v-model="checkExpressionModalConfig.show" @onAfterCommit="onCheckExpressionAfterCommit"/>
    <Modal v-model="ruleDependModalConfig.show" title="规则依赖详情" :width="800">
      <Tables ref="tablesRuleDepend" v-model="ruleDependModalConfig.data" :columns="ruleDependModalConfig.columns" size="small" :border="true" stripe/>
    </Modal>
  </div>
</template>

<script>
import Common from '_v/strategy-engine/common'
import CommonTools from '@/libs/common-tools'
import LogUtil from '@/libs/log'
import Message from '@/libs/message'
import RuleTables from '_c/tables-common/tables-v2'
import Tables from '_c/tables'
import RuleService from '@/api/strategy-engine/rule'
import StrategyService from '@/api/strategy-engine/strategy'
import BusinessSideService from '@/api/strategy-engine/businessside'
import SceneService from '@/api/strategy-engine/scene'

import RuleAddOrEdit from '_v/strategy-engine/rule/add-edit'
import CheckExpression from '_v/strategy-engine/common/check-expression'

export default {
  components: {
    RuleTables, Tables, RuleAddOrEdit, CheckExpression
  },
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
        pageType: 'edit',
        expressionRelationCustom: '@@'
      },
      checkExpressionModalConfig: {
        show: false
      },
      ruleAddOrEditModalConfig: {
        curId: '',
        show: false,
        data: {
          type: 1,
          row: {},
          sceneKey: '',
          pageType: 'edit'
        }
      },
      ruleDependModalConfig: {
        show: false,
        data: [],
        columns: [
          { title: '规则ID', key: 'id', minWidth: 100, width: 100, tooltip: true },
          { title: '规则名称', key: 'name', minWidth: 100, width: 100, tooltip: true },
          { title: '状态', key: 'stateName', minWidth: 50, width: 80 },
          { title: '表达式', key: 'expressionView', minWidth: 100, resizable: true, tooltip: true }
        ]
      },
      tableConfig: {
        loading: false,
        filterStr: '',
        filter: {
          id: '',
          ruleType: '',
          name: '',
          expression: '',
          description: ''
        },
        columns: this.getColumns(),
        columnsSelected: this.getColumns('selected'),
        data: {
          current: 1,
          pageSize: 10,
          total: 1,
          records: []
        }
      },
      formDataInit: {
        businessSide: [],
        scene: [],
        ruleType: [],
        type: Common.getStrategyType(),
        state: [],
        priority: 50,
        threshold: 0,
        score: 0
      },
      formData: {
        id: this.id,
        name: '',
        businessSideKey: '',
        sceneKey: '',
        type: 0,
        priority: 50,
        threshold: 0,
        score: 0,
        returnCode: '',
        abilitySource: '',
        description: '',
        state: 1,
        stateOld: '',
        expression: '',
        expressionRelation: '&&',
        curRuleIds: [], // 修改策略时记录之前已关联的规则ID
        selected: {
          ruleIds: [],
          ruleNames: [],
          rules: []
        },
        returnCodeType: '0'
      },
      ruleValidate: {
        name: [{
          required: true, message: '名称为空', trigger: 'blur'
        }],
        businessSideKey: [{
          required: true, message: '请选择业务方', trigger: 'blur'
        }],
        sceneKey: [{
          required: true, message: '请选择场景', trigger: 'blur'
        }],
        state: [{
          required: true, message: '请选择上线状态'
        }],
        type: [{
          required: true, message: '请选择类型'
        }],
        priority: [{
          required: true, message: '优先级为空'
        }],
        threshold: [{
          required: true, message: '命中阀值为空'
        }],
        score: [{
          required: true, message: '分值为空'
        }],
        description: [{
          required: true, message: '描述为空', trigger: 'blur'
        }],
        expressionRelation: [{
          required: true, message: '条件关系为空', trigger: 'blur'
        }],
        expression: [{
          required: true, message: '自定义条件关系为空', trigger: 'blur'
        }],
        rule: [{
          validator: this.validRule, trigger: 'blur'
        }]
      }
    }
  },
  created: function () {},
  mounted () {
    this.initBusinessSide()
  },
  computed: {},
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
        this.formData.selected.ruleNames = []
        this.formData.selected.ruleIds = []
        this.formData.selected.rules = []
        this.formData.curRuleIds = []
        this.tableConfig.data.records = []

        this.tableConfig.filter.id = ''
        this.tableConfig.filter.ruleType = ''
        this.tableConfig.filter.name = ''
        this.tableConfig.filter.expression = ''
        this.tableConfig.filter.description = ''
        this.clearForm()
        // 返回码类型复位
        this.formData.returnCodeType = '0'
        return
      }

      this.formDataInit.ruleType = Common.getRuleType(this.parentData.type)
      this.formDataInit.state = Common.getStrategyState(this.parentData.type)
      this.config.pageType = this.parentData.pageType

      if (CommonTools.isEditPage(this.config.pageType)) {
        StrategyService.getById(this.formData.id).then(res => {
          let data = res.data.data
          this.formData.id = data.id
          this.formData.name = data.name
          this.formData.businessSideKey = data.businessSideKey
          this.initScene(this.formData.businessSideKey)
          this.formData.sceneKey = data.sceneKey
          this.formData.type = data.type
          this.formData.priority = data.priority
          this.formData.threshold = data.threshold
          this.formData.score = data.score
          this.formData.returnCode = data.returnCode
          this.formData.abilitySource = data.abilitySource
          this.formData.expressionRelation = data.expressionRelation
          this.formData.expression = data.expression
          this.formData.description = data.description
          this.formData.state = data.state
          this.formData.stateOld = data.stateName
          this.formData.selected.ruleNames = this.generateRuleNames(data.rules)
          this.formData.selected.ruleIds = data.ruleIds
          this.formData.selected.rules = []
          this.formData.curRuleIds = data.ruleIds
          this.ruleAddOrEditModalConfig.data.sceneKey = data.sceneKey

          // 初始化已选条件
          RuleService.getListByStrategyId(this.formData.id).then(res => {
            let resultData = res.data.data
            let rules = CommonTools.isArrayNull(resultData) ? [] : resultData
            rules.forEach(item => {
              this.addSelectedRule(item)
            })
          })
          this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
        })
      } else {
        this.resetRuleTable()
        this.clearForm()
      }
    },
    addSelectedRule (rule) {
      if (!(this.formData.selected.ruleIds.indexOf(rule.id) > -1)) {
        this.formData.selected.ruleIds.push(rule.id)
      }
      if (!(this.formData.selected.ruleNames.indexOf(rule.name) > -1)) {
        this.formData.selected.ruleNames.push(rule.name)
      }

      let isExists = this.formData.selected.rules.some(selectedItem => {
        if (selectedItem.id === rule.id) {
          return true
        }
      })

      if (!isExists) {
        this.formData.selected.rules.push(rule)
      }
    },
    clearSelectedRule () {
      this.formData.selected.ruleIds = []
      this.formData.selected.ruleNames = []
      this.formData.selected.rules = []
    },
    validRule (rule, value, callback) {
      if (this.formData.selected.ruleIds.length === 0) {
        let error = '请选择条件'
        callback(error)
      } else {
        callback()
      }
    },
    onChangeExpressionRelation (expressionRelation) {
      if (this.formData.selected.ruleIds.length === 0) {
        Message.message('请选择条件', 'error', this)
      }
    },
    generateRuleNames (rules) {
      let ruleNames = []
      rules.forEach(rule => {
        ruleNames.push(rule.name)
      })
      return ruleNames
    },
    generateExpression () {
      if (this.formData.expressionRelation !== this.config.expressionRelationCustom) {
        this.formData.expression = this.formData.selected.ruleNames.join(this.formData.expressionRelation)
      }
    },
    onSelectionRule (selection, row) {
      this.addSelectedRule(row)
    },
    onSelectionCancelRule (selection, row) {
      CommonTools.arrayRemove(this.formData.selected.ruleIds, row.id)
      CommonTools.arrayRemove(this.formData.selected.ruleNames, row.name)
      CommonTools.arrayRemoveObjById(this.formData.selected.rules, row.id)
    },
    onSelectAllRule (selection) {
      selection.forEach(item => {
        this.onSelectionRule(selection, item)
      })
    },
    onSelectAllCancelRule (selection) {
      this.tableConfig.data.records.forEach(item => {
        this.onSelectionCancelRule(selection, item)
      })
    },
    onSelectionChangeRule (selection) {

    },
    onSelectionSelectedRule (selection, row) {
      this.onSelectionCancelRule(selection, row)
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
    },
    onSelectAllCancelSelectedRule (selection) {
      this.formData.selected.ruleIds = []
      this.formData.selected.ruleNames = []
      this.formData.selected.rules = []
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
    },
    onChangeType (type) {
      // 更新状态选项
      this.formDataInit.state = Common.getStrategyState(type)
      this.onChangeScene(this.formData.sceneKey)
      this.clearSelectedRule()
      this.formDataInit.ruleType = Common.getRuleType(type)

      // 加载条件
      if (CommonTools.isEditPage(this.config.pageType)) {
        this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
      } else {
        this.refreshRuleList(this.formData.sceneKey, this.formData.type, null)
      }
    },
    onChangeScene (sceneKey) {
      if (CommonTools.isNull(sceneKey)) {
        return
      }

      this.ruleAddOrEditModalConfig.data.sceneKey = sceneKey
    },
    onClickAddRule () {
      if (CommonTools.isNull(this.formData.sceneKey)) {
        Message.message('新增条件前请选择场景', 'error', this)
        return
      }

      this.ruleAddOrEditModalConfig.show = true
      this.ruleAddOrEditModalConfig.data.curId = ''
      this.ruleAddOrEditModalConfig.data.type = this.formData.type
      this.ruleAddOrEditModalConfig.data.pageType = 'add'
    },
    onRuleAddOrEditAfterCommit () {
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
      // 刷新已选, 防止修改导致的与未选不一致
      RuleService.getListByRuleIds(this.formData.selected.ruleIds).then(res => {
        this.formData.selected.rules = res.data.data
      })
      this.ruleAddOrEditModalConfig.show = false
    },
    clearForm () {
      this.$refs['modal_add_edit_strategy_form_id'].resetFields()
    },
    handleSubmit (name) {
      LogUtil.log('handleSubmit(): ' + name, this.formData)
      this.$refs[name].validate((valid) => {
        if (valid) {
          // 根据选择的条件与条件关系生成表达式
          this.generateExpression()

          // 策略状态(1下线状态规则修改后，状态仍为下线状态；2预上线规则修改后，状态仍为预上线；3上线状态规则修改后，状态变更为预上线；)
          if (this.formData.stateOld === '上线') {
            if (this.formData.type === 1) { // 普通规则置为预上线
              this.formData.state = '1'
              Message.message('已上线规则, 修改提交后重置为预上线状态, 请在列表页修改为上线', 'success', this)
            } else { // 其它置为下线
              this.formData.state = '2'
            }
          }

          let reqData = {
            name: this.formData.name,
            businessSideKey: this.formData.businessSideKey,
            sceneKey: this.formData.sceneKey,
            type: this.formData.type,
            priority: this.formData.priority,
            threshold: this.formData.threshold,
            score: this.formData.score,
            returnCode: this.formData.returnCode,
            abilitySource: this.formData.abilitySource,
            description: this.formData.description,
            state: this.formData.state,
            expressionRelation: this.formData.expressionRelation,
            expression: this.formData.expression,
            ruleIds: this.formData.selected.ruleIds,
            returnCodeType: this.formData.returnCodeType
          }
          if (CommonTools.isEditPage(this.config.pageType)) {
            reqData.id = this.formData.id
            StrategyService.update(reqData).then(res => {
              if (res.data.code === 200) {
                Message.message('更新规则成功', 'success', this)
                this.$emit('onAfterCommit', res.data.data.id)
              } else {
                Message.message('更新规则失败' + res.data.msg, 'error', this)
              }
            })
          } else {
            StrategyService.add(reqData).then(res => {
              if (res.data.code === 200) {
                Message.message('新建规则成功', 'success', this)
                this.$emit('onAfterCommit', res.data.data.id)
              } else {
                Message.message('新建规则失败' + res.data.msg, 'error', this)
              }
            })
          }
        } else {
          if (CommonTools.isEditPage(this.config.pageType)) {
            Message.message('更新规则失败', 'error', this)
          } else {
            Message.message('新建规则失败', 'error', this)
          }
        }
      })
    },
    refreshRuleList (sceneKey, type, excludeIdArray) {
      this.tableConfig.loading = true

      // 未选择策略类型则终止加载规则
      if (type === 0) {
        return
      }

      let reqData = {
        currentPage: this.tableConfig.data.current,
        pageSize: this.tableConfig.data.pageSize,

        id: this.tableConfig.filter.id,
        type: this.tableConfig.filter.ruleType,
        name: this.tableConfig.filter.name,
        strategyType: this.formData.type,
        expression: this.tableConfig.filter.expression,
        description: this.tableConfig.filter.description,
        sceneKey: this.formData.sceneKey
      }

      RuleService.getList(reqData).then(res => {
        let data = res.data.data
        let records = data.records

        this.tableConfig.data.current = data.current
        this.tableConfig.data.pageSize = data.size
        this.tableConfig.data.total = data.total
        this.tableConfig.data.records = records === null ? [] : records

        let allRules = CommonTools.isArrayNull(records) ? [] : records
        if (CommonTools.isArrayNotNull(excludeIdArray)) {
          allRules.forEach(item => {
            if (excludeIdArray.indexOf(item.id) >= 0) {
              item._checked = true
              this.addSelectedRule(item)
            }
          })
        }

        this.tableConfig.data.records = allRules
        this.tableConfig.loading = false
      })
    },
    resetRuleTable () {
      this.tableConfig.data.records = []
    },
    changePage (value) {
      this.tableConfig.data.current = value
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
    },
    pageSizeChange (value) {
      this.tableConfig.data.pageSize = value
      this.updateTableData()
    },
    getColumns (type) {
      let config = [
        { title: 'ID', key: 'id', minWidth: 70, width: 70, sortable: true, sortType: 'desc' },
        { title: '类型',
          key: 'type',
          minWidth: 90,
          width: 90,
          render: (h, params) => {
            return h('div', [Common.getRuleTypeByIndex(params.row.type)])
          }
        },
        { title: '名称', key: 'name', width: 200, tooltip: true },
        { title: '表达式', key: 'expression', minWidth: 400, tooltip: true, resizable: true },
        { title: '描述', key: 'description', width: 200, tooltip: true }
      ]

      if (type === 'selected') {
        config.push({ type: 'selection', width: 60, fixed: 'left' })
        config.push({
          title: '操作',
          key: 'handle',
          width: 200,
          fixed: 'right',
          options: [],
          button: [
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small',
                  disabled: false
                },
                style: {
                  marginRight: '1px'
                },
                on: {
                  click: () => {
                    RuleService.getRelationStrategyByRuleId(params.row.id).then(res => {
                      this.ruleDependModalConfig.show = true
                      this.ruleDependModalConfig.data = res.data.data
                    })
                  }
                }
              }, '依赖详情')
            },
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small',
                  disabled: false
                },
                style: {
                  marginRight: '1px'
                },
                on: {
                  click: () => {
                    this.ruleAddOrEditModalConfig.curId = params.row.id
                    this.ruleAddOrEditModalConfig.data.type = params.row.type
                    this.ruleAddOrEditModalConfig.data.pageType = 'edit'
                    this.ruleAddOrEditModalConfig.show = true
                  }
                }
              }, '修改')
            }
          ]
        })
      } else {
        config.push({ type: 'selection', width: 60, fixed: 'left' })
        config.push({
          title: '操作',
          key: 'handle',
          minWidth: 130,
          width: 200,
          fixed: 'right',
          options: [],
          button: [
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small',
                  disabled: false
                },
                style: {
                  marginRight: '1px'
                },
                on: {
                  click: () => {
                    RuleService.getRelationStrategyByRuleId(params.row.id).then(res => {
                      this.ruleDependModalConfig.show = true
                      this.ruleDependModalConfig.data = res.data.data
                    })
                  }
                }
              }, '依赖详情')
            },
            (h, params, vm) => {
              return h('Button', {
                props: {
                  size: 'small',
                  disabled: false
                },
                style: {
                  marginRight: '1px'
                },
                on: {
                  click: () => {
                    this.ruleAddOrEditModalConfig.curId = params.row.id
                    this.ruleAddOrEditModalConfig.data.type = params.row.type
                    this.ruleAddOrEditModalConfig.data.pageType = 'edit'
                    this.ruleAddOrEditModalConfig.show = true
                  }
                }
              }, '修改')
            },
            (h, params, vm) => {
              return h('Poptip', {
                props: {
                  title: '确认删除?',
                  confirm: true,
                  transfer: true
                },
                on: {
                  'on-ok': () => {
                    this.ruleAddOrEditModalConfig.curId = params.row.id
                    this.ruleAddOrEditModalConfig.data.row = params.row

                    if (this.formData.curRuleIds.indexOf(this.ruleAddOrEditModalConfig.curId) >= 0) {
                      Message.message('不能删除已绑定当前策略的规则, 请先取消绑定再进行规则删除操作.', 'error', this)
                      return
                    }

                    RuleService.delete(this.ruleAddOrEditModalConfig.curId).then(res => {
                      if (res.data.code === 200) {
                        this.refreshRuleList(this.formData.sceneKey, this.formData.type, null)
                      } else {
                        Message.message('条件' + params.row.id + '与规则(' + res.data.data + ')存在关联, 无法删除!', 'error', this)
                      }
                    })
                  }
                }
              }, [
                h('Button', {
                  props: {
                    size: 'small'
                  },
                  style: {
                    marginRight: '1px'
                  }
                }, '删除')
              ])
            }
          ]
        })
      }

      return config
    },
    onEnterFilter () {
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
    },
    onEnterFilterName () {
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
    },
    onEnterFilterExpression () {
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
    },
    onEnterFilterDescription () {
      this.refreshRuleList(this.formData.sceneKey, this.formData.type, this.formData.selected.ruleIds)
    },
    initBusinessSide () {
      BusinessSideService.getList({}).then(res => {
        let respData = res.data.data
        this.formDataInit.businessSide = []
        respData.forEach(item => {
          let temp = {}
          temp.label = item.name
          temp.value = item.businessSideKey
          this.formDataInit.businessSide.push(temp)
        })
      })
    },
    initScene (businessSideKey) {
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
      })
    },
    onClickCheckExpression () {
      this.checkExpressionModalConfig.show = true
    },
    onCheckExpressionAfterCommit () {
      this.checkExpressionModalConfig.show = false
    },
    onChangeReturnCodeType (value) {
      switch (value) {
        case '0': {
          break
        }
        case '1': {
          break
        }
      }
    }
  }
}
</script>

<style scoped>

</style>
