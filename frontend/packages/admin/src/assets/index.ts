/**
 * 静态资源集中导出，全项目仅在此处书写资源路径，其余一律引用本模块常量。
 * 禁止在组件或其它文件中硬编码图片/图标路径。
 */

// 登录与品牌
import imgLogo from './images/logo.svg'
import imgFeishu from './images/feishu.svg'
import imgDingtalk from './images/dingtalk.svg'
import imgGithub from './images/github.svg'
import imgGoogle from './images/google.svg'
import imgMicrosoft from './images/microsoft.svg'
import imgWechat from './images/wechat.svg'

export const IMG_LOGO = imgLogo
export const IMG_FEISHU_LOGO = imgFeishu
export const IMG_DINGTALK_LOGO = imgDingtalk
export const IMG_GITHUB_LOGO = imgGithub
export const IMG_GOOGLE_LOGO = imgGoogle
export const IMG_MICROSOFT_LOGO = imgMicrosoft
export const IMG_WECHAT_LOGO = imgWechat

// 预留：作者区背景图，可替换为实际资源后取消注释
// import imgBrandBg from './images/brand-bg.jpg'
// export const IMG_BRAND_BG = imgBrandBg as string
