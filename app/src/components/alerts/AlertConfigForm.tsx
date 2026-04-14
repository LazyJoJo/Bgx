import { Form, Select, InputNumber, Switch, Tag, Typography, Radio } from 'antd'

const { Option } = Select
const { Text } = Typography

export type ConfigMode = 'quick' | 'custom'

interface AlertConfigFormProps {
  configMode: ConfigMode
  alertType: string
  isBatchMode?: boolean
  onConfigModeChange: (mode: ConfigMode) => void
  onAlertTypeChange: (type: string) => void
}

const labelPrefix = (isBatchMode: boolean, n: number, label: string) =>
  isBatchMode ? `${n}. ${label}` : label

export const AlertConfigForm = ({
  configMode,
  alertType,
  isBatchMode = false,
  onConfigModeChange,
  onAlertTypeChange
}: AlertConfigFormProps) => {
  const step = isBatchMode ? 3 : 1

  return (
    <>
      <Form.Item label={labelPrefix(isBatchMode, step, '配置模式')}>
        <Radio.Group
          value={configMode}
          onChange={(e) => onConfigModeChange(e.target.value as ConfigMode)}
          optionType="button"
          buttonStyle="outline"
        >
          <Radio.Button value="custom">自定义模式</Radio.Button>
          <Radio.Button value="quick">快速模式</Radio.Button>
        </Radio.Group>
      </Form.Item>

      {configMode === 'custom' && (
        <Form.Item
          name="alertType"
          label={labelPrefix(isBatchMode, step + 1, '提醒类型')}
          rules={[{ required: true, message: '请选择提醒类型' }]}
        >
          <Select placeholder="请选择提醒类型" onChange={onAlertTypeChange}>
            <Option value="PRICE_ABOVE">价格超过</Option>
            <Option value="PRICE_BELOW">价格低于</Option>
            <Option value="PERCENTAGE_CHANGE">涨跌幅</Option>
          </Select>
        </Form.Item>
      )}

      {configMode === 'quick' && (
        <Form.Item label={labelPrefix(isBatchMode, step + 1, '提醒类型')}>
          <Tag color="orange">涨跌幅 1%</Tag>
          <Text type="secondary" style={{ marginLeft: 8 }}>
            快速模式：自动检测涨跌幅超过1%时发送提醒
          </Text>
        </Form.Item>
      )}

      {configMode === 'custom' && alertType !== 'PERCENTAGE_CHANGE' && (
        <Form.Item
          name="targetPrice"
          label={labelPrefix(isBatchMode, step + 2, '目标价格')}
          rules={[
            {
              validator: (_: unknown, value: number) => {
                if (value === undefined || value === null) {
                  return Promise.reject(new Error('请输入目标价格'))
                }
                if (value <= 0) {
                  return Promise.reject(new Error('价格必须大于0'))
                }
                return Promise.resolve()
              }
            }
          ]}
        >
          <InputNumber<number>
            style={{ width: '100%' }}
            placeholder="请输入目标价格"
            min={0}
            step={0.01}
            precision={2}
            formatter={(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
            parser={(value) => {
              if (!value) return 0
              return Number(value.replace(/¥\s?|(,*)/g, '')) || 0
            }}
          />
        </Form.Item>
      )}

      {configMode === 'custom' && alertType === 'PERCENTAGE_CHANGE' && (
        <Form.Item
          name="targetChangePercent"
          label={labelPrefix(isBatchMode, step + 2, '目标涨跌幅(%)')}
          rules={[
            {
              validator: (_: unknown, value: number) => {
                if (value === undefined || value === null) {
                  return Promise.reject(new Error('请输入目标涨跌幅'))
                }
                if (value === 0) {
                  return Promise.reject(new Error('涨跌幅不能为0'))
                }
                if (Math.abs(value) > 99) {
                  return Promise.reject(new Error('涨跌幅超出合理范围'))
                }
                return Promise.resolve()
              }
            }
          ]}
        >
          <InputNumber<number>
            style={{ width: '100%' }}
            placeholder="请输入目标涨跌幅"
            min={-100}
            max={100}
            step={0.1}
            precision={2}
            formatter={(value) => `${value}%`}
            parser={(value) => {
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
    </>
  )
}
