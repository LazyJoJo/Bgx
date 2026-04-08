import { Table, Tag, Button, Space, message } from 'antd'
import { useEffect } from 'react'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchFunds } from '@store/slices/fundsSlice'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'

const FundList = () => {
  const dispatch = useAppDispatch()
  const { list, loading } = useAppSelector(state => state.funds)

  useEffect(() => {
    dispatch(fetchFunds({}))
  }, [dispatch])

  const columns = [
    {
      title: '基金代码',
      dataIndex: 'code',
      key: 'code'
    },
    {
      title: '基金名称',
      dataIndex: 'name',
      key: 'name'
    },
    {
      title: '基金类型',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => {
        const typeMap: Record<string, string> = {
          '股票型': 'blue',
          '债券型': 'green',
          '混合型': 'orange',
          '货币型': 'purple'
        }
        const type = category || '其他'
        return <Tag color={typeMap[type] || 'default'}>{type}</Tag>
      }
    },
    {
      title: '状态',
      dataIndex: 'active',
      key: 'active',
      render: (active: boolean) => (
        <Tag color={active ? 'green' : 'default'}>
          {active ? '已启用' : '已停用'}
        </Tag>
      )
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Space size="middle">
          <Button 
            type="link" 
            icon={<EditOutlined />}
            onClick={() => message.info(`编辑基金：${record.code}`)}
          >
            编辑
          </Button>
          <Button 
            type="link" 
            danger
            icon={<DeleteOutlined />}
            onClick={() => message.warning(`删除功能开发中`)}
          >
            删除
          </Button>
        </Space>
      )
    }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>基金管理</h2>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => message.info('添加基金功能开发中')}
        >
          添加基金
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={list}
        loading={loading}
        rowKey="id"
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true
        }}
      />
    </div>
  )
}

export default FundList
