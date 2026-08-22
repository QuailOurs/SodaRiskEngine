import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

class BusinessSideService {
  static getList = (reqData) => {
    return axios.request({
      url: strategyengine + '/businessside/list',
      method: 'post',
      data: reqData
    })
  }

  static update = (reqData) => {
    return axios.request({
      url: strategyengine + '/businessSide/update',
      method: 'put',
      data: reqData
    })
  }

  static getById (id) {
    return axios.request({
      url: strategyengine + '/businessSide/' + id,
      method: 'get'
    })
  }

  static add = (reqData) => {
    return axios.request({
      url: strategyengine + '/businessSide/add',
      method: 'post',
      data: reqData
    })
  }

  static validNameExist = (name, excludeId) => {
    return axios.request({
      url: strategyengine + '/businessSide/existed/name/' + encodeURIComponent(name),
      params: excludeId ? { excludeId } : {},
      method: 'get'
    })
  }

  static validKeyExist = (businessSide, excludeId) => {
    return axios.request({
      url: strategyengine + '/businessSide/existed/key/' + encodeURIComponent(businessSide),
      params: excludeId ? { excludeId } : {},
      method: 'get'
    })
  }

  static getSystemKeySelectData = () => {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/businessSide/systemKey/list',
        method: 'get'
      }).then((res) => {
        let result = []
        if (res.data !== '') {
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

  static getSelectData = (reqData) => {
    return new Promise((resolve, reject) => {
      axios.request({
        url: strategyengine + '/businessside/list',
        method: 'post',
        data: reqData
      }).then((res) => {
        let result = []
        if (res.data !== '') {
          res.data.data.forEach(item => {
            result.push({
              value: item.businessSideKey,
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
}

export default BusinessSideService
