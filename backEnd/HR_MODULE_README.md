# EdgeHire HR模块后端开发文档

## 概述

本文档描述了EdgeHire项目中HR模块的后端实现，包括实体类、服务层、控制器层以及相关的API接口。

## 新增功能

### 1. 实体类 (Entity)

#### HrInfo - HR信息表
- `id`: 主键，自增
- `userId`: 用户ID，外键关联t_user表
- `company`: 所属公司
- `position`: 招聘岗位
- `experience`: 资历

#### SeekerInfo - 求职者信息表
- `id`: 主键，自增
- `userId`: 用户ID，外键关联t_user表
- `education`: 学历 (1-7: 小学到博士)
- `school`: 毕业院校
- `favor`: 理想岗位
- `membership`: 会员等级 (0-普通会员, 1-高级会员)

#### ResumeComment - 简历评论表
- `id`: 主键，自增
- `resumeId`: 简历ID，外键关联t_resume表
- `hrId`: HR用户ID，外键关联t_user表
- `comment`: 修改建议内容
- `score`: 评分 (1-10)
- `createTime`: 创建时间
- `status`: 状态 (1-有效, 0-已删除)

### 2. 服务层 (Service)

#### HrService
- HR信息的增删改查
- HR身份验证
- HR信息管理

#### SeekerService
- 求职者信息的增删改查
- 求职者搜索功能
- 求职者身份验证

#### ResumeCommentService
- 简历评论的增删改查
- 评分统计
- 评论权限控制

#### ResumeRecommendationService
- 简历推荐算法
- 简历搜索功能
- 匹配度计算

#### HrStatisticsService
- HR KPI数据统计
- 详细统计数据
- 评分分布统计

### 3. 控制器层 (Controller)

#### HrController (`/api/hr`)
- `GET /info/{userId}`: 获取HR信息
- `POST /info`: 创建HR信息
- `PUT /info/{id}`: 更新HR信息
- `PUT /info/user/{userId}`: 根据用户ID更新HR信息
- `GET /kpi/{hrId}`: 获取HR KPI数据
- `GET /stats/{hrId}`: 获取HR详细统计
- `GET /verify/{userId}`: 验证用户是否为HR

#### SeekerController (`/api/seeker`)
- `GET /info/{userId}`: 获取求职者信息
- `POST /info`: 创建求职者信息
- `PUT /info/{id}`: 更新求职者信息
- `PUT /info/user/{userId}`: 根据用户ID更新求职者信息
- `GET /search`: 搜索求职者
- `GET /membership/{membership}`: 根据会员等级获取求职者
- `GET /all`: 获取所有求职者
- `GET /verify/{userId}`: 验证用户是否为求职者

#### ResumeRecommendationController (`/api/hr/resume`)
- `GET /recommend/{hrId}`: 为HR推荐简历
- `GET /search/{hrId}`: 搜索简历

#### ResumeCommentController (`/api/resume/comment`)
- `POST /`: 创建简历评论
- `PUT /{commentId}`: 更新简历评论
- `GET /resume/{resumeId}`: 获取简历的所有评论
- `GET /hr/{hrId}`: 获取HR的所有评论
- `GET /resume/{resumeId}/score`: 获取简历平均评分
- `GET /resume/{resumeId}/hr/{hrId}`: 获取HR对特定简历的评论
- `DELETE /{commentId}/hr/{hrId}`: 删除评论
- `GET /hr/{hrId}/count`: 统计HR评论数量

## 数据库初始化

运行 `database_init.sql` 脚本来创建必要的数据库表和示例数据：

```sql
mysql -u root -p EdgeHire < database_init.sql
```

## 推荐算法

### 匹配度计算规则

1. **基础分**: 50分
2. **岗位匹配度**: 最高40分
   - 完全匹配: +40分
   - 关键词匹配: +20分
3. **学历加分**: 最高30分
   - 硕士及以上: +30分
   - 本科: +20分
   - 大专: +10分
4. **会员加分**: 最高20分
   - 高级会员: +20分

总分最高100分。

## 前端集成

前端可以使用 `frontEnd/src/api/hr.js` 中定义的API接口来调用后端服务。

### 使用示例

```javascript
import { hrApi, resumeRecommendationApi } from '@/api/hr'

// 获取HR KPI数据
const kpiData = await hrApi.getKpiData(hrId)

// 获取推荐简历
const recommendations = await resumeRecommendationApi.getRecommendations(hrId, 0, 10)
```

## 注意事项

1. **权限控制**: 确保只有HR角色的用户才能访问HR相关功能
2. **数据验证**: 所有输入数据都会进行验证
3. **错误处理**: API会返回适当的错误信息
4. **性能优化**: 使用了数据库索引来提高查询性能

## 测试建议

1. 使用Postman或类似工具测试API接口
2. 验证权限控制是否正常工作
3. 测试推荐算法的准确性
4. 检查数据库约束是否正确执行

## 后续扩展

1. 可以添加更复杂的推荐算法
2. 增加更多的统计维度
3. 实现实时通知功能
4. 添加简历收藏功能
