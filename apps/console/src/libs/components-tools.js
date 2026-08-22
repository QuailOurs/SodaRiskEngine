class ComponentsTools {
  /**
   * table 单元格根据列宽动态隐藏过长内容
   */
  static customTooltip (h, params, srcText) {
    let texts = srcText
    let hideLength = params.column._width * 0.2
    if (texts != null) {
      if (texts.length > hideLength) {
        texts = texts.slice(0, hideLength) + '...' // 进行数字截取
      }
    }
    return h('div', [
      h('Tooltip', {
        props: {
          placement: 'top',
          transfer: true
        }
      }, [texts, h('span', {
        slot: 'content',
        style: {
          whiteSpace: 'normal'
        }
      }, srcText)
      ])
    ])
  }
}

export default ComponentsTools
