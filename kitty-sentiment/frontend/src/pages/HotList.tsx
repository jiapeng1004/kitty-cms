import { Card, Table, Tag, Button, Input, Select, DatePicker, Typography, message } from 'antd'
import { SearchOutlined, RestOutlined } from '@ant-design/icons'
import { useCallback, useEffect, useState } from 'react'
import dayjs from 'dayjs'
import { errMessage } from '../api/client'
import { listSources } from '../api/api_sources'
import { listSentiments } from '../api/api_sentiment'

const { Title } = Typography
const { RangePicker } = DatePicker

interface Sentiment {
  id: number
  title: string
  url: string
  sentimentScore: number
  sentimentLabel: string
  sourceName?: string
  publishTime: string
}

interface SourceOpt {
  id: number
  name: string
}

export default function HotList() {
  const [data, setData] = useState<Sentiment[]>([])
  const [loading, setLoading] = useState(false)
  const [sources, setSources] = useState<SourceOpt[]>([])
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [total, setTotal] = useState(0)

  const [keywordInput, setKeywordInput] = useState('')
  const [applied, setApplied] = useState({
    keyword: '',
    sentimentLabel: '',
    sourceId: '',
    dateRange: undefined as [dayjs.Dayjs, dayjs.Dayjs] | undefined,
  })

  useEffect(() => {
    listSources()
      .then((res) => setSources(res.data.items || []))
      .catch(() => {})
  }, [])

  const fetchSentiments = useCallback(async () => {
    setLoading(true)
    try {
      const params: Record<string, string | number> = {
        page,
        page_size: pageSize,
      }

      if (applied.sentimentLabel) {
        params.sentiment_label = applied.sentimentLabel
      }

      if (applied.sourceId) {
        params.source_id = Number(applied.sourceId)
      }

      if (applied.keyword.trim()) {
        params.keyword = applied.keyword.trim()
      }

      if (applied.dateRange?.[0] && applied.dateRange?.[1]) {
        params.start_time = applied.dateRange[0].startOf('day').toISOString()
        params.end_time = applied.dateRange[1].endOf('day').toISOString()
      }

      const response = await listSentiments(params)
      setData(response.data.items || [])
      setTotal(response.data.pagination?.total ?? 0)
    } catch (error) {
      console.error('获取舆情列表失败:', error)
      message.error(errMessage(error, '获取舆情列表失败'))
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, applied])

  useEffect(() => {
    fetchSentiments()
  }, [fetchSentiments])

  const runSearch = () => {
    setApplied((prev) => ({ ...prev, keyword: keywordInput }))
    setPage(1)
  }

  const getSentimentColor = (score: number) => {
    if (score > 0.3) return 'success'
    if (score < -0.3) return 'error'
    return 'warning'
  }

  const columns = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      width: 400,
      render: (text: string, record: Sentiment) => (
        <a href={record.url} target="_blank" rel="noopener noreferrer">
          {text}
        </a>
      ),
    },
    {
      title: '情感倾向',
      dataIndex: 'sentimentLabel',
      key: 'sentimentLabel',
      width: 120,
      render: (text: string, record: Sentiment) => (
        <Tag color={getSentimentColor(record.sentimentScore)}>
          {text === 'positive' ? '正面' : text === 'negative' ? '负面' : '中性'}
        </Tag>
      ),
    },
    {
      title: '情感得分',
      dataIndex: 'sentimentScore',
      key: 'sentimentScore',
      width: 100,
      render: (text: number) => (
        <span style={{ fontWeight: 500 }}>{Number(text ?? 0).toFixed(3)}</span>
      ),
    },
    {
      title: '信息源',
      dataIndex: 'sourceName',
      key: 'sourceName',
      width: 150,
    },
    {
      title: '发布时间',
      dataIndex: 'publishTime',
      key: 'publishTime',
      width: 180,
      render: (text: string) => dayjs(text).format('YYYY-MM-DD HH:mm:ss'),
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Title level={4}>舆情榜单</Title>
        <div style={{ marginBottom: 16, display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Input
            placeholder="搜索标题或关键词"
            prefix={<SearchOutlined />}
            style={{ width: 220 }}
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
            onPressEnter={runSearch}
          />
          <Select
            placeholder="信息源"
            allowClear
            style={{ width: 160 }}
            options={sources.map((s) => ({ label: s.name, value: String(s.id) }))}
            value={applied.sourceId || undefined}
            onChange={(value) => {
              setApplied((p) => ({ ...p, sourceId: value ?? '' }))
              setPage(1)
            }}
          />
          <Select
            placeholder="情感倾向"
            style={{ width: 150 }}
            options={[
              { label: '全部', value: '' },
              { label: '正面', value: 'positive' },
              { label: '中性', value: 'neutral' },
              { label: '负面', value: 'negative' },
            ]}
            value={applied.sentimentLabel || undefined}
            onChange={(value) => {
              setApplied((p) => ({ ...p, sentimentLabel: value ?? '' }))
              setPage(1)
            }}
          />
          <RangePicker
            value={applied.dateRange}
            onChange={(dates) => {
              setApplied((p) => ({
                ...p,
                dateRange: dates as [dayjs.Dayjs, dayjs.Dayjs] | undefined,
              }))
              setPage(1)
            }}
          />
          <Button type="primary" icon={<RestOutlined />} onClick={runSearch} loading={loading}>
            查询
          </Button>
        </div>
        <Table
          columns={columns}
          dataSource={data}
          loading={loading}
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            onChange: (p, ps) => {
              setPage(p)
              setPageSize(ps || 20)
            },
          }}
          rowKey="id"
        />
      </Card>
    </div>
  )
}
