class Common {
  static getStrategyState (type) {
    if (type === 50) { // 累计过滤策略状态
      return [
        { value: 0, label: '下线' },
        { value: 2, label: '上线' }
      ]
    } else {
      return [
        { value: 0, label: '下线' },
        { value: 1, label: '预上线' },
        { value: 2, label: '上线' }
      ]
    }
  }

  static getState () {
    return [
      { value: 1, label: '启用' },
      { value: 0, label: '禁用' }
    ]
  }

  static getOp () {
    return [
      { value: '==', label: '等于' },
      { value: '!=', label: '不等于' },
      { value: 'EQUAL_STRING', label: '等于字符串' },
      { value: 'NOT_EQUAL_STRING', label: '不等于字符串' },
      { value: 'EQUAL_NULL_OBJECT', label: '等于空对象' },
      { value: '>', label: '大于' },
      { value: '<', label: '小于' },
      { value: '>=', label: '大于等于' },
      { value: '<=', label: '小于等于' },
      { value: 'INCLUDE', label: '包含' },
      { value: 'NOT_INCLUDE', label: '不包含' },
      { value: 'STARTS_WITH', label: '以...开头' },
      { value: 'END_WITH', label: '以...结尾' },
      { value: 'INTERSECTION', label: '数组交集' },
      { value: 'REGEXP', label: '正则表达式' },
      { value: 'FIELD_EQUAL', label: '字段相等' },
      { value: 'FIELD_NOT_EQUAL', label: '字段不相等' },
      { value: 'FIELD_EXISTS', label: '字段存在' },
      { value: 'FIELD_NOT_EXISTS', label: '字段不存在' },
      { value: 'FIELD_INCLUDE', label: '字段包含' },
      { value: 'FIELD_NOT_INCLUDE', label: '字段不包含' },
      { value: 'LENGTH_MORE_THAN_AND', label: '字符串长度大于等于' },
      { value: 'LENGTH_LESS_THAN_AND', label: '字符串长度小于等于' },
      { value: 'INDEX_MERGE_MORE_THAN', label: '指标合并大于' },
      { value: 'INDEX_MERGE_LESS_THAN', label: '指标合并小于' },
      { value: 'INDEX_MERGE_EQUAL', label: '指标合并等于' },
      { value: 'INDEX_MERGE_MORE_EQUAL', label: '指标合并大于等于' },
      { value: 'INDEX_MERGE_LESS_EQUAL', label: '指标合并小于等于' }
    ]
  }

  static getStrategyType () {
    return [
      { value: 1, label: '普通' },
      { value: 50, label: '累计过滤' }
    ]
  }

  static getRuleType (type) {
    if (type === 50) { // 累计过滤策略状态
      return [
        { value: 50, label: '累计过滤基础参数' }
      ]
    } else {
      return [
        { value: 1, label: '基础参数' },
        { value: 2, label: '名单' },
        { value: 4, label: '画像' },
        { value: 5, label: '累计特征' },
        { value: 6, label: '画像关联规模' },
        { value: 14, label: '算法' },
        { value: 15, label: '认证状态' },
        { value: 16, label: '计算' }
      ]
    }
  }

  static getRuleTypeByIndex (index) {
    let map = new Map()
    map.set(1, '基础参数')
    map.set(2, '名单')
    map.set(4, '画像')
    map.set(5, '累计特征')
    map.set(6, '画像关联规模')
    map.set(14, '算法')
    map.set(15, '认证状态')
    map.set(16, '计算')
    map.set(50, '累计过滤基础参数')
    return map.get(index)
  }

  static getBeforeMinute () {
    return [
      { value: '1', label: '一分钟' },
      { value: '10', label: '十分钟' },
      { value: '30', label: '半小时' },
      { value: '60', label: '一小时' },
      { value: '1440', label: '一天' },
      { value: '2880', label: '二天' },
      { value: '4320', label: '三天' }
    ]
  }

  // 根据规则类型判断是否是基础特征工具(2:名单工具,4:画像工具,6:画像关联规模工具,14:算法工具,15:认证工具)
  static isFeature (leftType) {
    return leftType === 2 || leftType === 4 || leftType === 6 || leftType === 14 || leftType === 15
  }

  // 16:计算工具
  static isCalculationFeature (leftType) {
    return leftType === 16
  }

  static isAuthFeature (leftType) {
    return leftType === 15
  }

  static hasArrValue (arr, key) {
    return arr.indexOf(key) >= 0
  }

  static isBaseFieldOrStatistics (leftType) {
    return leftType === 1 || leftType === 5
  }

  static getDateOpts () {
    return {
      shortcuts: [
        {
          text: '最近三天',
          value () {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 3)
            return [start, end]
          }
        },
        {
          text: '最近1周',
          value () {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          }
        },
        {
          text: '最近半个月',
          value () {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 15)
            return [start, end]
          }
        },
        {
          text: '最近1个月',
          value () {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          }
        },
        {
          text: '最近2个月',
          value () {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 60)
            return [start, end]
          }
        },
        {
          text: '最近3个月',
          value () {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
          }
        }
      ]
    }
  }

  static getDefaultStartDate () {
    let befDate = new Date(new Date().getTime() - 3600 * 1000 * 24 * 3)
    let byear = befDate.getFullYear()
    let bmonth = befDate.getMonth() + 1
    let bday = befDate.getDate()
    let bhour = befDate.getHours()
    let bmin = befDate.getMinutes()
    let bsec = befDate.getSeconds()
    return byear + '-' + bmonth + '-' + bday + ' ' + bhour + ':' + bmin + ':' + bsec
  }

  static getDefaultEndDate () {
    let nowDate = new Date()
    let year = nowDate.getFullYear()
    let month = nowDate.getMonth() + 1
    let day = nowDate.getDate()
    let hour = nowDate.getHours()
    let min = nowDate.getMinutes()
    let sec = nowDate.getSeconds()
    return year + '-' + month + '-' + day + ' ' + hour + ':' + min + ':' + sec
  }

  static getAuthType () {
    return [
      { value: '107', label: '手持身份证' },
      { value: '108', label: '人脸认证' },
      { value: '109', label: '支付认证' },
      { value: '110', label: '工作邮箱' },
      { value: '111', label: '微信认证' },
      { value: '112', label: '营业执照普通' },
      { value: '113', label: '营业执照第三方' },
      { value: '114', label: '对公账号' },
      { value: '115', label: '营业执照法人' },
      { value: '116', label: '品牌认证' },
      { value: '117', label: '银行卡实名' },
      { value: '118', label: '房本认证' },
      { value: '119', label: '网关认证' }
    ]
  }

  static getAuthState () {
    return [
      { value: '1', label: '认证通过' },
      { value: '2', label: '未认证' },
      { value: '3', label: '认证被拒绝' },
      { value: '4', label: '认证中' },
      { value: '5', label: '认证超时' },
      { value: '501', label: '待审核' }
    ]
  }

  static getBooleanValue () {
    return [
      { value: 'true', label: 'true' },
      { value: 'false', label: 'false' }
    ]
  }
}

export default Common
