import {createRouter, createWebHistory} from 'vue-router'
import {getToken, redirectToLogin} from '@/utils/authRedirect'
import Layout from '@/layouts/Layout.vue'
import CatalogPermissionPage from '@/views/CatalogPermissionPage.vue'
import StorageRoutePage from '@/views/StorageRoutePage.vue'
import StorageManagePage from '@/views/StorageManagePage.vue'
import MetadataManagePage from '@/views/MetadataManagePage.vue'
import CatalogTreePage from '@/views/CatalogTreePage.vue'
import MaterialWorkspacePage from '@/views/MaterialWorkspacePage.vue'
import TaskCenterPage from '@/views/TaskCenterPage.vue'
import TranscodePolicyPage from '@/views/TranscodePolicyPage.vue'
import InternalMessagePage from '@/views/InternalMessagePage.vue'
import ReviewWorkflowPage from '@/views/ReviewWorkflowPage.vue'
import ResourceManagePage from '@/views/ResourceManagePage.vue'
import ResourceDetailPage from '@/views/ResourceDetailPage.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: Layout,
            redirect: '/material',
            children: [
                {
                    path: 'material/recycle',
                    name: 'MaterialRecycleWorkspacePage',
                    component: MaterialWorkspacePage,
                    meta: { flush: true, immersive: true, recycle: true }
                },
                {
                    path: 'material',
                    name: 'MaterialWorkspacePage',
                    component: MaterialWorkspacePage,
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'material/resource/:resourceId',
                    name: 'ResourceDetailPage',
                    component: ResourceDetailPage,
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'catalog-tree',
                    name: 'CatalogTreePage',
                    component: CatalogTreePage
                },
                {
                    path: 'permissions',
                    name: 'CatalogPermissionPage',
                    component: CatalogPermissionPage
                },
                {
                    path: 'resources',
                    name: 'ResourceManagePage',
                    component: ResourceManagePage,
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'storage-route',
                    name: 'StorageRoutePage',
                    component: StorageRoutePage
                },
                {
                    path: 'storage-manage',
                    name: 'StorageManagePage',
                    component: StorageManagePage,
                    /** 与素材库一致：隐藏全局顶栏，由页面内返回条导航，避免「从素材进来仍顶着整站导航」 */
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'metadata',
                    name: 'MetadataManagePage',
                    component: MetadataManagePage
                },
                {
                    path: 'tasks',
                    name: 'TaskCenterPage',
                    component: TaskCenterPage
                },
                {
                    path: 'transcode-policy',
                    name: 'TranscodePolicyPage',
                    component: TranscodePolicyPage
                },
                {
                    path: 'messages',
                    name: 'InternalMessagePage',
                    component: InternalMessagePage
                },
                {
                    path: 'review',
                    name: 'ReviewWorkflowPage',
                    component: ReviewWorkflowPage
                }
            ]
        },
        {
            path: '/embed',
            children: [
                {
                    path: 'material/recycle',
                    name: 'EmbedMaterialRecycleWorkspacePage',
                    component: MaterialWorkspacePage,
                    meta: { flush: true, immersive: true, recycle: true }
                },
                {
                    path: 'material',
                    name: 'EmbedMaterialWorkspacePage',
                    component: MaterialWorkspacePage,
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'material/resource/:resourceId',
                    name: 'EmbedResourceDetailPage',
                    component: ResourceDetailPage,
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'catalog-tree',
                    name: 'EmbedCatalogTreePage',
                    component: CatalogTreePage
                },
                {
                    path: 'permissions',
                    name: 'EmbedCatalogPermissionPage',
                    component: CatalogPermissionPage
                },
                {
                    path: 'resources',
                    name: 'EmbedResourceManagePage',
                    component: ResourceManagePage,
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'storage-route',
                    name: 'EmbedStorageRoutePage',
                    component: StorageRoutePage
                },
                {
                    path: 'storage-manage',
                    name: 'EmbedStorageManagePage',
                    component: StorageManagePage,
                    meta: { flush: true, immersive: true }
                },
                {
                    path: 'metadata',
                    name: 'EmbedMetadataManagePage',
                    component: MetadataManagePage
                },
                {
                    path: 'tasks',
                    name: 'EmbedTaskCenterPage',
                    component: TaskCenterPage
                },
                {
                    path: 'transcode-policy',
                    name: 'EmbedTranscodePolicyPage',
                    component: TranscodePolicyPage
                },
                {
                    path: 'messages',
                    name: 'EmbedInternalMessagePage',
                    component: InternalMessagePage
                },
                {
                    path: 'review',
                    name: 'EmbedReviewWorkflowPage',
                    component: ReviewWorkflowPage
                }
            ]
        }
    ]
})

/** 无 token 时整站受保护，统一跳转登录（由 VITE_LOGIN_URL 或同域 /login 承载） */
router.beforeEach((to, _from, next) => {
    if (getToken()) {
        next()
        return
    }
    const path = to.path
    if (path === '/login' || path === '/register') {
        next()
        return
    }
    redirectToLogin()
    next(false)
})

export default router
