import { useState, useEffect } from 'react'
import {
  Form, Input, Select, Button, Card, Space, message, Alert
} from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { updateAlert, batchCreateAlert } from '@store/slices/alertsSlice'
import { SymbolSelector } from '@components/alerts/SymbolSelector'
import { AlertConfigForm } from '@components/alerts/AlertConfigForm'
import { BatchResultModal } from '@components/alerts/BatchResultModal'
import type { SymbolType, SearchResult } from '@components/alerts/SymbolSelector'
import type { ConfigMode } from '@components/alerts/AlertConfigForm'
import type { BatchResult } from '@components/alerts/BatchResultModal'

const { Option } = Select

const AlertCreate = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const dispatch = useAppDispatch()
  const { selectedAlert } = useAppSelector(state => state.alerts)

  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [alertType, setAlertType] = useState('PRICE_ABOVE')
  const [configMode, setConfigMode] = useState<ConfigMode>('custom')

  // Symbol selection state (create mode only)
  const [symbolType, setSymbolType] = useState<SymbolType>(null)
  const [selectedSymbols, setSelectedSymbols] = useState<string[]>([])
  const [symbolNamesMap, setSymbolNamesMap] = useState<Record<string, string>>({})

  // Batch result modal state
  const [resultModalVisible, setResultModalVisible] = useState(false)
  const [batchResult, setBatchResult] = useState<BatchResult | null>(null)

  const isEdit = !!id

  // Populate form in edit mode
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
    if (value === 'PERCENTAGE_CHANGE') {
      form.setFieldsValue({ targetPrice: undefined })
    } else {
      form.setFieldsValue({ targetChangePercent: undefined })
    }
  }

  const handleConfigModeChange = (mode: ConfigMode) => {
    setConfigMode(mode)
    if (mode === 'quick') {
      setAlertType('PERCENTAGE_CHANGE')
      form.setFieldsValue({
        alertType: 'PERCENTAGE_CHANGE',
        targetChangePercent: 1.0
      })
    }
  }

  const handleSymbolTypeChange = (type: SymbolType) => {
    setSymbolType(type)
    form.setFieldsValue({ symbolType: type })
  }

  const handleSymbolsChange = (symbols: string[]) => {
    setSelectedSymbols(symbols)
    form.setFieldsValue({ symbols })
  }

  const handleSearchResultsChange = (results: SearchResult[]) => {
    const newMap: Record<string, string> = { ...symbolNamesMap }
    results.forEach(r => {
      newMap[r.code] = r.name
    })
    setSymbolNamesMap(newMap)
  }

  const onFinish = async (values: any) => {
    setLoading(true)
    try {
      const userId = Number(localStorage.getItem('userId')) || 1
      const effectiveAlertType = configMode === 'quick' ? 'PERCENTAGE_CHANGE' : values.alertType
      const effectiveTargetChangePercent = configMode === 'quick' ? 1.0 : values.targetChangePercent

      if (isEdit) {
        const alertData = {
          userId,
          symbol: values.symbol,
          symbolType: values.symbolType,
          alertType: effectiveAlertType,
          targetPrice: effectiveAlertType === 'PERCENTAGE_CHANGE' ? undefined : values.targetPrice,
          targetChangePercent: effectiveAlertType === 'PERCENTAGE_CHANGE' ? effectiveTargetChangePercent : undefined,
          status: values.status ? 'ACTIVE' : 'INACTIVE'
        }
        await dispatch(updateAlert({ id: id!, data: alertData })).unwrap()
        message.success('提醒更新成功')
        navigate('/alerts')
        return
      }

      // Create mode: always use batch endpoint (supports 1 or more symbols)
      const symbols = Array.isArray(values.symbols) ? values.symbols : []
      const symbolNames = symbols.map(code => symbolNamesMap[code] || code)
      const batchData = {
        userId,
        symbols,
        symbolNames,
        symbolType: (symbolType || values.symbolType) as 'STOCK' | 'FUND',
        alertType: effectiveAlertType as 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE',
        targetPrice: effectiveAlertType === 'PERCENTAGE_CHANGE' ? undefined : values.targetPrice,
        targetChangePercent: effectiveAlertType === 'PERCENTAGE_CHANGE' ? effectiveTargetChangePercent : undefined
      }

      const result: any = await dispatch(batchCreateAlert(batchData)).unwrap()

      setBatchResult({ success: true, data: result, error: null })
      setResultModalVisible(true)

      if (result.successCount === result.totalCount) {
        message.success(`成功创建 ${result.successCount} 条提醒`)
        setTimeout(() => navigate('/alerts'), 1500)
      }
    } catch (error: any) {
      message.error(error.message || (isEdit ? '提醒更新失败' : '提醒创建失败'))
    } finally {
      setLoading(false)
    }
  }

  const handleRetryFailed = () => {
    if (!batchResult?.data) return
    const failedSymbols = batchResult.data.failureList.map(f => f.symbol)
    if (failedSymbols.length === 0) return
    setSelectedSymbols(failedSymbols)
    form.setFieldsValue({ symbols: failedSymbols })
    setResultModalVisible(false)
    message.info('已加载失败项，请确认后重新提交')
  }

  const handleModalClose = () => {
    setResultModalVisible(false)
    if (batchResult?.data?.successCount === batchResult?.data?.totalCount) {
      navigate('/alerts')
    }
  }

  const handleGoToList = () => {
    setResultModalVisible(false)
    navigate('/alerts')
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
        {/* Quick mode info banner (create mode only) */}
        {!isEdit && configMode === 'quick' && (
          <Alert
            message="快速订阅说明"
            description="选择需要监控的股票或基金，系统将在每个工作日的11:30和14:30自动检测涨跌幅，超过1%时发送风险提醒。"
            type="info"
            showIcon
            style={{ marginBottom: 24 }}
          />
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
          {/* Edit mode: simple symbol fields */}
          {isEdit && (
            <>
              <Form.Item
                name="symbol"
                label="标的代码"
                rules={[
                  {
                    validator: (_: unknown, value: string) => {
                      if (!value) return Promise.reject(new Error('请输入标的代码'))
                      if (value.length < 2 || value.length > 10) {
                        return Promise.reject(new Error('标的代码长度应在2-10个字符之间'))
                      }
                      return Promise.resolve()
                    }
                  }
                ]}
              >
                <Input placeholder="请输入股票代码或基金代码" />
              </Form.Item>

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
            </>
          )}

          {/* Create mode: multi-symbol selector */}
          {!isEdit && (
            <SymbolSelector
              symbolType={symbolType}
              selectedSymbols={selectedSymbols}
              onSymbolTypeChange={handleSymbolTypeChange}
              onSymbolsChange={handleSymbolsChange}
              onSearchResultsChange={handleSearchResultsChange}
            />
          )}

          {/* Alert config (config mode switcher + alert type + price fields + status) */}
          <AlertConfigForm
            configMode={configMode}
            alertType={alertType}
            isBatchMode={!isEdit}
            onConfigModeChange={handleConfigModeChange}
            onAlertTypeChange={handleAlertTypeChange}
          />

          <Form.Item>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                icon={<SaveOutlined />}
              >
                {isEdit
                  ? '更新提醒'
                  : configMode === 'quick'
                    ? '一键订阅'
                    : '创建提醒'
                }
              </Button>
              {configMode !== 'quick' && (
                <Button onClick={() => form.resetFields()}>
                  重置
                </Button>
              )}
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <BatchResultModal
        visible={resultModalVisible}
        batchResult={batchResult}
        onClose={handleModalClose}
        onRetryFailed={handleRetryFailed}
        onGoToList={handleGoToList}
      />
    </div>
  )
}

export default AlertCreate
