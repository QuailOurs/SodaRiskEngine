<template>
  <div class="engine-home">
    <Card dis-hover>
      <div class="hero">
        <div>
          <p class="eyebrow">SODA</p>
          <h1>面向业务安全的规则决策引擎</h1>
          <p class="summary">通过场景、参数、特征、规则与策略建立可解释的业务决策流程。</p>
        </div>
        <img class="project-logo" :src="projectLogo" alt="Soda project logo" />
      </div>
    </Card>

    <Row :gutter="16" class="entry-list">
      <i-col v-for="entry in entries" :key="entry.name" :md="8" :sm="12" :xs="24">
        <Card class="entry-card" @click.native="open(entry.name)">
          <Icon :type="entry.icon" :size="28" color="#2d8cf0" />
          <h3>{{ entry.title }}</h3>
          <p>{{ entry.description }}</p>
        </Card>
      </i-col>
    </Row>
  </div>
</template>

<script>
import projectLogo from '@/assets/images/soda-project-logo.png'

export default {
  name: 'home',
  data () {
    return {
      projectLogo,
      entries: [
        { name: 'scene', title: '场景管理', icon: 'md-git-branch', description: '定义规则运行的业务边界。' },
        { name: 'strategy', title: '策略管理', icon: 'md-options', description: '组合条件并管理策略状态。' },
        { name: 'parameter', title: '字段管理', icon: 'md-list-box', description: '维护规则所需的输入字段。' }
      ]
    }
  },
  methods: {
    open (name) {
      this.$router.push({ name })
    }
  }
}
</script>

<style lang="less" scoped>
.engine-home {
  .hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 150px;
  }
  .eyebrow {
    color: #2d8cf0;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 1.6px;
  }
  h1 {
    margin: 8px 0;
    color: #17233d;
    font-size: 28px;
  }
  .summary,
  .entry-card p {
    color: #808695;
  }
  .project-logo {
    width: 126px;
    height: 126px;
    object-fit: contain;
    flex: 0 0 auto;
    margin-left: 24px;
  }
  .entry-list {
    margin-top: 16px;
  }
  .entry-card {
    min-height: 150px;
    margin-bottom: 16px;
    cursor: pointer;
  }
  .entry-card h3 {
    margin: 12px 0 6px;
  }
}
</style>
