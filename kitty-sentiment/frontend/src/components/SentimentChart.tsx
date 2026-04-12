import React from 'react';
import { Card, Row, Col, Statistic } from 'antd';
import { SmileOutlined, MehOutlined, FrownOutlined } from '@ant-design/icons';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface SentimentChartProps {
  data: any[];
}

const SentimentChart: React.FC<SentimentChartProps> = ({ data }) => {
  // 计算情感统计
  const labelOf = (item: Record<string, unknown>) =>
    (item.sentimentLabel ?? item.sentiment_label) as string;
  const positiveCount = data.filter((item) => labelOf(item as Record<string, unknown>) === 'positive').length;
  const neutralCount = data.filter((item) => labelOf(item as Record<string, unknown>) === 'neutral').length;
  const negativeCount = data.filter((item) => labelOf(item as Record<string, unknown>) === 'negative').length;
  const totalCount = data.length;

  // 准备图表数据
  const chartData = [
    { name: '正面', value: positiveCount, color: '#52c41a' },
    { name: '中性', value: neutralCount, color: '#1890ff' },
    { name: '负面', value: negativeCount, color: '#f5222d' },
  ];

  return (
    <Card title="情感分析统计" style={{ marginBottom: 24 }}>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card bordered={false}>
            <Statistic
              title="正面情感"
              value={positiveCount}
              valueStyle={{ color: '#52c41a' }}
              prefix={<SmileOutlined />}
              suffix={`(${totalCount > 0 ? ((positiveCount / totalCount) * 100).toFixed(1) : 0}%)`}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card bordered={false}>
            <Statistic
              title="中性情感"
              value={neutralCount}
              valueStyle={{ color: '#1890ff' }}
              prefix={<MehOutlined />}
              suffix={`(${totalCount > 0 ? ((neutralCount / totalCount) * 100).toFixed(1) : 0}%)`}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card bordered={false}>
            <Statistic
              title="负面情感"
              value={negativeCount}
              valueStyle={{ color: '#f5222d' }}
              prefix={<FrownOutlined />}
              suffix={`(${totalCount > 0 ? ((negativeCount / totalCount) * 100).toFixed(1) : 0}%)`}
            />
          </Card>
        </Col>
      </Row>

      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Bar dataKey="value" fill="#1890ff" name="数量" />
        </BarChart>
      </ResponsiveContainer>
    </Card>
  );
};

export default SentimentChart;