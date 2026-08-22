<template>
  <Modal title="业务方管理" fullscreen v-bind="$attrs" v-on="$listeners">
    <BusinessSideIndex />
    <div slot="footer"></div>
  </Modal>
</template>

<script>
import BusinessSideIndex from './index.vue'

export default {
  name: 'BusinessSideModal',
  components: { BusinessSideIndex }
}
</script>
