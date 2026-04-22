import { RiskAlert } from '@/types'
import {
  CheckCircleOutlined,
  WarningOutlined,
} from '@ant-design/icons'
import RiskAlertCard from '@components/risk-alerts/RiskAlertCard'
import RiskAlertEmpty from '@components/risk-alerts/RiskAlertEmpty'
import { riskAlertsApi } from '@services/api/riskAlerts'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import {
  fetchRiskAlerts,
  markRiskAlertsAsRead,
  resetList
} from '@store/slices/riskAlertsSlice'
import { Badge, Button, Empty, message, Space, Spin } from 'antd'
import { useCallback, useEffect, useMemo, useState } from 'react'

/**
 * RiskAlertList - 风险提醒主页面
 * 
 * 功能：
 * - 按日期分组显示风险提醒
 * - 今天显示"暂时无风险"提示
 * - 使用 RiskAlertCard 显示每条提醒
 */
const RiskAlertList = () => {
  const dispatch = useAppDispatch()
  const { list, unreadCount, loading, hasMore, cursor } = useAppSelector(
    (state) => state.riskAlerts
  )

  // 暂时无风险提示的显示状态
  const [showEmptyPrompt, setShowEmptyPrompt] = useState(false)
  const [checkingEmpty, setCheckingEmpty] = useState(false)

  // 检查今天是否有风险
  // NOTE: list.length removed from dependencies to prevent infinite loop
  // The list.length fallback is not critical - if API fails, we just don't show empty prompt
  const checkTodayHasAlerts = useCallback(async (userId: number) => {
    setCheckingEmpty(true)
    try {
      const response = await riskAlertsApi.getTodaySummary(userId)
      if (response.success && response.data) {
        setShowEmptyPrompt(!response.data.hasAlerts)
      }
    } catch (error) {
      // Fallback: if API fails, don't show empty prompt (safer approach)
      setShowEmptyPrompt(false)
    } finally {
      setCheckingEmpty(false)
    }
  }, [])

  // 按日期分组
  const groupedByDate = useMemo(() => {
    return list.reduce((acc, alert) => {
      const date = alert.date
      if (!acc[date]) {
        acc[date] = []
      }
      acc[date].push(alert)
      return acc
    }, {} as Record<string, RiskAlert[]>)
  }, [list])

  // 日期排序（倒序）
  const sortedDates = useMemo(() => {
    return Object.keys(groupedByDate).sort((a, b) => b.localeCompare(a))
  }, [groupedByDate])

  // 统一日期格式化为 YYYY-MM-DD
  const formatDateToString = (date: Date): string => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  // 获取今天的日期字符串
  const todayStr = useMemo(() => {
    return formatDateToString(new Date())
  }, [])

  // 格式化日期显示
  // 只有当天显示"今天"，其他日期都显示具体日期
  const formatDateDisplay = (dateStr: string): string => {
    if (dateStr === todayStr) return '今天'
    return dateStr
  }

  useEffect(() => {
    // 初始加载
    dispatch(resetList())
    dispatch(fetchRiskAlerts({}))
    // 不再调用 fetchRiskAlertUnreadCount，因为 SSE 连接建立后会推送当前的未读数
    // 如果 SSE 未连接或推送失败，unread_count 会保持为 0，用户可以刷新页面重新获取
    const userId = Number(localStorage.getItem('userId'))
    if (userId) {
      checkTodayHasAlerts(userId)
    }

    return () => {
      dispatch(resetList())
    }
  }, [dispatch, checkTodayHasAlerts])

  // 检查是否应该显示空提示（今天没有任何风险）
  const shouldShowEmpty = useMemo(() => {
    // 只有今天且没有数据时才显示"暂时无风险"
    const todayHasData = sortedDates.includes(todayStr)
    if (!todayHasData && !checkingEmpty) {
      return showEmptyPrompt
    }
    return false
  }, [sortedDates, todayStr, showEmptyPrompt, checkingEmpty])

  const handleLoadMore = useCallback(() => {
    if (!loading && hasMore && cursor) {
      dispatch(fetchRiskAlerts({ cursor }))
    }
  }, [dispatch, loading, hasMore, cursor])

  const handleMarkAllRead = () => {
    dispatch(markRiskAlertsAsRead())
  }

  // 手动触发风险检测
  const [checking, setChecking] = useState(false)

  // 非当天日期的展开状态 - 默认只有今天展开，其他日期折叠
  const [expandedDates, setExpandedDates] = useState<Set<string>>(new Set([todayStr]))
  const handleManualCheck = async () => {
    setChecking(true)
    try {
      const response = await riskAlertsApi.checkRiskAlerts()
      if (response.success) {
        message.success('风险检测完成，正在刷新...')
        // 检测完成后刷新列表（不单独获取未读数，因为 SSE 会推送更新）
        dispatch(resetList())
        dispatch(fetchRiskAlerts({}))
        // 不再单独调用 fetchRiskAlertUnreadCount，避免覆盖 SSE 的实时更新
        // SSE 会在风险检测过程中推送 new_alert 和 unread_count_change 事件
        const userId = Number(localStorage.getItem('userId'))
        if (userId) {
          checkTodayHasAlerts(userId)
        }
      }
    } catch (error) {
      console.error('[RiskAlertList] Manual check failed:', error)
      message.error('风险检测失败')
    } finally {
      setChecking(false)
    }
  }

  // 切换日期展开状态
  const toggleDateExpanded = (date: string) => {
    setExpandedDates((prev) => {
      const next = new Set(prev)
      if (next.has(date)) {
        next.delete(date)
      } else {
        next.add(date)
      }
      return next
    })
  }

  const renderDateSection = (date: string, alerts: RiskAlert[]) => {
    const unreadAlerts = alerts.filter((a) => !a.isRead)
    const displayDate = formatDateDisplay(date)
    const isToday = date === todayStr
    // 默认折叠（不在expandedDates中），只有今天默认展开
    const isExpanded = isToday || expandedDates.has(date)

    return (
      <div key={date} style={{ marginBottom: '16px' }}>
        {/* 日期标题 */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '8px 0',
            marginBottom: '8px',
            cursor: isToday ? 'default' : 'pointer'
          }}
          onClick={() => !isToday && toggleDateExpanded(date)}
        >
          <Space size="middle">
            {!isToday && (
              <span style={{ fontSize: '12px', color: '#888' }}>
                {isExpanded ? '▼' : '▶'}
              </span>
            )}
            <span style={{
              fontWeight: 600,
              fontSize: '15px',
              color: '#333'
            }}>
              {displayDate}
            </span>
            {unreadAlerts.length > 0 && (
              <Badge count={unreadAlerts.length} size="small" />
            )}
          </Space>
          <span style={{ fontSize: '12px', color: '#888' }}>
            {alerts.length} 条提醒
          </span>
        </div>

        {/* 风险卡片列表 - 只有展开时才显示 */}
        {isExpanded && alerts.map((alert) => (
          <RiskAlertCard key={`${alert.symbol}_${alert.date}`} alert={alert} />
        ))}
      </div>
    )
  }

  return (
    <div style={{ padding: '0' }}>
      {/* 页面标题 */}
      <div
        style={{
          marginBottom: '20px',
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
        <Space size="middle">
          <Button
            icon={<CheckCircleOutlined />}
            onClick={handleMarkAllRead}
            disabled={unreadCount === 0}
          >
            全部已读
          </Button>
          <Button
            type="primary"
            icon={checking ? <Spin size="small" /> : <WarningOutlined />}
            onClick={handleManualCheck}
            disabled={checking}
          >
            {checking ? '检测中...' : '手动检测'}
          </Button>
        </Space>
      </div>

      {/* 加载状态 */}
      {loading && list.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <Spin size="large" />
        </div>
      ) : shouldShowEmpty ? (
        /* 暂时无风险提示 */
        <RiskAlertEmpty />
      ) : list.length === 0 && sortedDates.length === 0 ? (
        /* 无数据 */
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="暂无风险提醒"
        />
      ) : (
        /* 风险提醒列表 */
        <div>
          {sortedDates.map((date) =>
            renderDateSection(date, groupedByDate[date])
          )}

          {/* 加载更多 */}
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
