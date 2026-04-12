import React from 'react';
import { Card } from 'antd';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface SentimentTrendChartProps {
  data: any[];
}

const SentimentTrendChart: React.FC<SentimentTrendChartProps> = ({ data }) => {
  return (
    <Card title="情感趋势分析" style={{ marginBottom: 24 }}>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="time" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="positive" stroke="#52c41a" name="正面" />
          <Line type="monotone" dataKey="neutral" stroke="#1890ff" name="中性" />
          <Line type="monotone" dataKey="negative" stroke="#f5222d" name="负面" />
        </LineChart>
      </ResponsiveContainer>
    </Card>
  );
};

export default SentimentTrendChart;