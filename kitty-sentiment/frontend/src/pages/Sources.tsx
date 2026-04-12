import {
  Button,
  Card,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { useEffect, useState } from 'react'
import { errMessage } from '../api/client'
import {
  createSource,
  deleteSource,
  getSourceTypeTagSuggestions,
  listSources,
  updateSource,
  validateSourceSpider,
} from '../api/api_sources'
import PythonCodeEditor, { applySpiderAutoImports } from '../components/PythonCodeEditor'

const { Text } = Typography

interface SourceRow {
  id: number
  name: string
  url: string
  type: string
  enabled: boolean
  config?: Record<string, unknown>
  updatedAt: string
}

/** Python 字符串字面量转义（用于模板里 name = "..."） */
function escapePyString(s: string): string {
  return s.replace(/\\/g, '\\\\').replace(/"/g, '\\"')
}

/** 根据展示名称生成 Spider 代码模板；名称即 scrapy.Spider.name */
function spiderCodeTemplate(displayName: string): string {
  const n = displayName.trim() || 'demo_spider'
  return `class DemoSpider(scrapy.Spider):
    name = "${escapePyString(n)}"
    start_urls = ["https://example.com"]

    def parse(self, response):
        title = response.xpath("//title/text()").get() or "untitled"
        yield SentimentItem(
            title=title,
            url=response.url,
            content=response.text[:1000],
            publish_time=datetime.now(),
            sentiment_score=0.0,
            sentiment_label="neutral",
            keywords=title,
            source_id=1,
        )
`
}

export default function Sources() {
  const [rows, setRows] = useState<SourceRow[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [validateInfo, setValidateInfo] = useState<string>('')
  const [typeTagSuggestions, setTypeTagSuggestions] = useState<
    { tag: string; count: number }[]
  >([])
  const [form] = Form.useForm()

  const fetchSources = async () => {
    setLoading(true)
    try {
      const res = await listSources()
      setRows(res.data.items || [])
    } catch (e) {
      message.error(errMessage(e, '获取信息源失败'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSources()
  }, [])

  useEffect(() => {
    if (!open) {
      setTypeTagSuggestions([])
      return
    }
    getSourceTypeTagSuggestions(5)
      .then((res) => setTypeTagSuggestions(res.data.items || []))
      .catch(() => setTypeTagSuggestions([]))
  }, [open])

  const openCreate = () => {
    setEditingId(null)
    setValidateInfo('')
    form.resetFields()
    form.setFieldsValue({
      type: '',
      enabled: true,
      implMode: 'db',
      spiderClass: '',
      robotstxtObey: true,
      cookiesEnabled: false,
      spiderCode: spiderCodeTemplate('demo_spider'),
    })
    setOpen(true)
  }

  const openEdit = (row: SourceRow) => {
    setEditingId(row.id)
    setValidateInfo('')
    const cfg = (row.config || {}) as Record<string, unknown>
    form.setFieldsValue({
      name: row.name,
      url: row.url,
      type: row.type,
      implMode: (cfg.implementation as string) || ((cfg.spiderCode as string) ? 'db' : 'local'),
      enabled: row.enabled,
      spiderClass: (cfg.spiderClass as string) || '',
      robotstxtObey: cfg.robotstxtObey ?? true,
      cookiesEnabled: cfg.cookiesEnabled ?? false,
      spiderCode: (cfg.spiderCode as string) || spiderCodeTemplate(row.name),
    })
    setOpen(true)
  }

  const implMode = Form.useWatch('implMode', form) || 'db'
  const nameWatch = Form.useWatch('name', form) as string | undefined

  const appendTypeTag = (tag: string) => {
    const cur = String(form.getFieldValue('type') ?? '')
    const parts = cur
      .split(/[,，]/)
      .map((s) => s.trim())
      .filter(Boolean)
    if (parts.includes(tag)) return
    parts.push(tag)
    form.setFieldValue('type', parts.join(','))
  }

  /** 仅运行/代码相关字段；爬虫名 = 表单「名称」，不写入 config */
  const buildConfigFromForm = (v: Record<string, unknown>) => {
    const mode = String(v.implMode || 'db')
    const payload: Record<string, unknown> = {
      implementation: mode,
      robotstxtObey: Boolean(v.robotstxtObey ?? true),
      cookiesEnabled: Boolean(v.cookiesEnabled ?? false),
    }
    if (mode === 'db') {
      payload.spiderClass = String(v.spiderClass || '')
      payload.spiderCode = String(v.spiderCode || '')
    }
    return payload
  }

  const validateSpiderCode = async () => {
    const fields =
      implMode === 'db'
        ? ['implMode', 'name', 'spiderClass', 'spiderCode']
        : ['implMode', 'name']
    const v = await form.validateFields(fields)
    try {
      const cfg = buildConfigFromForm(v)
      const res = await validateSourceSpider({
        name: String(v.name || '').trim(),
        config: cfg,
      })
      if (res.data.ok) {
        const warns = (res.data.warnings || []) as string[]
        const tip = warns.length > 0 ? `（警告 ${warns.length} 条）` : ''
        setValidateInfo(`校验通过：${res.data.className} / ${res.data.spiderName}${tip}`)
        message.success('Spider 代码校验通过')
      } else {
        throw new Error(res.data.error || '校验失败')
      }
    } catch (e) {
      setValidateInfo('')
      message.error(errMessage(e, '校验失败'))
    }
  }

  const submit = async (v: Record<string, unknown>) => {
    try {
      const cfg = buildConfigFromForm(v)
      await validateSourceSpider({
        name: String(v.name || '').trim(),
        config: cfg,
      })
      const payload = {
        name: v.name,
        url: v.url,
        type: v.type,
        enabled: v.enabled,
        config: cfg,
      }
      if (editingId == null) {
        await createSource(payload)
        message.success('信息源已创建')
      } else {
        await updateSource(editingId, payload)
        message.success('信息源已更新')
      }
      setOpen(false)
      setEditingId(null)
      await fetchSources()
    } catch (e) {
      message.error(errMessage(e, '保存失败'))
    }
  }

  const remove = async (id: number) => {
    try {
      await deleteSource(id)
      message.success('已删除')
      await fetchSources()
    } catch (e) {
      message.error(errMessage(e, '删除失败'))
    }
  }

  const columns = [
    { title: '名称（即 Spider 名）', dataIndex: 'name', key: 'name', width: 200 },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 200,
      render: (v: string) => {
        const parts = String(v || '')
          .split(/[,，]/)
          .map((s) => s.trim())
          .filter(Boolean)
        if (parts.length === 0) return '—'
        return (
          <Space size={4} wrap>
            {parts.map((p) => (
              <Tag key={p} color="blue">
                {p}
              </Tag>
            ))}
          </Space>
        )
      },
    },
    {
      title: '运行参数',
      key: 'runtime',
      width: 220,
      render: (_: unknown, r: SourceRow) => {
        const cfg = (r.config || {}) as Record<string, unknown>
        const obey = cfg.robotstxtObey ?? true
        const ck = cfg.cookiesEnabled ?? false
        return (
          <Space size={4} wrap>
            <Tag color={obey ? 'green' : 'orange'}>robots {obey ? 'on' : 'off'}</Tag>
            <Tag color={ck ? 'blue' : 'default'}>cookies {ck ? 'on' : 'off'}</Tag>
          </Space>
        )
      },
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 90,
      render: (v: boolean) => <Tag color={v ? 'success' : 'default'}>{v ? '启用' : '禁用'}</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: unknown, r: SourceRow) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(r)}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => remove(r.id)}>
            删除
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="信息源与 Spider 编辑器（MVP）"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建信息源
          </Button>
        }
      >
        <Table rowKey="id" columns={columns} dataSource={rows} loading={loading} />
      </Card>

      <Modal
        title={editingId == null ? '新建信息源' : '编辑信息源'}
        open={open}
        width={900}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
      >
        <Form form={form} layout="vertical" onFinish={submit}>
          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item
              name="name"
              label="名称"
              rules={[{ required: true, message: '请填写名称' }]}
              extra="与 Scrapy spider 名一致：可填英文标识（如 bilibili_hot），也可直接填中文展示名。"
            >
              <Input style={{ width: 280 }} placeholder="例如：Bilibili热榜 或 bilibili_hot" />
            </Form.Item>
            <Form.Item name="url" label="URL" rules={[{ required: true }]}>
              <Input style={{ width: 320 }} />
            </Form.Item>
            <Form.Item
              name="type"
              label="类型"
              rules={[
                { required: true, message: '请填写类型标签' },
                { whitespace: true, message: '类型标签不能为空' },
              ]}
              extra={
                <div>
                  <div style={{ marginBottom: typeTagSuggestions.length ? 8 : 0 }}>
                    多个标签用英文逗号分隔；推荐词来自当前库中已有标签的使用频次（至多 5 个）。
                  </div>
                  {typeTagSuggestions.length > 0 ? (
                    <Space size={[6, 6]} wrap align="center">
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        推荐（点击追加）：
                      </Text>
                      {typeTagSuggestions.map((s) => (
                        <Tag
                          key={s.tag}
                          color="processing"
                          style={{ cursor: 'pointer', marginInlineEnd: 0 }}
                          onClick={() => appendTypeTag(s.tag)}
                        >
                          {s.tag}
                          <span style={{ opacity: 0.75 }}> ({s.count})</span>
                        </Tag>
                      ))}
                    </Space>
                  ) : null}
                </div>
              }
            >
              <Input style={{ width: 280 }} placeholder="例如：社交,热榜 或 资讯" />
            </Form.Item>
            <Form.Item name="enabled" label="启用" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item name="implMode" label="实现模式" rules={[{ required: true }]}>
              <Select style={{ width: 150 }}>
                <Select.Option value="db">db 动态类</Select.Option>
                <Select.Option value="local">local 本地类</Select.Option>
              </Select>
            </Form.Item>
          </Space>

          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item name="spiderClass" label="Spider Class(可选，仅 DB 模式)">
              <Input style={{ width: 220 }} placeholder="未填默认取第一个 Spider 子类" />
            </Form.Item>
            <Form.Item name="robotstxtObey" label="ROBOTSTXT_OBEY" valuePropName="checked">
              <Switch checkedChildren="true" unCheckedChildren="false" />
            </Form.Item>
            <Form.Item name="cookiesEnabled" label="COOKIES_ENABLED" valuePropName="checked">
              <Switch checkedChildren="true" unCheckedChildren="false" />
            </Form.Item>
          </Space>

          {implMode === 'db' ? (
            <Form.Item
              name="spiderCode"
              label="Spider 代码"
              rules={[{ required: true, message: '请输入 spiderCode' }]}
              extra={
                <span>
                  可直接写代码无需 import；后端自动注入常见依赖。类内 <Text code>name</Text>{' '}
                  建议与上方「名称」一致。如需在编辑器里显式写出 import，可用下方「自动补全 import」。
                </span>
              }
            >
              <PythonCodeEditor minRows={16} />
            </Form.Item>
          ) : (
            <Form.Item
              label="Local 模式说明"
              extra="将使用上方「名称」作为本地 Spider 类名（与存储的 spiderName 一致）。"
            >
              <Input disabled value="已启用 local 模式，运行时将按「名称」加载 src/spiders 中的 Spider" />
            </Form.Item>
          )}
          <Space wrap size={[8, 8]} align="start">
            {implMode === 'db' ? (
              <>
                <Button
                  type="default"
                  onClick={() =>
                    form.setFieldValue('spiderCode', spiderCodeTemplate(String(nameWatch || '')))
                  }
                >
                  插入模板
                </Button>
                <Button
                  type="default"
                  onClick={() => {
                    const cur = (form.getFieldValue('spiderCode') as string) || ''
                    form.setFieldValue('spiderCode', applySpiderAutoImports(cur))
                  }}
                >
                  自动补全 import
                </Button>
              </>
            ) : null}
            <Button type="default" onClick={validateSpiderCode}>
              校验 Spider 代码
            </Button>
            {validateInfo ? <Text type="success">{validateInfo}</Text> : null}
          </Space>
        </Form>
      </Modal>
    </div>
  )
}
