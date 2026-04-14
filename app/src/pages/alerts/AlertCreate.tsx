import { useState, useEffect, useCallback, useRef } from 'react'
import {
  Form, Input, Select, InputNumber, Button, Card, Space, message,
  Switch, Radio, Spin, Tag, Modal, List, Typography, Result, Divider
} from 'antd'
import {
  ArrowLeftOutlined, SaveOutlined,
  CheckCircleOutlined, CloseCircleOutlined, ExclamationCircleOutlined
} from '@ant-design/icons'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import {
  createAlert, updateAlert, batchCreateAlert
} from '@store/slices/alertsSlice'
import { stocksApi } from '@services/api/stocks'
import type { BatchCreateAlertResponse } from '@services/api/alerts'

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

type CreateMode = 'single' | 'batch'
type SymbolType = 'STOCK' | 'FUND' | null

// 批量创建结果类型
interface BatchResult {
  success: boolean
  data: BatchCreateAlertResponse | null
  error: string | null
}

const AlertCreate = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const dispatch = useAppDispatch()
  const { selectedAlert } = useAppSelector(state => state.alerts)

  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [alertType, setAlertType] = useState('PRICE_ABOVE')
  const [createMode, setCreateMode] = useState<CreateMode>(() => {
    // 从URL参数读取初始模式
    const mode = searchParams.get('mode')
    return mode === 'batch' ? 'batch' : 'single'
  })

  // 批量模式专用状态
  const [symbolType, setSymbolType] = useState<SymbolType>(createMode === 'batch' ? null : 'STOCK')
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searching, setSearching] = useState(false)
  const [selectedSymbols, setSelectedSymbols] = useState<string[]>([])
  const [resultModalVisible, setResultModalVisible] = useState(false)
  const [batchResult, setBatchResult] = useState<BatchResult | null>(null)

  // 搜索防抖定时器
  const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const isEdit = !!id

  // 编辑模式初始化
  useEffect(() => {
    if (isEdit && selectedAlert) {
      form.setFieldsValue({
        symbol: selectedAlert.symbol,
        symbolType: selectedAlert.symbolType,
        alertType: selectedAlert.alertType,
        targetPrice: selectedAlert.targetPrice,
        targetChangePercent: selectedAlert.targetChangePercent,
        status: selectedAlert.status === 'ACTIVE'
      })
      setAlertType(selectedAlert.alertType)
    }
  }, [isEdit, selectedAlert, form])

  // 批量模式：类型切换时清空已选和搜索结果
  useEffect(() => {
    if (createMode === 'batch') {
      form.setFieldsValue({ symbols: [] })
      setSelectedSymbols([])
      setSearchResults([])
    } else {
      setSymbolType('STOCK')
    }
  }, [createMode, form])

  // 提醒类型切换
  const handleAlertTypeChange = (value: string) => {
    setAlertType(value)
    if (value === 'PERCENTAGE_CHANGE') {
      form.setFieldsValue({ targetPrice: undefined })
    } else {
      form.setFieldsValue({ targetChangePercent: undefined })
    }
  }

  // 模式切换
  const handleModeChange = (e: any) => {
    setCreateMode(e.target.value)
    form.resetFields(['symbol', 'symbols', 'symbolType'])
    setSearchResults([])
    setSelectedSymbols([])
  }

  // 批量模式：标的类型选择
  const handleSymbolTypeChange = (value: SymbolType) => {
    if (symbolType !== null && value !== symbolType) {
      // 切换类型时确认清空
      Modal.confirm({
        title: '确认切换类型',
        icon: <ExclamationCircleOutlined />,
        content: '切换标的类型将清空已选标的，是否继续？',
        okText: '继续',
        cancelText: '取消',
        onOk: () => {
          setSymbolType(value)
          form.setFieldsValue({ symbols: [] })
          setSelectedSymbols([])
          setSearchResults([])
        }
      })
    } else {
      setSymbolType(value)
    }
  }

  // 搜索标的（带防抖）
  const searchSymbols = useCallback(async (keyword: string, type: SymbolType) => {
    if (!type || (!keyword || keyword.length < 1)) {
      setSearchResults([])
      return
    }

    // 清除之前的定时器
    if (searchTimerRef.current) {
      clearTimeout(searchTimerRef.current)
    }

    // 防抖300ms
    searchTimerRef.current = setTimeout(async () => {
      setSearching(true)
      try {
        const response = await stocksApi.searchTargets(keyword, type)
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
    }, 300)
  }, [])

  // 标的选择变化
  const handleSymbolsChange = (values: string[]) => {
    setSelectedSymbols(values)
    form.setFieldsValue({ symbols: values })

    // 校验数量限制
    if (values.length > 100) {
      message.warning('单次最多选择100个标的')
      // 截断到100个
      const truncated = values.slice(0, 100)
      setSelectedSymbols(truncated)
      form.setFieldsValue({ symbols: truncated })
    }
  }

  // 全选当前搜索结果
  const handleSelectAll = () => {
    const allCodes = searchResults.map(r => r.code)
    // 合并已选的和当前搜索结果
    const merged = Array.from(new Set([...selectedSymbols, ...allCodes]))
    if (merged.length > 100) {
      message.warning('最多只能选择100个标的')
      return
    }
    setSelectedSymbols(merged)
    form.setFieldsValue({ symbols: merged })
  }

  // 清空选择
  const handleClearSelection = () => {
    setSelectedSymbols([])
    form.setFieldsValue({ symbols: [] })
  }

  // 校验器 - 单选模式
  const validateSymbol = (_: any, value: string) => {
    if (!value) {
      return Promise.reject(new Error('请输入标的代码'))
    }
    if (value.length < 2 || value.length > 10) {
      return Promise.reject(new Error('标的代码长度应在2-10个字符之间'))
    }
    return Promise.resolve()
  }

  // 校验器 - 批量模式
  const validateSymbols = (_: any, value: string[]) => {
    if (!value || value.length === 0) {
      return Promise.reject(new Error('请至少选择一个标的'))
    }
    if (value.length > 100) {
      return Promise.reject(new Error('单次最多选择100个标的'))
    }
    return Promise.resolve()
  }

  // 校验器 - 目标价格
  const validateTargetPrice = (_: any, value: number) => {
    if (alertType !== 'PERCENTAGE_CHANGE') {
      if (value === undefined || value === null) {
        return Promise.reject(new Error('请输入目标价格'))
      }
      if (value <= 0) {
        return Promise.reject(new Error('价格必须大于0'))
      }
    }
    return Promise.resolve()
  }

  // 校验器 - 涨跌幅
  const validateTargetChangePercent = (_: any, value: number) => {
    if (alertType === 'PERCENTAGE_CHANGE') {
      if (value === undefined || value === null) {
        return Promise.reject(new Error('请输入目标涨跌幅'))
      }
      if (value === 0) {
        return Promise.reject(new Error('涨跌幅不能为0'))
      }
      if (Math.abs(value) > 99) {
        return Promise.reject(new Error('涨跌幅超出合理范围'))
      }
    }
    return Promise.resolve()
  }

  // 提交表单
  const onFinish = async (values: any) => {
    setLoading(true)
    try {
      const userId = Number(localStorage.getItem('userId')) || 1

      if (createMode === 'batch' && values.symbols) {
        // 批量创建模式
        const symbols = Array.isArray(values.symbols) ? values.symbols : []

        const batchData = {
          userId,
          symbols,
          symbolType: symbolType || values.symbolType,
          alertType: values.alertType,
          targetPrice: values.alertType === 'PERCENTAGE_CHANGE' ? undefined : values.targetPrice,
          targetChangePercent: values.alertType === 'PERCENTAGE_CHANGE' ? values.targetChangePercent : undefined
        }

        // 执行批量创建
        const result: any = await dispatch(batchCreateAlert(batchData)).unwrap()

        // 显示结果弹窗
        setBatchResult({
          success: true,
          data: result,
          error: null
        })
        setResultModalVisible(true)

        // 如果全部成功，返回列表页
        if (result.successCount === result.totalCount) {
          message.success(`成功创建 ${result.successCount} 条提醒`)
          setTimeout(() => navigate('/alerts'), 1500)
        }

      } else {
        // 单条创建模式
        const alertData = {
          userId,
          symbol: values.symbol,
          symbolType: values.symbolType,
          alertType: values.alertType,
          targetPrice: values.alertType === 'PERCENTAGE_CHANGE' ? undefined : values.targetPrice,
          targetChangePercent: values.alertType === 'PERCENTAGE_CHANGE' ? values.targetChangePercent : undefined,
          basePrice: values.basePrice,
          status: values.status ? 'ACTIVE' : 'INACTIVE'
        }

        if (isEdit) {
          await dispatch(updateAlert({ id: id!, data: alertData })).unwrap()
          message.success('提醒更新成功')
          navigate('/alerts')
        } else {
          const response = await dispatch(createAlert(alertData)).unwrap()
          if (!response.created && response.alert?.id) {
            message.info('该标的已存在提醒，正在跳转到编辑页面')
            navigate(`/alerts/${response.alert.id}`)
          } else {
            message.success('提醒创建成功')
            navigate('/alerts')
          }
        }
      }
    } catch (error: any) {
      message.error(error.message || (isEdit ? '提醒更新失败' : '提醒创建失败'))
    } finally {
      setLoading(false)
    }
  }

  // 重试失败项
  const handleRetryFailed = () => {
    if (!batchResult?.data) return

    const failedSymbols = batchResult.data.failureList.map(f => f.symbol)
    if (failedSymbols.length === 0) return

    // 用失败的标的重新提交
    form.setFieldsValue({ symbols: failedSymbols })
    setSelectedSymbols(failedSymbols)
    setResultModalVisible(false)
    message.info('已加载失败项，请确认后重新提交')
  }

  // 渲染批量创建结果
  const renderBatchResult = () => {
    if (!batchResult?.data) return null

    const { totalCount, successCount, failureCount, successList, failureList } = batchResult.data

    return (
      <div>
        <Result
          status={failureCount === 0 ? 'success' : failureCount === totalCount ? 'error' : 'warning'}
          title={
            failureCount === 0
              ? `全部成功！成功创建 ${successCount} 条提醒`
              : failureCount === totalCount
                ? `全部失败，共 ${failureCount} 条`
                : `部分成功：成功 ${successCount} 条，失败 ${failureCount} 条`
          }
        />

        <Divider orientation="left">成功列表 ({successCount})</Divider>
        <List
          size="small"
          dataSource={successList}
          renderItem={(item) => (
            <List.Item>
              <CheckCircleOutlined style={{ color: '#52c41a', marginRight: 8 }} />
              <Text strong>{item.symbol}</Text>
              <Text style={{ marginLeft: 8 }}>{item.symbolName}</Text>
            </List.Item>
          )}
        />

        {failureCount > 0 && (
          <>
            <Divider orientation="left">失败列表 ({failureCount})</Divider>
            <List
              size="small"
              dataSource={failureList}
              renderItem={(item) => (
                <List.Item>
                  <CloseCircleOutlined style={{ color: '#ff4d4f', marginRight: 8 }} />
                  <Text strong>{item.symbol}</Text>
                  <Text style={{ marginLeft: 8 }}>{item.symbolName}</Text>
                  <Text type="danger" style={{ marginLeft: 8 }}>原因: {item.reason}</Text>
                </List.Item>
              )}
            />
          </>
        )}
      </div>
    )
  }

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/alerts')}
          style={{ marginRight: '16px' }}
        >
          返回列表
        </Button>
        <h2>{isEdit ? '编辑提醒' : '创建提醒'}</h2>
      </div>

      <Card>
        {/* 批量/单选模式切换 - 仅创建时显示 */}
        {!isEdit && (
          <div style={{ marginBottom: '24px' }}>
            <Radio.Group
              value={createMode}
              onChange={handleModeChange}
              optionType="button"
              buttonStyle="solid"
            >
              <Radio.Button value="single">单选模式</Radio.Button>
              <Radio.Button value="batch">批量模式</Radio.Button>
            </Radio.Group>
          </div>
        )}

        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{
            symbolType: 'STOCK',
            alertType: 'PRICE_ABOVE',
            status: true
          }}
        >
          {/* 单选模式 */}
          {createMode === 'single' && (
            <Form.Item
              name="symbol"
              label="标的代码"
              rules={[{ validator: validateSymbol }]}
            >
              <Input placeholder="请输入股票代码或基金代码" />
            </Form.Item>
          )}

          {/* 批量模式 - 类型前置选择 */}
          {createMode === 'batch' && (
            <>
              <Form.Item
                name="symbolType"
                label="1. 选择标的类型"
                rules={[{ required: true, message: '请先选择标的类型' }]}
              >
                <Radio.Group
                  value={symbolType}
                  onChange={(e) => handleSymbolTypeChange(e.target.value)}
                  optionType="button"
                  buttonStyle="solid"
                >
                  <Radio.Button value="STOCK">股票</Radio.Button>
                  <Radio.Button value="FUND">基金</Radio.Button>
                </Radio.Group>
              </Form.Item>

              {/* 只有选择了类型后才显示标的选择 */}
              {symbolType && (
                <Form.Item
                  name="symbols"
                  label="2. 选择标的（可多选）"
                  rules={[{ validator: validateSymbols }]}
                  extra={
                    <Space>
                      <Text type="secondary">已选择 {selectedSymbols.length} 个标的</Text>
                      {selectedSymbols.length > 0 && (
                        <Button size="small" onClick={handleClearSelection}>清空选择</Button>
                      )}
                    </Space>
                  }
                >
                  <Select
                    mode="multiple"
                    showSearch
                    placeholder={`输入代码或名称搜索${symbolType === 'STOCK' ? '股票' : '基金'}...`}
                    value={selectedSymbols}
                    onChange={handleSymbolsChange}
                    filterOption={false}
                    notFoundContent={searching ? <Spin size="small" /> : '请输入关键词搜索'}
                    onSearch={(value) => {
                      searchSymbols(value, symbolType)
                    }}
                    allowClear
                    style={{ width: '100%' }}
                    dropdownRender={(menu) => (
                      <>
                        {menu}
                        {searchResults.length > 0 && (
                          <>
                            <Divider style={{ margin: '8px 0' }} />
                            <div style={{ padding: '8px', textAlign: 'center' }}>
                              <Button
                                type="link"
                                size="small"
                                onClick={handleSelectAll}
                                disabled={selectedSymbols.length >= 100}
                              >
                                全选当前搜索结果 ({searchResults.length}个)
                              </Button>
                            </div>
                          </>
                        )}
                      </>
                    )}
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
              )}
            </>
          )}

          {/* 单选模式的标的类型 */}
          {createMode === 'single' && (
            <Form.Item
              name="symbolType"
              label="标的类型"
              rules={[{ required: true, message: '请选择标的类型' }]}
            >
              <Select placeholder="请选择标的类型">
                <Option value="STOCK">股票</Option>
                <Option value="FUND">基金</Option>
              </Select>
            </Form.Item>
          )}

          <Form.Item
            name="alertType"
            label={createMode === 'batch' ? '3. 提醒类型' : '提醒类型'}
            rules={[{ required: true, message: '请选择提醒类型' }]}
          >
            <Select placeholder="请选择提醒类型" onChange={handleAlertTypeChange}>
              <Option value="PRICE_ABOVE">价格超过</Option>
              <Option value="PRICE_BELOW">价格低于</Option>
              <Option value="PERCENTAGE_CHANGE">涨跌幅</Option>
            </Select>
          </Form.Item>

          {alertType !== 'PERCENTAGE_CHANGE' && (
            <Form.Item
              name="targetPrice"
              label={createMode === 'batch' ? '4. 目标价格' : '目标价格'}
              rules={[{ validator: validateTargetPrice }]}
            >
              <InputNumber
                style={{ width: '100%' }}
                placeholder="请输入目标价格"
                min={0}
                step={0.01}
                precision={2}
                formatter={(value: any) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={(value: string | undefined) => {
                  if (!value) return 0
                  const parsed = value.replace(/¥\s?|(,*)/g, '')
                  return Number(parsed) || 0
                }}
              />
            </Form.Item>
          )}

          {alertType === 'PERCENTAGE_CHANGE' && (
            <Form.Item
              name="targetChangePercent"
              label={createMode === 'batch' ? '4. 目标涨跌幅(%)' : '目标涨跌幅(%)'}
              rules={[{ validator: validateTargetChangePercent }]}
            >
              <InputNumber
                style={{ width: '100%' }}
                placeholder="请输入目标涨跌幅"
                min={-100}
                max={100}
                step={0.1}
                precision={2}
                formatter={(value: any) => `${value}%`}
                parser={(value: string | undefined) => {
                  if (!value) return 0
                  return Number(value.replace('%', '')) || 0
                }}
              />
            </Form.Item>
          )}

          <Form.Item
            name="status"
            label="启用状态"
            valuePropName="checked"
          >
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                {isEdit ? '更新提醒' : (createMode === 'batch' ? '批量创建提醒' : '创建提醒')}
              </Button>
              <Button onClick={() => form.resetFields()}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      {/* 批量创建结果弹窗 */}
      <Modal
        title="批量创建结果"
        open={resultModalVisible}
        onCancel={() => {
          setResultModalVisible(false)
          if (batchResult?.data?.successCount === batchResult?.data?.totalCount) {
            navigate('/alerts')
          }
        }}
        footer={[
          batchResult?.data && batchResult.data.failureCount > 0 && (
            <Button key="retry" type="primary" danger onClick={handleRetryFailed}>
              重试失败项 ({batchResult.data.failureCount})
            </Button>
          ),
          <Button
            key="back"
            type="primary"
            onClick={() => {
              setResultModalVisible(false)
              navigate('/alerts')
            }}
          >
            {batchResult?.data?.successCount === batchResult?.data?.totalCount ? '完成' : '返回列表'}
          </Button>
        ]}
        width={720}
      >
        {renderBatchResult()}
      </Modal>
    </div>
  )
}

export default AlertCreate
