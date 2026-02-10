const noTokenBackUrl = "/pages/auth/login";
const hasTokenBackUrl = "/pages/first/index"
// 应用全局配置
export default {
	// 服务平台类型（SNOWY或SNOWY_CLOUD）
	SERVER_TYPE: 'SNOWY',
	// 是否是企业版
	IS_ENTTERPRISE: true,
	// 请求超时
	TIMEOUT: 60000,
	TOKEN_NAME: 'token',
	// Token前缀，注意最后有个空格，如不需要需设置空字符串 // Bearer
	TOKEN_PREFIX: '',
	// 系统基础配置，这些是数据库中保存起来的
	SYS_BASE_CONFIG: {
		// 默认logo
		SNOWY_SYS_LOGO: '/static/logo.png',
		// 背景图
		SNOWY_SYS_BACK_IMAGE: '',
		// 系统名称
		SNOWY_SYS_NAME: 'Snowy',
		// 版本
		SNOWY_SYS_VERSION: '2.0',
		// 版权
		SNOWY_SYS_COPYRIGHT: 'Snowy ©2022 Created by xiaonuo.vip',
		// 版权跳转URL
		SNOWY_SYS_COPYRIGHT_URL: 'https://www.xiaonuo.vip',
		// 默认文件存储
		SNOWY_SYS_DEFAULT_FILE_ENGINE: 'LOCAL',
		// 是否开启验证码
		SNOWY_SYS_DEFAULT_CAPTCHA_OPEN: 'true',
		// 默认重置密码
		SNOWY_SYS_DEFAULT_PASSWORD: '123456',
		//腾讯滑块验证码开关
		SNOWY_LOGIN_VERIFY_FLAG:'1'

	},
	// 是否启用环境配置页面
	ENABLE_CONFIG_PAGE: true,
	// 首页配置
	HOME_CONFIGS: [
		// 轮播
		{
			name: "轮播",
			code: "swiper",
			isShow: true,
		},
		// 公告
		{
			name: "公告",
			code: "notice",
			isShow: true,
		},
		// 待办
		{
			name: "待办",
			code: "todo",
			isShow: true,
		},
		// 日程
		{
			name: "日程",
			code: "schedule",
			isShow: true,
		},
		// 图表
		{
			name: "图表",
			code: "chart",
			isShow: true,
		},
	],
	// 没有token访问退回页面
	NO_TOKEN_BACK_URL: noTokenBackUrl,
	// 不需要登录（没有token）页面白名单
	NO_TOKEN_WHITE_LIST: [
		noTokenBackUrl, 
		'/', 
		'/pages/config/index', 
		'/pages/config/form', 
		'/pages/common/webview/index',
		'/pages/biz/share/shareTransfer',
		'/pages/biz/share/shareList',
		'/pages/biz/share/shareDetail',
		'/pages/biz/share/shareLogin',
		'/pages/publish/publish-article',
		'/pages/publish/select-image',
		'/pages/publish/publish-photos',
		'/pages/publish/publish-video',
		'/pages/publish/publish-small-video',
		'/pages/publish/publish-audio',
		'/pages/publish/cut-image',
		'/pages/search/index',
		'/pages/biz/audit/AuditDetailPage',
		'/pages/biz/audit/NoteWebPage',
		'/pages/activity/SelectOrg',
		// '/pages/home/index',
		'/pages/inspiration/index',
		'/pages/resource/ResourceHome',
		'/pages/caseLibrary/index'
	],
	// 有token访问退回页面
	HAS_TOKEN_BACK_URL: hasTokenBackUrl,
	// 登录（有token）可以访问的页面白名单
	HAS_TOKEN_WHITE_LIST: [
		hasTokenBackUrl, 
		'/pages/resource/index',
		'/pages/resource/upload',
		'/pages/msg/index',
		'/pages/msg/detail', 
		'/pages/work/index', 
		'/pages/dataCenter/index',
		'/packageA/pages/dataCenter/productDetail/index',
		'/packageA/pages/dataCenter/accountDetail/index',
		'/pages/mine/index', 
		// '/pages/mine/setting/index', 
		'/pages/mine/info/edit', 
		'/pages/mine/home-config/index', 
		'/pages/mine/pwd/index', 
		'/pages/mine/info/index',
		'/pages/topic/my-topic/index',
		'/pages/topic/create-topic/index',
		'/pages/topic/topic-review/index',
		'/pages/selectUsers/index',
		'/pages/topic/topic_detail/index',
		'/pages/topic/topic-library/index',
		'/pages/topic/selectOrg/index',
		'/pages/topic/topic-review/detail',
		'/pages/activity/create-activity/index',
		'/pages/activity/activity-detail/index',
		'/pages/activity/activity-library/index',
		'/pages/activity/activity-review/index',
		'/pages/activity/activity-review/detail',
		'/pages/task/create-task/index',
		'/pages/task/task-library/index',
		'/pages/task/task-detail/index',
		'/pages/task/relative-content/index',
		'/pages/review-process/index',
		'/pages/first/index',
		'/pages/resource/detail',
		'/pages/promotion-instructions/index',
		'/pages/promotion-instructions/DepartmentIndex',
		'/pages/biz/article/PersonalArticleLibraryHome',
		'/pages/rank-list/index',
		'/pages/publish/write-info',
		'/pages/manuscript-review/index',
		'/pages/biz/article/ai/AiWriteCreatePage',
		'/pages/biz/article/ai/AiHotCreatePage',
		'/pages/biz/article/WriteImageTextPage',
		'/pages/biz/article/WriteInfo',
		'/pages/biz/audit/AuditPage',
		'/pages/publish/selectChannel',
		'/pages/biz/audit/AuditProcessPage',
		'/pages/promotion-instructions/create',
		'/pages/promotion-instructions/detail',
		'/pages/first/MoreActivityListPage',
		'/pages/promotion-instructions/ScanSummarize',
		'/pages/topic/TopicProduct',
		'/pages/topic/ResourcePool',
		'/pages/promotion-instructions/projectReview',
		'/pages/promotion-instructions/ScanChildProgress',
		'/pages/resource/subjectDetail',
		'/pages/resource/calendarList',
		'/pages/promotion-instructions/SelectReceiver',
		'/pages/inspiration/index'



	],
	LISENSE_CODE: ""
}