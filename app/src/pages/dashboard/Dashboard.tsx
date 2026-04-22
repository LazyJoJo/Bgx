import {
  BellOutlined,
  EyeOutlined,
  FundOutlined,
  StockOutlined,
  WarningOutlined
} from '@ant-design/icons'
import { dashboardApi } from '@services/api/dashboard'
import { Subscription, subscriptionsApi } from '@services/api/subscriptions'
import { Button, Card, Col, Row, Space, Statistic, Table, Tag, Typography } from 'antd'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

const { Title } = Typography

const Dashboard = () => {
  const navigate = useNavigate()
  const [stats, setStats] = useState({
    totalStocks: 0,
    totalFunds: 0,
    activeSubscriptions: 0,
    riskAlertCount: 0
  })
  const [recentSubscriptions, setRecentSubscriptions] = useState<Subscription[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    setLoading(true)
    try {
      const userId = Number(localStorage.getItem('userId')) || 1

      const [statsResponse, subscriptionsResponse] = await Promise.all([
        dashboardApi.getDashboardStats(),
        subscriptionsApi.getSubscriptions(userId, { limit: 5 })
      ])

      const subscriptions = subscriptionsResponse.success ? subscriptionsResponse.data : []

      // 活跃订阅数
      const activeSubscriptionsCount = subscriptions.filter((s: Subscription) => s.status === 'ACTIVE').length

      setStats({
        totalStocks: statsResponse.totalStocks || 0,
        totalFunds: statsResponse.totalFunds || 0,
        activeSubscriptions: activeSubscriptionsCount,
        riskAlertCount: statsResponse.triggeredAlerts || 0
      })

      setRecentSubscriptions(subscriptions.slice(0, 5))
    } catch (error) {
      console.error('获取仪表盘数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const subscriptionColumns = [
    {
      title: '标的',
      dataIndex: 'symbol',
      key: 'symbol',
      render: (symbol: string, record: Subscription) => (
        <Space>
          {record.symbolType === 'STOCK' ? <StockOutlined /> : <FundOutlined />}
          <span>{symbol}</span>
          <span style={{ color: '#888' }}>({record.symbolName})</span>
        </Space>
      )
    },
    {
      title: '提醒类型',
      dataIndex: 'alertType',
      key: 'alertType',
      render: (type: string) => {
        const typeMap: Record<string, { text: string; color: string }> = {
          PRICE_ABOVE: { text: '价格上限', color: 'red' },
          PRICE_BELOW: { text: '价格下限', color: 'green' },
          PERCENTAGE_CHANGE: { text: '涨跌幅', color: 'blue' }
        }
        const config = typeMap[type] || { text: type, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '目标值',
      dataIndex: 'targetPrice',
      key: 'targetPrice',
      render: (value: number, record: Subscription) => {
        if (record.alertType === 'PERCENTAGE_CHANGE') {
          return record.targetChangePercent != null ? `${record.targetChangePercent}%` : '-'
        }
        return value != null ? `¥${value.toFixed(2)}` : '-'
      }
    },
    {
      title: '当前价格',
      dataIndex: 'currentPrice',
      key: 'currentPrice',
      render: (price: number) => price != null ? `¥${price.toFixed(2)}` : '-'
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusMap: Record<string, { text: string; color: string }> = {
          ACTIVE: { text: '已启用', color: 'green' },
          TRIGGERED: { text: '已触发', color: 'orange' },
          INACTIVE: { text: '已停用', color: 'default' }
        }
        const config = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Subscription) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/subscriptions/edit/${record.id}`)}
          >
            查看详情
          </Button>
        </Space>
      )
    }
  ]

  return (
    <div>
      <Title level={2}>仪表盘</Title>

      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="股票总数"
              value={stats.totalStocks}
              prefix={<StockOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="基金总数"
              value={stats.totalFunds}
              prefix={<FundOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card
            hoverable
            onClick={() => navigate('/subscriptions')}
            style={{ cursor: 'pointer' }}
          >
            <Statistic
              title="活跃订阅"
              value={stats.activeSubscriptions}
              prefix={<BellOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card
            hoverable
            onClick={() => navigate('/risk-alerts')}
            style={{ cursor: 'pointer' }}
          >
            <Statistic
              title="风险提醒"
              value={stats.riskAlertCount}
              prefix={<WarningOutlined />}
              valueStyle={{ color: stats.riskAlertCount > 0 ? '#f5222d' : '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={24}>
          <Card
            title="最近订阅"
            extra={
              <Button type="primary" onClick={() => navigate('/subscriptions')}>
                查看全部
              </Button>
            }
          >
            <Table
              columns={subscriptionColumns}
              dataSource={recentSubscriptions}
              loading={loading}
              pagination={false}
              rowKey="id"
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard