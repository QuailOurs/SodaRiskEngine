let message = {
  message (msg, type, vueObj, extMsg) {
    vueObj.$Message.config({
      top: 10,
      duration: 3
    })

    if (extMsg) {
      msg += ', ' + extMsg
    }

    switch (type) {
      case 'info':
        vueObj.$Message.info({
          content: msg,
          closable: true
        })
        break
      case 'success':
        vueObj.$Message.success({
          content: msg,
          closable: true
        })
        break
      case 'warning':
        vueObj.$Message.warning({
          content: msg,
          closable: true
        })
        break
      case 'error':
        vueObj.$Message.error({
          content: msg,
          closable: true
        })
        break
      default:
        vueObj.$Message.warning({
          content: '提示异常',
          closable: true
        })
    }
  }
}

export default message
