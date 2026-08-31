import { Card, Col, Row, Typography } from 'antd'

const features = [
  { title: 'AI 创作', description: '基于 DeepSeek 大模型的智能创作能力，支持流式输出' },
  { title: '任务管理', description: '创作任务的创建、调度、状态跟踪与结果管理' },
  { title: '内容沉淀', description: '创作作品的归档、检索与二次创作' },
]

function Home() {
  return (
    <div>
      <Typography.Title level={2}>欢迎使用 AI 创作平台</Typography.Title>
      <Typography.Paragraph type="secondary">项目骨架已就绪，功能模块开发中</Typography.Paragraph>
      <Row gutter={[16, 16]}>
        {features.map((feature) => (
          <Col xs={24} sm={12} lg={8} key={feature.title}>
            <Card title={feature.title}>{feature.description}</Card>
          </Col>
        ))}
      </Row>
    </div>
  )
}

export default Home
