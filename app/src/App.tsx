import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Layout } from 'antd'
import { useState } from 'react'

import Header from '@components/layout/Header'
import Sidebar from '@components/layout/Sidebar'
import Dashboard from '@pages/dashboard/Dashboard'
import StockList from '@pages/stocks/StockList'
import FundList from '@pages/funds/FundList'
import FundAnalysis from '@pages/funds/FundAnalysis'
import AlertList from '@pages/alerts/AlertList'
import AlertCreate from '@pages/alerts/AlertCreate'
import RiskAlertList from '@pages/risk-alerts/RiskAlertList'
import ErrorBoundary from '@components/ErrorBoundary'

const { Content } = Layout

function App() {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Layout style={{ minHeight: '100vh' }}>
          <Sidebar collapsed={collapsed} onCollapse={setCollapsed} />
          <Layout style={{ marginLeft: collapsed ? 80 : 256, transition: 'margin-left 0.2s' }}>
            <Header collapsed={collapsed} onCollapse={setCollapsed} />
            <Content style={{ margin: '24px 16px', padding: 24, background: '#fff' }}>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/stocks" element={<StockList />} />
                <Route path="/funds" element={<FundList />} />
                <Route path="/fund-analysis" element={<FundAnalysis />} />
                <Route path="/alerts" element={<AlertList />} />
                <Route path="/alerts/edit/:id" element={<AlertCreate />} />
                <Route path="/alerts/create" element={<AlertCreate />} />
                <Route path="/risk-alerts" element={<RiskAlertList />} />
              </Routes>
            </Content>
          </Layout>
        </Layout>
      </BrowserRouter>
    </ErrorBoundary>
  )
}

export default App