# Web.py 快速上手指南

## 安装与设置

```bash
# 安装web.py
pip install web.py

# 创建项目目录结构
mkdir myapp
cd myapp
mkdir static templates
touch app.py
```

## 基本应用结构

```python
import web

# URL路由映射
urls = (
    '/', 'Index',
    '/items', 'Items',
    '/items/(\d+)', 'Item'
)

# 初始化应用
app = web.application(urls, globals())

# 如果需要模板
render = web.template.render('templates/')

# 如果需要会话
web.config.debug = False  # 在生产环境中设置为False
session = web.session.Session(
    app, 
    web.session.DiskStore('sessions'),
    initializer={'login': 0, 'user': ''}
)

# 首页控制器
class Index:
    def GET(self):
        return render.index()

# 运行应用
if __name__ == "__main__":
    app.run()
```

## URL路由

```python
# 基本路由
'/path', 'ClassName'

# 带参数的路由
'/items/(\d+)', 'Item'  # 正则表达式捕获数字ID
'/user/(.*)', 'User'    # 捕获任意字符

# 在类中获取参数
class Item:
    def GET(self, item_id):
        # item_id 是从URL捕获的参数
        return f"查看物品 #{item_id}"
```

## 请求处理方法

```python
class MyController:
    # 处理GET请求
    def GET(self):
        # 获取查询参数
        params = web.input(name=None, age=0)
        name = params.name
        age = params.age
        return f"Hello {name}, you are {age} years old"
    
    # 处理POST请求
    def POST(self):
        # 获取表单数据
        data = web.input()
        return f"Received: {data.name}"
    
    # 处理PUT请求
    def PUT(self):
        data = web.data()  # 获取原始请求体
        return "Updated successfully"
    
    # 处理DELETE请求
    def DELETE(self):
        return "Deleted successfully"
```

## 模板渲染

```python
# 设置模板引擎
render = web.template.render('templates/', base='layout')

class Index:
    def GET(self):
        # 传递变量给模板
        return render.index(title="首页", items=["项目1", "项目2"])
```

**templates/layout.html:**
```html
$def with (content)
<!DOCTYPE html>
<html>
<head>
    <title>My App</title>
    <link rel="stylesheet" href="/static/style.css">
</head>
<body>
    <header>
        <h1>我的Web.py应用</h1>
    </header>
    <main>
        $:content
    </main>
    <footer>
        &copy; 2023
    </footer>
</body>
</html>
```

**templates/index.html:**
```html
$def with (title, items)
<h2>$title</h2>
<ul>
$for item in items:
    <li>$item</li>
</ul>
```

## 数据库连接与操作

```python
# 设置数据库连接
db = web.database(
    dbn='mysql',  # 或 'postgres', 'sqlite'
    host='localhost',
    db='myapp',
    user='username',
    pw='password'
)

# 查询数据
def get_items():
    return db.select('items', order='id DESC')

# 查询单个记录
def get_item(item_id):
    return db.select('items', where='id=$id', vars={'id': item_id})

# 插入数据
def add_item(name, description):
    return db.insert('items', name=name, description=description)

# 更新数据
def update_item(item_id, name, description):
    return db.update('items', 
        where='id=$id', 
        vars={'id': item_id}, 
        name=name, 
        description=description
    )

# 删除数据
def delete_item(item_id):
    return db.delete('items', where='id=$id', vars={'id': item_id})
```

## 表单处理

```python
# 定义表单
login_form = web.form.Form(
    web.form.Textbox('username', web.form.notnull, description="用户名"),
    web.form.Password('password', web.form.notnull, description="密码"),
    web.form.Button('登录')
)

class Login:
    def GET(self):
        form = login_form()
        return render.login(form)
    
    def POST(self):
        form = login_form()
        if not form.validates():
            return render.login(form)
        
        username = form.d.username
        password = form.d.password
        
        if check_credentials(username, password):
            session.login = 1
            session.user = username
            raise web.seeother('/')
        else:
            return render.login(form, error="用户名或密码错误")
```

### 扩展表单验证

```python
# 扩展表单验证
def validate_length(min_length=1, max_length=None):
    message = '长度必须在%d到%d之间' % (min_length, max_length or 99999)
    
    def validator(value):
        if len(value) < min_length:
            return False, '内容太短'
        if max_length and len(value) > max_length:
            return False, '内容太长'
        return True, None
    
    return validator

def validate_email(value):
    import re
    email_regex = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    if not re.match(email_regex, value):
        return False, '请输入有效的电子邮件地址'
    return True, None

# 更复杂的表单
registration_form = web.form.Form(
    web.form.Textbox('username', 
                     web.form.notnull,
                     validate_length(min_length=3, max_length=20),
                     description="用户名"),
    web.form.Textbox('email',
                     web.form.notnull,
                     validate_email,
                     description="电子邮件"),
    web.form.Password('password',
                      web.form.notnull,
                      validate_length(min_length=8),
                      description="密码"),
    web.form.Password('password2',
                      web.form.notnull,
                      description="确认密码"),
    web.form.Button('注册'),
    validators = [
        web.form.Validator("两次密码输入不一致", 
                           lambda i: i.password == i.password2)
    ]
)
```

## 文件上传处理

```python
# 文件上传表单
upload_form = web.form.Form(
    web.form.File('file', description="选择文件"),
    web.form.Button('上传')
)

class Upload:
    def GET(self):
        form = upload_form()
        return render.upload(form=form)
    
    def POST(self):
        form = upload_form()
        if not form.validates():
            return render.upload(form=form, error="请选择文件")
        
        # 获取上传的文件
        x = web.input(file={})
        
        # 检查文件是否存在
        if 'file' not in x or not x.file.filename:
            return render.upload(form=form, error="请选择文件")
        
        # 获取文件信息
        filename = x.file.filename
        file_content = x.file.file.read()
        
        # 文件类型验证
        allowed_extensions = ['.jpg', '.png', '.pdf']
        ext = os.path.splitext(filename)[1].lower()
        if ext not in allowed_extensions:
            return render.upload(form=form, error="只允许上传JPG、PNG或PDF文件")
        
        # 保存文件
        upload_dir = 'static/uploads'
        if not os.path.exists(upload_dir):
            os.makedirs(upload_dir)
        
        # 生成安全的文件名
        safe_filename = str(uuid.uuid4()) + ext
        filepath = os.path.join(upload_dir, safe_filename)
        
        with open(filepath, 'wb') as f:
            f.write(file_content)
        
        return render.upload_success(filename=filename, filepath='/'+filepath)
```

**templates/upload.html:**
```html
$def with (form, error=None)
<h2>文件上传</h2>

$if error:
    <div class="error">$error</div>

<form method="POST" enctype="multipart/form-data">
    $:form.render()
</form>
```

**templates/upload_success.html:**
```html
$def with (filename, filepath)
<h2>文件上传成功</h2>
<p>您上传的文件 <strong>$filename</strong> 已成功保存。</p>
<p>文件路径: <a href="$filepath" target="_blank">查看文件</a></p>
<p><a href="/upload">继续上传</a></p>
```

## 会话管理

```python
# 初始化会话
session = web.session.Session(
    app, 
    web.session.DiskStore('sessions'),
    initializer={'login': 0, 'user': ''}
)

# 检查用户是否登录
def logged_in():
    if session.get('login', 0) == 1:
        return True
    return False

# 登录保护装饰器
def require_login(func):
    def wrapper(*args, **kwargs):
        if not logged_in():
            raise web.seeother('/login')
        return func(*args, **kwargs)
    return wrapper

# 使用登录保护
class ProtectedPage:
    @require_login
    def GET(self):
        return "这是受保护的内容"
```

## CSRF防护

