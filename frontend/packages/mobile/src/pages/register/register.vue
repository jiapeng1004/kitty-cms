<template>
  <view class="register-container">
    <view class="register-header">
      <image class="logo" src="/static/logo.png"></image>
      <text class="title">Kitty CMS</text>
    </view>
    
    <view class="register-form">
      <view class="form-item">
        <input 
          v-model="username" 
          type="text" 
          placeholder="用户名" 
          class="input"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="password" 
          type="password" 
          placeholder="密码" 
          class="input"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="confirmPassword" 
          type="password" 
          placeholder="确认密码" 
          class="input"
        />
      </view>
      <view class="form-item">
        <input 
          v-model="email" 
          type="text" 
          placeholder="邮箱" 
          class="input"
        />
      </view>
      <button class="register-btn" @click="register">立即注册</button>
      
      <view class="login-link">
        <text>已有账号？</text>
        <navigator url="/pages/login/login" hover-class="nav-hover">立即登录</navigator>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: '',
      email: ''
    }
  },
  methods: {
    register() {
      // 表单验证
      if (!this.username || !this.password || !this.confirmPassword || !this.email) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        })
        return
      }
      
      if (this.password !== this.confirmPassword) {
        uni.showToast({
          title: '两次密码输入不一致',
          icon: 'none'
        })
        return
      }
      
      // 邮箱格式验证
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(this.email)) {
        uni.showToast({
          title: '请输入正确的邮箱格式',
          icon: 'none'
        })
        return
      }
      
      // 调用注册接口
      uni.request({
        url: '/api/user/register',
        method: 'POST',
        data: {
          username: this.username,
          password: this.password,
          email: this.email
        },
        success: (res) => {
          if (res.data.code === 200) {
            uni.showToast({
              title: '注册成功',
              icon: 'success'
            })
            // 跳转到登录页
            setTimeout(() => {
              uni.navigateTo({
                url: '/pages/login/login'
              })
            }, 1500)
          } else {
            uni.showToast({
              title: res.data.msg || '注册失败',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          console.error('注册失败:', err)
          uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 40rpx;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.register-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 20rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
}

.register-form {
  width: 100%;
  max-width: 500rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 10rpx;
  font-size: 28rpx;
  background-color: #fff;
}

.register-btn {
  width: 100%;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  font-size: 32rpx;
  border-radius: 10rpx;
  margin-bottom: 30rpx;
}

.login-link {
  display: flex;
  justify-content: center;
  font-size: 28rpx;
  color: #666;
}

.login-link navigator {
  color: #007aff;
  margin-left: 10rpx;
}

.nav-hover {
  opacity: 0.7;
}
</style>