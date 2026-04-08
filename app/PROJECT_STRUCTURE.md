#项目结构树状图

##完整项目结构

```
Bgx/                                    # 项目根目录
├── app/                               #前端应用目录
│   ├── src/                          #目录码目录
│   │   ├── assets/                   #资源
│   │   │  └── styles/               #样式文件
│   │   │       └── index.css        #全局样式
│   │   ├── components/               #可复用组件
│   │   │  └── layout/               #布局组件
│   │   │       ├── Header.tsx        # 顶部导航栏⭐
│   │   │       └── Sidebar.tsx       # 侧边栏菜单⭐
│   │   ├── pages/                    # 页面组件
│   │   │   ├── dashboard/            # 仪表盘模块
│   │   │   │  └── Dashboard.tsx     # 仪表盘主页⭐
│   │   │   ├── stocks/               #模块
│   │   │   │  └── StockList.tsx     #列表
│   │   │   ├── funds/                #基金模块
│   │   │   │  └── FundList.tsx      #基金列表
│   │   │   └── alerts/                # 提醒模块 ⭐⭐⭐
│   │   │       ├── AlertList.tsx     # 提醒列表页面 ⭐
│   │   │       └── AlertCreate.tsx   # 创建/编辑提醒页面 ⭐
│   │   ├── services/                  # 服务层
│   │   │  └── api/                   # API接口层
│   │   │       ├── client.ts          # HTTP客户端配置⭐
│   │   │       ├── stocks.ts          #相关API
│   │   │       ├── funds.ts           #基金相关API
│   │   │       ├── alerts.ts          # 提醒相关API⭐
│   │   │       └── dashboard.ts       # 仪表盘相关API
│   │   ├── store/                     #状态管理
│   │   │   ├── index.ts               # Redux store配置 ⭐
│   │   │   ├── hooks.ts               # 自定义Redux hooks⭐
│   │   │  └── slices/                 #状态切片
│   │   │       ├── stocksSlice.ts     #状态管理
│   │   │       ├── fundsSlice.ts      # 基金状态管理
│   │   │       └── alertsSlice.ts     # 提醒状态管理⭐
│   │   ├── types/                      # TypeScript类型定义
│   │   │   └── index.ts                #全局类型定义⭐
│   │   ├── App.tsx                     #应用根组件⭐
│   │   └── main.tsx                    #应用入口文件⭐
│   ├── package.json                    # 项目依赖配置⭐
│   ├── tsconfig.json                   # TypeScript配置⭐
│   ├── tsconfig.node.json              # Node.js环境TS配置
│   ├── vite.config.ts                  # Vite构建配置⭐
│   ├── index.html                      # HTML模板⭐
│   ├── README.md                       # 项目使用说明⭐
│   ├── IMPLEMENTATION.md               # 实现详情文档⭐
│   ├── APP_ARCHITECTURE.md             #应用架构文档⭐
│   ├── FRONTEND_ARCHITECTURE.md        #前端架构文档⭐
│   ├── FUNCTION_DETAILS.md              #功能点详细说明⭐
│   ├── start.bat                       # Windows启动脚本⭐
│  └── start.sh                        # Linux/Mac启动脚本⭐
├── backend/                            #后端应用目录
│   ├── src/                           #后端源代码
│   │   ├── main/                      # 主代码目录
│   │   │   ├── java/                  # Java源代码
│   │   │   │  └── com/stock/fund/    # 项目包结构
│   │   │   │       ├── StockFundApplication.java  #应用启动类⭐
│   │   │   │       ├── domain/        #层⭐
│   │   │   │       │   ├── entity/    #实体
│   │   │   │       │   │   ├── stock/ #实体
│   │   │   │       │   │   │  └── Stock.java  #实体类⭐
│   │   │   │       │   │   ├── fund/  #基金实体
│   │   │   │       │   │   │   └── Fund.java   #基金实体类⭐
│   │   │   │       │   │  └── alert/ # 提醒实体⭐
│   │   │   │       │   │       ├── PriceAlert.java    # 价格提醒实体⭐
│   │   │   │       │   │       └── AlertHistory.java  # 提醒历史实体⭐
│   │   │   │       │   ├── repository/ #仓储
│   │   │   │       │   │   ├── stock/  #仓储
│   │   │   │       │   │   │  └── StockRepository.java  #仓储接口⭐
│   │   │   │       │   │   ├── fund/    #基仓储
│   │   │   │       │   │   │  └── FundRepository.java   # 基金仓储接口⭐
│   │   │   │       │   │  └── alert/   # 提醒仓储⭐
│   │   │   │       │   │       ├── PriceAlertRepository.java    # 提醒仓储接口⭐
│   │   │   │       │   │       └── AlertHistoryRepository.java  #仓储接口 ⭐
│   │   │   │       │  └── service/     #服务
│   │   │   │       │       ├── stock/   #领域服务
│   │   │   │       │       │   └── StockDomainService.java  #领域服务⭐
│   │   │   │       │       ├── fund/    # 基金领域服务
│   │   │   │       │       │   └── FundDomainService.java   # 基金领域服务⭐
│   │   │   │       │      └── alert/   # 提醒领域服务⭐
│   │   │   │       │           └── AlertDomainService.java  # 提醒领域服务 ⭐
│   │   │   │       ├── application/     #应用层⭐
│   │   │   │       │   ├── service/     #应用服务
│   │   │   │       │   │   ├── stock/   #应用服务
│   │   │   │       │   │   │  └── StockAppService.java  #应用服务⭐
│   │   │   │       │   │   ├── fund/    # 基金应用服务
│   │   │   │       │   │   │  └── FundAppService.java   # 基金应用服务⭐
│   │   │   │       │   │  └── alert/   # 提醒应用服务⭐
│   │   │   │       │   │      └── AlertAppService.java  # 提醒应用服务 ⭐
│   │   │   │       │  └── dto/         # 数据传输对象
│   │   │   │       │       ├── stock/   #DTO
│   │   │   │       │       │   └── StockDTO.java  #股票数据传输对象⭐
│   │   │   │       │       ├── fund/    # 基金DTO
│   │   │   │       │       │   └── FundDTO.java   # 基金数据传输对象 ⭐
│   │   │   │       │      └── alert/   # 提醒DTO⭐
│   │   │   │       │           ├── PriceAlertDTO.java     # 提醒数据传输对象⭐
│   │   │   │       │           └── AlertHistoryDTO.java  #历数据传输对象 ⭐
│   │   │   │       ├── infrastructure/  #设施层⭐
│   │   │   │       │   ├── entity/      #持化实体
│   │   │   │       │   │   ├── stock/   #持久化实体
│   │   │   │       │   │   │  └── StockPO.java  #持久化对象⭐
│   │   │   │       │   │   ├── fund/    # 基金持久化实体
│   │   │   │       │   │   │  └── FundPO.java   # 基金持久化对象⭐
│   │   │   │       │   │  └── alert/   # 提醒持久化实体⭐
│   │   │   │       │   │       ├── PriceAlertPO.java     # 提醒持久化对象⭐
│   │   │   │       │   │       └── AlertHistoryPO.java   #历持久化对象 ⭐
│   │   │   │       │   ├── mapper/      # 数据映射器
│   │   │   │       │   │   ├── stock/   #映射器
│   │   │   │       │   │   │   └── StockMapper.java  #映射器⭐
│   │   │   │       │   │   ├── fund/    # 基金映射器
│   │   │   │       │   │   │   └── FundMapper.java   # 基金映射器⭐
│   │   │   │       │   │  └── alert/   # 提醒映射器⭐
│   │   │   │       │   │       ├── PriceAlertMapper.java     # 提醒映射器⭐
│   │   │   │       │   │       └── AlertHistoryMapper.java   #历映射器 ⭐
│   │   │   │       │   ├── repository/  # 仓储实现
│   │   │   │       │   │   ├── stock/   #仓储实现
│   │   │   │       │   │   │  └── StockRepositoryImpl.java  #股仓储实现⭐
│   │   │   │       │   │   ├── fund/    # 基金仓储实现
│   │   │   │       │   │   │   └── FundRepositoryImpl.java   # 基金仓储实现 ⭐
│   │   │   │       │   │  └── alert/   # 提醒仓储实现⭐
│   │   │   │       │   │       ├── PriceAlertRepositoryImpl.java     # 提醒仓储实现⭐
│   │   │   │       │   │       └── AlertHistoryRepositoryImpl.java   #历仓储实现 ⭐
│   │   │   │       │  └── config/      #基础设施配置
│   │   │   │       │      └── MyBatisConfig.java  # MyBatis配置⭐
│   │   │   │      └── interfaces/      #接口层⭐
│   │   │   │           ├── controller/  # 控制器
│   │   │   │           │   ├── stock/  #控制器
│   │   │   │           │   │  └── StockController.java  #控制器⭐
│   │   │   │           │   ├── fund/   # 基金控制器
│   │   │   │           │   │  └── FundController.java   # 基金控制器⭐
│   │   │   │           │  └── alert/   # 提醒控制器⭐
│   │   │   │           │      └── AlertController.java   # 提醒控制器 ⭐
│   │   │   │          └── assembler/   #器
│   │   │   │               ├── stock/  #装配器
│   │   │   │               │  └── StockAssembler.java  #股票装配器⭐
│   │   │   │               ├── fund/   #基金装配器
│   │   │   │               │  └── FundAssembler.java   # 基金装配器⭐
│   │   │   │               └── alert/  # 提醒装配器 ⭐
│   │   │   │                   └── AlertAssembler.java  # 提醒装配器 ⭐
│   │   │  └── resources/               #资源文件
│   │   │       ├── application.yml      #应用配置文件⭐
│   │   │       ├── application-dev.yml  # 开发环境配置⭐
│   │   │       ├── application-prod.yml # 生产环境配置⭐
│   │   │       └── mapper/               # MyBatis映射文件
│   │   │           ├── stock/           #映射文件
│   │   │           │  └── StockMapper.xml  #SQL映射⭐
│   │   │           ├── fund/            # 基金映射文件
│   │   │           │  └── FundMapper.xml   # 基金SQL映射⭐
│   │   │           └── alert/           # 提醒映射文件⭐
│   │   │               ├── PriceAlertMapper.xml     # 提醒SQL映射⭐
│   │   │               └── AlertHistoryMapper.xml   #历SQL映射⭐
│   │   └── test/                       #测试代码
│   │      └── java/                   # 测试源代码
│   │          └── com/stock/fund/     # 测试包结构
│   │               ├── StockFundApplicationTests.java  #应用测试类 ⭐
│   │               └── domain/          #测试
│   │                  └── service/     #服务测试
│   │                       ├── stock/  #服务测试
│   │                       │  └── StockDomainServiceTest.java  #领域服务测试⭐
│   │                       ├── fund/   #基金服务测试
│   │                       │   └── FundDomainServiceTest.java   # 基金领域服务测试⭐
│   │                       └── alert/  # 提醒服务测试⭐
│   │                           └── AlertDomainServiceTest.java  # 提醒领域服务测试 ⭐
│   ├── pom.xml                         # Maven配置文件⭐
│   ├── ARCHITECTURE_DESIGN.md          #架设计设计文档⭐
│  └── README.md                       #后端说明文档⭐
├──前端架构设计.md                      #前端架构设计文档⭐
├──后端架构图.md                        #后端架构图文档⭐
├──总方案.md                          #总体方案文档⭐
├── 数据库设计.md                        # 数据库设计文档⭐
└── Bgx.code-workspace                  # VS Code工作区配置 ⭐
```