```python
# CSRF令牌生成和验证
import random
import string

def csrf_token():
    if not session.get('csrf_token'):
        session.csrf_token = ''.join(random.choice(string.ascii_letters + string.digits) for _ in range(32))
    return session.csrf_token

def csrf_protected(func):
    def decorated(*args, **kwargs):
        inp = web.input()
        if not (inp.get('csrf_token') and inp.csrf_token == session.get('csrf_token')):
            raise web.HTTPError('403 Forbidden', {'Content-Type': 'text/html'}, 
                              '跨站请求伪造验证失败')
        return func(*args, **kwargs)
    return decorated

# 在表单中使用CSRF令牌
login_form = web.form.Form(
    web.form.Textbox('username', web.form.notnull, description="用户名"),
    web.form.Password('password', web.form.notnull, description="密码"),
    web.form.Hidden('csrf_token'),
    web.form.Button('登录')
)

class Login:
    def GET(self):
        form = login_form()
        # 设置CSRF令牌
        form.csrf_token.value = csrf_token()
        return render.login(form=form, error=None)
    
    @csrf_protected
    def POST(self):
        # 处理登录逻辑...
        pass
```

## 静态文件服务

```python
# 在URL映射中添加
urls = (
    # ...其他路由
    '/static/(.*)', 'web.httpserver.StaticMiddleware'
)

# 或者在app.py中添加
if __name__ == "__main__":
    # 设置静态文件目录
    web.config.debug = False
    app.run()
```

## JSON API 支持

```python
import json

# 返回JSON数据
class ApiItems:
    def GET(self):
        items = db.select('items', order='id DESC')
        # 转换为列表字典
        items_list = [{'id': item.id, 'name': item.name, 'description': item.description} for item in items]
        # 设置Content-Type并返回JSON
        web.header('Content-Type', 'application/json')
        return json.dumps(items_list)
    
    def POST(self):
        try:
            # 获取JSON请求体
            data = json.loads(web.data().decode('utf-8'))
            # 数据验证
            if 'name' not in data or not data['name']:
                return self.error('名称不能为空')
                
            item_id = db.insert('items', name=data['name'], description=data.get('description', ''))
            web.header('Content-Type', 'application/json')
            return json.dumps({'id': item_id, 'success': True})
        except json.JSONDecodeError:
            return self.error('无效的JSON格式')
    
    def error(self, message):
        web.header('Content-Type', 'application/json')
        return json.dumps({'success': False, 'error': message})
```

## 缓存机制

```python
import time

# 简单内存缓存实现
cache = {}

def cached(expires=3600):
    """
    简单的函数缓存装饰器
    expires: 缓存过期时间（秒）
    """
    def decorator(func):
        def wrapper(*args, **kwargs):
            key = f"{func.__name__}:{str(args)}:{str(kwargs)}"
            current_time = time.time()
            
            # 检查缓存是否存在且未过期
            if key in cache and current_time - cache[key]['time'] < expires:
                return cache[key]['result']
            
            # 执行函数并缓存结果
            result = func(*args, **kwargs)
            cache[key] = {'result': result, 'time': current_time}
            return result
        return wrapper
    return decorator

# 使用缓存装饰器
@cached(expires=60)  # 缓存1分钟
def get_all_items():
    return db.select('items', order='id DESC')

# 在控制器中使用
class Items:
    def GET(self):
        items = get_all_items()
        return render.items(items=items)
```

## 重定向和HTTP状态码

```python
# 重定向
raise web.seeother('/new-url')  # 303重定向
raise web.redirect('/permanent-url')  # 301永久重定向

# 设置HTTP状态码
class NotFound:
    def GET(self):
        raise web.notfound()  # 返回404

class ServerError:
    def GET(self):
        raise web.internalerror()  # 返回500

# 自定义HTTP响应
def custom_error():
    return web.HTTPError(
        "400 Bad Request", 
        {"Content-Type": "text/html"}, 
        "参数错误"
    )
```

## 请求中间件

