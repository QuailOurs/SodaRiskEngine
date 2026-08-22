import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

export const getFeatureStatisticsList = (reqData) => {
  return axios.request({
    url: strategyengine + '/featureStatistics/list',
    method: 'post',
    data: reqData
  })
}
export const addFeatureStatistics = (reqData) => {
  return axios.request({
    url: strategyengine + '/featureStatistics/add',
    method: 'post',
    data: reqData
  })
}
export const getFeatureStatistics = (id) => {
  return axios.request({
    url: strategyengine + '/featureStatistics/' + id,
    method: 'get'
  })
}

export const updateFeatureStatistics = (reqData) => {
  return axios.request({
    url: strategyengine + '/featureStatistics/update',
    method: 'put',
    data: reqData
  })
}
export const enableFeatureStatistics = (id) => {
  return axios.request({
    url: strategyengine + '/featureStatistics/enable/' + id,
    method: 'patch'
  })
}
export const forbiddenFeatureStatistics = (id) => {
  return axios.request({
    url: strategyengine + '/featureStatistics/forbidden/' + id,
    method: 'patch'
  })
}
export const existedFeatureStatistics = (sceneKey, featureStatisticsName) => {
  return axios.request({
    url: strategyengine + '/featureStatistics/existed?sceneKey=' + encodeURIComponent(sceneKey) + '&featureStatisticsName=' + encodeURIComponent(featureStatisticsName),
    method: 'get'
  })
}
