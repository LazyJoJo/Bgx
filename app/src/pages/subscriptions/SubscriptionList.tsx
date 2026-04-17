import { useEffect, useState } from 'react'
import { Table, Tag, Button, Space, Select, Input, Form, Row, Col, message, Popconfirm, Typography, Card, Badge } from 'antd'
import { PlusOutlined, DeleteOutlined, PlayCircleOutlined, PauseCircleOutlined, ReloadOutlined, EditOutlined } from '@ant-design/icons'
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
import SubscriptionEdit from '@components/subscriptions/SubscriptionEdit'
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
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [editingSubscription, setEditingSubscription] = useState<Subscription | null>(null)
  const [form] = Form.useForm()

  // 加载订阅列表
  useEffect(() => {
    dispatch(fetchSubscriptions(filters))
  }, [dispatch, filters])

  // 同步 filters 到表单
  useEffect(() => {
    form.setFieldsValue(filters)
  }, [form, filters])

  // 筛选条件变化处理（仅处理下拉选择类筛选，忽略 symbol 搜索框输入）
  const handleFilterChange = (changedValues: any, allValues: any) => {
    // 如果变化来自 symbol 字段，直接忽略，避免输入过程中频繁发请求
    if ('symbol' in changedValues) {
      return
    }
    const newFilters: any = {}
    if (allValues.symbolType) newFilters.symbolType = allValues.symbolType
    if (allValues.status) newFilters.status = allValues.status
    if (allValues.alertType) newFilters.alertType = allValues.alertType
    if (allValues.symbol) newFilters.symbol = allValues.symbol
    dispatch(setFilters(newFilters))
  }

  // 搜索框处理
  const handleSymbolSearch = (value: string) => {
    const newFilters: any = { ...filters }
    if (value.trim()) {
      newFilters.symbol = value.trim()
    } else {
      delete newFilters.symbol
    }
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

  // 打开编辑弹窗
  const handleOpenEdit = (subscription: Subscription) => {
    setEditingSubscription(subscription)
    setEditModalVisible(true)
  }

  // 编辑成功回调
  const handleEditSuccess = () => {
    dispatch(fetchSubscriptions(filters))
    message.success('订阅更新成功')
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
            <Text strong>{symbol || '-'}</Text>
          </Space>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.symbolName || '-'}</Text>
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
        const { color, label } = config[alertType] || { color: 'default', label: alertType || '未知' }
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
          return value !== undefined && value !== null ? `${value}%` : '-'
        }
        if (record.alertType === 'PRICE_ABOVE' || record.alertType === 'PRICE_BELOW') {
          return record.targetPrice !== undefined && record.targetPrice !== null ? `¥${record.targetPrice.toFixed(2)}` : '-'
        }
        return '-'
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: SubscriptionStatus) => {
        const config: Record<SubscriptionStatus, { color: string; label: string; tagColor: string }> = {
          ACTIVE: { color: '#52c41a', label: '生效中', tagColor: 'success' },
          INACTIVE: { color: '#d9d9d9', label: '已停用', tagColor: 'default' },
          TRIGGERED: { color: '#faad14', label: '已触发', tagColor: 'warning' },
        }
        const { color, label, tagColor } = config[status] || { color: '#d9d9d9', label: status || '未知', tagColor: 'default' }
        return (
          <Tag color={tagColor} style={{ color: color, borderColor: color }}>
            {label}
          </Tag>
        )
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (createdAt: string) => {
        if (!createdAt) return '-'
        try {
          return new Date(createdAt).toLocaleString('zh-CN')
        } catch {
          return '-'
        }
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: any, record: SubscriptionListResponse) => (
        <Button
          type="link"
          icon={<EditOutlined />}
          onClick={() => handleOpenEdit(record as unknown as Subscription)}
        >
          编辑
        </Button>
      ),
    },
  ]

  return (
    <div className="subscription-list-container">
      {/* 页面标题 */}
      <div className="page-header">
        <div className="page-title">
          <h2>订阅管理</h2>
          {(() => {
            const activeCount = list.filter((item) => item.status === 'ACTIVE').length
            return activeCount > 0 ? (
              <Badge
                count={activeCount}
                style={{ marginLeft: 8, backgroundColor: '#52c41a' }}
              />
            ) : null
          })()}
        </div>
      </div>

      {/* 筛选和操作区域 - 表格上方 */}
      <Card className="filter-card" bordered={false}>
        <Form
          form={form}
          layout="vertical"
          onValuesChange={handleFilterChange}
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
                  <Input.Search
                    placeholder="搜索标的代码或名称"
                    allowClear
                    style={{ width: 180 }}
                    onSearch={handleSymbolSearch}
                    onPressEnter={(e) => handleSymbolSearch((e.target as HTMLInputElement).value)}
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
        {/* 批量操作区域 - 表格上方，只有选中时才显示 */}
        {selectedRowKeys.length > 0 && (
          <div className="batch-actions" style={{ marginBottom: 16, padding: '12px 16px', background: '#f6f6f6', borderRadius: 4 }}>
            <Space size="middle">
              <Text type="secondary">
                已选择 <Text strong>{selectedRowKeys.length}</Text> 项
              </Text>
              <Button
                icon={<PlayCircleOutlined />}
                onClick={handleBatchActivate}
              >
                批量启用
              </Button>
              <Button
                icon={<PauseCircleOutlined />}
                onClick={handleBatchDeactivate}
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
                >
                  批量删除
                </Button>
              </Popconfirm>
            </Space>
          </div>
        )}

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
      </Card>

      {/* 创建订阅弹窗 */}
      <SubscriptionCreate
        visible={createModalVisible}
        onClose={() => setCreateModalVisible(false)}
        onSuccess={handleCreateSuccess}
      />

      {/* 编辑订阅弹窗 */}
      <SubscriptionEdit
        visible={editModalVisible}
        subscription={editingSubscription}
        onClose={() => {
          setEditModalVisible(false)
          setEditingSubscription(null)
        }}
        onSuccess={handleEditSuccess}
      />
    </div>
  )
}

export default SubscriptionList
