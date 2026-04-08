import { useState, useEffect } from 'react'
import { Table, Button, Space, Tag, Input, Select, Modal } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchAlerts, deleteAlert } from '@store/slices/alertsSlice'
import { PriceAlert } from '@/types'

const { Search } = Input
const { Option } = Select

const AlertList = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { list, loading } = useAppSelector(state => state.alerts)
  
  const [searchParams, setSearchParams] = useState({
    symbol: '',
    symbolType: '',
    status: '',
    alertType: ''
  })
  const [deleteModal, setDeleteModal] = useState({
    visible: false,
    alertId: ''
  })

  useEffect(() => {
    dispatch(fetchAlerts(searchParams))
  }, [dispatch, searchParams])

  const handleSearch = (value: string) => {
    setSearchParams(prev => ({ ...prev, symbol: value }))
  }

  const handleFilterChange = (field: string, value: string) => {
    setSearchParams(prev => ({ ...prev, [field]: value }))
  }

  const handleDelete = (id: string) => {
    setDeleteModal({ visible: true, alertId: id })
  }

  const confirmDelete = async () => {
    try {
      await dispatch(deleteAlert(deleteModal.alertId)).unwrap()
      setDeleteModal({ visible: false, alertId: '' })
    } catch (error) {
      console.error('删除提醒失败:', error)
    }
  }

  const columns = [
    {
      title: '标的代码',
      dataIndex: 'symbol',
      key: 'symbol',
      sorter: (a: PriceAlert, b: PriceAlert) => a.symbol.localeCompare(b.symbol)
    },
    {
      title: '标的类型',
      dataIndex: 'symbolType',
      key: 'symbolType',
      render: (type: string) => (
        <Tag color={type === 'STOCK' ? 'blue' : 'green'}>
          {type === 'STOCK' ? '股票' : '基金'}
        </Tag>
      ),
      filters: [
        { text: '股票', value: 'STOCK' },
        { text: '基金', value: 'FUND' }
      ],
      onFilter: (value: any, record: PriceAlert) => record.symbolType === value
    },
    {
      title: '提醒类型',
      dataIndex: 'alertType',
      key: 'alertType',
      render: (type: string) => {
        const typeMap: Record<string, string> = {
          PRICE_ABOVE: '价格上限',
          PRICE_BELOW: '价格下限',
          PERCENTAGE_CHANGE: '涨跌幅'
        }
        return typeMap[type] || type
      }
    },
    {
      title: '目标值',
      dataIndex: 'targetPrice',
      key: 'targetPrice',
      render: (value: number, record: PriceAlert) => {
        if (record.alertType === 'PERCENTAGE_CHANGE') {
          return `${record.targetChangePercent}%`
        }
        return `¥${value?.toFixed(2)}`
      }
    },
    {
      title: '当前价格',
      dataIndex: 'currentPrice',
      key: 'currentPrice',
      render: (price: number) => `¥${price.toFixed(2)}`
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusMap: Record<string, { text: string; color: string }> = {
          ACTIVE: { text: '已启用', color: 'green' },
          TRIGGERED: { text: '已触发', color: 'orange' },
          INACTIVE: { text: '已禁用', color: 'default' }
        }
        const config = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleString('zh-CN')
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 150,
      render: (_: any, record: PriceAlert) => (
        <Space size="middle">
          <Button 
            type="link" 
            icon={<EditOutlined />}
            onClick={() => navigate(`/alerts/edit/${record.id}`)}
          >
           编辑
          </Button>
          <Button 
            type="link" 
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
        </Space>
      )
    }
  ]

  return (
    <div>
      <div style={{ marginBottom: '16px', display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Search
            placeholder="搜索标的代码"
            allowClear
            onSearch={handleSearch}
            style={{ width: 200 }}
          />
          <Select
            placeholder="标的类型"
            style={{ width: 120 }}
            allowClear
            onChange={(value) => handleFilterChange('symbolType', value || '')}
          >
            <Option value="STOCK">股票</Option>
            <Option value="FUND">基金</Option>
          </Select>
          <Select
            placeholder="状态"
            style={{ width: 120 }}
            allowClear
            onChange={(value) => handleFilterChange('status', value || '')}
          >
            <Option value="ACTIVE">已启用</Option>
            <Option value="TRIGGERED">已触发</Option>
            <Option value="INACTIVE">已禁用</Option>
          </Select>
        </Space>
        
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => navigate('/alerts/create')}
        >
          创建提醒
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={list}
        loading={loading}
        rowKey="id"
        scroll={{ x: 1200 }}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条记录`
        }}
      />

      <Modal
        title="确认删除"
        open={deleteModal.visible}
        onOk={confirmDelete}
        onCancel={() => setDeleteModal({ visible: false, alertId: '' })}
        okText="确认"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确定要删除这个提醒吗？此操作不可撤销。</p>
      </Modal>
    </div>
  )
}

export default AlertList