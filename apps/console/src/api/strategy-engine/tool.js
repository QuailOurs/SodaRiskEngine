import axios from '@/libs/api.request'
import services from '@/config/services'
import CommonTools from '@/libs/common-tools'
const { strategyengine } = services

class ToolService {
  static getToolList = (reqData) => {
    return axios.request({
      url: strategyengine + '/tool/list',
      method: 'post',
      data: reqData
    })
  }
  static enableTool = (id) => {
    return axios.request({
      url: strategyengine + '/tool/enable/' + id,
      method: 'patch'
    })
  }
  static forbiddenTool = (id) => {
    return axios.request({
      url: strategyengine + '/tool/forbidden/' + id,
      method: 'patch'
    })
  }

  static getSelectData = (reqData) => {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/tool/list',
        method: 'post',
        data: reqData
      }).then((res) => {
        let result = []

        if (!CommonTools.isNull(res.data)) {
          res.data.data.forEach(item => {
            result.push({
              value: item.id,
              label: item.name + '(' + item.description + ')'
            })
          })
        }

        return resolve(result)
      }).catch(err => {
        reject(err)
      })
    })
  }

  static getToolFieldCascaderDataGroupByTool = (toolType, multiple) => {
    return axios.request({
      url: strategyengine + '/tool/getToolFieldCascaderDataGroupByTool/toolType/' + toolType + '/multiple/' + multiple,
      method: 'get'
    })
  }
}

export default ToolService
