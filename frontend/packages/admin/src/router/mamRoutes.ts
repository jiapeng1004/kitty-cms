import type {RouteRecordRaw} from 'vue-router'
import MamLayout from '@/mam/layouts/Layout.vue'
import CatalogPermissionPage from '@/mam/views/CatalogPermissionPage.vue'
import StorageRoutePage from '@/mam/views/StorageRoutePage.vue'
import StorageManagePage from '@/mam/views/StorageManagePage.vue'
import MetadataManagePage from '@/mam/views/MetadataManagePage.vue'
import CatalogTreePage from '@/mam/views/CatalogTreePage.vue'
import MaterialWorkspacePage from '@/mam/views/MaterialWorkspacePage.vue'
import TaskCenterPage from '@/mam/views/TaskCenterPage.vue'
import TranscodePolicyPage from '@/mam/views/TranscodePolicyPage.vue'
import InternalMessagePage from '@/mam/views/InternalMessagePage.vue'
import ReviewWorkflowPage from '@/mam/views/ReviewWorkflowPage.vue'
import ResourceManagePage from '@/mam/views/ResourceManagePage.vue'
import ResourceDetailPage from '@/mam/views/ResourceDetailPage.vue'

const mamPageMeta = {hideAdminChrome: true} as const

/** MAM 业务页：挂在统一 RootLayout 下，由 Layout 根据 meta 切换 Mam 内容壳 */
export const mamContentRoutes: RouteRecordRaw[] = [
    {
        path: 'material/recycle',
        name: 'MaterialRecycleWorkspacePage',
        component: MaterialWorkspacePage,
        meta: {...mamPageMeta, flush: true, immersive: true, recycle: true}
    },
    {
        path: 'material',
        name: 'MaterialWorkspacePage',
        component: MaterialWorkspacePage,
        meta: {...mamPageMeta, flush: true, immersive: true}
    },
    {
        path: 'material/resource/:resourceId',
        name: 'ResourceDetailPage',
        component: ResourceDetailPage,
        meta: {...mamPageMeta, flush: true, immersive: true}
    },
    {
        path: 'catalog-tree',
        name: 'CatalogTreePage',
        component: CatalogTreePage,
        meta: mamPageMeta
    },
    {
        path: 'permissions',
        name: 'CatalogPermissionPage',
        component: CatalogPermissionPage,
        meta: mamPageMeta
    },
    {
        path: 'resources',
        name: 'ResourceManagePage',
        component: ResourceManagePage,
        meta: {...mamPageMeta, flush: true, immersive: true}
    },
    {
        path: 'storage-route',
        name: 'StorageRoutePage',
        component: StorageRoutePage,
        meta: mamPageMeta
    },
    {
        path: 'storage-manage',
        name: 'StorageManagePage',
        component: StorageManagePage,
        meta: {...mamPageMeta, flush: true, immersive: true}
    },
    {
        path: 'metadata',
        name: 'MetadataManagePage',
        component: MetadataManagePage,
        meta: mamPageMeta
    },
    {
        path: 'tasks',
        name: 'TaskCenterPage',
        component: TaskCenterPage,
        meta: mamPageMeta
    },
    {
        path: 'transcode-policy',
        name: 'TranscodePolicyPage',
        component: TranscodePolicyPage,
        meta: mamPageMeta
    },
    {
        path: 'messages',
        name: 'InternalMessagePage',
        component: InternalMessagePage,
        meta: mamPageMeta
    },
    {
        path: 'review',
        name: 'ReviewWorkflowPage',
        component: ReviewWorkflowPage,
        meta: mamPageMeta
    }
]

const embedMeta = {hideAdminChrome: true} as const

/** 嵌入模式：供 iframe / 第三方宿主使用，路径前缀 /embed */
export const embedMamRoutes: RouteRecordRaw[] = [
    {
        path: '/embed',
        component: MamLayout,
        meta: embedMeta,
        children: [
            {
                path: 'material/recycle',
                name: 'EmbedMaterialRecycleWorkspacePage',
                component: MaterialWorkspacePage,
                meta: {...embedMeta, flush: true, immersive: true, recycle: true}
            },
            {
                path: 'material',
                name: 'EmbedMaterialWorkspacePage',
                component: MaterialWorkspacePage,
                meta: {...embedMeta, flush: true, immersive: true}
            },
            {
                path: 'material/resource/:resourceId',
                name: 'EmbedResourceDetailPage',
                component: ResourceDetailPage,
                meta: {...embedMeta, flush: true, immersive: true}
            },
            {
                path: 'catalog-tree',
                name: 'EmbedCatalogTreePage',
                component: CatalogTreePage,
                meta: embedMeta
            },
            {
                path: 'permissions',
                name: 'EmbedCatalogPermissionPage',
                component: CatalogPermissionPage,
                meta: embedMeta
            },
            {
                path: 'resources',
                name: 'EmbedResourceManagePage',
                component: ResourceManagePage,
                meta: {...embedMeta, flush: true, immersive: true}
            },
            {
                path: 'storage-route',
                name: 'EmbedStorageRoutePage',
                component: StorageRoutePage,
                meta: embedMeta
            },
            {
                path: 'storage-manage',
                name: 'EmbedStorageManagePage',
                component: StorageManagePage,
                meta: {...embedMeta, flush: true, immersive: true}
            },
            {
                path: 'metadata',
                name: 'EmbedMetadataManagePage',
                component: MetadataManagePage,
                meta: embedMeta
            },
            {
                path: 'tasks',
                name: 'EmbedTaskCenterPage',
                component: TaskCenterPage,
                meta: embedMeta
            },
            {
                path: 'transcode-policy',
                name: 'EmbedTranscodePolicyPage',
                component: TranscodePolicyPage,
                meta: embedMeta
            },
            {
                path: 'messages',
                name: 'EmbedInternalMessagePage',
                component: InternalMessagePage,
                meta: embedMeta
            },
            {
                path: 'review',
                name: 'EmbedReviewWorkflowPage',
                component: ReviewWorkflowPage,
                meta: embedMeta
            }
        ]
    }
]
