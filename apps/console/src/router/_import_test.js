// 单元测试环境使用同步组件加载，避免动态路由初始化时触发异步分包。
module.default = file => require('@/view/' + file + '.vue').default
