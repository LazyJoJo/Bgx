import { Layout, Menu } from 'antd'
import {
  DashboardOutlined,
  StockOutlined,
  FundOutlined,
  BellOutlined,
  BarChartOutlined,
  SettingOutlined,
  UserOutlined,
  WarningOutlined
} from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'

const { Sider } = Layout

interface SidebarProps {
  collapsed: boolean
  onCollapse?: (collapsed: boolean) => void
}

const Sidebar = ({ collapsed }: SidebarProps) => {
  const navigate = useNavigate()
  const location = useLocation()

  const activeKey = location.pathname.split('/')[1] || 'dashboard'

  return (
    <Sider 
      collapsed={collapsed}
      width={256}
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        background: '#fff',
        boxShadow: '2px 0 8px 0 rgba(29,35,41,.05)'
      }}
    >
      <div style={{ 
        height: '32px', 
        margin: '16px', 
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: collapsed ? '14px' : '18px'
      }}>
        {collapsed ? 'BGX' : '股票基金系统'}
      </div>
      
      <Menu
        mode="inline"
        selectedKeys={[activeKey]}
        items={[
          {
            key: 'dashboard',
            icon: <DashboardOutlined />,
            label: '仪表盘',
            onClick: () => navigate('/dashboard')
          },
          {
            key: 'stocks',
            icon: <StockOutlined />,
            label: '股票管理',
            onClick: () => navigate('/stocks')
          },
          {
            key: 'funds',
            icon: <FundOutlined />,
            label: '基金管理',
            onClick: () => navigate('/funds')
          },
          {
            key: 'alerts',
            icon: <BellOutlined />,
            label: '提醒设置',
            onClick: () => navigate('/alerts')
          },
          {
            key: 'risk-alerts',
            icon: <WarningOutlined />,
            label: '风险提醒',
            onClick: () => navigate('/risk-alerts')
          },
          {
            type: 'divider'
          },
          {
            key: 'analysis',
            icon: <BarChartOutlined />,
            label: '数据分析',
            children: [
              {
                key: 'analysis-stocks',
                label: '股票分析',
                onClick: () => navigate('/analysis/stocks')
              },
              {
                key: 'analysis-funds',
                label: '基金分析',
                onClick: () => navigate('/fund-analysis')
              }
            ]
          },
          {
            type: 'divider'
          },
          {
            key: 'settings',
            icon: <SettingOutlined />,
            label: '系统设置',
            onClick: () => navigate('/settings')
          },
          {
            key: 'profile',
            icon: <UserOutlined />,
            label: '个人中心',
            onClick: () => navigate('/profile')
          }
        ]}
        style={{ 
          height: 'calc(100% - 64px)',
          borderRight: 0 
        }}
      />
    </Sider>
  )
}

export default Sidebar