<template>
  <view class="login-container">
    <view class="login-header">
      <image class="logo" src="/static/logo.png"></image>
      <text class="title">Kitty CMS</text>
    </view>
    
    <view class="login-form">
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
      <button class="login-btn" @click="login">密码登录</button>
      
      <view class="divider">
        <text class="divider-text">或者</text>
      </view>
      
      <button class="feishu-btn" @click="feishuLogin">
        <image class="feishu-icon" src="/static/logo.png"></image>
        <text>飞书登录</text>
      </button>
      
      <view class="register-link">
        <text>还没有账号？</text>
        <navigator url="/pages/register/register" hover-class="nav-hover">立即注册</navigator>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: ''
    }
  },
  methods: {
    // 密码登录
    login() {
      if (!this.username || !this.password) {
        uni.showToast({
          title: '请输入用户名和密码',
          icon: 'none'
        })
        return
      }
      
      // 调用登录接口
      uni.request({
        url: '/kitty-user/api/user/login',
        method: 'POST',
        data: {
          username: this.username,
          password: this.password
        },
        success: (res) => {
          if (res.data.code === 200) {
            // 登录成功，保存token
            uni.setStorageSync('token', res.data.data.token)
            uni.showToast({
              title: '登录成功',
              icon: 'success'
            })
            // 跳转到首页
            setTimeout(() => {
              uni.switchTab({
                url: '/pages/index/index'
              })
            }, 1500)
          } else {
            uni.showToast({
              title: res.data.msg || '登录失败',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          console.error('登录失败:', err)
          uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
          })
        }
      })
    },
    
    // 飞书登录
    feishuLogin() {
      // 在小程序中，使用uni.login获取code
      // 在H5中，跳转到飞书授权页面
      if (uni.getSystemInfoSync().platform === 'h5') {
        // H5端处理
        const appId = 'your_feishu_app_id' // 替换为你的飞书App ID
        const redirectUri = encodeURIComponent('/api/user/feishu/callback') // 替换为你的回调地址
        const scope = 'user:email,user:openid,user:id'
        
        const authUrl = `https://open.feishu.cn/open-apis/authen/v1/index?app_id=${appId}&redirect_uri=${redirectUri}&scope=${scope}`
        window.location.href = authUrl
      } else {
        // 小程序端处理
        uni.login({
          provider: 'feishu',
          success: (res) => {
            if (res.code) {
              // 将code发送到后端获取token
              uni.request({
                url: '/api/user/feishu/login',
                method: 'POST',
                data: {
                  code: res.code
                },
                success: (response) => {
                  if (response.data.code === 200) {
                    // 登录成功，保存token
                    uni.setStorageSync('token', response.data.data.token)
                    uni.showToast({
                      title: '登录成功',
                      icon: 'success'
                    })
                    // 跳转到首页
                    setTimeout(() => {
                      uni.switchTab({
                        url: '/pages/index/index'
                      })
                    }, 1500)
                  } else {
                    uni.showToast({
                      title: response.data.msg || '登录失败',
                      icon: 'none'
                    })
                  }
                },
                fail: (err) => {
                  console.error('飞书登录失败:', err)
                  uni.showToast({
                    title: '网络错误，请稍后重试',
                    icon: 'none'
                  })
                }
              })
            }
          },
          fail: (err) => {
            console.error('获取飞书授权码失败:', err)
            uni.showToast({
              title: '飞书登录失败，请稍后重试',
              icon: 'none'
            })
          }
        })
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 40rpx;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-header {
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

.login-form {
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

.login-btn {
  width: 100%;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  font-size: 32rpx;
  border-radius: 10rpx;
  margin-bottom: 30rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin: 30rpx 0;
}

.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1rpx;
  background-color: #ddd;
}

.divider-text {
  padding: 0 20rpx;
  color: #999;
  font-size: 24rpx;
}

.feishu-btn {
  width: 100%;
  height: 80rpx;
  background-color: #00b42a;
  color: #fff;
  font-size: 32rpx;
  border-radius: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
}

.feishu-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 10rpx;
}

.register-link {
  display: flex;
  justify-content: center;
  font-size: 28rpx;
  color: #666;
}

.register-link navigator {
  color: #007aff;
  margin-left: 10rpx;
}

.nav-hover {
  opacity: 0.7;
}
</style>