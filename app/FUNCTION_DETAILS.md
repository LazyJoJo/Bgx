#前端功能点详细说明文档

## 1. 系统布局功能

### 1.1 顶部导航栏 (Header)
**文件位置**: `src/components/layout/Header.tsx`

####核心功能点
- **系统标识显示**
  -显示系统名称"基金数据系统"
  -折叠/展开控制按钮
  -响应式设计适配

- **主导航菜单**
  - 仪表盘导航
  -管理导航
  -基金管理导航
  -提醒导航
  -高亮状态同步

- **快捷操作区**
  - "创建提醒"主按钮
  - 通知图标和未读数量显示
  - 用户头像和个人菜单

- **用户菜单功能**
  - 个人资料查看
  - 系统设置
  - 退出登录功能

#### 技术实现要点
```typescript
//状态状态管理
const [collapsed, setCollapsed] = useState(false)
const [notifications] = useState(3)

//路导航
const navigate = useNavigate()
const location = useLocation()

//点击处理
const menuItems = [
  {
    key: 'dashboard',
    icon: <DashboardOutlined />,
    label: '仪表盘',
    onClick: () => navigate('/dashboard')
  }
  // ...其菜单项
]
```

### 1.2 侧边栏菜单 (Sidebar)
**文件位置**: `src/components/layout/Sidebar.tsx`

#### 核心功能点
- **菜单结构组织**
  - 主功能模块分组
  -子支持
  - 图标可视化增强
  -固定布局设计

- **交互功能**
  -折叠/展开
  -路由状态同步
  -响应式适配
  -优化

####结构结构详情
```
主菜单项:
├── 仪表盘 (Dashboard)
├──管理 (Stocks)
├──基管理 (Funds)
├──提醒 (Alerts) ⭐
├── 数据分析 (Analysis)
│   ├──分析
│  └──基金分析
├──系统设置 (Settings)
└── 个人中心 (Profile)
```

## 2. 仪表盘功能

### 2.1 仪表盘主页 (Dashboard)
**文件位置**: `src/pages/dashboard/Dashboard.tsx`

####核心功能点
- **数据统计展示**
  -总数统计卡片
  -基金总数统计卡片
  -提醒数量
  -已触发提醒数量

- **最近提醒列表**
  - 最近5条提醒展示
  - 状态标签显示
  -快速查看详情
  - "查看全部"跳转

- **数据获取机制**
  - 仪表盘统计数据API调用
  - 最近提醒数据获取
  - 加载状态管理
  -错误处理

#### UI组件使用
```typescript
//统卡片组件
<Statistic
  title="股票总数"
  value={stats.totalStocks}
  prefix={<StockOutlined />}
  valueStyle={{ color: '#1890ff' }}
/>

// 提醒表格列定义
const alertColumns = [
  {
    title: '标的',
    dataIndex: 'symbol',
    key: 'symbol',
    render: (symbol: string, record: PriceAlert) => (
      <Space>
        {record.symbolType === 'STOCK' ? <StockOutlined /> : <FundOutlined />}
        <span>{symbol}</span>
      </Space>
    )
  }
  // ...其他列定义
]
```

## 3.风提醒功能⭐

### 3.1 提醒列表页面 (AlertList)
**文件位置**: `src/pages/alerts/AlertList.tsx`

####核心功能点
- **数据展示**
  -表格形式展示提醒列表
  -支持分页功能
  -响应式表格设计
  - 状态标签可视化

- **搜索筛选**
  -按代码搜索
  - 按标的类型筛选（股票/基金）
  - 按状态筛选（启用/触发/禁用）
  -按提醒类型筛选

- **批量操作**
  -多选支持
  -批删除功能
  - 状态批量修改

- **单操作**
  -编辑提醒
  - 删除提醒（确认对话框）
  - 状态切换

####表格列详细说明
|列名 | 字段 |渲方式 |功能 |
|------|------|----------|------|
|标的代码 | symbol | 文本 |基信息显示 |
| 标的类型 | symbolType | 标签 |/基金区分 |
| 提醒类型 | alertType | 文本 | 价格上限/下限/涨跌幅 |
|目标值 | targetPrice | 数值 |条值显示 |
| 当前价格 | currentPrice | 数值 | 实时价格 |
|状态 | status |标签 |启/触发/禁用状态 |
| 创建时间 | createdAt | 时间 | 创建时间显示 |
| 操作 | action |组 |编辑/删除操作 |

#### 搜索筛选功能
```typescript
// 搜索参数状态
const [searchParams, setSearchParams] = useState({
  symbol: '',
  symbolType: '',
  status: '',
  alertType: ''
})

// 搜索处理
const handleSearch = (value: string) => {
  setSearchParams(prev => ({ ...prev, symbol: value }))
}

//处理
const handleFilterChange = (field: string, value: string) => {
  setSearchParams(prev => ({ ...prev, [field]: value || '' }))
}
```

### 3.2 提醒创建/编辑页面 (AlertCreate)
**文件位置**: `src/pages/alerts/AlertCreate.tsx`

#### 核心功能点
- **表单管理**
  - 创建和编辑模式切换
  -动态表单字段显示
  - 数据验证和错误提示
  -表单重置功能

- **提醒类型配置**
  - **价格上限提醒**: 当价格超过设定值时触发
  - **价格下限提醒**: 当价格低于设定值时触发
  - **涨跌幅提醒**: 当价格涨跌幅达到设定百分比时触发

- **标的类型支持**
  -类型提醒
  -基类型提醒

- **状态管理**
  -启/禁用状态切换
  - 创建时默认启用
  -编辑时保持原有状态

####表单字段详细说明
| 字段名 | 类型 |必 |条显示 |验证规则 |
|--------|------|------|----------|----------|
|标的代码 | 输入框 | 是 |总是显示 | 2-10字符长度 |
| 标的类型 | 选择器 | 是 |总是显示 |/基金 |
| 提醒类型 | 选择器 | 是 |总是显示 | 三种类型 |
|目标价格 | 数字输入 |条 |跌幅类型 | 最小值0 |
|涨 | 数字输入 |条件 |类型 | -100%到100% |
|启状态 | 开关 |否总是显示 |值 |

####动态表单逻辑
```typescript
// 提醒类型状态管理
const [alertType, setAlertType] = useState('PRICE_ABOVE')

//动态字段显示
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
    />
  </Form.Item>
)}
```

#### 数据验证逻辑
```typescript
//标的代码验证
const validateSymbol = (_: any, value: string) => {
  if (!value) {
    return Promise.reject('请输入标的代码')
  }
  if (value.length < 2 || value.length > 10) {
    return Promise.reject('标的代码长度应在2-10个字符之间')
  }
  return Promise.resolve()
}

//表单提交处理
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
```

## 4. 数据管理功能

### 4.1列表页面 (StockList)
**文件位置**: `src/pages/stocks/StockList.tsx`

#### 核心功能点
- **数据展示**
  -基本信息表格
  - 实时价格显示
  -额和涨跌幅可视化
  - 成交量数据展示

- **数据更新**
  -定数据数据刷新
  - 加载状态管理
  -错误处理机制

####表格列设计
|列名 | 字段 |渲特点 |
|------|------|----------|
|代码 | symbol |基本文本 |
|名称 | name | 基本文本 |
| 当前价格 | currentPrice | 数值格式化 |
|额 | change |区分正负 |
| | changePercent |标签形式，颜色区分 |
| 成交量 | volume | 数值格式化 |

### 4.2基金列表页面 (FundList)
**文件位置**: `src/pages/funds/FundList.tsx`

#### 核心功能点
- **基金数据展示**
  -基金基本信息
  -净和日涨跌显示
  -基金类型标识
  -等级标签

- **分类展示**
  -按基金类型分组
  -等级可视化
  -收率展示

####表格列设计
|列名 | 字段 |渲特点 |
|------|------|----------|
|基金代码 | fundCode |基本文本 |
| 基金名称 | fundName | 基本文本 |
| 基金类型 | fundType |标签形式 |
| 当前净值 | currentNav |精度格式化 |
| 日涨跌 | dailyChange |区分 |
| 日涨跌幅 | dailyChangePercent |标签形式 |
|年收益率 | annualizedReturn |百比格式 |
|等级 | riskLevel |标签形式 |

## 5. 状态管理功能

### 5.1 Redux Store配置
**文件位置**: `src/store/index.ts`

####核心功能
- **全局状态管理**
  - stocks:数据状态
  - funds:基金数据状态
  - alerts: 提醒数据状态

- **类型安全**
  - RootState 类型定义
  - AppDispatch 类型定义
  -状态选择器类型推导

### 5.2 提醒状态管理 (alertsSlice)
**文件位置**: `src/store/slices/alertsSlice.ts`

####状态结构
```typescript
interface AlertState {
  list: PriceAlert[]        // 提醒列表
  history: AlertHistory[]   // 提醒历史
  selectedAlert: PriceAlert | null  // 选中提醒
  loading: boolean          // 加载状态
  error: string | null      //错误信息
}
```

####异步操作
- `fetchAlerts`: 获取提醒列表
- `fetchAlertHistory`: 获取提醒历史
- `createAlert`: 创建新提醒
- `updateAlert`: 更新提醒
- `deleteAlert`: 删除提醒

####同步操作
- `setSelectedAlert`: 设置选中提醒
- `clearError`: 清除错误信息

## 6. API服务功能

### 6.1 HTTP客户端配置
**文件位置**: `src/services/api/client.ts`

