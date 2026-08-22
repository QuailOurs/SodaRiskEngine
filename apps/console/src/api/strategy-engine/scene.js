import axios from '@/libs/api.request'
import services from '@/config/services'
import CommonTools from '@/libs/common-tools'
const { strategyengine } = services

class SceneService {
  static getSceneList = (reqData) => {
    return axios.request({
      url: strategyengine + '/scene/list',
      method: 'post',
      data: reqData
    })
  }

  static getListBySystemKey = (systemKey) => {
    return axios.request({
      url: strategyengine + '/scene/list/' + systemKey,
      method: 'get'
    })
  }

  static updateScene = (reqData) => {
    return axios.request({
      url: strategyengine + '/scene/update',
      method: 'put',
      data: reqData
    })
  }

  static getById (id) {
    return axios.request({
      url: strategyengine + '/scene/' + id,
      method: 'get'
    })
  }

  static addScene = (reqData) => {
    return axios.request({
      url: strategyengine + '/scene/add',
      method: 'post',
      data: reqData
    })
  }
  static existedScene = (businessSide, name) => {
    return axios.request({
      url: strategyengine + '/scene/existed?businessSide=' + encodeURIComponent(businessSide) + '&name=' + encodeURIComponent(name),
      method: 'get'
    })
  }
  static sceneNameList = () => {
    return axios.request({
      url: strategyengine + '/scene/sceneName/list',
      method: 'get'
    })
  }
  static sceneNameListByBusinessSideKey = (businessSideKey) => {
    return axios.request({
      url: strategyengine + '/scene/sceneName/list/' + businessSideKey,
      method: 'get'
    })
  }

  static getSelectData (reqData) {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/scene/list',
        method: 'post',
        data: reqData
      }).then((res) => {
        let result = []

        if (CommonTools.nonNull(res.data) && CommonTools.nonNull(res.data.data)) {
          res.data.data.forEach(item => {
            result.push({
              value: item.sceneKey,
              label: item.name
            })
          })
        }

        return resolve(result)
      }).catch(err => {
        reject(err)
      })
    })
  }

  static getByOaAccount (oaAccount) {
    return axios.request({
      url: strategyengine + '/emp/info/account/' + oaAccount,
      method: 'get'
    })
  }
}

export default SceneService
