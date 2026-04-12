import {
  Card,
  Table,
  Tag,
  Space,
  Button,
  Modal,
  Form,
  Input,
  Select,
  Switch,
  DatePicker,
  message,
  Typography,
  Divider,
} from 'antd'
import {
  PlusOutlined,
  PlayCircleOutlined,
  StopOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
} from '@ant-design/icons'
import { useEffect, useState } from 'react'
import { Dayjs } from 'dayjs'
import { errMessage } from '../api/client'
import { listSources } from '../api/api_sources'
import {
  createTask,
  deleteTask,
  exportTaskExecutionsCsv,
  listTaskExecutions,
  listTasks,
  startTask,
  stopTask,
  updateTask,
  validateTaskFrequency,
} from '../api/api_tasks'

const { Text, Paragraph } = Typography

interface Task {
  id: number
  name: string
  type: string
  sourceId: number
  sourceName: string
  status: string
  lastRun: string
  nextRun: string
  frequency: string
  runtimeConfig?: {
    robotstxtObey?: boolean
    cookiesEnabled?: boolean
  }
  lastExecution?: {
    id: number
    status: string
    startedAt?: string
    finishedAt?: string
    errorMessage?: string
  } | null
}

interface TaskExecution {
  id: number
  status: string
  triggerMode?: string
  spiderName?: string
  startedAt?: string
  finishedAt?: string
  itemsCount?: number
  requestCount?: number
  errorMessage?: string
  errorTrace?: string
}

const CRON_PRESETS: { label: string; value: string }[] = [
  { label: '仅手动', value: 'manual' },
  { label: '每小时整点', value: '0 * * * *' },
  { label: '每 6 小时', value: '0 */6 * * *' },
  { label: '每天 0 点', value: '0 0 * * *' },
  { label: '每周一 9 点', value: '0 9 * * 1' },
]

