import { useEffect, useCallback, useState } from 'react'
import { Table, Tag, Button, Space, Select, Form, Row, Col, message, Popconfirm, Typography, Card, Alert, Badge } from 'antd'
import { PlusOutlined, DeleteOutlined, PlayCircleOutlined, PauseCircleOutlined, ReloadOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import {
  fetchSubscriptions,
  batchDeleteSubscriptions,
  batchActivateSubscriptions,
  batchDeactivateSubscriptions,
  setFilters,
  clearFilters,
} from '@store/slices/subscriptionsSlice'
import { subscriptionsApi, SymbolType, SubscriptionStatus, AlertType, Subscription } from '@services/api/subscriptions'
import SubscriptionCreate from '@components/subscriptions/SubscriptionCreate'
import './SubscriptionList.css'

const { Text } = Typography

// 订阅列表响应类型
interface SubscriptionListResponse {
  id: number
  userId: number
  symbol: string
  symbolName: string
  symbolType: SymbolType
  alertType: AlertType
  targetPrice?: number
  targetChangePercent?: number
  status: SubscriptionStatus
  remark?: string
  createdAt: string
  updatedAt: string
}

const SubscriptionList: React.FC = () => {
  const dispatch = useAppDispatch()
  const { list, loading, filters } = useAppSelector((state) => state.subscriptions)
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [form] = Form.useForm()

  // 加载订阅列表
  useEffect(() => {
    dispatch(fetchSubscriptions(filters))
  }, [dispatch, filters])

  // 筛选条件变化处理
  const handleFilterChange = (changedValues: any, allValues: any) => {
    const newFilters: any = {}
    if (allValues.symbolType) newFilters.symbolType = allValues.symbolType
    if (allValues.status) newFilters.status = allValues.status
    if (allValues.alertType) newFilters.alertType = allValues.alertType
    if (allValues.symbol) newFilters.symbol = allValues.symbol
    dispatch(setFilters(newFilters))
  }

  // 重置筛选
  const handleResetFilters = () => {
    form.resetFields()
    dispatch(clearFilters())
  }

  // 刷新列表
  const handleRefresh = () => {
    dispatch(fetchSubscriptions(filters))
    message.success('刷新成功')
  }

  // 打开创建弹窗
  const handleOpenCreate = () => {
    setCreateModalVisible(true)
  }

  // 创建成功回调
  const handleCreateSuccess = () => {
    dispatch(fetchSubscriptions(filters))
    message.success('订阅创建成功')
  }

  // 批量启用
  const handleBatchActivate = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先选择要启用的订阅')
      return
    }
    try {
      await dispatch(batchActivateSubscriptions(selectedRowKeys as number[])).unwrap()
      message.success(`成功启用 ${selectedRowKeys.length} 个订阅`)
      setSelectedRowKeys([])
      dispatch(fetchSubscriptions(filters))
    } catch (error: any) {
      message.error(error || '批量启用失败')
    }
  }

  // 批量停用
  const handleBatchDeactivate = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先选择要停用的订阅')
      return
    }
    try {
      await dispatch(batchDeactivateSubscriptions(selectedRowKeys as number[])).unwrap()
      message.success(`成功停用 ${selectedRowKeys.length} 个订阅`)
      setSelectedRowKeys([])
      dispatch(fetchSubscriptions(filters))
    } catch (error: any) {
      message.error(error || '批量停用失败')
    }
  }

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请先选择要删除的订阅')
      return
    }
    try {
      await dispatch(batchDeleteSubscriptions(selectedRowKeys as number[])).unwrap()
      message.success(`成功删除 ${selectedRowKeys.length} 个订阅`)
      setSelectedRowKeys([])
      dispatch(fetchSubscriptions(filters))
    } catch (error: any) {
      message.error(error || '批量删除失败')
    }
  }

  // 行选择配置
  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => setSelectedRowKeys(keys),
  }

  // 表格列配置
  const columns = [
    {
      title: '标的',
      dataIndex: 'symbol',
      key: 'symbol',
      width: 200,
      render: (symbol: string, record: SubscriptionListResponse) => (
        <Space direction="vertical" size={0}>
          <Space>
            <Tag color={record.symbolType === 'STOCK' ? 'blue' : 'green'}>
              {record.symbolType === 'STOCK' ? '股票' : '基金'}
            </Tag>
            <Text strong>{symbol}</Text>
          </Space>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.symbolName}</Text>
        </Space>
      ),
    },
    {
      title: '提醒类型',
      dataIndex: 'alertType',
      key: 'alertType',
      width: 140,
      render: (alertType: AlertType) => {
        const config: Record<AlertType, { color: string; label: string }> = {
          PERCENTAGE_CHANGE: { color: 'orange', label: '涨跌幅' },
          PRICE_ABOVE: { color: 'red', label: '价格上限' },
          PRICE_BELOW: { color: 'green', label: '价格下限' },
        }
        const { color, label } = config[alertType] || { color: 'default', label: alertType }
        return <Tag color={color}>{label}</Tag>
      },
    },
    {
      title: '阈值',
      dataIndex: 'targetChangePercent',
      key: 'targetChangePercent',
      width: 120,
      render: (value: number, record: SubscriptionListResponse) => {
        if (record.alertType === 'PERCENTAGE_CHANGE') {
          return value ? `${value}%` : '-'
        }
        if (record.alertType === 'PRICE_ABOVE' || record.alertType === 'PRICE_BELOW') {
          return record.targetPrice ? `¥${record.targetPrice.toFixed(2)}` : '-'
        }
        return '-'
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: SubscriptionStatus) => {
        const config: Record<SubscriptionStatus, { color: string; label: string }> = {
          ACTIVE: { color: 'success', label: '启用' },
          INACTIVE: { color: 'default', label: '停用' },
          TRIGGERED: { color: 'warning', label: '已触发' },
        }
        const { color, label } = config[status] || { color: 'default', label: status }
        return <Badge status={status === 'ACTIVE' ? 'success' : status === 'TRIGGERED' ? 'warning' : 'default'} text={label} />
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
      render: (remark: string) => remark || '-',
    },
  ]

  return (
    <div className="subscription-list-container">
      {/* 页面标题 */}
      <div className="page-header">
        <div className="page-title">
          <h2>订阅管理</h2>
          <Badge count={list.length} style={{ marginLeft: 8 }} />
        </div>
      </div>

      {/* 筛选和操作区域 - 表格上方 */}
      <Card className="filter-card" bordered={false}>
        <Form
          form={form}
          layout="vertical"
          onValuesChange={handleFilterChange}
          initialValues={filters}
        >
          <Row gutter={16} align="middle">
            <Col flex="auto">
              <Space wrap size={[12, 8]}>
                <Form.Item name="symbolType" style={{ marginBottom: 0 }}>
                  <Select
                    placeholder="标的类型"
                    allowClear
                    style={{ width: 120 }}
                    onChange={() => handleFilterChange({}, form.getFieldsValue())}
                  >
                    <Select.Option value="STOCK">股票</Select.Option>
                    <Select.Option value="FUND">基金</Select.Option>
                  </Select>
                </Form.Item>

                <Form.Item name="status" style={{ marginBottom: 0 }}>
                  <Select
                    placeholder="状态"
                    allowClear
                    style={{ width: 100 }}
                    onChange={() => handleFilterChange({}, form.getFieldsValue())}
                  >
                    <Select.Option value="ACTIVE">启用</Select.Option>
                    <Select.Option value="INACTIVE">停用</Select.Option>
                    <Select.Option value="TRIGGERED">已触发</Select.Option>
                  </Select>
                </Form.Item>

                <Form.Item name="alertType" style={{ marginBottom: 0 }}>
                  <Select
                    placeholder="提醒类型"
                    allowClear
                    style={{ width: 120 }}
                    onChange={() => handleFilterChange({}, form.getFieldsValue())}
                  >
                    <Select.Option value="PERCENTAGE_CHANGE">涨跌幅</Select.Option>
                    <Select.Option value="PRICE_ABOVE">价格上限</Select.Option>
                    <Select.Option value="PRICE_BELOW">价格下限</Select.Option>
                  </Select>
                </Form.Item>

                <Form.Item name="symbol" style={{ marginBottom: 0 }}>
                  <Select
                    showSearch
                    placeholder="搜索标的代码或名称"
                    allowClear
                    style={{ width: 180 }}
                    filterOption={(input, option) =>
                      (option?.label as string)?.toLowerCase().includes(input.toLowerCase())
                    }
                    onChange={() => handleFilterChange({}, form.getFieldsValue())}
                    options={list.map(item => ({
                      value: item.symbol,
                      label: `${item.symbol} - ${item.symbolName}`,
                    }))}
                  />
                </Form.Item>
              </Space>
            </Col>

            <Col>
              <Space>
                <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
                  刷新
                </Button>
                <Button type="link" onClick={handleResetFilters}>
                  重置
                </Button>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleOpenCreate}
                >
                  创建订阅
                </Button>
              </Space>
            </Col>
          </Row>
        </Form>
      </Card>

      {/* 表格区域 */}
      <Card className="table-card" bordered={false}>
        <Alert
          message="订阅说明"
          description="您可以根据需要创建股票或基金的风险提醒订阅。系统将在每个交易日的指定时间检测涨跌幅或价格变化，超过阈值时发送提醒通知。"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />

        <Table
          rowSelection={rowSelection}
          columns={columns}
          dataSource={list}
          rowKey="id"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            defaultPageSize: 20,
          }}
          scroll={{ x: 1000 }}
        />

        {/* 批量操作区域 - 表格下方 */}
        {list.length > 0 && (
          <div className="batch-actions">
            <div className="batch-actions-left">
              <Text type="secondary">
                已选择 <Text strong>{selectedRowKeys.length}</Text> 项
              </Text>
            </div>
            <div className="batch-actions-right">
              <Space size="middle">
                <Button
                  icon={<PlayCircleOutlined />}
                  onClick={handleBatchActivate}
                  disabled={selectedRowKeys.length === 0}
                >
                  批量启用
                </Button>
                <Button
                  icon={<PauseCircleOutlined />}
                  onClick={handleBatchDeactivate}
                  disabled={selectedRowKeys.length === 0}
                >
                  批量停用
                </Button>
                <Popconfirm
                  title="确认删除"
                  description={`确定要删除选中的 ${selectedRowKeys.length} 个订阅吗？删除后无法恢复。`}
                  onConfirm={handleBatchDelete}
                  okText="确认删除"
                  cancelText="取消"
                  okButtonProps={{ danger: true }}
                >
                  <Button
                    danger
                    icon={<DeleteOutlined />}
                    disabled={selectedRowKeys.length === 0}
                  >
                    批量删除
                  </Button>
                </Popconfirm>
              </Space>
            </div>
          </div>
        )}
      </Card>

      {/* 创建订阅弹窗 */}
      <SubscriptionCreate
        visible={createModalVisible}
        onClose={() => setCreateModalVisible(false)}
        onSuccess={handleCreateSuccess}
      />
    </div>
  )
}

export default SubscriptionList