####核心功能
- **请求配置**
  -基URL配置
  -设置
  - 请求头配置

- **拦截器**
  - 请求拦截器：添加认证token
  -响应拦截器：统一错误处理

- **认证管理**
  - Token自动添加
  - 未授权处理
  - Token过期刷新

### 6.2 提醒API接口
**文件位置**: `src/services/api/alerts.ts`

####接口列表
```typescript
export const alertsApi = {
  // 获取提醒列表
  getAlerts: (params?: any) => 
    apiClient.get<ApiResponse<PageResponse<PriceAlert>>>('/alerts', { params }),

  // 获取提醒详情
  getAlert: (id: string) => 
    apiClient.get<ApiResponse<PriceAlert>>(`/alerts/${id}`),

  // 创建提醒
  createAlert: (data: any) => 
    apiClient.post<ApiResponse<PriceAlert>>('/alerts', data),

  // 更新提醒
  updateAlert: (id: string, data: any) => 
    apiClient.put<ApiResponse<PriceAlert>>(`/alerts/${id}`, data),

  // 删除提醒
  deleteAlert: (id: string) => 
    apiClient.delete<ApiResponse<void>>(`/alerts/${id}`),

  // 获取提醒历史
  getAlertHistory: (params?: any) => 
    apiClient.get<ApiResponse<PageResponse<AlertHistory>>>('/alerts/history', { params }),

  //启提醒
  enableAlert: (id: string) => 
    apiClient.post<ApiResponse<PriceAlert>>(`/alerts/${id}/enable`),

  //提醒
  disableAlert: (id: string) => 
    apiClient.post<ApiResponse<PriceAlert>>(`/alerts/${id}/disable`),
}
```

## 7. 类型系统功能

### 7.1 TypeScript 类型定义
**文件位置**: `src/types/index.ts`

#### 核心类型
```typescript
//类型
export interface Stock {
  id: string
  symbol: string
  name: string
  currentPrice: number
  change: number
  changePercent: number
  volume: number
  marketCap: number
  peRatio: number
  dividendYield: number
  lastUpdated: string
}

//基金类型
export interface Fund {
  id: string
  fundCode: string
  fundName: string
  fundType: string
  currentNav: number
  dailyChange: number
  dailyChangePercent: number
  annualizedReturn: number
  riskLevel: string
  fundManager: string
  establishmentDate: string
  lastUpdated: string
}

// 提醒类型
export interface PriceAlert {
  id: string
  symbol: string
  symbolType: 'STOCK' | 'FUND'
  alertType: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
  targetPrice?: number
  targetChangePercent?: number
  currentPrice: number
  status: 'ACTIVE' | 'TRIGGERED' | 'INACTIVE'
  userId: string
  createdAt: string
  updatedAt: string
}

// API响应类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
```

## 8.路功能

### 8.1路由配置
**文件位置**: `src/App.tsx`

####路结构
```typescript
<Routes>
  <Route path="/" element={<Dashboard />} />
  <Route path="/dashboard" element={<Dashboard />} />
  <Route path="/stocks" element={<StockList />} />
  <Route path="/funds" element={<FundList />} />
  <Route path="/alerts" element={<AlertList />} />
  <Route path="/alerts/create" element={<AlertCreate />} />
  <Route path="/alerts/edit/:id" element={<AlertCreate />} />
</Routes>
```

####路由参数处理
```typescript
//编辑页面参数获取
const { id } = useParams()
const isEdit = !!id

//路导航
const navigate = useNavigate()
navigate('/alerts')           //列表页
navigate('/alerts/create')     // 创建页
navigate(`/alerts/edit/${id}`) //编辑页
```

## 9.样功能

### 9.1全局样式
**文件位置**: `src/assets/styles/index.css`

#### 样式特性
- **Ant Design样重置**
- **自定义滚动条样式**
- **响应式断点设计**
  -移动端: < 768px
  -平: 768px - 1024px
  -: > 1024px
- **全局CSS变量**
- **工具类定义**

## 10.构建和部署功能

### 10.1 Vite配置
**文件位置**: `vite.config.ts`

####核心配置
- **路径别名**
  - `@` → `src/`
  - `@components` → `src/components/`
  - `@pages` → `src/pages/`
  - `@services` → `src/services/`
  - `@store` → `src/store/`
  - `@types` → `src/types/`

- **开发服务器**
  -端口: 3000
  - API代理: `/api` → `http://localhost:8080`

- **构建优化**
  - 代码分割策略
  -压缩
  - 输出目录配置

### 10.2启脚动脚本
**文件位置**: 
- `start.bat` (Windows)
- `start.sh` (Linux/Mac)

#### 自动化流程
1.检查依赖安装
2. 自动安装缺失依赖
3.启动开发服务器
4.打开浏览器访问

---
*本文档详细描述了前端应用的所有功能点，便于开发人员理解和维护*