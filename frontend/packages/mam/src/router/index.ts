import {createRouter, createWebHistory} from 'vue-router'
import Layout from '@/layouts/Layout.vue'
import CatalogPermissionPage from '@/views/CatalogPermissionPage.vue'
import ResourceManagePage from '@/views/ResourceManagePage.vue'
import StorageRoutePage from '@/views/StorageRoutePage.vue'
import MetadataManagePage from '@/views/MetadataManagePage.vue'
import CatalogTreePage from '@/views/CatalogTreePage.vue'
import TaskCenterPage from '@/views/TaskCenterPage.vue'
import TranscodePolicyPage from '@/views/TranscodePolicyPage.vue'
import InternalMessagePage from '@/views/InternalMessagePage.vue'
import ReviewWorkflowPage from '@/views/ReviewWorkflowPage.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: Layout,
            redirect: '/catalog-tree',
            children: [
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
                    component: ResourceManagePage
                },
                {
                    path: 'storage-route',
                    name: 'StorageRoutePage',
                    component: StorageRoutePage
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
                    component: ResourceManagePage
                },
                {
                    path: 'storage-route',
                    name: 'EmbedStorageRoutePage',
                    component: StorageRoutePage
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

export default router
