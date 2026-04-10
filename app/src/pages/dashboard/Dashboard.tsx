import { PriceAlert } from '@/types'
import {
  BellOutlined,
  EyeOutlined,
  FundOutlined,
  RiseOutlined,
  StockOutlined,
  WarningOutlined
} from '@ant-design/icons'
import { alertsApi } from '@services/api/alerts'
import { dashboardApi } from '@services/api/dashboard'
import { riskAlertsApi } from '@services/api/riskAlerts'
import { Button, Card, Col, Row, Space, Statistic, Table, Tag, Typography } from 'antd'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

const { Title } = Typography

const Dashboard = () => {
  const navigate = useNavigate()
  const [stats, setStats] = useState({
    totalStocks: 0,
    totalFunds: 0,
    activeAlerts: 0,
    triggeredAlerts: 0,
    riskAlertCount: 0
  })
  const [recentAlerts, setRecentAlerts] = useState<PriceAlert[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    setLoading(true)
    try {
      const [statsResponse, alertsResponse, activeAlertsResponse, todayRiskCountResponse] = await Promise.all([
        dashboardApi.getDashboardStats(),
        dashboardApi.getRecentAlerts(5),
        alertsApi.getUserActiveAlerts(1),
        riskAlertsApi.getTodayRiskAlertCount(1)
      ])

      const todayRiskCount = todayRiskCountResponse.success ? todayRiskCountResponse.data.total : 0
      const activeAlertsCount = activeAlertsResponse.success ? activeAlertsResponse.data.length : 0
      // 活跃提醒 = 价格提醒活跃数 + 当天风险提醒数
      const totalActiveAlerts = activeAlertsCount + todayRiskCount

      setStats({
        totalStocks: statsResponse.totalStocks || 0,
        totalFunds: statsResponse.totalFunds || 0,
        activeAlerts: totalActiveAlerts,
        triggeredAlerts: statsResponse.triggeredAlerts || 0,
        riskAlertCount: todayRiskCount
      })

      setRecentAlerts(alertsResponse.data || [])
    } catch (error) {
      console.error('获取仪表盘数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const alertColumns = [
    {
      title: '标的',
      dataIndex: 'symbol',
      key: 'symbol',
      render: (symbol: string, record: PriceAlert) => (
        <Space>
          {record.symbolType === 'STOCK' ? <StockOutlined /> : <FundOutlined />}
          <span>{symbol}</span>
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
      render: (value: number, record: PriceAlert) => {
        if (record.alertType === 'PERCENTAGE_CHANGE') {
          return `${record.targetChangePercent}%`
        }
        return `¥${value?.toFixed(2)}`
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
          INACTIVE: { text: '已禁用', color: 'default' }
        }
        const config = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: PriceAlert) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/alerts/${record.id}`)}
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
          <Card>
            <Statistic
              title="活跃提醒"
              value={stats.activeAlerts}
              prefix={<BellOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已触发提醒"
              value={stats.triggeredAlerts}
              prefix={<RiseOutlined />}
              valueStyle={{ color: '#f5222d' }}
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
            title="最近提醒"
            extra={
              <Button type="primary" onClick={() => navigate('/alerts')}>
                查看全部
              </Button>
            }
          >
            <Table
              columns={alertColumns}
              dataSource={recentAlerts}
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