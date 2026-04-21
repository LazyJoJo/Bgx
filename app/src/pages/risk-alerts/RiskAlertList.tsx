import { RiskAlert } from '@/types'
import {
  CheckCircleOutlined,
  FundOutlined,
  StockOutlined,
  WarningOutlined,
} from '@ant-design/icons'
import { riskAlertsApi } from '@services/api/riskAlerts'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import {
  fetchRiskAlerts,
  fetchRiskAlertUnreadCount,
  markRiskAlertsAsRead,
  resetList,
} from '@store/slices/riskAlertsSlice'
import { Badge, Button, Collapse, Empty, Space, Spin, Tag } from 'antd'
import { useCallback, useEffect } from 'react'

const { Panel } = Collapse

const RiskAlertList = () => {
  const dispatch = useAppDispatch()
  const { list, unreadCount, loading, hasMore, cursor } = useAppSelector(
    (state) => state.riskAlerts
  )

  // DEBUG: 测试 SSE 功能 - 调用后端接口触发风险检测
  const handleTestSSE = async () => {
    console.log('[DEBUG] Triggering risk alert check via API')
    try {
      const response = await riskAlertsApi.checkRiskAlerts()
      console.log('[DEBUG] Risk check API response:', response)
      // 如果需要，可以刷新列表
      // dispatch(fetchRiskAlerts({}))
    } catch (error) {
      console.error('[DEBUG] Risk check API error:', error)
    }
  }

  // 按日期分组
  const groupedByDate = list.reduce((acc, alert) => {
    const date = alert.date
    if (!acc[date]) {
      acc[date] = []
    }
    acc[date].push(alert)
    return acc
  }, {} as Record<string, RiskAlert[]>)

  // 日期排序（倒序）
  const sortedDates = Object.keys(groupedByDate).sort((a, b) =>
    b.localeCompare(a)
  )

  useEffect(() => {
    // 初始加载
    dispatch(resetList())
    dispatch(fetchRiskAlerts({}))
    dispatch(fetchRiskAlertUnreadCount())
    return () => {
      dispatch(resetList())
    }
  }, [dispatch])

  const handleLoadMore = useCallback(() => {
    if (!loading && hasMore && cursor) {
      dispatch(fetchRiskAlerts({ cursor }))
    }
  }, [dispatch, loading, hasMore, cursor])

  const handleMarkAllRead = () => {
    dispatch(markRiskAlertsAsRead())
  }

  const renderAlertCard = (alert: RiskAlert) => {
    const icon = alert.symbolType === 'STOCK' ? <StockOutlined /> : <FundOutlined />
    const typeColor = alert.symbolType === 'STOCK' ? 'blue' : 'green'
    const changeColor =
      alert.latestChangePercent > 0
        ? 'red'
        : alert.latestChangePercent < 0
          ? 'green'
          : 'default'

    return (
      <div
        key={`${alert.symbol}_${alert.date}`}
        style={{
          padding: '12px 16px',
          borderBottom: '1px solid #f0f0f0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Tag icon={icon} color={typeColor}>
            {alert.symbolType === 'STOCK' ? '股票' : '基金'}
          </Tag>
          <div>
            <div style={{ fontWeight: 500 }}>
              {alert.symbolName} ({alert.symbol})
            </div>
            <div style={{ fontSize: '12px', color: '#888' }}>
              今日已触发 {alert.triggerCount} 次风险提醒
            </div>
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '18px', fontWeight: 'bold', color: changeColor }}>
            {alert.latestChangePercent != null
              ? `${alert.latestChangePercent > 0 ? '+' : ''}${alert.latestChangePercent.toFixed(2)}%`
              : '-'}
          </div>
          <div style={{ fontSize: '12px', color: '#888' }}>
            最大波动: {alert.maxChangePercent != null
              ? `${alert.maxChangePercent > 0 ? '+' : ''}${alert.maxChangePercent.toFixed(2)}%`
              : '-'}
          </div>
        </div>
      </div>
    )
  }

  const renderDetails = (alert: RiskAlert) => {
    return (
      <div style={{ padding: '8px 16px', background: '#fafafa' }}>
        <div
          style={{
            fontSize: '12px',
            color: '#666',
            marginBottom: '8px',
          }}
        >
          触发明细 ({alert.details.length} 条)
        </div>
        {alert.details.map((detail, index) => (
          <div
            key={detail.id || index}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              padding: '6px 0',
              borderBottom:
                index < alert.details.length - 1
                  ? '1px dashed #e8e8e8'
                  : 'none',
              fontSize: '13px',
            }}
          >
            <span style={{ color: '#888' }}>
              {new Date(detail.triggeredAt).toLocaleString('zh-CN')}
            </span>
            <span
              style={{
                color:
                  detail.changePercent > 0
                    ? 'red'
                    : detail.changePercent < 0
                      ? 'green'
                      : '#666',
                fontWeight: 500,
              }}
            >
              {detail.changePercent != null
                ? `${detail.changePercent > 0 ? '+' : ''}${detail.changePercent.toFixed(2)}%`
                : '-'}
            </span>
          </div>
        ))}
      </div>
    )
  }

  const renderDateSection = (date: string, alerts: RiskAlert[]) => {
    const unreadAlerts = alerts.filter((a) => !a.isRead)
    const displayDate = date || '未知日期'
    const todayStr = new Date().toISOString().split('T')[0]
    return (
      <Collapse
        key={date || 'unknown'}
        ghost
        style={{ marginBottom: '8px' }}
        expandIconPosition="end"
      >
        <Panel
          key={date || 'unknown'}
          header={
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                width: '100%',
                paddingRight: '8px',
              }}
            >
              <Space>
                <span style={{ fontWeight: 500 }}>
                  {displayDate === todayStr
                    ? '今天'
                    : displayDate}
                </span>
                <Badge count={unreadAlerts.length} size="small" />
              </Space>
              <span style={{ fontSize: '12px', color: '#888' }}>
                {alerts.length} 条提醒
              </span>
            </div>
          }
        >
          {alerts.map((alert) => (
            <div key={`${alert.symbol}_${alert.date}`}>
              {renderAlertCard(alert)}
              <Collapse ghost>
                <Panel
                  key="details"
                  header={
                    <span style={{ fontSize: '12px', color: '#1890ff' }}>
                      查看详情 ({alert.details.length} 条明细)
                    </span>
                  }
                >
                  {renderDetails(alert)}
                </Panel>
              </Collapse>
            </div>
          ))}
        </Panel>
      </Collapse>
    )
  }

  return (
    <div style={{ padding: '0' }}>
      <div
        style={{
          marginBottom: '16px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Space size="middle">
          <h2 style={{ margin: 0 }}>
            <WarningOutlined /> 风险提醒
          </h2>
          <Badge count={unreadCount} size="small" />
        </Space>
        <Space>
          <Button
            type="primary"
            icon={<CheckCircleOutlined />}
            onClick={handleMarkAllRead}
            disabled={unreadCount === 0}
          >
            全部已读
          </Button>
          {/* DEBUG: 测试 SSE 功能 */}
          <Button
            danger
            onClick={handleTestSSE}
          >
            [测试SSE]
          </Button>
        </Space>
      </div>

      {loading && list.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <Spin size="large" />
        </div>
      ) : list.length === 0 ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="暂无风险提醒"
        />
      ) : (
        <div>
          {sortedDates.map((date) =>
            renderDateSection(date, groupedByDate[date])
          )}

          {hasMore && (
            <div style={{ textAlign: 'center', padding: '16px' }}>
              <Button onClick={handleLoadMore} loading={loading}>
                加载更多
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default RiskAlertList
