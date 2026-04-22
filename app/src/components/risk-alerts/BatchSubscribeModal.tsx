import apiClient from '@services/api/client'
import { stocksApi } from '@services/api/stocks'
import { Alert, Form, message, Modal, Select, Space, Spin, Tag, Typography } from 'antd'
import { useCallback, useEffect, useState } from 'react'

const { Option } = Select
const { Text } = Typography

// 标的搜索结果类型
interface SearchResult {
  id: number
  code: string
  name: string
  type: string
  market?: string
  active?: boolean
}

// 批量订阅响应类型
interface BatchSubscribeResponse {
  successCount: number
  failCount: number
  successList: any[]
  failList: Array<{ symbol: string; reason: string }>
}

interface BatchSubscribeModalProps {
  visible: boolean
  onClose: () => void
  onSuccess: () => void
}

const BatchSubscribeModal: React.FC<BatchSubscribeModalProps> = ({ visible, onClose, onSuccess }) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [symbolType, setSymbolType] = useState<string>('STOCK')
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searching, setSearching] = useState(false)
  const [selectedSymbols, setSelectedSymbols] = useState<string[]>([])

  // 当模态框关闭时重置表单
  useEffect(() => {
    if (!visible) {
      form.resetFields()
      setSearchResults([])
      setSelectedSymbols([])
      setSymbolType('STOCK')
    }
  }, [visible, form])

  // 搜索标的
  const searchSymbols = useCallback(async (keyword: string, type: string, loadAll: boolean = false) => {
    if ((!keyword || keyword.length < 1) && !loadAll) {
      setSearchResults([])
      return
    }

    setSearching(true)
    try {
      const searchKeyword = loadAll ? '' : keyword
      const response = await stocksApi.searchTargets(searchKeyword, type)
      if (response.success && response.data) {
        setSearchResults(response.data)
      } else {
        setSearchResults([])
      }
    } catch (error) {
      console.error('搜索标的失败:', error)
      setSearchResults([])
    } finally {
      setSearching(false)
    }
  }, [])

  // 标的类型变化时重新加载数据
  const handleSymbolTypeChange = (value: string) => {
    setSymbolType(value)
    form.setFieldsValue({ symbols: [] })
    setSelectedSymbols([])
    setSearchResults([])

    // 重新加载对应类型的标的
    searchSymbols('', value, true)
  }

  // 批量订阅
  const handleSubscribe = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)

      const userId = Number(localStorage.getItem('userId')) || 1
      const symbols = values.symbols

      const response: any = await apiClient.post('/risk-alerts/subscribe', {
        userId,
        symbolType: values.symbolType,
        symbols,
        alertType: 'PERCENTAGE_CHANGE',
        targetChangePercent: 1.0,
        isActive: true
      })

      // apiClient拦截器已返回response.data，结构为：{code, message, data}
      if (response.code === 200) {
        const result: BatchSubscribeResponse = response.data
        message.success(`成功订阅 ${result.successCount} 个标的的风险提醒`)

        if (result.failCount > 0) {
          const failMessages = result.failList.map(f => `${f.symbol}: ${f.reason}`).join('; ')
          message.warning(`${result.failCount} 个标的订阅失败: ${failMessages}`, 5)
        }

        form.resetFields()
        onSuccess()
        onClose()
      } else {
        message.error(response.message || '订阅失败')
      }
    } catch (error: any) {
      console.error('批量订阅失败:', error)
      if (error.response?.data?.message) {
        message.error(error.response.data.message)
      } else if (error.message) {
        message.error(error.message)
      } else {
        message.error('订阅失败，请重试')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      title="批量订阅风险提醒"
      open={visible}
      onOk={handleSubscribe}
      onCancel={onClose}
      confirmLoading={loading}
      width={700}
      okText="批量订阅"
      cancelText="取消"
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          symbolType: 'STOCK'
        }}
      >
        <Alert
          message="订阅说明"
          description="选择需要监控的股票或基金，系统将在每个工作日的11:30和14:30自动检测涨跌幅，超过1%时发送风险提醒。"
          type="info"
          showIcon
          style={{ marginBottom: 24 }}
        />

        <Form.Item
          name="symbolType"
          label="标的类型"
          rules={[{ required: true, message: '请选择标的类型' }]}
        >
          <Select
            placeholder="请选择标的类型"
            onChange={handleSymbolTypeChange}
          >
            <Option value="STOCK">
              <Space>
                <Tag color="blue">股票</Tag>
                <span>股票市场</span>
              </Space>
            </Option>
            <Option value="FUND">
              <Space>
                <Tag color="green">基金</Tag>
                <span>基金市场</span>
              </Space>
            </Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="symbols"
          label="选择标的"
          rules={[{ required: true, message: '请选择至少一个标的' }]}
          extra={
            <Text type="secondary">
              可搜索股票代码或名称，支持多选
            </Text>
          }
        >
          <Select
            mode="multiple"
            showSearch
            placeholder={symbolType === 'STOCK' ? '搜索股票代码或名称...' : '搜索基金代码或名称...'}
            filterOption={false}
            notFoundContent={searching ? <Spin size="small" /> : '请输入关键词搜索'}
            allowClear
            style={{ width: '100%' }}
            maxTagCount="responsive"
            onSearch={(value) => {
              searchSymbols(value, symbolType)
            }}
            onFocus={() => {
              // 首次聚焦时加载所有活跃标的
              if (searchResults.length === 0) {
                searchSymbols('', symbolType, true)
              }
            }}
            onChange={(value) => {
              setSelectedSymbols(value)
            }}
          >
            {searchResults.map((result) => (
              <Option key={result.code} value={result.code}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>
                    <Tag color={result.type === 'STOCK' ? 'blue' : 'green'}>
                      {result.type === 'STOCK' ? '股票' : '基金'}
                    </Tag>
                    <strong>{result.code}</strong>
                    <span style={{ marginLeft: 8 }}>{result.name}</span>
                    {result.market && <Tag style={{ marginLeft: 4 }}>{result.market}</Tag>}
                  </span>
                </div>
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item label="已选标的">
          <div style={{ maxHeight: 150, overflowY: 'auto' }}>
            {selectedSymbols.length > 0 ? (
              <Space wrap>
                {selectedSymbols.map((code) => {
                  const result = searchResults.find(r => r.code === code)
                  return (
                    <Tag key={code} color="blue" style={{ marginBottom: 8 }}>
                      {result ? `${result.name}(${code})` : code}
                    </Tag>
                  )
                })}
              </Space>
            ) : (
              <Text type="secondary">暂未选择任何标的</Text>
            )}
          </div>
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default BatchSubscribeModal
