import { useState, useEffect } from 'react'
import { Table, Card, Input, Button, DatePicker, Typography, Row, Col, Tag, Empty, message, Pagination, TableProps } from 'antd'
import { SearchOutlined, SyncOutlined } from '@ant-design/icons'
import { fundsApi, FundQuoteData } from '@/services/api/funds'
import dayjs from 'dayjs'
import type { SorterResult } from 'antd/es/table/interface'

const { Title } = Typography
const { RangePicker } = DatePicker

// 排序字段映射（前端字段名 -> 后端数据库字段名）
const sortFieldMap: Record<string, string> = {
  fundCode: 'fund_code',
  fundName: 'fund_name',
  nav: 'nav',
  changePercent: 'change_percent',
  quoteDate: 'quote_date',
}

interface SortConfig {
  field: string | null
  order: 'ascend' | 'descend' | null
}

const FundAnalysis = () => {
  const [loading, setLoading] = useState(false)
  const [refreshing, setRefreshing] = useState(false)
  const [fundQuotes, setFundQuotes] = useState<FundQuoteData[]>([])
  const [searchCode, setSearchCode] = useState('')
  const [searchName, setSearchName] = useState('')
  const [dateRange, setDateRange] = useState<[string, string] | null>(null)
  const [sortConfig, setSortConfig] = useState<SortConfig>({ field: 'fundCode', order: 'ascend' })
  
  // 分页状态
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  })

  // 从数据库分页查询基金净值
  const fetchQuotesPage = async (
    pageNum: number = 1, 
    pageSize: number = 20, 
    sort?: SortConfig
  ) => {
    const currentSort = sort || sortConfig
    setLoading(true)
    try {
      const params: any = {
        pageNum,
        pageSize,
      }
      
      // 添加排序参数
      if (currentSort.field && currentSort.order) {
        params.orderBy = sortFieldMap[currentSort.field] || currentSort.field
        params.orderDirection = currentSort.order === 'ascend' ? 'ASC' : 'DESC'
      }
      
      // 添加搜索条件
      if (searchCode) params.fundCode = searchCode
      if (searchName) params.fundName = searchName
      if (dateRange) {
        params.startDate = dateRange[0]
        params.endDate = dateRange[1]
      }
      
      const response = await fundsApi.getQuotesPage(params)
      const result = response as any
      if (result.success) {
        setFundQuotes(result.data.records || [])
        setPagination({
          current: result.data.pageNum,
          pageSize: result.data.pageSize,
          total: result.data.total,
        })
      } else {
        setFundQuotes([])
        setPagination({ current: 1, pageSize: 20, total: 0 })
      }
    } catch (error) {
      console.error('获取基金净值失败:', error)
      message.error('获取基金净值失败')
      setFundQuotes([])
    } finally {
      setLoading(false)
    }
  }

  // 初始加载
  useEffect(() => {
    fetchQuotesPage(1, pagination.pageSize)
  }, [])

  // 搜索按钮点击
  const handleSearch = () => {
    fetchQuotesPage(1, pagination.pageSize)
  }

  // 重置搜索条件
  const handleReset = () => {
    setSearchCode('')
    setSearchName('')
    setDateRange(null)
    setSortConfig({ field: 'fundCode', order: 'ascend' })
    fetchQuotesPage(1, pagination.pageSize, { field: 'fundCode', order: 'ascend' })
  }

  // 实时刷新 - 调用外部API获取最新数据并保存到数据库
  const handleRefresh = async () => {
    setRefreshing(true)
    try {
      const response = await fundsApi.refreshQuotes()
      const result = response as any
      if (result.success) {
        message.success(`实时刷新成功，更新了 ${result.data?.length || 0} 条记录`)
        // 刷新后重新加载当前页数据
        fetchQuotesPage(pagination.current, pagination.pageSize)
      } else {
        message.error(result.message || '刷新失败')
      }
    } catch (error) {
      console.error('实时刷新失败:', error)
      message.error('实时刷新失败')
    } finally {
      setRefreshing(false)
    }
  }

  // 处理表格排序变化
  const handleTableChange: TableProps<FundQuoteData>['onChange'] = (
    _pagination,
    _filters,
    sorter,
  ) => {
    const sortResult = sorter as SorterResult<FundQuoteData>
    const newSortConfig: SortConfig = {
      field: sortResult.field as string || null,
      order: sortResult.order || null,
    }
    setSortConfig(newSortConfig)
    fetchQuotesPage(1, pagination.pageSize, newSortConfig)
  }

  // 处理分页变化
  const handlePageChange = (page: number, pageSize: number) => {
    fetchQuotesPage(page, pageSize)
  }

  // 表格列定义
  const columns: TableProps<FundQuoteData>['columns'] = [
    {
      title: '基金代码',
      dataIndex: 'fundCode',
      key: 'fundCode',
      width: 120,
      sorter: true,
      sortOrder: sortConfig.field === 'fundCode' ? sortConfig.order : null,
    },
    {
      title: '基金名称',
      dataIndex: 'fundName',
      key: 'fundName',
      width: 200,
      ellipsis: true,
      sorter: true,
      sortOrder: sortConfig.field === 'fundName' ? sortConfig.order : null,
    },
    {
      title: '单位净值',
      dataIndex: 'nav',
      key: 'nav',
      width: 110,
      align: 'right',
      sorter: true,
      sortOrder: sortConfig.field === 'nav' ? sortConfig.order : null,
      render: (nav?: number) => nav ? `¥${nav.toFixed(4)}` : '-',
    },
    {
      title: '昨日净值',
      dataIndex: 'prevNetValue',
      key: 'prevNetValue',
      width: 100,
      align: 'right',
      render: (prevNetValue?: number) => prevNetValue ? `¥${prevNetValue.toFixed(4)}` : '-',
    },
    {
      title: '涨跌额',
      dataIndex: 'changeAmount',
      key: 'changeAmount',
      width: 100,
      align: 'right',
      render: (changeAmount?: number) => {
        if (changeAmount === undefined || changeAmount === null) return '-'
        return <span style={{ color: changeAmount >= 0 ? '#f5222d' : '#52c41a' }}>
          {changeAmount >= 0 ? '+' : ''}{changeAmount.toFixed(4)} ¥
        </span>
      },
    },
    {
      title: '涨跌幅',
      dataIndex: 'changePercent',
      key: 'changePercent',
      width: 110,
      align: 'right',
      sorter: true,
      sortOrder: sortConfig.field === 'changePercent' ? sortConfig.order : null,
      render: (changePercent?: number) => {
        if (changePercent === undefined || changePercent === null) return '-'
        return (
          <Tag color={changePercent >= 0 ? 'red' : 'green'}>
            {changePercent >= 0 ? '+' : ''}{changePercent.toFixed(2)}%
          </Tag>
        )
      },
    },
    {
      title: '数据日期',
      dataIndex: 'quoteDate',
      key: 'quoteDate',
      width: 120,
      sorter: true,
      sortOrder: sortConfig.field === 'quoteDate' ? sortConfig.order : null,
    },
    {
      title: '更新时间',
      dataIndex: 'quoteTimeOnly',
      key: 'quoteTimeOnly',
      width: 100,
    },
  ]

  return (
    <div>
      <Title level={2}>基金分析 - 实时涨跌情况</Title>

      {/* 筛选区域 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={4}>
            <Input
              placeholder="请输入基金代码"
              value={searchCode}
              onChange={(e) => setSearchCode(e.target.value)}
              prefix={<SearchOutlined />}
              allowClear
              onPressEnter={handleSearch}
            />
          </Col>
          <Col span={4}>
            <Input
              placeholder="请输入基金名称"
              value={searchName}
              onChange={(e) => setSearchName(e.target.value)}
              prefix={<SearchOutlined />}
              allowClear
              onPressEnter={handleSearch}
            />
          </Col>
          <Col span={6}>
            <RangePicker
              value={dateRange ? [dayjs(dateRange[0]), dayjs(dateRange[1])] : null}
              onChange={(dates) => {
                if (dates && dates[0] && dates[1]) {
                  setDateRange([
                    dates[0].format('YYYY-MM-DD'),
                    dates[1].format('YYYY-MM-DD')
                  ])
                } else {
                  setDateRange(null)
                }
              }}
            />
          </Col>
          <Col span={10} style={{ textAlign: 'left' }}>
            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} style={{ marginRight: 8 }}>
              搜索
            </Button>
            <Button onClick={handleReset} style={{ marginRight: 8 }}>
              重置
            </Button>
            <Button 
              type="primary" 
              icon={<SyncOutlined spin={refreshing} />} 
              onClick={handleRefresh}
              loading={refreshing}
              danger
            >
              实时刷新
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 提示信息 */}
      <Card style={{ marginBottom: 16, background: '#f6f8fa' }}>
        <Typography.Text type="secondary">
          💡 提示：点击表头可按该列排序。支持按基金代码、名称模糊搜索，支持按日期范围筛选。点击"实时刷新"按钮可从外部API获取最新数据（UPSERT模式）。
        </Typography.Text>
      </Card>

      {/* 数据表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={fundQuotes}
          loading={loading}
          rowKey={(record) => `${record.fundCode}-${record.quoteDate}`}
          pagination={false}
          scroll={{ x: 1000 }}
          onChange={handleTableChange}
          locale={{
            emptyText: (
              <Empty description="暂无数据，请点击'实时刷新'按钮获取最新行情" />
            )
          }}
        />
        
        {/* 后端分页 */}
        <div style={{ marginTop: 16, textAlign: 'right' }}>
          <Pagination
            current={pagination.current}
            pageSize={pagination.pageSize}
            total={pagination.total}
            showSizeChanger
            showQuickJumper
            showTotal={(total) => `共 ${total} 条记录`}
            pageSizeOptions={['10', '20', '50', '100']}
            onChange={handlePageChange}
            onShowSizeChange={handlePageChange}
          />
        </div>
      </Card>
    </div>
  )
}

export default FundAnalysis
