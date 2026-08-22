class CommonTools {
  static isEditPage (pageType) {
    return pageType === 'edit'
  }

  static isAddPage (PageType) {
    return PageType === 'add'
  }

  static isArrayNull (array) {
    return (array === null || array === undefined || array.length === 0)
  }

  static isArrayNotNull (array) {
    return (array !== null && array !== undefined && array.length > 0)
  }

  static isNull (str) {
    return (str === null || str === undefined || str === 'undefined' || str.length === 0)
  }

  static nonNull (str) {
    return (str !== null && str !== undefined && str !== 'undefined')
  }

  static unique (arr) {
    let hash = []
    for (let i = 0; i < arr.length; i++) {
      if (arr.indexOf(arr[i]) === i) {
        hash.push(arr[i])
      }
    }
    return hash
  }

  static arrayRemove (array, value) {
    let index = array.indexOf(value)
    if (index > -1) {
      array.splice(index, 1)
    }
  }

  static arrayRemoveObjById (_arr, id) {
    let length = _arr.length
    for (let i = 0; i < length; i++) {
      if (_arr[i].id === id) {
        if (i === 0) {
          _arr.shift() // 删除并返回数组的第一个元素
          return _arr
        } else if (i === length - 1) {
          _arr.pop() // 删除并返回数组的最后一个元素
          return _arr
        } else {
          _arr.splice(i, 1) // 删除下标为i的元素
          return _arr
        }
      }
    }
  }

  /**
   * 判断一个数组是否存在某个元素值
   */
  static hasArrValue = (arr, key) => {
    return arr.indexOf(key) >= 0
  }
}

export default CommonTools
