import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

class CommonService {
  static checkRegExp (reqData) {
    return axios.request({
      url: strategyengine + '/checkExpression/checkRegExp',
      method: 'post',
      data: reqData
    })
  }
}

export default CommonService
