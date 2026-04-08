import { useState, useEffect } from 'react'
import { Form, Input, Select, InputNumber, Button, Card, Space, message, Switch } from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { createAlert, updateAlert } from '@store/slices/alertsSlice'

const { Option } = Select

const AlertCreate = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const dispatch = useAppDispatch()
  const { selectedAlert } = useAppSelector(state => state.alerts)
  
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [alertType, setAlertType] = useState('PRICE_ABOVE')

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
    //清空相关字段
    if (value === 'PERCENTAGE_CHANGE') {
      form.setFieldsValue({ targetPrice: undefined })
    } else {
      form.setFieldsValue({ targetChangePercent: undefined })
    }
  }

  const onFinish = async (values: any) => {
    setLoading(true)
    try {
      const alertData = {
        symbol: values.symbol,
        symbolType: values.symbolType,
        alertType: values.alertType,
        targetPrice: values.alertType === 'PERCENTAGE_CHANGE' ? undefined : values.targetPrice,
        targetChangePercent: values.alertType === 'PERCENTAGE_CHANGE' ? values.targetChangePercent : undefined,
        status: values.status ? 'ACTIVE' : 'INACTIVE'
      }

      if (isEdit) {
        await dispatch(updateAlert({ id: id!, data: alertData })).unwrap()
        message.success('提醒更新成功')
      } else {
        await dispatch(createAlert(alertData)).unwrap()
        message.success('提醒创建成功')
      }
      
      navigate('/alerts')
    } catch (error) {
      message.error(isEdit ? '提醒更新失败' : '提醒创建失败')
    } finally {
      setLoading(false)
    }
  }

  const validateSymbol = (_: any, value: string) => {
    if (!value) {
      return Promise.reject('请输入标的代码')
    }
    //简单的格式验证
    if (value.length < 2 || value.length > 10) {
      return Promise.reject('标的代码长度应在2-10个字符之间')
    }
    return Promise.resolve()
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
          <Form.Item
            name="symbol"
            label="标的代码"
            rules={[{ validator: validateSymbol }]}
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
                {isEdit ? '更新提醒' : '创建提醒'}
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