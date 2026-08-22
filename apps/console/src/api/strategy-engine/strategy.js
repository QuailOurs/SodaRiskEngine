import axios from '@/libs/api.request'
import services from '@/config/services'
import CommonTools from '@/libs/common-tools'
const { strategyengine } = services

class StrategyService {
  static getList (reqData) {
    return axios.request({
      url: strategyengine + '/strategy/list',
      method: 'post',
      data: reqData
    })
  }

  static getById (id) {
    return axios.request({
      url: strategyengine + '/strategy/id/' + id,
      method: 'get'
    })
  }

  static add (reqData) {
    return axios.request({
      url: strategyengine + '/strategy/add',
      method: 'post',
      data: reqData
    })
  }

  static update (reqData) {
    return axios.request({
      url: strategyengine + '/strategy/update',
      method: 'post',
      data: reqData
    })
  }

  static updateState (reqData) {
    return axios.request({
      url: strategyengine + '/strategy/update/state',
      method: 'post',
      data: reqData
    })
  }

  static getReturnCodeListBySceneKey (sceneKey) {
    return axios.request({
      url: strategyengine + '/list/sceneKey/' + sceneKey,
      method: 'get'
    })
  }

  static getStatisticsFilterSelectData (reqData) {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/strategy/list',
        method: 'post',
        data: reqData
      }).then((res) => {
        let result = []

        if (CommonTools.nonNull(res.data) && CommonTools.nonNull(res.data.data)) {
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

  static getReturnCodeSelectData (sceneKey) {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/strategy/return-code/list/sceneKey/' + sceneKey,
        method: 'get'
      }).then((res) => {
        let result = []

        if (CommonTools.nonNull(res.data) && CommonTools.nonNull(res.data.data)) {
          res.data.data.forEach(item => {
            result.push({
              value: item,
              label: item
            })
          })
        }

        return resolve(result)
      }).catch(err => {
        reject(err)
      })
    })
  }
}

export default StrategyService
