import { Card, Row, Col, Statistic, Alert, Typography, message, Tag } from 'antd'
import { FireOutlined, EyeOutlined, CommentOutlined, RestOutlined } from '@ant-design/icons'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { errMessage } from '../api/client'
import { getStats, listSentiments } from '../api/api_sentiment'
import SentimentChart from '../components/SentimentChart'
import SentimentTrendChart from '../components/SentimentTrendChart'

const { Text } = Typography

interface Stats {
  totalSentiments: number
  todaySentiments: number
  activeSources: number
  runningTasks: number
}

interface HotItem {
  id: number
  title: string
  url: string
  sentimentScore: number
  sentimentLabel: string
  views?: number
  likes?: number
  comments?: number
  publishTime: string
}

export default function Dashboard() {
  const [stats, setStats] = useState<Stats>({
    totalSentiments: 0,
    todaySentiments: 0,
    activeSources: 0,
    runningTasks: 0,
  })
  const [hotList, setHotList] = useState<HotItem[]>([])
  const [sentimentData, setSentimentData] = useState<HotItem[]>([])
  const [trendData, setTrendData] = useState<
    { time: string; positive: number; neutral: number; negative: number }[]
  >([])

  useEffect(() => {
    fetchStats()
    fetchHotList()
  }, [])

  const fetchStats = async () => {
    try {
      const response = await getStats()
      setStats(response.data)
    } catch (error) {
      console.error('获取统计信息失败:', error)
      message.error(errMessage(error, '获取统计信息失败，请稍后重试'))
    }
  }

  const fetchHotList = async () => {
    try {
      const response = await listSentiments({
        page: 1,
        page_size: 10,
        sort: 'publish_time',
        order: 'desc',
      })
      const items = response.data.items || []
      setHotList(items)
      setSentimentData(items)
    } catch (error) {
      console.error('获取热门舆情失败:', error)
      message.error(errMessage(error, '获取热门舆情失败，请稍后重试'))
    }
  }

  const handleRefresh = async () => {
    await fetchStats()
    await fetchHotList()
  }

  useEffect(() => {
    const mockTrendData = [
      { time: '00:00', positive: 12, neutral: 20, negative: 8 },
      { time: '04:00', positive: 15, neutral: 25, negative: 10 },
      { time: '08:00', positive: 18, neutral: 30, negative: 12 },
      { time: '12:00', positive: 20, neutral: 35, negative: 15 },
      { time: '16:00', positive: 17, neutral: 32, negative: 13 },
      { time: '20:00', positive: 19, neutral: 38, negative: 16 },
    ]
    setTrendData(mockTrendData)
  }, [])

  const getSentimentColor = (score: number) => {
    if (score > 0.3) return 'success'
    if (score < -0.3) return 'error'
    return 'warning'
  }

  return (
    <div style={{ padding: 24 }}>
      <Alert
        message="爬虫合规性提醒"
        description={
          <div>
            <Text strong>本系统严格遵守 robots.txt 协议</Text>
            <p style={{ margin: '8px 0' }}>
              所有爬虫在访问网站前都会自动检查 robots.txt。若站点禁止爬取，将拒绝执行。
            </p>
            <Text type="warning">
              注意：请确保使用符合目标站点服务条款与法律法规。
            </Text>
          </div>
        }
        type="info"
        style={{ marginBottom: 24 }}
        action={
          <button
            type="button"
            onClick={handleRefresh}
            style={{
              border: 'none',
              background: 'none',
              color: '#1890ff',
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            <RestOutlined /> 刷新数据
          </button>
        }
        banner
      />

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="舆情总数"
              value={stats.totalSentiments}
              prefix={<FireOutlined style={{ color: '#ff4d4f' }} />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="今日新增"
              value={stats.todaySentiments}
              prefix={<FireOutlined style={{ color: '#faad14' }} />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="监控源数量"
              value={stats.activeSources}
              prefix={<EyeOutlined style={{ color: '#1890ff' }} />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="运行中任务"
              value={stats.runningTasks}
              prefix={<CommentOutlined style={{ color: '#722ed1' }} />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={12}>
          <SentimentChart data={sentimentData as unknown as Record<string, unknown>[]} />
        </Col>
        <Col span={12}>
          <SentimentTrendChart data={trendData} />
        </Col>
      </Row>

      <Card
        title="热门舆情"
        extra={
          <Link to="/hotlist" style={{ fontSize: 14 }}>
            查看全部
          </Link>
        }
      >
        {hotList.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {hotList.slice(0, 5).map((item) => (
              <div
                key={item.id}
                style={{
                  padding: '12px',
                  border: '1px solid #f0f0f0',
                  borderRadius: '8px',
                  transition: 'all 0.3s',
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <a
                    href={item.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{ fontSize: 16, fontWeight: 500 }}
                  >
                    {item.title}
                  </a>
                  <Tag color={getSentimentColor(item.sentimentScore)}>
                    {item.sentimentLabel === 'positive'
                      ? '正面'
                      : item.sentimentLabel === 'negative'
                        ? '负面'
                        : '中性'}
                  </Tag>
                </div>
                <div
                  style={{
                    marginTop: 8,
                    fontSize: 12,
                    color: '#999',
                    display: 'flex',
                    gap: 16,
                    flexWrap: 'wrap',
                  }}
                >
                  <span>浏览 {(item.views ?? 0).toLocaleString()}</span>
                  <span>点赞 {(item.likes ?? 0).toLocaleString()}</span>
                  <span>评论 {(item.comments ?? 0).toLocaleString()}</span>
                  <span>时间 {item.publishTime}</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
            暂无舆情数据，请先启动爬取任务
          </div>
        )}
      </Card>
    </div>
  )
}
