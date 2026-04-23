import { fundsApi } from '@services/api/funds'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchFunds } from '@store/slices/fundsSlice'
import { Button, Form, Input, message, Modal, Space, Spin, Typography } from 'antd'
import { AxiosError } from 'axios'
import { useCallback, useEffect, useRef, useState } from 'react'

const { Text } = Typography

interface FundCreateModalProps {
    visible: boolean
    onClose: () => void
}

interface FundInfo {
    name: string
    type: string
}

interface AddFundResponse {
    success: boolean
    code?: number
    message?: string
    data?: FundInfo
}

// Error message extraction helper
const getErrorMessage = (error: unknown): string => {
    if (error instanceof AxiosError) {
        return error.response?.data?.message || error.message
    }
    if (error instanceof Error) {
        return error.message
    }
    return '操作失败，请重试'
}

const FundCreateModal: React.FC<FundCreateModalProps> = ({ visible, onClose }) => {
    const [form] = Form.useForm()
    const [loading, setLoading] = useState(false)
    const [fetching, setFetching] = useState(false)
    const [fundInfo, setFundInfo] = useState<FundInfo | null>(null)
    const [fundCodeInput, setFundCodeInput] = useState('')
    const dispatch = useAppDispatch()
    const existingFunds = useAppSelector((state) => state.funds.list)
    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

    // Reset form when modal opens/closes
    useEffect(() => {
        if (!visible) {
            form.resetFields()
            setFundInfo(null)
            setFundCodeInput('')
        }
    }, [visible, form])

    // Cleanup debounce timer on unmount
    useEffect(() => {
        return () => {
            if (debounceRef.current) {
                clearTimeout(debounceRef.current)
            }
        }
    }, [])

    // Validate fund code and fetch fund info with debounce
    const handleValidateCode = useCallback(async (fundCode: string) => {
        if (!fundCode || fundCode.length < 6) {
            setFundInfo(null)
            return
        }

        // Clear previous debounce timer
        if (debounceRef.current) {
            clearTimeout(debounceRef.current)
        }

        // Set new debounce timer (300ms)
        debounceRef.current = setTimeout(async () => {
            setFetching(true)
            try {
                const response = await fundsApi.getFundDetail(fundCode)
                if (response.success && response.data) {
                    setFundInfo({
                        name: response.data.name || '',
                        type: response.data.type || ''
                    })
                } else {
                    setFundInfo(null)
                }
            } catch (error) {
                console.error('Failed to fetch fund info:', error)
                setFundInfo(null)
            } finally {
                setFetching(false)
            }
        }, 300)
    }, [])

    // Handle form submission
    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            setLoading(true)

            const fundCode = values.fundCode.trim()

            // Check if fund already exists in the list (FR-4 / AC-7)
            const isAlreadyAdded = existingFunds.some(
                (fund) => String(fund.fundCode).trim() === fundCode
            )
            if (isAlreadyAdded) {
                message.warning('该基金已在监控列表中')
                setLoading(false)
                return
            }

            const response = await fundsApi.addFundTarget(fundCode) as unknown as AddFundResponse

            if (response.success || response.code === 200) {
                message.success(`基金 ${fundInfo?.name || fundCode} 添加成功`)
                form.resetFields()
                setFundInfo(null)
                // Refresh fund list
                dispatch(fetchFunds({}))
                onClose()
            } else {
                message.error(response.message || '添加基金失败，请检查基金代码是否正确')
            }
        } catch (error: unknown) {
            console.error('Failed to add fund:', error)
            message.error(getErrorMessage(error))
        } finally {
            setLoading(false)
        }
    }

    return (
        <Modal
            title="添加基金"
            open={visible}
            onCancel={onClose}
            width={480}
            footer={null}
            destroyOnClose
        >
            <Form
                form={form}
                layout="vertical"
                initialValues={{
                    fundCode: ''
                }}
            >
                <Form.Item
                    name="fundCode"
                    label="基金代码"
                    rules={[
                        { required: true, message: '请输入基金代码' },
                        { pattern: /^[0-9]{6}$/, message: '基金代码为6位数字' }
                    ]}
                    extra="请输入6位基金代码，系统将从新浪财经获取基金信息"
                >
                    <Input
                        placeholder="例如：110022"
                        maxLength={6}
                        onChange={(e) => {
                            // Only allow numbers
                            const value = e.target.value.replace(/\D/g, '')
                            form.setFieldValue('fundCode', value)
                            setFundCodeInput(value)
                            if (value.length === 6) {
                                handleValidateCode(value)
                            } else {
                                setFundInfo(null)
                            }
                        }}
                        suffix={
                            fetching ? (
                                <Spin size="small" />
                            ) : fundInfo ? (
                                <Text type="success" style={{ fontSize: 12 }}>已找到</Text>
                            ) : null
                        }
                    />
                </Form.Item>

                {fundInfo && (
                    <div style={{
                        padding: '12px 16px',
                        background: '#f6ffed',
                        border: '1px solid #b7eb8f',
                        borderRadius: '8px',
                        marginBottom: '16px'
                    }}>
                        <Text strong style={{ display: 'block', marginBottom: '4px' }}>
                            {fundInfo.name}
                        </Text>
                        <Text type="secondary" style={{ fontSize: '12px' }}>
                            类型：{fundInfo.type || '未知'} | 代码：{form.getFieldValue('fundCode')}
                        </Text>
                    </div>
                )}

                <Form.Item style={{ marginBottom: 0 }}>
                    <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                        <Button onClick={onClose} disabled={loading}>
                            取消
                        </Button>
                        <Button
                            type="primary"
                            onClick={handleSubmit}
                            loading={loading}
                            disabled={fundCodeInput.length !== 6}
                        >
                            添加
                        </Button>
                    </Space>
                </Form.Item>
            </Form>
        </Modal>
    )
}

export default FundCreateModal