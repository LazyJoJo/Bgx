import { useState, useEffect, useCallback } from 'react'
import { Modal, Form, Select, message, Spin, Tag, Typography, Space, InputNumber, Divider, Button, Switch } from 'antd'
import { CheckCircleOutlined, ExclamationCircleOutlined, CheckCircleFilled, StopOutlined } from '@ant-design/icons'
import { stocksApi } from '@services/api/stocks'
import { subscriptionsApi, SymbolType, AlertType, Subscription } from '@services/api/subscriptions'
import { useAppDispatch } from '@store/hooks'
import { updateSubscription } from '@store/slices/subscriptionsSlice'
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

interface SubscriptionEditProps {
  visible: boolean
  subscription: Subscription | null
  onClose: () => void
  onSuccess: () => void
}

const SubscriptionEdit: React.FC<SubscriptionEditProps> = ({ visible, subscription, onClose, onSuccess }) => {
  const [form] = Form.useForm()
  const dispatch = useAppDispatch()
  const [loading, setLoading] = useState(false)
  const [symbolType, setSymbolType] = useState<SymbolType>('STOCK')
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searching, setSearching] = useState(false)
  const [existingSubscriptions, setExistingSubscriptions] = useState<Subscription[]>([])
  const [loadingExisting, setLoadingExisting] = useState(false)
  const [isActive, setIsActive] = useState(true)

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
      setSymbolType('STOCK')
      setExistingSubscriptions([])
      setIsActive(true)
    }
  }, [visible, form])

  // 当subscription变化时，预填充表单数据
  useEffect(() => {
    if (subscription && visible) {
      setSymbolType(subscription.symbolType)
      setIsActive(subscription.status === 'ACTIVE')
      form.setFieldsValue({
        symbol: subscription.symbol,
        symbolType: subscription.symbolType,
        alertType: subscription.alertType,
        targetPrice: subscription.targetPrice,
        targetChangePercent: subscription.targetChangePercent,
        remark: subscription.remark ? subscription.remark.split(',') : [],
      })
      // 加载已存在订阅用于搜索
      fetchExistingSubscriptions(subscription.symbolType)
    }
  }, [subscription, visible, form, fetchExistingSubscriptions])

  // 标的类型变化处理
  const handleSymbolTypeChange = (value: SymbolType) => {
    setSymbolType(value)
    form.setFieldsValue({ symbol: undefined })
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
        // 标记已添加的标的（排除当前编辑的）
        const subscribedCodes = existingSubscriptions
          .filter(s => s.symbol !== subscription?.symbol)
          .map(s => s.symbol)
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
  }, [existingSubscriptions, subscription?.symbol])

  // 检查标的是否已订阅
  const isSymbolSubscribed = (code: string): boolean => {
    return existingSubscriptions.some(s => s.symbol === code && s.symbol !== subscription?.symbol)
  }

  // 首次聚焦时加载数据
  const handleFocus = () => {
    if (searchResults.length === 0) {
      searchSymbols('', symbolType, true)
      fetchExistingSubscriptions(symbolType)
    }
  }

  // 提交编辑
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)

      if (!subscription) {
        message.error('订阅信息不存在')
        setLoading(false)
        return
      }

      const updateData: any = {
        alertType: values.alertType,
        remark: values.remark?.join(',') || '',
        isActive: isActive,
      }

      // 根据提醒类型添加相应字段
      if (values.alertType === 'PERCENTAGE_CHANGE') {
        updateData.targetChangePercent = values.targetChangePercent
        updateData.targetPrice = undefined
      } else {
        updateData.targetPrice = values.targetPrice
        updateData.targetChangePercent = undefined
      }

      const result = await dispatch(updateSubscription({ id: subscription.id, data: updateData })).unwrap()

      if (result) {
        message.success('订阅更新成功')
        onSuccess()
        onClose()
      }
    } catch (error: any) {
      console.error('更新订阅失败:', error)
      message.error(error || '更新失败，请重试')
    } finally {
      setLoading(false)
    }
  }

  // 计算选中的标的名称
  const selectedSymbolCode = form.getFieldValue('symbol')
  const selectedResult = searchResults.find(r => r.code === selectedSymbolCode)

  return (
    <Modal
      title="编辑订阅"
      open={visible}
      onCancel={onClose}
      width={600}
      footer={null}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          symbolType: 'STOCK',
          alertType: 'PERCENTAGE_CHANGE',
          targetChangePercent: 1.0,
        }}
      >
        {/* 标的类型（不可修改） */}
        <Form.Item
          name="symbolType"
          label="标的类型"
        >
          <Select
            placeholder="请选择标的类型"
            onChange={handleSymbolTypeChange}
            disabled
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

        {/* 标的（不可修改，因为标的本身不能变更） */}
        <Form.Item
          name="symbol"
          label="标的"
          extra="标的一旦创建不可修改，如需更改请删除后重新创建"
        >
          <Select
            showSearch
            placeholder="搜索标的"
            disabled
            filterOption={(input, option) =>
              (option?.label as string)?.toLowerCase().includes(input.toLowerCase())
            }
            options={subscription ? [{
              value: subscription.symbol,
              label: `${subscription.symbol} - ${subscription.symbolName}`,
            }] : []}
          />
        </Form.Item>

        {selectedResult && (
          <div style={{ marginBottom: 16, padding: '12px', background: '#fafafa', borderRadius: 8 }}>
            <Space>
              <Tag color={selectedResult.type === 'STOCK' ? 'blue' : 'green'}>
                {selectedResult.type === 'STOCK' ? '股票' : '基金'}
              </Tag>
              <Text strong>{selectedResult.code}</Text>
              <Text>{selectedResult.name}</Text>
            </Space>
          </div>
        )}

        <Divider />

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

        {/* 状态开关 */}
        <Form.Item
          label="生效状态"
          extra="关闭后将暂停此订阅的风险提醒"
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <Switch
              checked={isActive}
              onChange={setIsActive}
              checkedChildren={<CheckCircleFilled />}
              unCheckedChildren={<StopOutlined />}
              disabled={loading}
            />
            <Typography.Text style={{ color: isActive ? '#52c41a' : '#d9d9d9' }}>
              {isActive ? '已启用' : '已停用'}
            </Typography.Text>
          </div>
        </Form.Item>

        <Divider />

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
          <Button onClick={onClose} disabled={loading}>
            取消
          </Button>
          <Button
            type="primary"
            loading={loading}
            onClick={handleSubmit}
            style={{ minWidth: '140px' }}
          >
            保存修改
          </Button>
        </div>
      </Form>
    </Modal>
  )
}

export default SubscriptionEdit