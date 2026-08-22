import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

class RuleService {
  static getListByStrategyId (id) {
    return axios.request({
      url: strategyengine + '/rule/list/strategyId/' + id,
      method: 'get'
    })
  }

  static getById (id) {
    return axios.request({
      url: strategyengine + '/rule/detail/' + id,
      method: 'get'
    })
  }

  static getFreeListBySceneKey (sceneKey) {
    return axios.request({
      url: strategyengine + '/rule/freelist/sceneKey/' + sceneKey,
      method: 'get'
    })
  }

  static getListBySceneKey (sceneKey) {
    return axios.request({
      url: strategyengine + '/rule/list/sceneKey/' + sceneKey,
      method: 'get'
    })
  }

  static getList (reqData) {
    return axios.request({
      url: strategyengine + '/rule/list',
      method: 'post',
      data: reqData
    })
  }

  static getListByRuleIds (reqData) {
    return axios.request({
      url: strategyengine + '/rule/list/ruleIds',
      method: 'post',
      data: reqData
    })
  }

  static getCascaderDataGroupByRuleType (sceneKey, ruleType) {
    return axios.request({
      url: strategyengine + '/rule/getCascaderDataGroupByRuleType/' + sceneKey + '/ruleType/' + ruleType,
      method: 'get'
    })
  }

  static add (reqData) {
    return axios.request({
      url: strategyengine + '/rule/add',
      method: 'post',
      data: reqData
    })
  }

  static update (reqData) {
    return axios.request({
      url: strategyengine + '/rule/update',
      method: 'post',
      data: reqData
    })
  }

  static delete (id) {
    return axios.request({
      url: strategyengine + '/rule/delete/' + id,
      method: 'delete'
    })
  }

  static validExist (sceneKey, name) {
    return axios.request({
      url: strategyengine + '/rule/validExist/' + sceneKey + '/' + name,
      method: 'get'
    })
  }

  static getRelationStrategyByRuleId (ruleId) {
    return axios.request({
      url: strategyengine + '/rule/relation/strategy/' + ruleId,
      method: 'get'
    })
  }
}

export default RuleService