export default function TaskManager() {
  const [data, setData] = useState<Task[]>([])
  const [loading, setLoading] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [sources, setSources] = useState<{ id: number; name: string }[]>([])
  const [historyOpen, setHistoryOpen] = useState(false)
  const [historyRows, setHistoryRows] = useState<TaskExecution[]>([])
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historyTask, setHistoryTask] = useState<Task | null>(null)
  const [historyStatus, setHistoryStatus] = useState<string | undefined>(undefined)
  const [historyRange, setHistoryRange] = useState<[Dayjs, Dayjs] | null>(null)
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailExecution, setDetailExecution] = useState<TaskExecution | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    fetchTasks()
    fetchSources()
  }, [])

  const fetchSources = async () => {
    try {
      const response = await listSources()
      setSources(response.data.items || [])
    } catch (error) {
      console.error('获取信息源失败:', error)
    }
  }

  const fetchTasks = async () => {
    setLoading(true)
    try {
      const response = await listTasks()
      setData(response.data.items || [])
    } catch (error) {
      console.error('获取任务列表失败:', error)
      message.error(errMessage(error, '获取任务列表失败'))
    } finally {
      setLoading(false)
    }
  }

  const openCreate = () => {
    setEditingId(null)
    form.resetFields()
    form.setFieldsValue({
      type: 'hotlist',
      frequency: 'manual',
      runtimeConfig: { robotstxtObey: true, cookiesEnabled: false },
    })
    setIsModalOpen(true)
  }

  const openEdit = (record: Task) => {
    setEditingId(record.id)
    form.setFieldsValue({
      name: record.name,
      type: record.type,
      sourceId: record.sourceId,
      frequency: record.frequency || 'manual',
      runtimeConfig: {
        robotstxtObey: record.runtimeConfig?.robotstxtObey ?? true,
        cookiesEnabled: record.runtimeConfig?.cookiesEnabled ?? false,
      },
    })
    setIsModalOpen(true)
  }

  const validateFrequencyRule = async (_: unknown, value: string) => {
    const v = (value ?? '').trim()
    if (!v) {
      throw new Error('请输入爬取频率（Cron）或选择 manual')
    }
    const { data } = await validateTaskFrequency(v)
    if (!data.ok) {
      throw new Error(data.error || 'Cron 表达式无效')
    }
    if (data.frequency && data.frequency !== v) {
      form.setFieldValue('frequency', data.frequency)
    }
  }

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      if (editingId != null) {
        await updateTask(editingId, values)
        message.success('任务已更新')
      } else {
        await createTask(values)
        message.success('任务创建成功')
      }
      setIsModalOpen(false)
      setEditingId(null)
      form.resetFields()
      fetchTasks()
    } catch (error) {
      message.error(errMessage(error, editingId != null ? '更新失败' : '创建失败'))
    }
  }

  const handleStartTask = async (id: number) => {
    const now = new Date().toISOString()
    setData((prev) =>
      prev.map((t) => (t.id === id ? { ...t, status: 'running', lastRun: now } : t)),
    )
    try {
      await startTask(id)
      message.success('任务已启动，爬虫在后台执行')
    } catch (error) {
      message.error(errMessage(error, '启动任务失败'))
    } finally {
      await fetchTasks()
    }
  }

  const handleStopTask = async (id: number) => {
    setData((prev) =>
      prev.map((t) => (t.id === id ? { ...t, status: 'stopped' } : t)),
    )
    try {
      await stopTask(id)
      message.success('任务已停止')
    } catch (error) {
      message.error(errMessage(error, '停止任务失败'))
    } finally {
      await fetchTasks()
    }
  }

  const handleDeleteTask = async (id: number) => {
    try {
      await deleteTask(id)
      message.success('任务已删除')
    } catch (error) {
      message.error(errMessage(error, '删除任务失败'))
    } finally {
      await fetchTasks()
    }
  }

  const loadHistory = async (
    task: Task,
    opts?: { status?: string; range?: [Dayjs, Dayjs] | null },
  ) => {
    const status = opts?.status ?? historyStatus
    const range = opts?.range ?? historyRange
    setHistoryLoading(true)
    try {
      const res = await listTaskExecutions(task.id, {
        page: 1,
        page_size: 100,
        status,
        start_at: range?.[0]?.toISOString(),
        end_at: range?.[1]?.toISOString(),
      })
      setHistoryRows(res.data.items || [])
    } catch (error) {
      message.error(errMessage(error, '获取执行历史失败'))
    } finally {
      setHistoryLoading(false)
    }
  }

  const openHistory = async (task: Task) => {
    setHistoryTask(task)
    setHistoryStatus(undefined)
    setHistoryRange(null)
    setHistoryRows([])
    setHistoryOpen(true)
    await loadHistory(task, { status: undefined, range: null })
  }

  const exportHistoryCsv = async () => {
    if (!historyTask) return
    try {
      const res = await exportTaskExecutionsCsv(historyTask.id, {
        status: historyStatus,
        start_at: historyRange?.[0]?.toISOString(),
        end_at: historyRange?.[1]?.toISOString(),
      })
      const blob = res.data as Blob
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `task-${historyTask.id}-executions.csv`
      a.click()
      URL.revokeObjectURL(url)
      message.success('已导出 CSV')
    } catch (error) {
      message.error(errMessage(error, '导出 CSV 失败'))
    }
  }

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (text: string) => (
        <Tag color={text === 'hotlist' ? 'blue' : 'green'}>{text}</Tag>
      ),
    },
    {
      title: '信息源',
      dataIndex: 'sourceName',
      key: 'sourceName',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (text: string) => (
        <Tag
          color={
            text === 'running'
              ? 'processing'
              : text === 'completed'
                ? 'success'
                : text === 'failed'
                  ? 'error'
                  : text === 'stopped'
                    ? 'default'
                    : 'warning'
          }
        >
          {text === 'running'
            ? '运行中'
            : text === 'completed'
              ? '已完成'
              : text === 'failed'
                ? '失败'
                : text === 'stopped'
                  ? '已停止'
                  : '待机'}
        </Tag>
      ),
    },
    {
      title: '爬取频率 (Cron)',
      dataIndex: 'frequency',
      key: 'frequency',
      ellipsis: true,
      width: 220,
    },
    {
      title: '运行参数',
      key: 'runtimeConfig',
      width: 180,
      render: (_: unknown, record: Task) => (
        <Space size={4} wrap>
          <Tag color={record.runtimeConfig?.robotstxtObey ?? true ? 'green' : 'orange'}>
            robots {record.runtimeConfig?.robotstxtObey ?? true ? 'on' : 'off'}
          </Tag>
          <Tag color={record.runtimeConfig?.cookiesEnabled ? 'blue' : 'default'}>
            cookies {record.runtimeConfig?.cookiesEnabled ? 'on' : 'off'}
          </Tag>
        </Space>
      ),
    },
    {
      title: '上次运行',
      dataIndex: 'lastRun',
      key: 'lastRun',
      width: 180,
    },
    {
      title: '最近结果',
      key: 'lastExecution',
      width: 220,
      render: (_: unknown, record: Task) => {
        const x = record.lastExecution
        if (!x) return <Tag>暂无</Tag>
        return (
          <Space direction="vertical" size={0}>
            <Tag color={x.status === 'completed' ? 'success' : x.status === 'failed' ? 'error' : 'processing'}>
              {x.status}
            </Tag>
            {x.errorMessage ? (
              <Text type="danger" ellipsis style={{ maxWidth: 180 }}>
                {x.errorMessage}
              </Text>
            ) : null}
          </Space>
        )
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      render: (_: unknown, record: Task) => (
        <Space>
          {record.status === 'running' ? (
            <Button
              type="link"
              icon={<StopOutlined />}
              onClick={() => handleStopTask(record.id)}
            >
              停止
            </Button>
          ) : (
            <Button
              type="link"
              icon={<PlayCircleOutlined />}
              onClick={() => handleStartTask(record.id)}
            >
              启动
            </Button>
          )}
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(record)}>
            编辑
          </Button>
          <Button type="link" onClick={() => openHistory(record)}>
            历史
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteTask(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="爬取任务管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建任务
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={data}
          loading={loading}
          rowKey="id"
        />
      </Card>

      <Modal
        title={editingId != null ? '编辑任务' : '新建任务'}
        open={isModalOpen}
        onCancel={() => {
          setIsModalOpen(false)
          setEditingId(null)
          form.resetFields()
        }}
        onOk={() => form.submit()}
        destroyOnClose
        width={560}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            type: 'hotlist',
            frequency: 'manual',
            runtimeConfig: { robotstxtObey: true, cookiesEnabled: false },
          }}
        >
          <Form.Item
            name="name"
            label="任务名称"
            rules={[{ required: true, message: '请输入任务名称' }]}
          >
            <Input placeholder="例如：Bilibili热榜监控" />
          </Form.Item>
          <Form.Item
            name="type"
            label="任务类型"
            rules={[{ required: true, message: '请选择任务类型' }]}
          >
            <Select>
              <Select.Option value="hotlist">热点榜单</Select.Option>
              <Select.Option value="keyword">关键词监控</Select.Option>
              <Select.Option value="source">指定源监控</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="sourceId"
            label="信息源"
            rules={[{ required: true, message: '请选择信息源' }]}
          >
            <Select placeholder="选择要爬取的信息源">
              {sources.map((source) => (
                <Select.Option key={source.id} value={source.id}>
                  {source.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item label="常用 Cron 预设">
            <Space size={[8, 8]} wrap>
              {CRON_PRESETS.map((p) => (
                <Button
                  key={p.value}
                  size="small"
                  type="dashed"
                  onClick={() => {
                    form.setFieldsValue({ frequency: p.value })
                    form.validateFields(['frequency']).catch(() => {})
                  }}
                >
                  {p.label}
                </Button>
              ))}
            </Space>
          </Form.Item>

          <Form.Item
            name="frequency"
            label="爬取频率"
            extra={
              <Paragraph type="secondary" style={{ marginBottom: 0, fontSize: 12 }}>
                填写 Unix 风格 <Text code>5 段 Cron</Text>（分 时 日 月 周），或填写{' '}
                <Text code>manual</Text> 表示仅手动执行、不由调度器按时间表跑。调度执行需另行对接 APScheduler
                等。
              </Paragraph>
            }
            rules={[{ required: true }, { validator: validateFrequencyRule }]}
          >
            <Input placeholder="例如：0 */6 * * * 或 manual" autoComplete="off" />
          </Form.Item>

          <Form.Item label="任务运行时参数（每个任务独立）">
            <Space size={24} wrap>
              <Form.Item
                name={['runtimeConfig', 'robotstxtObey']}
                valuePropName="checked"
                noStyle
              >
                <Switch checkedChildren="遵守 robots" unCheckedChildren="忽略 robots" />
              </Form.Item>
              <Form.Item
                name={['runtimeConfig', 'cookiesEnabled']}
                valuePropName="checked"
                noStyle
              >
                <Switch checkedChildren="启用 cookies" unCheckedChildren="禁用 cookies" />
              </Form.Item>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`执行历史 - ${historyTask?.name || ''}`}
        open={historyOpen}
        onCancel={() => setHistoryOpen(false)}
        footer={null}
        width={900}
      >
        <Space style={{ marginBottom: 12 }} wrap>
          <Select
            allowClear
            placeholder="状态筛选"
            style={{ width: 140 }}
            value={historyStatus}
            onChange={(v) => setHistoryStatus(v)}
          >
            <Select.Option value="running">running</Select.Option>
            <Select.Option value="completed">completed</Select.Option>
            <Select.Option value="failed">failed</Select.Option>
            <Select.Option value="queued">queued</Select.Option>
          </Select>
          <DatePicker.RangePicker
            showTime
            value={historyRange}
            onChange={(v) => {
              if (!v || !v[0] || !v[1]) {
                setHistoryRange(null)
                return
              }
              setHistoryRange([v[0], v[1]])
            }}
          />
          <Button
            onClick={() => {
              if (historyTask) loadHistory(historyTask)
            }}
          >
            查询
          </Button>
          <Button
            onClick={() => {
              setHistoryStatus(undefined)
              setHistoryRange(null)
              if (historyTask) loadHistory(historyTask, { status: undefined, range: null })
            }}
          >
            重置
          </Button>
          <Button icon={<DownloadOutlined />} onClick={() => exportHistoryCsv()}>
            导出 CSV
          </Button>
        </Space>
        <Table
          rowKey="id"
          loading={historyLoading}
          dataSource={historyRows}
          pagination={{ pageSize: 8 }}
          columns={[
            { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
            { title: '触发', dataIndex: 'triggerMode', key: 'triggerMode', width: 90 },
            { title: 'Spider', dataIndex: 'spiderName', key: 'spiderName', width: 140 },
            {
              title: '状态',
              dataIndex: 'status',
              key: 'status',
              width: 90,
              render: (v: string) => (
                <Tag color={v === 'completed' ? 'success' : v === 'failed' ? 'error' : 'processing'}>{v}</Tag>
              ),
            },
            { title: '开始时间', dataIndex: 'startedAt', key: 'startedAt', width: 180 },
            { title: '结束时间', dataIndex: 'finishedAt', key: 'finishedAt', width: 180 },
            { title: '条数', dataIndex: 'itemsCount', key: 'itemsCount', width: 80 },
            { title: '请求数', dataIndex: 'requestCount', key: 'requestCount', width: 90 },
            {
              title: '失败信息',
              dataIndex: 'errorMessage',
              key: 'errorMessage',
              render: (_: string | undefined, row: TaskExecution) =>
                row.errorMessage ? (
                  <Space>
                    <Text type="danger" ellipsis style={{ maxWidth: 220 }}>
                      {row.errorMessage}
                    </Text>
                    <Button
                      type="link"
                      size="small"
                      onClick={() => {
                        setDetailExecution(row)
                        setDetailOpen(true)
                      }}
                    >
                      详情
                    </Button>
                  </Space>
                ) : (
                  '-'
                ),
            },
          ]}
        />
        <Divider style={{ margin: '12px 0' }} />
        <Text type="secondary">每次任务执行都会落库，支持查看成功/失败及失败原因。</Text>
      </Modal>

      <Modal
        title={`失败详情 - 执行#${detailExecution?.id || ''}`}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={null}
        width={860}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <Text strong>错误摘要</Text>
          <Input.TextArea value={detailExecution?.errorMessage || ''} autoSize={{ minRows: 2, maxRows: 4 }} readOnly />
          <Text strong>异常堆栈</Text>
          <Input.TextArea
            value={detailExecution?.errorTrace || ''}
            autoSize={{ minRows: 10, maxRows: 20 }}
            readOnly
            style={{ fontFamily: 'Consolas, Menlo, monospace' }}
          />
        </Space>
      </Modal>
    </div>
  )
}
