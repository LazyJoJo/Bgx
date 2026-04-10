import { Table, Tag } from 'antd'
import { useEffect } from 'react'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { fetchStocks } from '@store/slices/stocksSlice'

const StockList = () => {
  const dispatch = useAppDispatch()
  const { list, loading } = useAppSelector(state => state.stocks)

  useEffect(() => {
    dispatch(fetchStocks({}))
  }, [dispatch])

  const columns = [
    {
      title: '股票代码',
      dataIndex: 'symbol',
      key: 'symbol'
    },
    {
      title: '股票名称',
      dataIndex: 'name',
      key: 'name'
    },
    {
      title: '当前价格',
      dataIndex: 'currentPrice',
      key: 'currentPrice',
      render: (price: number) => price != null ? `¥${price.toFixed(2)}` : '-'
    },
    {
      title: '涨跌额',
      dataIndex: 'change',
      key: 'change',
      render: (change: number) => (
        <span style={{ color: change >= 0 ? '#f5222d' : '#52c41a' }}>
          {change != null ? `${change >= 0 ? '+' : ''}${change.toFixed(2)}` : '-'}
        </span>
      )
    },
    {
      title: '涨跌幅',
      dataIndex: 'changePercent',
      key: 'changePercent',
      render: (percent: number) => (
        <Tag color={percent >= 0 ? 'red' : 'green'}>
          {percent != null ? `${percent >= 0 ? '+' : ''}${percent.toFixed(2)}%` : '-'}
        </Tag>
      )
    },
    {
      title: '成交量',
      dataIndex: 'volume',
      key: 'volume',
      render: (volume: number) => volume != null ? volume.toLocaleString() : '-'
    }
  ]

  return (
    <div>
      <h2>股票列表</h2>
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

export default StockList