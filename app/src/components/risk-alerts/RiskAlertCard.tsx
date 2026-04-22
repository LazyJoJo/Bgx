import type { RiskAlert, RiskAlertDetail } from '@/types'
import {
    BellOutlined,
    CheckCircleOutlined,
    DownOutlined,
    FundOutlined,
    StockOutlined,
    UpOutlined,
} from '@ant-design/icons'
import { Collapse, Tag } from 'antd'
import React, { useState } from 'react'
import RiskAlertStats from './RiskAlertStats'

const { Panel } = Collapse

interface RiskAlertCardProps {
    alert: RiskAlert
}

const getChangeColor = (value: number): string => {
    if (value > 0) return '#eb2f96'
    if (value < 0) return '#52c41a'
    return '#999'
}

const formatPercent = (value: number | undefined | null): string => {
    if (value == null) return '-'
    return `${value > 0 ? '+' : ''}${value.toFixed(2)}%`
}

const formatTime = (timeStr: string): string => {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}

/**
 * RiskAlertCard - 单条风险提醒卡片
 * 
 * 显示：
 * - 基金/股票代码和名称
 * - 最大涨幅 / 最小涨幅 / 当前涨幅
 * - 触发次数
 * - 明细列表（可折叠）
 */
const RiskAlertCard: React.FC<RiskAlertCardProps> = ({
    alert
}) => {
    const [expanded, setExpanded] = useState(false)

    const icon = alert.symbolType === 'STOCK' ? <StockOutlined /> : <FundOutlined />
    const typeColor = alert.symbolType === 'STOCK' ? 'blue' : 'green'
    const isCleared = alert.status === 'CLEARED'

    const details = alert.details || []

    const renderDetailItem = (detail: RiskAlertDetail, index: number) => {
        const detailColor = getChangeColor(detail.changePercent)
        return (
            <div
                key={detail.id || index}
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 12px',
                    borderBottom: index < details.length - 1 ? '1px dashed #e8e8e8' : 'none',
                    background: index % 2 === 0 ? '#fff' : '#fafafa'
                }}
            >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                    <span style={{ fontSize: '12px', color: '#888' }}>
                        {formatTime(detail.triggeredAt)}
                    </span>
                    {detail.timePoint && (
                        <span style={{ fontSize: '11px', color: '#aaa' }}>
                            触发点 {detail.timePoint.substring(0, 5)}
                        </span>
                    )}
                </div>
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px'
                }}>
                    <span style={{
                        fontSize: '12px',
                        color: '#666'
                    }}>
                        {detail.currentPrice?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                    <span style={{
                        fontSize: '15px',
                        fontWeight: 600,
                        color: detailColor,
                        display: 'flex',
                        alignItems: 'center',
                        gap: '2px'
                    }}>
                        {detail.changePercent > 0 ? <UpOutlined style={{ fontSize: '10px' }} /> :
                            detail.changePercent < 0 ? <DownOutlined style={{ fontSize: '10px' }} /> : null}
                        {formatPercent(detail.changePercent)}
                    </span>
                </div>
            </div>
        )
    }

    return (
        <div
            style={{
                background: '#fff',
                borderRadius: '8px',
                border: `1px solid ${isCleared ? '#d9d9d9' : '#ffccc7'}`,
                marginBottom: '12px',
                overflow: 'hidden',
                opacity: isCleared ? 0.75 : 1
            }}
        >
            {/* 主信息区 */}
            <div
                style={{
                    padding: '16px',
                    cursor: details.length > 0 ? 'pointer' : 'default'
                }}
                onClick={() => details.length > 0 && setExpanded(!expanded)}
            >
                {/* 第一行：标题和状态 */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'flex-start',
                    marginBottom: '12px'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <Tag icon={icon} color={typeColor}>
                            {alert.symbolType === 'STOCK' ? '股票' : '基金'}
                        </Tag>
                        <div>
                            <div style={{ fontWeight: 600, fontSize: '15px', color: '#333' }}>
                                {alert.symbolName}
                            </div>
                            <div style={{ fontSize: '12px', color: '#888' }}>
                                {alert.symbol}
                            </div>
                        </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        {isCleared ? (
                            <Tag icon={<CheckCircleOutlined />} color="default">
                                已解除
                            </Tag>
                        ) : (
                            <Tag icon={<BellOutlined />} color="orange">
                                跟踪中
                            </Tag>
                        )}
                        {!alert.isRead && (
                            <div style={{
                                width: '8px',
                                height: '8px',
                                borderRadius: '50%',
                                background: '#ff4d4f'
                            }} />
                        )}
                    </div>
                </div>

                {/* 第二行：涨跌幅统计 */}
                <RiskAlertStats
                    currentPercent={alert.latestChangePercent}
                    maxPercent={alert.maxChangePercent}
                    minPercent={alert.minChangePercent}
                />

                {/* 第三行：触发次数和时间 */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginTop: '12px',
                    paddingTop: '12px',
                    borderTop: '1px solid #f0f0f0'
                }}>
                    <div style={{ fontSize: '12px', color: '#888' }}>
                        今日已触发 <span style={{ color: '#333', fontWeight: 500 }}>{details.length}</span> 次
                    </div>
                    <div style={{ fontSize: '12px', color: '#888' }}>
                        最新: {formatTime(alert.latestTriggeredAt)}
                    </div>
                </div>
            </div>

            {/* 明细列表（可折叠） */}
            {details.length > 0 && (
                <Collapse
                    ghost
                    activeKey={expanded ? ['details'] : []}
                    onChange={(keys) => setExpanded(keys.includes('details'))}
                    style={{ background: '#fafafa' }}
                >
                    <Panel
                        key="details"
                        header={
                            <span style={{ fontSize: '12px', color: '#1890ff' }}>
                                查看详情 ({details.length} 条明细)
                            </span>
                        }
                    >
                        <div style={{
                            borderRadius: '4px',
                            overflow: 'hidden',
                            border: '1px solid #e8e8e8'
                        }}>
                            {details.map((detail, index) => renderDetailItem(detail, index))}
                        </div>
                    </Panel>
                </Collapse>
            )}
        </div>
    )
}

export default RiskAlertCard
