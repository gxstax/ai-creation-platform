import { Layout, Typography } from 'antd'
import { Navigate, Route, Routes } from 'react-router-dom'
import Home from '@/pages/Home'

const { Header, Content, Footer } = Layout

function App() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center' }}>
        <Typography.Title level={4} style={{ color: '#fff', margin: 0 }}>
          AI 创作平台
        </Typography.Title>
      </Header>
      <Content style={{ padding: 24 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Content>
      <Footer style={{ textAlign: 'center' }}>AI 创作平台 ©2026</Footer>
    </Layout>
  )
}

export default App
