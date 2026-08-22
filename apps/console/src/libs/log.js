let log = {
  json2str (info) {
    return JSON.stringify(info)
  },
  jsonlog (info, arg) {
    console.info(this.json2str(info), arg)
  },
  log (info, ...arg) {
    console.info(info, arg)
  }
}

export default log
