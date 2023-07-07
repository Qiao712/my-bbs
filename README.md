### 论坛系统
* 贴子发表: 
  * 支持富文本，可插入图片
  * 贴子热度排序
  * 贴子全文搜索
* 评论回复功能: 支持两级评论
* 点赞功能: 贴子评论点赞
* 消息通知
* 用户私信
* 后台管理:
  * 贴子、用户、评论、板块管理
  * RBAC 权限管理
* 统一的文件管理，过期图片定期删除

### 重点考虑/待优化的点：
* 消息推送、站内消息系统
* 实时聊天
* 点赞、浏览量、热榜优化
* 接口缓存、热点数据缓存
* 关注者动态推送

### 演示
* [前台](http://8.141.151.176)
* [后台](http://8.141.151.176/admin)
* [前端GitHub仓库](https://github.com/Qiao712/my-bbs-web)

### 技术栈
* Spring Boot 2.7.2
* MyBatis Plus
* 阿里云OSS
* MySQL 8
* ElasticSearch 7.17.5
* Redis 6.2.6
* Kafka 3.3.1

## 功能列表
### 发帖系统
* 问题
  * [x] 发布（支持富文本编辑，插入图片）
    * [x] 图片数量限制
    * [x] 富文本内容XSS防御(在前端进行过滤)
    * [ ] 临时图片的使用、管理
    * [ ] 标签和样式的限制(前端)
  * [x] 浏览
  * [x] 删除
  * [x] 管理员管理页面(后台管理)
  * [x] 搜索
  * [x] 点赞
    * [x] **使用Redis优化**
  * [x] **贴子排序**
    * [x] **贴子热度值计算**
    * [ ] **优化贴子热度值计算**
    * [ ] 优化浏览量记录
  * [ ] *审核(后台管理)*
  
* 评论/回答
  * [x] 发布
    * [x] 图片数量限制
    * [ ] 临时图片的使用、管理
  * [x] 查询
  * [x] 删除
  * [x] 管理员删除(后台管理)
  * [x] 点赞
  * [ ] *审核(后台管理)*

* 板块
  * [x] 创建(后台管理)
  * [x] 编辑
  * [x] (前端)板块logo设置
  * [ ] 删除
  * [ ] 设置版主
  * [ ] 贴子顶置
  * [ ] 贴子加精

### 用户系统

- [x] 普通用户注册
- [x] 头像
  - [x] 获取
  - [x] 修改
- [x] **角色管理**
  - [x] **角色权限分配**
  - [ ] 优化完善权限分配
- [ ] *用户管理*
  - [x] 用户列表
  - [x] 角色列表
  - [ ] 当前已登录角色列表
- [x] 登录
  - [ ] 微信登录
  - [ ] 短信登录
- [ ] 邮箱绑定
- [ ] 等级积分
- [ ] 关注
- [ ] 贴子收藏

### 通知系统
- [x] 私信
* 系统通知
  - [x] 回复
  - [ ] 关注者动态
  - [ ] 系统相关
  - [ ] 管理员手动发送通知
    - [ ] **消息广播**

### 其他后台管理
* 统计信息
  * [x] 浏览量统计
  * [x] 发帖量统计
  * [x] 评论量统计

* 系统设置
  * [x] 首页走马灯广告
  * [ ] 论坛名称设置
  * [x] 论坛logo设置

- [x] 文件列表

### 其他
- [ ] 用户文件上传限制

### 
* 我：
  * 基础框架
  * 权限系统
    * AuthenticationService
    * UserService
    * RoleService
* 通神：
  * 答案评论编辑发布浏览
    * AnswerService
    * CommentService
    * LikeService
* 清哥：
  * 问题编辑发布浏览
    * StaticsService
    * QuestionService
* 杰瑞：
  * 私聊
    * ChatService
  * 消息提示
    * MessageService
* 分神：
  * 搜索
    * SearchService
  * 文件上传系统OSS
    * FileService

* 猪哥
  * 分类管理（后端接口）
    * CategoryService
  * 
---------

* 问题编辑发布浏览
  * StaticsService
  * QuestionService
  * CategoryService
* 点赞系统
  * LikeService
* 答案评论编辑发布浏览
  * AnswerService
  * CommentService
* 私聊
  * ChatService
* 消息提示
  * MessageService
* 搜索
  * SearchService
* 文件上传系统OSS
  * FileService