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
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchNotificationUnreadCount } from '@store/slices/notificationsSlice'
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
  const { totalUnreadCount } = useAppSelector((state) => state.notifications)
  const riskAlertUnreadCount = useAppSelector((state) => state.riskAlerts.unreadCount)
  // 导航栏未读计数 = 通知未读 + 风险提醒未读
  const totalAlertCount = totalUnreadCount + riskAlertUnreadCount

  // 组件挂载时获取未读计数
  useEffect(() => {
    dispatch(fetchNotificationUnreadCount())
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
      key: 'alerts',
      icon: <BellOutlined />,
      label: '提醒',
      onClick: () => navigate('/alerts')
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