```python
import logging

# 中间件类
class LoggerMiddleware:
    def __init__(self, app):
        self.app = app
        self.logger = logging.getLogger('web.py')
        self.logger.setLevel(logging.INFO)
        handler = logging.FileHandler('access.log')
        formatter = logging.Formatter('%(asctime)s - %(message)s')
        handler.setFormatter(formatter)
        self.logger.addHandler(handler)
    
    def __call__(self, environ, start_response):
        # 记录请求信息
        path = environ.get('PATH_INFO', '')
        method = environ.get('REQUEST_METHOD', '')
        ip = environ.get('REMOTE_ADDR', '')
        self.logger.info(f"{ip} - {method} {path}")
        
        # 继续处理请求
        return self.app(environ, start_response)

# 添加中间件
app = web.application(urls, globals())
app.app = LoggerMiddleware(app.app)
```

## 国际化和本地化支持

```python
# 安装和导入gettext
import gettext
import os

# 设置语言
def setup_i18n(lang='zh_CN'):
    # 设置语言环境
    os.environ['LANG'] = lang
    
    # 设置翻译目录
    translations_dir = os.path.join(os.path.dirname(__file__), 'translations')
    
    # 创建翻译对象
    translation = gettext.translation('messages', translations_dir, 
                                    languages=[lang], fallback=True)
    translation.install()
    return translation.gettext

# 初始化翻译函数
_ = setup_i18n()

# 在Web.py中使用
class Index:
    def GET(self):
        # 使用翻译函数
        welcome_message = _("欢迎来到我的网站")
        return render.index(message=welcome_message)
```

## 调试与错误处理

```python
# 开启调试模式(开发环境)
web.config.debug = True

# 定义自定义错误页
def notfound():
    return web.notfound(render.notfound())

def internalerror():
    return web.internalerror(render.error())

app.notfound = notfound
app.internalerror = internalerror
```

### 详细的错误处理示例

```python
import logging

# 自定义异常类
class UserNotFoundError(Exception):
    pass

class PermissionDeniedError(Exception):
    pass

# 在控制器中使用
class UserProfile:
    def GET(self, user_id):
        try:
            # 查找用户
            users = db.select('users', where='id=$id', vars={'id': int(user_id)})
            if not users:
                raise UserNotFoundError(f"用户ID {user_id} 不存在")
            
            user = users[0]
            
            # 权限检查
            if not logged_in() and user.is_private:
                raise PermissionDeniedError("您无权查看此用户资料")
            
            return render.user_profile(user=user)
            
        except UserNotFoundError as e:
            # 返回404错误
            return web.notfound(render.error(message=str(e)))
            
        except PermissionDeniedError as e:
            # 返回403错误
            web.ctx.status = '403 Forbidden'
            return render.error(message=str(e))
            
        except Exception as e:
            # 记录未知错误并返回500
            logging.error(f"未处理的异常: {str(e)}")
            return web.internalerror(render.error(message="服务器内部错误"))
```

**templates/error.html:**
```html
$def with (message="页面发生错误")
<div class="error-container">
    <h2>出错了</h2>
    <p>$message</p>
    <p><a href="/">返回首页</a></p>
</div>
```

**templates/notfound.html:**
```html
$def with ()
<div class="not-found">
    <h2>404 - 页面未找到</h2>
    <p>您请求的页面不存在。</p>
    <p><a href="/">返回首页</a></p>
</div>
```

## 上线部署

```bash
# 使用内置服务器(不建议生产环境)
python app.py 8080

# 使用Gunicorn部署(推荐)
pip install gunicorn
gunicorn app:app.wsgifunc() --workers=4 --bind=0.0.0.0:8080

# 使用uWSGI部署
pip install uwsgi
uwsgi --http :8080 --wsgi-file app.py --callable app.wsgifunc() --processes 4
```

**Nginx配置示例:**
```nginx
server {
    listen 80;
    server_name myapp.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /static/ {
        alias /path/to/myapp/static/;
        expires 30d;
    }
}
```

## 完整应用示例

