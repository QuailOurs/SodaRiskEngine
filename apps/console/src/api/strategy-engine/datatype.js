import axios from '@/libs/api.request'
import services from '@/config/services'
const { strategyengine } = services

class DataTypeService {
  static getDataTypeList = (reqData) => {
    return axios.request({
      url: strategyengine + '/dataType/list',
      method: 'post',
      data: reqData
    })
  }
}

export default DataTypeService
