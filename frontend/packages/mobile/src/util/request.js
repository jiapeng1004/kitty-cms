import store from '@/store'
import { getToken } from '@/utils/auth'
import { errorCodeMap, reloadCodes } from '@/utils/error-code'
import sysConfig from '@/config'
import { prefixUrl } from "@/utils/api-adaptive"
import { ref } from 'vue'
// #ifdef H5
import { pathAddRedirectUrl } from '@/utils/common'
import { getH5RouteByUrl, isNingxia } from '@/utils/common'
// #endif
const { TIMEOUT, TOKEN_NAME, TOKEN_PREFIX, NO_TOKEN_BACK_URL } = sysConfig
const requestCount = ref(0)

//不需要提示的消息
const noSHowMessage = [
	"分享需要密码",
	"分享已失效",
	"分享次数用尽",
	"分享已过期"
]

const getCookie = (name) => {
	const cookies = document.cookie.split('; ');
	for (const cookie of cookies) {
		const [key, value] = cookie.split('=');
		if (key === name) {
			return decodeURIComponent(value);
		}
	}
	return null;
}
const request = config => {
	// 是否需要设置 token
	config.header = config.header || {}
	if (getToken() && !(config?.isToken === false)) {
		config.header[TOKEN_NAME] = TOKEN_PREFIX + getToken()
	}
	if (store.getters.tenantCode && !isNingxia()) {
		config.header.tencode = store.getters.tenantCode
	}

	//console.log('cookie',getCookie('login_cmc_id'))
	return new Promise((resolve, reject) => {
		requestCount.value++
		if (!config.isShowLoading) {
			uni.$snowy.modal.loading('努力加载中')
		}

		if (config.url.includes('/cmsback/web') || config.url.includes('/editor/bianji')) {
			config.baseUrl = store.getters.tenantDomain.replace('mms', 'console')
			if(store.getters.cmc_console_url && store.getters.cmc_console_url!=''){
				config.baseUrl = store.getters.cmc_console_url
			}
		}
		uni.request({
			method: config.method || 'GET',
			timeout: config.timeout || TIMEOUT,
			url: `${config.baseUrl || store.getters.allEnv[store.getters.envKey].baseUrl}${prefixUrl(config.url)}`,
			data: config.data,
			header: {
				'login_cmc_id': getCookie('login_cmc_id'),
				'login_cmc_tid': getCookie('login_cmc_tid'),
				'version':'cms2',
				domain: store.getters.tenantDomain,
				...config.header
			},
			sslVerify: false,
			//dataType: config.dataType || 'json'
		}).then(response => {
			const code = response.data?.code || response.statusCode || 200
			const msg = response.data?.msg || errorCodeMap[code] || errorCodeMap['default']
			if (reloadCodes.includes(code) &&!config.isShowLoading) {
				uni.showModal({
					title: '系统提示',
					content: '登录过期请重新登录',
					showCancel:false,
					confirmText: '去登录',
					success: function (res) {
						if (res.confirm) {
							store.commit('CLEAR_cache')
							// #ifdef H5
							uni.$snowy.tab.reLaunch(pathAddRedirectUrl(NO_TOKEN_BACK_URL, getH5RouteByUrl()))
							// #endif
							// #ifndef H5
							uni.$snowy.tab.reLaunch(NO_TOKEN_BACK_URL)
							// #endif
						}
					}
				})
				// uni.$snowy.modal.confirm(msg || '登录状态已过期，您可以清除缓存，重新进行登录?').then(() => {

				// })
				reject('无效的会话，或者会话已过期，请重新登录。')
			} else if (code !== 200) {
				if (!noSHowMessage.includes(msg)&&!config.isShowLoading) {
					uni.$snowy.modal.alert(msg)
				}

				reject(code)
			}
			// 是否返回服务端原数据
			if (config.isReturnOriginalData) {
				resolve(response?.data)
			} else {
				resolve(response?.data?.data)
			}
		}).catch(error => {
			let { errMsg } = error
			if (errMsg === 'Network Error') {
				errMsg = '后端接口连接异常'
			} else if (errMsg.includes('timeout')) {
				errMsg = '系统接口请求超时'
			} else if (errMsg.includes('Request failed with status code')) {
				errMsg = '系统接口' + errMsg.substr(errMsg.length - 3) + '异常'
			} else if (errMsg.includes('request:fail')) {
				errMsg = '请求失败'
			}
			console.log('errMsg', errMsg)
			if (!config.isShowLoading){
				uni.$snowy.modal.alert(errMsg)
			}
			reject(error)
		}).finally(() => {
			requestCount.value--
			if (requestCount.value === 0) {
				uni.$snowy.modal.closeLoading()
			}
		})
	})
}
export default request
