import {
  BellOutlined,
  DashboardOutlined,
  FundOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SettingOutlined,
  StockOutlined,
  UserOutlined
} from '@ant-design/icons'
import { useRiskAlertSSE } from '@hooks/useRiskAlertSSE'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchRiskAlertUnreadCount } from '@store/slices/riskAlertsSlice'
import { Avatar, Badge, Button, Dropdown, Layout, Menu } from 'antd'
import { useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'

const { Header: AntHeader } = Layout

interface HeaderProps {
  collapsed?: boolean
  onCollapse?: (collapsed: boolean) => void
}

const Header = ({ collapsed, onCollapse }: HeaderProps) => {
  const navigate = useNavigate()
  const location = useLocation()
  const dispatch = useAppDispatch()
  const riskAlertUnreadCount = useAppSelector((state) => state.riskAlerts.unreadCount)
  // 导航栏未读计数 = 风险提醒未读
  const totalAlertCount = riskAlertUnreadCount

  // 获取 userId（从 localStorage 解析，带验证）
  const storedUserId = localStorage.getItem('userId')
  const parsedUserId = storedUserId ? Number(storedUserId) : NaN
  const userId = Number.isInteger(parsedUserId) && parsedUserId > 0 ? parsedUserId : 1

  // 启用 SSE 实时推送（isConnected 可用于显示连接状态指示器）
  useRiskAlertSSE({
    enabled: true,
    userId,
    autoConnect: true
  })

  // 组件挂载时初始化未读数（一次性）
  // SSE 推送会覆盖此值，所以这是安全的
  useEffect(() => {
    dispatch(fetchRiskAlertUnreadCount())
  }, [dispatch])

  const menuItems = [
    {
      key: 'dashboard',
      icon: <DashboardOutlined />,
      label: '仪表盘',
      onClick: () => navigate('/dashboard')
    },
    {
      key: 'stocks',
      icon: <StockOutlined />,
      label: '股票',
      onClick: () => navigate('/stocks')
    },
    {
      key: 'funds',
      icon: <FundOutlined />,
      label: '基金',
      onClick: () => navigate('/funds')
    },
    {
      key: 'subscriptions',
      icon: <BellOutlined />,
      label: '订阅管理',
      onClick: () => navigate('/subscriptions')
    }
  ]

  const activeKey = location.pathname.split('/')[1] || 'dashboard'

  return (
    <AntHeader style={{
      padding: 0,
      background: '#fff',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      boxShadow: '0 1px 4px rgba(0,21,41,.08)'
    }}>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <Button
          type="text"
          icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          onClick={() => onCollapse?.(!collapsed)}
          style={{
            fontSize: '16px',
            width: 64,
            height: 64,
          }}
        />
      </div>

      <div style={{ display: 'flex', alignItems: 'center', marginRight: '24px' }}>
        <Menu
          mode="horizontal"
          selectedKeys={[activeKey]}
          items={menuItems}
          style={{
            border: 'none',
            background: 'transparent',
            lineHeight: '64px'
          }}
        />
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <Badge count={totalAlertCount} size="small">
          <Button
            type="text"
            icon={<BellOutlined />}
            style={{ fontSize: '18px' }}
          />
        </Badge>

        <Dropdown
          menu={{
            items: [
              {
                key: 'profile',
                label: '个人资料',
                icon: <UserOutlined />
              },
              {
                key: 'settings',
                label: '设置',
                icon: <SettingOutlined />
              },
              {
                type: 'divider'
              },
              {
                key: 'logout',
                label: '退出登录',
                icon: <LogoutOutlined />,
                danger: true
              }
            ]
          }}
          placement="bottomRight"
        >
          <Avatar
            size="large"
            icon={<UserOutlined />}
            style={{ cursor: 'pointer' }}
          />
        </Dropdown>
      </div>
    </AntHeader>
  )
}

export default Header