```python
import web
import os

# URL路由
urls = (
    '/', 'Index',
    '/items', 'Items',
    '/item/(\d+)', 'Item',
    '/login', 'Login',
    '/logout', 'Logout',
    '/static/(.*)', 'web.httpserver.StaticMiddleware'
)

app = web.application(urls, globals())

# 配置
web.config.debug = False
render = web.template.render('templates/', base='layout')

# 数据库连接
db = web.database(dbn='sqlite', db='myapp.db')

# 会话管理
session_store = web.session.DiskStore('sessions')
session = web.session.Session(app, session_store, initializer={'login': 0, 'user': ''})

# 表单定义
login_form = web.form.Form(
    web.form.Textbox('username', web.form.notnull, description="用户名"),
    web.form.Password('password', web.form.notnull, description="密码"),
    web.form.Button('登录')
)

item_form = web.form.Form(
    web.form.Textbox('name', web.form.notnull, description="名称"),
    web.form.Textarea('description', description="描述"),
    web.form.Button('保存')
)

# 辅助函数
def logged_in():
    return session.get('login', 0) == 1

def require_login(func):
    def wrapper(*args, **kwargs):
        if not logged_in():
            raise web.seeother('/login')
        return func(*args, **kwargs)
    return wrapper

# 控制器
class Index:
    def GET(self):
        return render.index(logged_in=logged_in(), user=session.get('user', ''))

class Items:
    @require_login
    def GET(self):
        items = db.select('items', order='id DESC')
        form = item_form()
        return render.items(items=items, form=form)
    
    @require_login
    def POST(self):
        form = item_form()
        if not form.validates():
            items = db.select('items', order='id DESC')
            return render.items(items=items, form=form)
        
        db.insert('items', name=form.d.name, description=form.d.description)
        raise web.seeother('/items')

class Item:
    @require_login
    def GET(self, item_id):
        items = db.select('items', where='id=$id', vars={'id': int(item_id)})
        if not items:
            raise web.notfound()
        item = items[0]
        form = item_form()
        form.fill(name=item.name, description=item.description)
        return render.item(item=item, form=form)
    
    @require_login
    def POST(self, item_id):
        form = item_form()
        if not form.validates():
            items = db.select('items', where='id=$id', vars={'id': int(item_id)})
            if not items:
                raise web.notfound()
            return render.item(item=items[0], form=form)
        
        db.update('items', where='id=$id', vars={'id': int(item_id)},
                  name=form.d.name, description=form.d.description)
        raise web.seeother('/items')
    
    @require_login
    def DELETE(self, item_id):
        db.delete('items', where='id=$id', vars={'id': int(item_id)})
        return "删除成功"

class Login:
    def GET(self):
        if logged_in():
            raise web.seeother('/')
        form = login_form()
        return render.login(form=form, error=None)
    
    def POST(self):
        form = login_form()
        if not form.validates():
            return render.login(form=form, error=None)
        
        # 简化的用户验证(实际应用中应使用加密密码)
        users = db.select('users', where='username=$name AND password=$pass',
                          vars={'name': form.d.username, 'pass': form.d.password})
        
        if users:
            session.login = 1
            session.user = form.d.username
            raise web.seeother('/')
        else:
            return render.login(form=form, error="用户名或密码错误")

class Logout:
    def GET(self):
        session.login = 0
        session.user = ''
        raise web.seeother('/')

# 初始化数据库(实际应用中应使用迁移脚本)
def init_db():
    # 检查数据库是否已存在
    if not os.path.exists('myapp.db'):
        db.query('''
        CREATE TABLE users (
            id INTEGER PRIMARY KEY,
            username TEXT UNIQUE,
            password TEXT
        )
        ''')
        
        db.query('''
        CREATE TABLE items (
            id INTEGER PRIMARY KEY,
            name TEXT,
            description TEXT
        )
        ''')
        
        # 添加测试用户
        db.insert('users', username='admin', password='password')

# 错误处理
def notfound():
    return web.notfound(render.notfound())

def internalerror():
    return web.internalerror(render.error())

app.notfound = notfound
app.internalerror = internalerror

if __name__ == "__main__":
    init_db()
    app.run()
```
