## 关于 [ProcessOn](https://www.processon.com)

非常好用的思维导图网站，不仅支持思维导图，还支持流程图、原型图、UML 等。比我之前用的百度脑图强多了。

直接登录网站就可以编辑，非常适合我在图书馆公用电脑学习使用。

但是，它是付费的，免费用户只能存放 9 个文件。

本程序实现自动增加你的文件数量，理论上可以无限增加，哈哈。

效果图：

![效果图](https://upload-images.jianshu.io/upload_images/5690299-07594fd37eda1f83.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

增加到 100 个左右就够了，不要搞太多，以免引起官方注意。
请低调使用，不要涉及商业行为。

## 用法

- 安装依赖: `pip install requests bs4` 

- 在你的 `processon` 的账号中心找到你的邀请链接 `URL`。

- 运行脚本 `python processon.py URL` 。此处 `URL` 是你的邀请链接。

- 效果图：

![效果图](https://upload-images.jianshu.io/upload_images/5690299-1235dc17a96262d6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 扩充文件数思路

我发现在用户的账号中心有这样的东西：

![邀请链接](https://upload-images.jianshu.io/upload_images/5690299-8c3228ba522c1855.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

当然，可以找别人通过自己链接注册，然而，还是自己动手，丰衣足食。

我细细观察，又发现注册只需邮箱，然后它会发一条验证链接给注册邮箱，只要点击链接后就注册完成，而邀请链接的用户就可以增加 3 个文件数了！

所以，我找了一个临时邮箱网站，[https://temp-mail.org/zh/](https://temp-mail.org/zh/)，它会给你一个邮箱账号，类似 free sms online。然后拿这个邮箱账号去注册，再回到临时邮箱网站验证就可以了。

## 编程思路

### 1. 先来看看注册表单

![注册](https://upload-images.jianshu.io/upload_images/5690299-892570595b743eed.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

用户名我用随机生成的 7 位数加上邮箱后缀，密码、昵称都是随机产生的 7 位数。

```python
'email': user + domain,
'pass': str(random.randint(1000000, 9999999)),
'fullname': str(random.randint(1000000, 9999999))
```

需要注意网站通过 cookies 识别出邀请链接，所以在提交表单前需要 get(邀请链接url)，再 post 提交表单，两次请求在同一个 session，这样才能共享 cookies 。

### 2. 更改 temp mail 邮箱

![更改邮箱表单](https://upload-images.jianshu.io/upload_images/5690299-75166eb422410257.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

抓包发现：post 表单数据需要 csrf 字段。所以 post 前先用 get 方法，从响应中提取 csrf 字段值。

### 3. 获取注册验证链接

这步比较简单，在 temp mail 的「刷新」标签获取到邮件，get 请求进去，在中响应中提取出注册验证链接，最后请求注册验证链接即可。

需要注意的是注册验证邮件 temp mail 不一定马上就能收到，所以我写了个死循环，不断检测是否收到邮件，当收到邮件时才跳出。

### 4. 大更新

发现多次注册封 IP 的情况并不严重，而使用所谓的免费代理反而带来一堆问题。所以不再使用 IP 代理。

由于网站邮箱域名经常更改，所以不再写死邮箱域名，而是每次启动脚本时把邮箱域名爬下来。

引入多线程，大大提高爬取速度。