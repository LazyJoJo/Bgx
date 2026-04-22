import { CheckCircleOutlined, ExclamationCircleOutlined, RocketOutlined, SettingOutlined } from '@ant-design/icons'
import { stocksApi } from '@services/api/stocks'
import { Subscription, subscriptionsApi, SymbolType } from '@services/api/subscriptions'
import { Alert, Button, Divider, Form, InputNumber, message, Modal, Select, Space, Spin, Tag, Typography } from 'antd'
import { useCallback, useEffect, useState } from 'react'
import './SubscriptionCreate.css'

const { Option } = Select
const { Text } = Typography

// 标的搜索结果类型
interface SearchResult {
  id: number
  code: string
  name: string
  type: SymbolType
  market?: string
  active?: boolean
}

interface SubscriptionCreateProps {
  visible: boolean
  onClose: () => void
  onSuccess: () => void
}

type CreateMode = 'quick' | 'custom'

const SubscriptionCreate: React.FC<SubscriptionCreateProps> = ({ visible, onClose, onSuccess }) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [createMode, setCreateMode] = useState<CreateMode>('quick')
  const [symbolType, setSymbolType] = useState<SymbolType>('STOCK')
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searching, setSearching] = useState(false)
  const [selectedSymbols, setSelectedSymbols] = useState<string[]>([])
  const [existingSubscriptions, setExistingSubscriptions] = useState<Subscription[]>([])
  const [loadingExisting, setLoadingExisting] = useState(false)

  // 获取已存在的订阅列表
  const fetchExistingSubscriptions = useCallback(async (type: SymbolType) => {
    setLoadingExisting(true)
    try {
      const userId = Number(localStorage.getItem('userId')) || 1
      const response = await subscriptionsApi.getSubscriptions(userId, { symbolType: type })
      if (response.success) {
        setExistingSubscriptions(response.data || [])
      }
    } catch (error) {
      console.error('获取已存在订阅失败:', error)
    } finally {
      setLoadingExisting(false)
    }
  }, [])

  // 当模态框关闭时重置表单
  useEffect(() => {
    if (!visible) {
      form.resetFields()
      setSearchResults([])
      setSelectedSymbols([])
      setSymbolType('STOCK')
      setCreateMode('quick')
      setExistingSubscriptions([])
    }
  }, [visible, form])

  // 标的类型变化时重新加载数据
  const handleSymbolTypeChange = (value: SymbolType) => {
    setSymbolType(value)
    form.setFieldsValue({ symbols: [] })
    setSelectedSymbols([])
    setSearchResults([])
    fetchExistingSubscriptions(value)
  }

  // 搜索标的
  const searchSymbols = useCallback(async (keyword: string, type: SymbolType, loadAll: boolean = false) => {
    if ((!keyword || keyword.length < 1) && !loadAll) {
      setSearchResults([])
      return
    }

    setSearching(true)
    try {
      const searchKeyword = loadAll ? '' : keyword
      const response = await stocksApi.searchTargets(searchKeyword, type)
      if (response.success && response.data) {
        // 标记已添加的标的
        const subscribedCodes = existingSubscriptions.map(s => s.symbol)
        const resultsWithStatus = response.data.map((r: any) => ({
          ...r,
          isSubscribed: subscribedCodes.includes(r.code)
        }))
        setSearchResults(resultsWithStatus)
      } else {
        setSearchResults([])
      }
    } catch (error) {
      console.error('搜索标的失败:', error)
      setSearchResults([])
    } finally {
      setSearching(false)
    }
  }, [existingSubscriptions])

  // 检查标的是否已订阅
  const isSymbolSubscribed = (code: string): boolean => {
    return existingSubscriptions.some(s => s.symbol === code)
  }

  // 快速订阅
  const handleQuickSubscribe = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)

      const userId = Number(localStorage.getItem('userId')) || 1
      const symbols = values.symbols

      if (!symbols || symbols.length === 0) {
        message.warning('请选择至少一个标的')
        setLoading(false)
        return
      }

      const response: any = await subscriptionsApi.batchCreateSubscriptions({
        userId,
        symbolType: values.symbolType,
        symbols,
        alertType: 'PERCENTAGE_CHANGE',
        targetChangePercent: 1.0,
        isActive: true,
      })

      if (response.code === 200 || response.success) {
        const result = response.data || response
        message.success(`成功订阅 ${result.successCount || symbols.length} 个标的的风险提醒`)
        form.resetFields()
        onSuccess()
        onClose()
      } else {
        message.error(response.message || '订阅失败')
      }
    } catch (error: any) {
      console.error('快速订阅失败:', error)
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

  // 自定义订阅
  const handleCustomSubscribe = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)

      const userId = Number(localStorage.getItem('userId')) || 1
      const symbols = values.symbols

      if (!symbols || symbols.length === 0) {
        message.warning('请选择至少一个标的')
        setLoading(false)
        return
      }

      // 批量创建自定义订阅
      const response: any = await subscriptionsApi.batchCreateSubscriptions({
        userId,
        symbolType: values.symbolType,
        symbols,
        alertType: values.alertType || 'PERCENTAGE_CHANGE',
        targetPrice: values.targetPrice,
        targetChangePercent: values.targetChangePercent,
        remark: values.remark,
        isActive: true,
      })

      if (response.code === 200 || response.success) {
        const result = response.data || response
        message.success(`成功创建 ${result.successCount || symbols.length} 个自定义订阅`)
        form.resetFields()
        onSuccess()
        onClose()
      } else {
        message.error(response.message || '订阅创建失败')
      }
    } catch (error: any) {
      console.error('自定义订阅失败:', error)
      if (error.response?.data?.message) {
        message.error(error.response.data.message)
      } else if (error.message) {
        message.error(error.message)
      } else {
        message.error('订阅创建失败，请重试')
      }
    } finally {
      setLoading(false)
    }
  }

  // 首次聚焦时加载数据
  const handleFocus = () => {
    if (searchResults.length === 0) {
      searchSymbols('', symbolType, true)
      fetchExistingSubscriptions(symbolType)
    }
  }

  return (
    <Modal
      title="创建订阅"
      open={visible}
      onCancel={onClose}
      width={800}
      footer={null}
      destroyOnClose
    >
      {/* 模式选择卡片 */}
      <div className="mode-selection">
        <div
          className={`mode-card ${createMode === 'quick' ? 'active' : ''}`}
          onClick={() => setCreateMode('quick')}
        >
          <div className="mode-card-icon">
            <RocketOutlined />
          </div>
          <div className="mode-card-content">
            <div className="mode-card-title">快速订阅</div>
            <div className="mode-card-subtitle">一键订阅涨跌幅超过1%的风险提醒</div>
          </div>
          {createMode === 'quick' && (
            <div className="mode-card-check">
              <CheckCircleOutlined />
            </div>
          )}
        </div>

        <div
          className={`mode-card ${createMode === 'custom' ? 'active' : ''}`}
          onClick={() => setCreateMode('custom')}
        >
          <div className="mode-card-icon">
            <SettingOutlined />
          </div>
          <div className="mode-card-content">
            <div className="mode-card-title">自定义设置</div>
            <div className="mode-card-subtitle">自定义提醒参数</div>
          </div>
          {createMode === 'custom' && (
            <div className="mode-card-check">
              <CheckCircleOutlined />
            </div>
          )}
        </div>
      </div>

      <Divider />

      <Form
        form={form}
        layout="vertical"
        initialValues={{
          symbolType: 'STOCK',
          alertType: 'PERCENTAGE_CHANGE',
          targetChangePercent: 1.0,
        }}
      >
        <Alert
          message={createMode === 'quick' ? '快速订阅模式' : '自定义设置模式'}
          description={
            createMode === 'quick'
              ? '系统将自动为您设置涨跌幅1%的提醒条件，检测到超过1%波动时发送提醒。'
              : '您可以自定义提醒条件，包括涨跌幅阈值、价格提醒等。'
          }
          type={createMode === 'quick' ? 'info' : 'success'}
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
            disabled={loading}
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
              可搜索股票/基金代码或名称，支持多选。已添加的标的显示"已添加"标记
            </Text>
          }
        >
          <Select
            mode="multiple"
            showSearch
            placeholder={symbolType === 'STOCK' ? '搜索股票代码或名称...' : '搜索基金代码或名称...'}
            filterOption={false}
            notFoundContent={searching ? <Spin size="small" /> : loadingExisting ? <Spin size="small" tip="加载中..." /> : '请输入关键词搜索'}
            allowClear
            style={{ width: '100%' }}
            maxTagCount="responsive"
            onSearch={(value) => searchSymbols(value, symbolType)}
            onFocus={handleFocus}
            onChange={(value) => setSelectedSymbols(value)}
            disabled={loading}
            tagRender={({ value, closable, onClose }) => {
              const isSubscribed = isSymbolSubscribed(value as string)
              return (
                <Tag
                  closable={closable && !isSubscribed}
                  onClose={onClose}
                  color={isSubscribed ? 'default' : 'blue'}
                  style={{ marginRight: 4 }}
                  icon={isSubscribed ? <ExclamationCircleOutlined /> : undefined}
                >
                  {isSubscribed ? '已添加' : value}
                </Tag>
              )
            }}
          >
            {searchResults.map((result) => {
              const isSubscribed = isSymbolSubscribed(result.code)
              return (
                <Option
                  key={result.code}
                  value={result.code}
                  disabled={isSubscribed}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>
                      <Tag color={result.type === 'STOCK' ? 'blue' : 'green'}>
                        {result.type === 'STOCK' ? '股票' : '基金'}
                      </Tag>
                      <strong>{result.code}</strong>
                      <span style={{ marginLeft: 8 }}>{result.name}</span>
                      {result.market && <Tag style={{ marginLeft: 4 }}>{result.market}</Tag>}
                    </span>
                    {isSubscribed && (
                      <Tag color="orange" icon={<CheckCircleOutlined />}>已添加</Tag>
                    )}
                  </div>
                </Option>
              )
            })}
          </Select>
        </Form.Item>

        <Form.Item label="已选标的">
          <div style={{ maxHeight: 120, overflowY: 'auto', padding: '8px 12px', background: '#fafafa', borderRadius: '8px' }}>
            {selectedSymbols.length > 0 ? (
              <Space wrap size={[4, 4]}>
                {selectedSymbols.map((code) => {
                  const result = searchResults.find(r => r.code === code)
                  const isSubscribed = isSymbolSubscribed(code)
                  return (
                    <Tag
                      key={code}
                      color={isSubscribed ? 'orange' : 'blue'}
                      icon={isSubscribed ? <CheckCircleOutlined /> : undefined}
                    >
                      {result ? `${result.name}(${code})` : code}
                      {isSubscribed && ' - 已添加'}
                    </Tag>
                  )
                })}
              </Space>
            ) : (
              <Text type="secondary">暂未选择任何标的</Text>
            )}
          </div>
        </Form.Item>

        {/* 自定义模式额外选项 */}
        {createMode === 'custom' && (
          <>
            <Divider orientation="left">自定义提醒设置</Divider>

            <Form.Item
              name="alertType"
              label="提醒类型"
              rules={[{ required: true, message: '请选择提醒类型' }]}
            >
              <Select placeholder="请选择提醒类型" disabled={loading}>
                <Option value="PERCENTAGE_CHANGE">
                  <Space>
                    <Tag color="orange">涨跌幅</Tag>
                    <span>涨跌幅达到阈值时提醒</span>
                  </Space>
                </Option>
                <Option value="PRICE_ABOVE">
                  <Space>
                    <Tag color="red">价格上限</Tag>
                    <span>价格超过阈值时提醒</span>
                  </Space>
                </Option>
                <Option value="PRICE_BELOW">
                  <Space>
                    <Tag color="green">价格下限</Tag>
                    <span>价格低于阈值时提醒</span>
                  </Space>
                </Option>
              </Select>
            </Form.Item>

            <Form.Item
              noStyle
              shouldUpdate={(prevValues, currentValues) => prevValues.alertType !== currentValues.alertType}
            >
              {({ getFieldValue }) => {
                const alertType = getFieldValue('alertType')
                return (
                  <>
                    {alertType === 'PERCENTAGE_CHANGE' && (
                      <Form.Item
                        name="targetChangePercent"
                        label="涨跌幅阈值"
                        rules={[{ required: true, message: '请输入涨跌幅阈值' }]}
                        extra="当涨跌幅超过此值时发送提醒，例：输入1.0表示1%"
                      >
                        <InputNumber
                          min={0.1}
                          max={100}
                          precision={1}
                          addonAfter="%"
                          style={{ width: '100%' }}
                          placeholder="请输入涨跌幅阈值"
                          disabled={loading}
                        />
                      </Form.Item>
                    )}

                    {alertType === 'PRICE_ABOVE' && (
                      <Form.Item
                        name="targetPrice"
                        label="目标价格上限"
                        rules={[{ required: true, message: '请输入目标价格' }]}
                        extra="当价格超过此值时发送提醒"
                      >
                        <InputNumber
                          min={0}
                          precision={2}
                          addonAfter="元"
                          style={{ width: '100%' }}
                          placeholder="请输入目标价格"
                          disabled={loading}
                        />
                      </Form.Item>
                    )}

                    {alertType === 'PRICE_BELOW' && (
                      <Form.Item
                        name="targetPrice"
                        label="目标价格下限"
                        rules={[{ required: true, message: '请输入目标价格' }]}
                        extra="当价格低于此值时发送提醒"
                      >
                        <InputNumber
                          min={0}
                          precision={2}
                          addonAfter="元"
                          style={{ width: '100%' }}
                          placeholder="请输入目标价格"
                          disabled={loading}
                        />
                      </Form.Item>
                    )}
                  </>
                )
              }}
            </Form.Item>

            <Form.Item
              name="remark"
              label="备注"
              extra="可选，添加备注信息方便识别"
            >
              <Select
                mode="tags"
                placeholder="请输入备注（可选）"
                style={{ width: '100%' }}
                disabled={loading}
                maxTagCount={5}
              />
            </Form.Item>
          </>
        )}

        <Divider />

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
          <Button onClick={onClose} disabled={loading}>
            取消
          </Button>
          <Button
            type="primary"
            loading={loading}
            onClick={createMode === 'quick' ? handleQuickSubscribe : handleCustomSubscribe}
            style={{ minWidth: '140px' }}
          >
            {createMode === 'quick' ? '一键订阅' : '创建订阅'}
          </Button>
        </div>
      </Form>
    </Modal>
  )
}

export default SubscriptionCreate
