import React, { Component, ErrorInfo, ReactNode } from 'react'
import { Result, Button, Card } from 'antd'

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
  errorInfo: ErrorInfo | null
}

class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null
    }
  }

  static getDerivedStateFromError(error: Error): Partial<State> {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    this.setState({
      error,
      errorInfo
    })
    // 记录错误日志
    console.error('ErrorBoundary caught an error:', error, errorInfo)
  }

  handleReload = (): void => {
    window.location.reload()
  }

  handleGoHome = (): void => {
    this.setState({ hasError: false, error: null, errorInfo: null })
    window.location.href = '/'
  }

  render(): ReactNode {
    const { hasError, error, errorInfo } = this.state
    const { children, fallback } = this.props

    if (hasError) {
      if (fallback) {
        return fallback
      }

      return (
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          padding: '24px'
        }}>
          <Card style={{ maxWidth: 600, textAlign: 'center' }}>
            <Result
              status="error"
              title="页面出错了"
              subTitle="抱歉，页面加载时遇到了问题。请尝试刷新页面或返回首页。"
              extra={[
                <Button key="reload" type="primary" onClick={this.handleReload}>
                  刷新页面
                </Button>,
                <Button key="home" onClick={this.handleGoHome}>
                  返回首页
                </Button>
              ]}
            />
            {process.env.NODE_ENV === 'development' && error && (
              <div style={{
                marginTop: 24,
                textAlign: 'left',
                padding: 16,
                background: '#f5f5f5',
                borderRadius: 8,
                fontSize: 12
              }}>
                <p><strong>Error:</strong> {error.message}</p>
                {errorInfo && (
                  <p><strong>Component Stack:</strong> {errorInfo.componentStack}</p>
                )}
              </div>
            )}
          </Card>
        </div>
      )
    }

    return children
  }
}

export default ErrorBoundary
