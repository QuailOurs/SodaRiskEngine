<template>
  <div class="user-avatar-dropdown">
    <Dropdown @on-click="handleClick">
      <Badge :dot="!!messageUnreadCount">
        <Avatar :src="userAvatar"/>
      </Badge>
      <Icon :size="18" type="md-arrow-dropdown"></Icon>
      <DropdownMenu slot="list">
<!--        <DropdownItem name="message">-->
<!--          消息中心<Badge style="margin-left: 10px" :count="messageUnreadCount"></Badge>-->
<!--        </DropdownItem>-->
        <DropdownItem name="logout">退出登录</DropdownItem>
      </DropdownMenu>
    </Dropdown>
  </div>
</template>

<script>
import './user.less'
import { mapActions } from 'vuex'
import { localSave } from '@/libs/util'
import Cookies from 'js-cookie'
export default {
  name: 'User',
  props: {
    userAvatar: {
      type: String,
      default: ''
    },
    messageUnreadCount: {
      type: Number,
      default: 0
    }
  },
  methods: {
    ...mapActions([
      'handleLogOut'
    ]),
    onLogout () {
      this.handleLogOut().then(() => {
        localSave('dynamicRouter', []) // 清空本地路由
        Cookies.remove('SSO_SESSION_ID')
        this.$router.replace({ name: 'home' })
      })
    },
    message () {
      this.$router.push({
        name: 'message_page'
      })
    },
    handleClick (name) {
      switch (name) {
        case 'logout': this.onLogout()
          break
        case 'message': this.message()
          break
      }
    }
  }
}
</script>