## 核心功能模块标识

###⭐⭐⭐提醒模块（核心功能）
- **前端**: AlertList.tsx, AlertCreate.tsx
- **后端**: PriceAlert实体、AlertController、AlertDomainService等
- **功能**: 价格提醒、涨跌幅提醒、状态管理

###⭐⭐基金数据模块
- **前端**: StockList.tsx, FundList.tsx
- **后端**: Stock实体、Fund实体及相关服务
- **功能**: 数据展示、实时更新

### ⭐⭐系统基础模块
- **前端**: Header.tsx, Sidebar.tsx, Dashboard.tsx
- **后端**:基架构、配置管理
- **功能**:系统导航、数据统计

##技术栈分布

###前端技术栈 (app/)
- **框架**: React 18 + TypeScript
- **UI库**: Ant Design 5.0+
- **状态管理**: Redux Toolkit
- **构建工具**: Vite
- **路由**: React Router v6

###后端技术栈 (backend/)
- **框架**: Spring Boot 3.x
- **语言**: Java 17+
- **数据库**: MyBatis-Plus
- **构建工具**: Maven
- **测试**: JUnit 5

## 文档体系

###架设计设计文档
- `前端架构设计.md` -前端架构设计规范
- `后端架构图.md` -后端架构图解
- `总体方案.md` - 项目总体方案
- `数据库设计.md` - 数据库设计规范

### 实现文档
- `ARCHITECTURE_DESIGN.md` -后端架构设计
- `APP_ARCHITECTURE.md` - 前端应用架构
- `FRONTEND_ARCHITECTURE.md` - 前端架构设计
- `FUNCTION_DETAILS.md` -功能点详细说明

### 使用文档
- `README.md` (前后端) - 使用说明
- `IMPLEMENTATION.md` - 实现详情

###配置文件
- `pom.xml` - 后端依赖管理
- `package.json` -前端依赖管理
- `application.yml` -后端配置
- `vite.config.ts` - 前端构建配置

---
*此树状图展示了项目的完整文件结构，便于快速定位和理解项目组织*