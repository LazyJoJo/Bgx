import { ArrowDownOutlined, ArrowUpOutlined, MinusOutlined } from '@ant-design/icons'
import React from 'react'

interface RiskAlertStatsProps {
    currentPercent: number
    maxPercent: number
    minPercent: number
}

/**
 * Get color based on change percent value
 * - Positive (涨): red
 * - Negative (跌): green
 * - Zero: gray
 */
const getChangeColor = (value: number): string => {
    if (value > 0) return '#eb2f96'      // 红色-涨
    if (value < 0) return '#52c41a'       // 绿色-跌
    return '#999'                          // 灰色-平
}

/**
 * Format change percent with sign prefix
 */
const formatPercent = (value: number | undefined | null): string => {
    if (value == null) return '-'
    return `${value > 0 ? '+' : ''}${value.toFixed(2)}%`
}

/**
 * RiskAlertStats - 显示最高/最低/当前涨跌幅
 * 
 * 颜色规则：
 * - 正数（涨）：红色
 * - 负数（跌）：绿色
 * - 零：灰色
 */
const RiskAlertStats: React.FC<RiskAlertStatsProps> = ({
    currentPercent,
    maxPercent,
    minPercent
}) => {
    const currentColor = getChangeColor(currentPercent)
    const maxColor = getChangeColor(maxPercent)
    const minColor = getChangeColor(minPercent)

    const getIcon = (value: number) => {
        if (value > 0) return <ArrowUpOutlined style={{ color: '#eb2f96' }} />
        if (value < 0) return <ArrowDownOutlined style={{ color: '#52c41a' }} />
        return <MinusOutlined style={{ color: '#999' }} />
    }

    return (
        <div style={{
            display: 'flex',
            gap: '16px',
            alignItems: 'center',
            flexWrap: 'wrap'
        }}>
            {/* 当前涨跌幅 */}
            <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '12px', color: '#888', marginBottom: '2px' }}>
                    当前
                </div>
                <div style={{
                    fontSize: '18px',
                    fontWeight: 'bold',
                    color: currentColor,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    justifyContent: 'center'
                }}>
                    {getIcon(currentPercent)}
                    {formatPercent(currentPercent)}
                </div>
            </div>

            {/* 分隔线 */}
            <div style={{
                width: '1px',
                height: '36px',
                background: '#e8e8e8'
            }} />

            {/* 最高涨幅 */}
            <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '12px', color: '#888', marginBottom: '2px' }}>
                    最大涨幅
                </div>
                <div style={{
                    fontSize: '16px',
                    fontWeight: 500,
                    color: maxColor,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    justifyContent: 'center'
                }}>
                    {getIcon(maxPercent)}
                    {formatPercent(maxPercent)}
                </div>
            </div>

            {/* 分隔线 */}
            <div style={{
                width: '1px',
                height: '36px',
                background: '#e8e8e8'
            }} />

            {/* 最低跌幅 */}
            <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '12px', color: '#888', marginBottom: '2px' }}>
                    最大跌幅
                </div>
                <div style={{
                    fontSize: '16px',
                    fontWeight: 500,
                    color: minColor,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    justifyContent: 'center'
                }}>
                    {getIcon(minPercent)}
                    {formatPercent(minPercent)}
                </div>
            </div>
        </div>
    )
}

export default RiskAlertStats
