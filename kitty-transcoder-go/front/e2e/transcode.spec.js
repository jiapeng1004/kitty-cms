// @ts-check
import { test, expect } from '@playwright/test'

const API_BASE = process.env.TRANSCODER_API_URL || 'http://localhost:9703/kitty-transcoder'

test.describe('转码服务 E2E', () => {
  let accessKeyId
  let secretKey

  test.beforeAll(async () => {
    const res = await fetch(API_BASE + '/api/auth/access-key', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'e2e-test' })
    })
    const data = await res.json()
    accessKeyId = data.accessKeyId
    secretKey = data.secretKey
    if (!accessKeyId || !secretKey) throw new Error('创建 Access Key 失败')
  })

  test('登录页展示并可用', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByRole('heading', { name: /转码服务登录|AK\/SK/ })).toBeVisible()
    await page.getByPlaceholder('AK...').fill(accessKeyId)
    await page.getByPlaceholder('SK...').fill(secretKey)
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page).toHaveURL(/\/(tasks)?$/)
  })

  test('完整流程：登录 -> 策略列表 -> 创建策略 -> 任务列表 -> 创建任务 -> 任务详情', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('AK...').fill(accessKeyId)
    await page.getByPlaceholder('SK...').fill(secretKey)
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page).toHaveURL(/\//)

    await page.goto('/strategies')
    await expect(page.getByText('新建策略')).toBeVisible()
    await page.getByRole('button', { name: '新建策略' }).click()
    await page.getByLabel('名称').fill('E2E策略')
    await page.getByLabel('目标格式').fill('mp4')
    await page.getByRole('button', { name: '创建' }).click()
    await expect(page.getByText('创建成功')).toBeVisible()

    await page.goto('/tasks')
    await expect(page.getByRole('button', { name: '新建任务' })).toBeVisible()
    await page.getByRole('button', { name: '新建任务' }).click()
    await expect(page.getByText('新建转码任务')).toBeVisible()
    await page.getByPlaceholder('/path/to/video.mp4').fill('/tmp/test.mp4')
    await page.locator('.ant-select').click()
    await page.getByText('E2E策略', { exact: true }).waitFor({ state: 'visible', timeout: 10000 })
    await page.getByText('E2E策略', { exact: true }).click()
    await page.getByRole('button', { name: '提交' }).click()
    await expect(page.getByText('任务已创建')).toBeVisible()
    await expect(page).toHaveURL(/\/tasks\/task_/)

    await expect(page.getByText('输出路径').locator('..')).toBeVisible()
    await expect(page.getByText('状态').locator('..')).toBeVisible()
  })

  test('Access Key 列表页', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('AK...').fill(accessKeyId)
    await page.getByPlaceholder('SK...').fill(secretKey)
    await page.getByRole('button', { name: '登录' }).click()
    await page.goto('/access-keys')
    await expect(page.getByRole('button', { name: '创建 Access Key' })).toBeVisible()
    await expect(page.getByText('Access Key ID')).toBeVisible()
  })
})
