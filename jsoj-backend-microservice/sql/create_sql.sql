# 数据库初始化


-- 创建库
create database if not exists jsoj;

-- 切换库
use jsoj;

-- 用户表
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userPassword varchar(512)                         not null comment '密码',
    userPhone    varchar(16)                          null comment '手机号',
    userEmail    varchar(256)                         null comment '用户邮箱',
    unionId      varchar(256)                         null comment '微信开放平台id',
    mpOpenId     varchar(256)                         null comment '公众号openId',
    userName     varchar(128)                         null comment '用户昵称',
    userAvatar   varchar(1024)                        null comment '用户头像',
    userProfile  varchar(512)                         null comment '用户简介',
    userRole     varchar(8) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint    default 0                 not null comment '是否删除'
)
    comment '用户' collate = utf8mb4_unicode_ci;

create table question
(
    id          bigint auto_increment comment 'id'
        primary key,
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    answer      text                               null comment '题目答案',
    submitNum   int      default 0                 not null comment '题目提交数',
    acceptedNum int      default 0                 not null comment '题目通过数',
    judgeCase   text                               null comment '判题用例（json 数组）',
    judgeConfig text                               null comment '判题配置（json 对象）',
    thumbNum    int      default 0                 not null comment '点赞数',
    favourNum   int      default 0                 not null comment '收藏数',
    userId      bigint                             not null comment '创建用户 id',
    userStatus  tinyint  default 0                 not null comment '用户-题目状态（0-未解决）',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '题目' collate = utf8mb4_unicode_ci;

create index idx_userId
    on question (userId);

# 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(128)                       not null comment '编程语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    status     int      default 0                 not null comment '判题状态（0-待判题、1-判题中、2-成功、3-失败）',
    questionId bigint                             not null comment '题目Id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目提交表';

# 用户-题目状态表
create table user_question_status
(
    id             int auto_increment comment '主键'
        primary key,
    userId         bigint                             not null comment '用户Id',
    questionId     bigint                             not null comment '题目Id',
    questionStatus tinyint                            null comment '该用户与这道题的状态(1-尝试过  2-已解决)',
    createTime     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint user_question_status_ibfk_1
        foreign key (userId) references user (id),
    constraint user_question_status_ibfk_2
        foreign key (questionId) references question (id)
)
    comment '用户-题目状态表';

create index questionId
    on user_question_status (questionId);

create index user_question_status_userId_index
    on user_question_status (userId)
    comment '用户id索引';