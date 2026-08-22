import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

class ParamService {
  static getParamList = (reqData) => {
    return axios.request({
      url: strategyengine + '/parameter/list',
      method: 'post',
      data: reqData
    })
  }
  static getParamListBySceneKey = (sceneKey) => {
    return axios.request({
      url: strategyengine + '/parameter/list/' + sceneKey,
      method: 'get'
    })
  }

  static updateParam = (reqData) => {
    return axios.request({
      url: strategyengine + '/parameter/update',
      method: 'put',
      data: reqData
    })
  }

  static addParam = (reqData) => {
    return axios.request({
      url: strategyengine + '/parameter/add',
      method: 'post',
      data: reqData
    })
  }

  static deleteParam = (id) => {
    return axios.request({
      url: strategyengine + '/parameter/delete/' + id,
      method: 'delete'
    })
  }

  static existedKeyParam = (sceneKey, paramKey) => {
    return axios.request({
      url: strategyengine + '/parameter/existedKey?sceneKey=' + encodeURIComponent(sceneKey) + '&paramKey=' + encodeURIComponent(paramKey),
      method: 'get'
    })
  }
  static existedNameParam = (sceneKey, paramName) => {
    return axios.request({
      url: strategyengine + '/parameter/existedName?sceneKey=' + encodeURIComponent(sceneKey) + '&paramName=' + encodeURIComponent(paramName),
      method: 'get'
    })
  }

  static getCascaderDataGroupParamIdBySceneKey = (sceneKey, multiple) => {
    return axios.request({
      url: strategyengine + '/parameter/getCascaderDataGroupParamIdBySceneKey/sceneKey/' + sceneKey + '/multiple/' + multiple,
      method: 'get'
    })
  }

  static getCascaderDataGroupParamKeyBySceneKey = (sceneKey, multiple) => {
    return axios.request({
      url: strategyengine + '/parameter/getCascaderDataGroupParamKeyBySceneKey/sceneKey/' + sceneKey + '/multiple/' + multiple,
      method: 'get'
    })
  }

  static getSelectData (sceneKey) {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/parameter/list/' + sceneKey,
        method: 'get'
      }).then((res) => {
        let result = []

        res.data.data.forEach(item => {
          result.push({
            value: item.paramKey,
            label: item.paramKey
          })
        })

        return resolve(result)
      }).catch(err => {
        reject(err)
      })
    })
  }
}

export default ParamService
