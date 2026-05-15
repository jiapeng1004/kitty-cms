import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadMaterialFiles } from '@/mam/utils/material_chunk_upload_flow'

export function useMaterialFileUpload(options: {
  getCatalogId: () => string | undefined
  getParentId?: () => string
  onFinished: () => void | Promise<void>
}) {
  const uploading = ref(false)
  const progressText = ref('')
  /** 多文件同时触发 customRequest 时串行执行，避免并发抢锁 */
  let uploadChain: Promise<void> = Promise.resolve()

  function uploadFiles(files: File[]) {
    const catalogId = options.getCatalogId()
    if (!catalogId) {
      message.warning('请先选择栏目')
      return Promise.resolve()
    }
    if (!files?.length) {
      return Promise.resolve()
    }
    uploadChain = uploadChain.then(() => runUpload(files))
    return uploadChain
  }

  async function runUpload(files: File[]) {
    uploading.value = true
    progressText.value = ''
    try {
      await uploadMaterialFiles(files, {
        catalogId: options.getCatalogId()!,
        parentId: options.getParentId?.() ?? '0',
        onProgress: (p) => {
          if (p.phase === 'prepare') {
            progressText.value = `${p.fileName}：准备上传…`
          } else if (p.phase === 'upload') {
            progressText.value = `${p.fileName}：${p.sentChunks}/${p.totalChunks} 片`
          } else {
            progressText.value = `${p.fileName}：完成`
          }
        }
      })
      message.success(files.length > 1 ? `已上传 ${files.length} 个文件` : '上传完成')
      await options.onFinished()
    } catch (e: unknown) {
      const err = e as { message?: string }
      message.error(err?.message || '上传失败')
    } finally {
      uploading.value = false
      progressText.value = ''
    }
  }

  return { uploading, progressText, uploadFiles }
}
