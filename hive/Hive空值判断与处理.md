# Hive空值判断与处理

`Hive`中空值判断基本分两种

（1）`NULL` 与` \N`
`Hive`在底层数据中如何保存和标识`NULL`，是由 `alter table name SET SERDEPROPERTIES('serialization.null.format' = '\N');` 参数控制的

比如：

1.设置 `alter table name SET SERDEPROPERTIES('serialization.null.format' = '\N'); `
则：底层数据保存的是`'\N'`,通过查询显示的是`'NULL'`
这时如果查询为空值的字段可通过 语句：`a is null` 或者 `a='\\N'`

2.设置 `alter tablename SET SERDEPROPERTIES('serialization.null.format' = 'NULL');`
则：底层数据保存的是`'NULL'`,通过查询显示的是`'NULL'`
这时如果查询为空值的字段可通过 语句：`a is null` 或者 `a='NULL'`

（2）`''` 与 `length（xx）=0`
`''` 表示的是字段不为`null`且为空字符串，此时用 `a is null` 是无法查询这种值的，必须通过 `a=''`  或者 `length(a)=0` 查询 