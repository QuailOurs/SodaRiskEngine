<template>
  <div>
    <Modal :title="'表达式验证工具'" :width="600" draggable
           :styles="{top: '20px'}"
           v-bind="$attrs"
           v-on="$listeners">
      <Form :label-width="100" ref="form_id" :model="formData" :rules="ruleValidate">
        <Row>
          <i-col span="24">
            <FormItem label="测试数据" prop="testData">
              <Input v-model="formData.testData" type="textarea" :autosize="true" placeholder="Enter something..."/>
            </FormItem>
          </i-col>
          <i-col span="24">
            <FormItem label="待匹配字段" prop="searchField">
              <Input v-model="formData.searchField" type="textarea" :autosize="true" placeholder="Enter something..."/>
            </FormItem>
          </i-col>
          <i-col span="24">
            <FormItem label="正则表达式" prop="regExp">
              <Input v-model="formData.regExp" type="textarea" :autosize="true" placeholder="Enter something..."/>
            </FormItem>
          </i-col>
          <i-col span="24">
            <Input v-model="formData.checkResult" type="textarea" :autosize="true" placeholder="校验结果"/>
          </i-col>
        </Row>
      </Form>
      <div slot="footer">
        <Button type="primary" size="large" @click="handleSubmit('form_id')">校验</Button>
      </div>
    </Modal>
  </div>
</template>

<script>

import Message from '@/libs/message'
import LogUtil from '@/libs/log'

import CommonService from '@/api/strategy-engine/common'

export default {
  components: {},
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
        buttonSize: 'small',
        modalVisible: false
      },
      formData: {
        testData: '',
        searchField: '',
        regExp: '',
        checkResult: ''
      },
      ruleValidate: {
        testData: [{
          required: true, message: '测试数据为空'
        }],
        searchField: [{
          required: true, message: '待匹配字段为空'
        }],
        regExp: [{
          required: true, message: '正则表达式为空'
        }]
      }
    }
  },
  created: function () {},
  mounted () {},
  watch: {},
  methods: {
    handleSubmit (name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          let reqData = {
            testData: this.formData.testData,
            searchField: this.formData.searchField,
            regExp: this.formData.regExp
          }

          CommonService.checkRegExp(reqData).then(res => {
            this.formData.checkResult = LogUtil.json2str(res.data.data)
          })

          Message.message('成功', 'success', this)
        } else {
          Message.message('失败', 'error', this)
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
