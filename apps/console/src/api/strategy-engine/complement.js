import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

class ComplementService {
  static getComplementList = (reqData) => {
    return axios.request({
      url: strategyengine + '/complement/list',
      method: 'post',
      data: reqData
    })
  }
  static getComplementKeyByToolId = (reqData) => {
    return axios.request({
      url: strategyengine + '/complement/complementKeyList',
      method: 'post',
      data: reqData
    })
  }
  static getParamListBySceneKey = (sceneKey) => {
    return axios.request({
      url: strategyengine + '/complement/list/' + sceneKey,
      method: 'get'
    })
  }

  static updateParam = (reqData) => {
    return axios.request({
      url: strategyengine + '/complement/update',
      method: 'put',
      data: reqData
    })
  }

  static addParam = (reqData) => {
    return axios.request({
      url: strategyengine + '/complement/add',
      method: 'post',
      data: reqData
    })
  }

  static addToolComplementParam = (reqData) => {
    return axios.request({
      url: strategyengine + '/complement/addComplementKey',
      method: 'post',
      data: reqData
    })
  }

  static deleteParam = (id) => {
    return axios.request({
      url: strategyengine + '/complement/delete/' + id,
      method: 'delete'
    })
  }

  static enableComplement = (id) => {
    return axios.request({
      url: strategyengine + '/complement/enable/' + id,
      method: 'patch'
    })
  }

  static forbiddenComplement = (id) => {
    return axios.request({
      url: strategyengine + '/complement/forbidden/' + id,
      method: 'patch'
    })
  }
  static existedKeyParam = (sceneKey, paramKey) => {
    return axios.request({
      url: strategyengine + '/complement/existedKey?sceneKey=' + encodeURIComponent(sceneKey) + '&paramKey=' + encodeURIComponent(paramKey),
      method: 'get'
    })
  }
  static existedNameParam = (sceneKey, paramName) => {
    return axios.request({
      url: strategyengine + '/complement/existedName?sceneKey=' + encodeURIComponent(sceneKey) + '&paramName=' + encodeURIComponent(paramName),
      method: 'get'
    })
  }

  static getCascaderDataGroupParamIdBySceneKey = (sceneKey, multiple) => {
    return axios.request({
      url: strategyengine + '/complement/getCascaderDataGroupParamIdBySceneKey/sceneKey/' + sceneKey + '/multiple/' + multiple,
      method: 'get'
    })
  }

  static getCascaderDataGroupParamKeyBySceneKey = (sceneKey, multiple) => {
    return axios.request({
      url: strategyengine + '/complement/getCascaderDataGroupParamKeyBySceneKey/sceneKey/' + sceneKey + '/multiple/' + multiple,
      method: 'get'
    })
  }

  static getSelectData (sceneKey) {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/complement/list/' + sceneKey,
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

export default ComplementService
