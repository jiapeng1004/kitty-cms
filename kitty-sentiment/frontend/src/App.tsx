import { Routes, Route, Navigate, useNavigate, useLocation, Outlet } from 'react-router-dom'
import { Layout, Menu } from 'antd'
import {
  DashboardOutlined,
  FireOutlined,
  ToolOutlined,
  DatabaseOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import { useState } from 'react'

import Dashboard from './pages/Dashboard'
import HotList from './pages/HotList'
import TaskManager from './pages/TaskManager'
import Sources from './pages/Sources'
import Settings from './pages/Settings'
import Login from './pages/Login'

const { Header, Content, Sider } = Layout

function AppShell() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  const pathToKey: Record<string, string> = {
    '/': 'dashboard',
    '/hotlist': 'hotlist',
    '/tasks': 'taskmanager',
    '/sources': 'sources',
    '/settings': 'settings',
  }
  const currentMenu = pathToKey[location.pathname] || 'dashboard'

  const menuItems = [
    { key: 'dashboard', icon: <DashboardOutlined />, label: '仪表盘', path: '/' },
    {
      key: 'hotlist',
      icon: <FireOutlined />,
      label: '舆情榜单',
      path: '/hotlist',
    },
    {
      key: 'taskmanager',
      icon: <ToolOutlined />,
      label: '任务管理',
      path: '/tasks',
    },
    {
      key: 'sources',
      icon: <DatabaseOutlined />,
      label: '信息源',
      path: '/sources',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
      path: '/settings',
    },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsed={collapsed}
        onCollapse={(value) => setCollapsed(value)}
        width={240}
        style={{ background: '#fff', borderRight: '1px solid #e8e8e8' }}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 20,
            fontWeight: 'bold',
            color: '#1890ff',
          }}
        >
          🐱 KittySentiment
        </div>
        <Menu
          mode="inline"
          selectedKeys={[currentMenu]}
          items={menuItems}
          onClick={({ key }) => {
            const item = menuItems.find((i) => i.key === key)
            if (item) {
              navigate(item.path)
            }
          }}
          theme="light"
          style={{ borderRight: 'none' }}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            padding: '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <span style={{ fontSize: 18, fontWeight: 500 }}>
            {menuItems.find((item) => item.key === currentMenu)?.label}
          </span>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span style={{ color: '#595959' }}>
              当前时间: {new Date().toLocaleString('zh-CN')}
            </span>
          </div>
        </Header>
        <Content style={{ margin: '24px', background: '#f0f2f5', minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<AppShell />}>
        <Route index element={<Dashboard />} />
        <Route path="hotlist" element={<HotList />} />
        <Route path="tasks" element={<TaskManager />} />
        <Route path="sources" element={<Sources />} />
        <Route path="settings" element={<Settings />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default App
