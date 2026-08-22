import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

class FeatureStatisticsRuleService {
  static getListByStrategyId (id) {
    return axios.request({
      url: strategyengine + '/featureStatisticsRule/list/strategyId/' + id,
      method: 'get'
    })
  }

  static getById (id) {
    return axios.request({
      url: strategyengine + '/featureStatisticsRule/detail/' + id,
      method: 'get'
    })
  }

  static getListBySceneKey (sceneKey) {
    return axios.request({
      url: strategyengine + '/featureStatisticsRule/list/sceneKey/' + sceneKey,
      method: 'get'
    })
  }

  static add (reqData) {
    return axios.request({
      url: strategyengine + '/featureStatisticsRule/add',
      method: 'post',
      data: reqData
    })
  }

  static update (reqData) {
    return axios.request({
      url: strategyengine + '/featureStatisticsRule/update',
      method: 'post',
      data: reqData
    })
  }

  static delete (id) {
    return axios.request({
      url: strategyengine + '/featureStatisticsRule/delete/' + id,
      method: 'delete'
    })
  }

  static validExist (name) {
    return axios.request({
      url: strategyengine + '/featureStatisticsRule/validExist/' + name,
      method: 'get'
    })
  }
}

export default FeatureStatisticsRuleService
