import { render, screen } from '@testing-library/react'
import Home from './index'

describe('Home page', () => {
  it('renders welcome title', () => {
    render(<Home />)
    expect(screen.getByText('欢迎使用 AI 创作平台')).toBeInTheDocument()
  })

  it('renders feature cards', () => {
    render(<Home />)
    expect(screen.getByText('AI 创作')).toBeInTheDocument()
    expect(screen.getByText('任务管理')).toBeInTheDocument()
  })
})
