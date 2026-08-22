<template>
  <div>
    <Modal title="新增工具补全信息"
           @on-visible-change="onVisibleChange"
           v-bind="$attrs"
           v-on="$listeners">
      <Form ref="add" :model="formData" :rules="ruleValidate" :label-width="80">
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
                <Input v-model="formData.complementKey" placeholder="补全key，不能输入中文"/>
            </FormItem>
          </i-col>
        </Row>
         <Row :gutter="10">
          <i-col span="24">
            <FormItem label="备注" prop="description">
                <Input v-model="formData.description" placeholder="描述"/>
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
import ComplementService from '@/api/strategy-engine/complement'
export default {
  name: 'AddToolComplement',
  props: {
    tool: {
      type: Array
    }
  },
  data () {
    return {
      formDataInit: {
        tool: [],
        complementKey: []
      },
      formData: {
        complementKey: '',
        tool: '',
        description: ''
      },
      paramSelectConfig: {
        loading: false,
        options: []
      },
      finishButtonDisabled: false,
      ruleValidate: {
        tool: [{
          required: true, type: 'number', message: '请选择补全工具', trigger: 'blur'
        }],
        complementKey: [{
          required: true, message: '请输入补全key', trigger: 'blur'
        }],
        description: [{
          required: true, message: '请输入描述', trigger: 'blur'
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
        this.formData.tool = this.formDataInit.tool[0].value
      }
    },
    clearForm () {
      this.$refs['add'].resetFields()
    },
    finishSubmit (name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            complementKey: this.formData.complementKey,
            toolId: this.formData.tool,
            description: this.formData.description,
            state: 1
          }
          ComplementService.addToolComplementParam(reqData).then(res => {
            if (res.data.code !== null && res.data.code === 200) {
              Message.message('新增成功', 'success', this)
              this.$emit('onAfterCommit')
            } else {
              Message.message('新增失败,数据已存在或其他错误', 'error', this)
              this.$emit('onAfterCommit')
            }
          })
        } else {
          Message.message('新增失败', 'error', this)
        }
      })
    }
  },
  watch: {
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
