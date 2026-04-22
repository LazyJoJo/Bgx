import { CheckCircleOutlined } from '@ant-design/icons'
import React from 'react'

/**
 * RiskAlertEmpty - 无风险提示组件
 * 
 * 当天内没有风险时显示此提示
 */
const RiskAlertEmpty: React.FC = () => {
    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '48px 24px',
            background: '#fafafa',
            borderRadius: '8px',
            margin: '16px 0'
        }}>
            <CheckCircleOutlined style={{
                fontSize: '48px',
                color: '#52c41a',
                marginBottom: '16px'
            }} />
            <div style={{
                fontSize: '18px',
                fontWeight: 500,
                color: '#333',
                marginBottom: '8px'
            }}>
                暂时无风险
            </div>
            <div style={{
                fontSize: '14px',
                color: '#888',
                textAlign: 'center'
            }}>
                今日所有关注的基金和股票均无异常波动
            </div>
        </div>
    )
}

export default RiskAlertEmpty
