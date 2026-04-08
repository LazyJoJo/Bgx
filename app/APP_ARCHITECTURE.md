#基数据系统前端架构文档

## 项目概述

这是一个基于React 18 + TypeScript + Ant Design的现代化前端应用，用于股票和基金数据的展示、分析和风险提醒管理。

##技术架构

### 核心技术栈
- **框架**: React 18.2.0
- **语言**: TypeScript 5.0+
- **UI组件库**: Ant Design 5.14.0
- **状态管理**: Redux Toolkit 2.2.1
- **路由管理**: React Router v6.22.0
- **HTTP客户端**: Axios 1.6.7
- **构建工具**: Vite 5.1.0
- **代码规范**: ESLint + Prettier

## 项目目录结构

```
app/
├── src/                           #目录
│   ├── assets/                    #资源
│   │  └── styles/               #全样式样式文件
│   │       └── index.css          #全局样式入口
│   ├── components/                #可复用组件
│   │  └── layout/               #布相关组件
│   │       ├── Header.tsx        # 顶部导航栏组件
│   │       └── Sidebar.tsx       # 侧边栏菜单组件
│   ├── pages/                     # 页面组件
│   │   ├── dashboard/            # 仪表盘模块
│   │   │  └── Dashboard.tsx     # 仪表盘主页
│   │   ├── stocks/              #模块
│   │   │  └── StockList.tsx     #列表页面
│   │   ├── funds/                #基金模块
│   │   │  └── FundList.tsx      # 基金列表页面
│   │   └── alerts/               #提醒模块 ⭐
│   │       ├── AlertList.tsx     # 提醒列表页面
│   │       └── AlertCreate.tsx   # 创建/编辑提醒页面
│   ├── services/                 # 服务层
│   │  └── api/                  # API接口层
│   │       ├── client.ts         # HTTP客户端配置
│   │       ├── stocks.ts         #相关API
│   │       ├── funds.ts          # 基金相关API
│   │       ├── alerts.ts         # 提醒相关API
│   │       └── dashboard.ts      # 仪表盘相关API
│   ├── store/                     #状态管理
│   │   ├── index.ts              # Redux store配置
│   │   ├── hooks.ts              # 自定义Redux hooks
│   │   └── slices/               #状态切片
│   │       ├── stocksSlice.ts    #状态管理
│   │       ├── fundsSlice.ts    # 基金状态管理
│   │       └── alertsSlice.ts   # 提醒状态管理
│   ├── types/                     # TypeScript类型定义
│   │  └── index.ts              #全局类型定义
│   ├── App.tsx                   #应用根组件
│  └── main.tsx                  #应用入口文件
├── package.json                   # 项目依赖配置
├── tsconfig.json                  # TypeScript配置
├── tsconfig.node.json             # Node.js环境TS配置
├── vite.config.ts                # Vite构建配置
├── index.html                     # HTML模板
├── README.md                      # 项目使用说明
├── IMPLEMENTATION.md              # 实现详情文档
├── start.bat                      # Windows启动脚本
└── start.sh                       # Linux/Mac启动脚本
```

##核心功能模块

### 1.系统
#### Header组件 (src/components/layout/Header.tsx)
**功能描述**: 顶部导航栏组件
**主要功能点**:
- 系统标题和logo展示
- 主导航菜单（仪表盘、股票、基金、提醒）
- 创建提醒快捷按钮
- 通知图标和数量显示
- 用户头像和下拉菜单
-响应式折叠按钮
-路由状态同步高亮

**UI元素**:
- Ant Design Layout.Header
- Menu组件（水平导航）
- Button组件（操作按钮）
- Badge组件（通知计数）
- Dropdown组件（用户菜单）
- Avatar组件（用户头像）

#### Sidebar组件 (src/components/layout/Sidebar.tsx)
**功能描述**: 侧边栏导航菜单
**主要功能点**:
- 系统功能模块导航
-支持菜单折叠功能
-子菜单结构
- 图标增强可视化
-固定布局设计
-响应式适配

**菜单结构**:
- 仪表盘（Dashboard）
-管理管理（Stocks）
-基金管理（Funds）
-提醒（Alerts）⭐
- 数据分析（子菜单）
-系统设置（Settings）
- 个人中心（Profile）

### 2. 仪表盘模块
#### Dashboard 页面 (src/pages/dashboard/Dashboard.tsx)
**功能描述**: 系统概览和数据统计
**核心功能**:
- 系统统计数据卡片展示
- 最近提醒列表显示
- 数据图表（预留集成点）
-快导航入口
- 实时数据更新

**数据展示区域**:
-📊总数统计
-📊基金总数统计
-📊提醒数量
- 📊已触发提醒数量
- 📋 最近提醒列表（表格）

### 3.提醒模块⭐
这是系统的**核心功能模块**，提供完整的风险监控能力。

#### AlertList 页面 (src/pages/alerts/AlertList.tsx)
**功能描述**: 提醒列表管理页面
**主要功能点**:
-⭐醒列表展示（表格形式）
- ⭐多维度搜索筛选
- ⭐批操作支持
- ⭐ 状态可视化管理
- ⭐响应式表格设计
- ⭐ 分页功能支持

**表格列字段**:
-标的代码
-标的类型（股票/基金）
- 提醒类型
-目标值
- 当前价格
-状态（启用/触发/禁用）
- 创建时间
- 操作（编辑/删除）

**搜索筛选功能**:
-按代码搜索
- 按标的类型筛选
- 按状态筛选
- 按提醒类型筛选

**交互功能**:
- 点击"创建提醒"按钮 →跳转提醒创建页
-点击"编辑"操作 →跳转编辑页
-点击"删除"操作 →弹确认删除

#### AlertCreate 页面 (src/pages/alerts/AlertCreate.tsx)
**功能描述**: 提醒创建/编辑页面
**核心功能**:
-⭐ 三种提醒类型设置
- ⭐双的支持（股票/基金）
- ⭐状态动态条件表单
- ⭐前端数据验证
- ⭐支持

**表单字段**:
-📝标的代码输入（文本）
-🔘标的类型选择（单选）
-⚡选择
-值设定（输入数字，不同条件下不同的目标参数）
- 䓬容启制（ 开发能切换/启动编发标时隐藏置提示编级状态说明

**用户 **用户交互**

