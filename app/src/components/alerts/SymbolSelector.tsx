import { useState, useCallback, useRef } from 'react'
import {
  Form, Select, Radio, Button, Spin, Tag, Space, Typography, Divider, Modal, message
} from 'antd'
import { ExclamationCircleOutlined } from '@ant-design/icons'
import { stocksApi } from '@services/api/stocks'

const { Option } = Select
const { Text } = Typography

export type SymbolType = 'STOCK' | 'FUND' | null

interface SearchResult {
  id: number
  code: string
  name: string
  type: string
  market?: string
  active?: boolean
}

interface SymbolSelectorProps {
  symbolType: SymbolType
  selectedSymbols: string[]
  onSymbolTypeChange: (type: SymbolType) => void
  onSymbolsChange: (symbols: string[]) => void
  onSearchResultsChange?: (results: SearchResult[]) => void
}

export const SymbolSelector = ({
  symbolType,
  selectedSymbols,
  onSymbolTypeChange,
  onSymbolsChange,
  onSearchResultsChange
}: SymbolSelectorProps) => {
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searching, setSearching] = useState(false)
  const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const handleSymbolTypeChange = (value: SymbolType) => {
    if (symbolType !== null && value !== symbolType) {
      Modal.confirm({
        title: '确认切换类型',
        icon: <ExclamationCircleOutlined />,
        content: '切换标的类型将清空已选标的，是否继续？',
        okText: '继续',
        cancelText: '取消',
        onOk: () => {
          onSymbolTypeChange(value)
          onSymbolsChange([])
          setSearchResults([])
          onSearchResultsChange?.([])
        }
      })
    } else {
      onSymbolTypeChange(value)
    }
  }

  const searchSymbols = useCallback(async (keyword: string, type: SymbolType) => {
    if (!type || !keyword || keyword.length < 1) {
      setSearchResults([])
      return
    }

    if (searchTimerRef.current) {
      clearTimeout(searchTimerRef.current)
    }

    searchTimerRef.current = setTimeout(async () => {
      setSearching(true)
      try {
        const response = await stocksApi.searchTargets(keyword, type)
        if (response.success && response.data) {
          setSearchResults(response.data)
          onSearchResultsChange?.(response.data)
        } else {
          setSearchResults([])
          onSearchResultsChange?.([])
        }
      } catch (error) {
        console.error('搜索标的失败:', error)
        setSearchResults([])
        onSearchResultsChange?.([])
      } finally {
        setSearching(false)
      }
    }, 300)
  }, [])

  const handleSymbolsChange = (values: string[]) => {
    if (values.length > 100) {
      message.warning('单次最多选择100个标的')
      onSymbolsChange(values.slice(0, 100))
      return
    }
    onSymbolsChange(values)
  }

  const handleSelectAll = () => {
    const allCodes = searchResults.map(r => r.code)
    const merged = Array.from(new Set([...selectedSymbols, ...allCodes]))
    if (merged.length > 100) {
      message.warning('最多只能选择100个标的')
      return
    }
    onSymbolsChange(merged)
  }

  const handleClearSelection = () => {
    onSymbolsChange([])
  }

  const validateSymbols = (_: unknown, value: string[]) => {
    if (!value || value.length === 0) {
      return Promise.reject(new Error('请至少选择一个标的'))
    }
    if (value.length > 100) {
      return Promise.reject(new Error('单次最多选择100个标的'))
    }
    return Promise.resolve()
  }

  return (
    <>
      <Form.Item
        name="symbolType"
        label="1. 选择标的类型"
        rules={[{ required: true, message: '请先选择标的类型' }]}
      >
        <Radio.Group
          value={symbolType}
          onChange={(e) => handleSymbolTypeChange(e.target.value)}
          optionType="button"
          buttonStyle="solid"
        >
          <Radio.Button value="STOCK">股票</Radio.Button>
          <Radio.Button value="FUND">基金</Radio.Button>
        </Radio.Group>
      </Form.Item>

      {symbolType && (
        <Form.Item
          name="symbols"
          label="2. 选择标的（可多选）"
          rules={[{ validator: validateSymbols }]}
          extra={
            <Space>
              <Text type="secondary">已选择 {selectedSymbols.length} 个标的</Text>
              {selectedSymbols.length > 0 && (
                <Button size="small" onClick={handleClearSelection}>清空选择</Button>
              )}
            </Space>
          }
        >
          <Select
            mode="multiple"
            showSearch
            placeholder={`输入代码或名称搜索${symbolType === 'STOCK' ? '股票' : '基金'}...`}
            value={selectedSymbols}
            onChange={handleSymbolsChange}
            filterOption={false}
            notFoundContent={searching ? <Spin size="small" /> : '请输入关键词搜索'}
            onSearch={(value) => searchSymbols(value, symbolType)}
            allowClear
            style={{ width: '100%' }}
            dropdownRender={(menu) => (
              <>
                {menu}
                {searchResults.length > 0 && (
                  <>
                    <Divider style={{ margin: '8px 0' }} />
                    <div style={{ padding: '8px', textAlign: 'center' }}>
                      <Button
                        type="link"
                        size="small"
                        onClick={handleSelectAll}
                        disabled={selectedSymbols.length >= 100}
                      >
                        全选当前搜索结果 ({searchResults.length}个)
                      </Button>
                    </div>
                  </>
                )}
              </>
            )}
          >
            {searchResults.map((result) => (
              <Option key={result.code} value={result.code}>
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
    </>
  )
}
