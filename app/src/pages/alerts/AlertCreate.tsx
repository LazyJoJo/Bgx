import { useState, useEffect, useCallback } from 'react'
import { Form, Input, Select, InputNumber, Button, Card, Space, message, Switch, Radio, Spin, Tag } from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { createAlert, updateAlert, batchCreateAlert } from '@store/slices/alertsSlice'
import { stocksApi } from '@services/api/stocks'

const { Option } = Select

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

const AlertCreate = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const dispatch = useAppDispatch()
  const { selectedAlert } = useAppSelector(state => state.alerts)

  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [alertType, setAlertType] = useState('PRICE_ABOVE')
  const [createMode, setCreateMode] = useState<CreateMode>('single')
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searching, setSearching] = useState(false)

  const isEdit = !!id

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

  const handleAlertTypeChange = (value: string) => {
    setAlertType(value)
    // 清空相关字段
    if (value === 'PERCENTAGE_CHANGE') {
      form.setFieldsValue({ targetPrice: undefined })
    } else {
      form.setFieldsValue({ targetChangePercent: undefined })
    }
  }

  const onFinish = async (values: any) => {
    setLoading(true)
    try {
      const userId = Number(localStorage.getItem('userId')) || 1

      if (createMode === 'batch' && values.symbols) {
        // 批量创建模式 - symbols已经是数组，直接使用
        const symbols = Array.isArray(values.symbols) ? values.symbols : values.symbols.split(/[,，\n]/).map((s: string) => s.trim()).filter((s: string) => s.length > 0)

        const batchData = {
          userId,
          symbols,
          symbolType: values.symbolType,
          alertType: values.alertType,
          targetPrice: values.alertType === 'PERCENTAGE_CHANGE' ? undefined : values.targetPrice,
          targetChangePercent: values.alertType === 'PERCENTAGE_CHANGE' ? values.targetChangePercent : undefined,
          basePrice: values.basePrice,
          status: values.status ? 'ACTIVE' : 'INACTIVE'
        }

        await dispatch(batchCreateAlert(batchData)).unwrap()
        message.success(`成功创建 ${symbols.length} 个提醒`)
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
          // AC-1.4: 检查是否已存在（created=false），如果存在则跳转到编辑页
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

  const validateSymbol = (_: any, value: string) => {
    if (!value) {
      return Promise.reject('请输入标的代码')
    }
    if (value.length < 2 || value.length > 10) {
      return Promise.reject('标的代码长度应在2-10个字符之间')
    }
    return Promise.resolve()
  }

  const validateSymbols = (_: any, value: string[]) => {
    if (!value || value.length === 0) {
      return Promise.reject('请选择至少一个标的')
    }
    return Promise.resolve()
  }

  // 搜索标的 - 如果loadAll为true则加载所有（不过滤关键词）
  const searchSymbols = useCallback(async (keyword: string, type?: string, loadAll: boolean = false) => {
    // 如果没有关键词且不是要加载所有，则清空结果
    if ((!keyword || keyword.length < 1) && !loadAll) {
      setSearchResults([])
      return
    }
    setSearching(true)
    try {
      // 如果loadAll为true，keyword置空以获取所有
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

  const handleModeChange = (e: any) => {
    setCreateMode(e.target.value)
    form.resetFields(['symbol', 'symbols'])
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

          {/* 批量模式 - 可搜索的多选下拉框 */}
          {createMode === 'batch' && (
            <Form.Item
              name="symbols"
              label="选择标的（批量）"
              rules={[{ validator: validateSymbols }]}
              extra="搜索并选择多个股票或基金，支持按代码或名称搜索"
            >
              <Select
                mode="multiple"
                showSearch
                placeholder="输入代码或名称搜索..."
                filterOption={false}
                notFoundContent={searching ? <Spin size="small" /> : null}
                onSearch={(value) => {
                  const symbolType = form.getFieldValue('symbolType')
                  searchSymbols(value, symbolType)
                }}
                onFocus={() => {
                  // 首次聚焦时加载所有活跃标的
                  if (searchResults.length === 0) {
                    searchSymbols('', form.getFieldValue('symbolType'), true)
                  }
                }}
                allowClear
                style={{ width: '100%' }}
              >
                {searchResults.map((result) => (
                  <Option key={result.code} value={result.code} label={result.name}>
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

          <Form.Item
            name="alertType"
            label="提醒类型"
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
              label="目标价格"
              rules={[{ required: true, message: '请输入目标价格' }]}
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
              label="目标涨跌幅(%)"
              rules={[{ required: true, message: '请输入目标涨跌幅' }]}
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
    </div>
  )
}

export default AlertCreate
