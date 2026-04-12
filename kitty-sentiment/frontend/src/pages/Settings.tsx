import { Card, Form, Input, Select, Switch, Button, message } from 'antd'
import { SaveOutlined } from '@ant-design/icons'
import { useEffect, useState } from 'react'
import { errMessage } from '../api/client'
import { getSettings, saveSettings } from '../api/api_settings'

interface SettingsForm {
  databaseType: string
  databaseHost: string
  databasePort: number
  databaseName: string
  databaseUser: string
  databasePassword: string
  crawlerTimeout: number
  maxRetries: number
  enableSentimentAnalysis: boolean
  enableKeywordExtraction: boolean
}

export default function Settings() {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const load = async () => {
      try {
        const response = await getSettings()
        form.setFieldsValue(response.data)
      } catch (error) {
        console.error('获取设置失败:', error)
        message.error(errMessage(error, '获取设置失败'))
      }
    }
    load()
  }, [form])

  const handleSave = async (values: SettingsForm) => {
    setLoading(true)
    try {
      await saveSettings(values as unknown as Record<string, unknown>)
      message.success('已保存')
    } catch (error) {
      message.error(errMessage(error, '保存失败'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <Card title="系统设置">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
          initialValues={{
            databaseType: 'sqlite',
            crawlerTimeout: 30,
            maxRetries: 3,
            enableSentimentAnalysis: true,
            enableKeywordExtraction: true,
          }}
        >
          <Form.Item
            label="数据库类型"
            name="databaseType"
            rules={[{ required: true, message: '请选择数据库类型' }]}
          >
            <Select>
              <Select.Option value="sqlite">SQLite</Select.Option>
              <Select.Option value="mysql">MySQL</Select.Option>
              <Select.Option value="doris">Doris</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="数据库主机" name="databaseHost">
            <Input placeholder="SQLite 可留空" />
          </Form.Item>

          <Form.Item label="数据库端口" name="databasePort">
            <Input type="number" placeholder="例如：3306" />
          </Form.Item>

          <Form.Item
            label="数据库名称"
            name="databaseName"
            rules={[{ required: true, message: '请输入数据库名称' }]}
          >
            <Input placeholder="例如：sentiment.db" />
          </Form.Item>

          <Form.Item label="数据库用户" name="databaseUser">
            <Input placeholder="SQLite 可留空" />
          </Form.Item>

          <Form.Item label="数据库密码" name="databasePassword">
            <Input.Password placeholder="只读展示为脱敏；修改请使用环境变量" />
          </Form.Item>

          <Form.Item
            label="爬虫超时时间（秒）"
            name="crawlerTimeout"
            rules={[{ required: true, message: '请输入爬虫超时时间' }]}
          >
            <Input type="number" placeholder="例如：30" />
          </Form.Item>

          <Form.Item
            label="最大重试次数"
            name="maxRetries"
            rules={[{ required: true, message: '请输入最大重试次数' }]}
          >
            <Input type="number" placeholder="例如：3" />
          </Form.Item>

          <Form.Item
            label="启用情感分析"
            name="enableSentimentAnalysis"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>

          <Form.Item
            label="启用关键词提取"
            name="enableKeywordExtraction"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={loading}>
              保存设置
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
