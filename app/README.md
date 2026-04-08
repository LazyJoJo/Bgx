#基金数据系统前端

## 项目概述

这是一个基于React 18 + TypeScript + Ant Design的现代化前端应用，用于股票和基金数据的展示、分析和风险提醒管理。

##技术栈

- **框架**: React 18
- **语言**: TypeScript 5.0+
- **UI库**: Ant Design 5.0+
- **状态管理**: Redux Toolkit
- **路由**: React Router v6
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **图表库**: ECharts 5.0+

## 项目结构

```
src/
├── assets/           #资源
│  └── styles/       #全局样式
├── components/       # 通用组件
│   └── layout/       #布局组件
├── pages/            # 页面组件
│   ├── dashboard/    # 仪表盘
│   ├── stocks/       #页面
│   ├── funds/        #基页面
│  └── alerts/       # 提醒页面
├── services/         # 服务层
│   └── api/          # API调用
├── store/            # Redux状态管理
│   └── slices/       #状态切片
├── types/            # TypeScript类型定义
├── App.tsx           #应用根组件
└── main.tsx          #应用入口
```

##功能模块

### 1. 仪表盘 (Dashboard)
-系统统计数据展示
- 最近提醒列表
-快导航入口

### 2.管理管理 (Stocks)
-列表展示
- 实时价格更新
-可视化

### 3.基金管理 (Funds)
-基金列表展示
-净变化跟踪
-等级标识

### 4.提醒 (Alerts) ⭐
- **提醒创建**:支持价格上限、价格下限、涨跌幅提醒
- **提醒管理**: 查看、编辑、删除提醒
- **状态监控**: 实时显示提醒状态(已启用/已触发/已禁用)
- **条件设置**:灵的价格和涨跌幅条件配置

##快速开始

###安装依赖
```bash
npm install
```

### 开发环境启动
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

###预览构建结果
```bash
npm run preview
```

##风提醒功能详解

### 提醒类型
1. **价格上限提醒**: 当价格超过设定值时触发
2. **价格下限提醒**: 当价格低于设定值时触发
3. **涨跌幅提醒**: 当价格涨跌幅达到设定百分比时触发

###核心功能
-✅支持股票和基金两种标的类型
- ✅的条件设置
- ✅ 实时状态监控
- ✅ 提醒历史记录
- ✅批管理和筛选
- ✅响应式界面设计

### API接口
后端API通过 `/api/v1`前缀代理到 `http://localhost:8080`

## 开发规范

### 代码风格
- 使用TypeScript严格模式
-遵循ESLint和Prettier规范
-组件使用函数式组件 + Hooks

###状态管理
- 使用Redux Toolkit管理全局状态
-功能模块对应一个slice
-异步操作使用createAsyncThunk

###路由配置
```
/ - 仪表盘
/stocks -列表列表
/funds -基列表
/alerts - 提醒列表
/alerts/create - 创建提醒
/alerts/edit/:id -编辑提醒
```

## 部署说明

###环境变量
```bash
# 开发环境
VITE_API_BASE_URL=http://localhost:8080

# 生产环境
VITE_API_BASE_URL=https://your-api-domain.com
```

###构建配置
构建后的文件位于 `dist/`目录，可直接部署到静态服务器。

## 注意事项

1.后端保后端服务正常运行在8080端口
2. 开发环境默认代理API请求到后端
3. 生产环境需要配置正确的API基础URL
4.前端依赖后端的WebSocket实时推送功能

##后续开发计划

- [ ] 实时数据推送功能
- [ ] 图表可视化组件
- [ ] 数据分析功能
- [ ] 用户权限管理
- [ ]国化支持
- [ ]移动端适配优化