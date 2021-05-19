# 基于acme.sh从Let's encrypt生成免费且自动更新的SSL证书

## 0x00 前言

**通过 Let’s encrypt 可以获得 90 天免费且可续期的 SSL 证书，而利用 acme.sh 可以自动生成和更新**，就来介绍一下**配置的过程**吧。

> 当然，你还可以尝试用 Let’s Encrypt 的 `certbot` 工具来签发证书，不过要装一堆库吧，我也不记得了……

下面的内容涉及 acme.sh 的安装，证书的签发及认证，如何安装到 nginx，以及自动更新证书、更新 acme.sh 等。

## 0x01 安装 acme.sh

```bash
curl https://get.acme.sh | sh
```

或者

```bash
wget -O -  https://get.acme.sh | sh
```

执行上面的命令，它会：

- 从 GitHub 上下载 sh 脚本并执行
- 把文件解压到用户的 `~/.acme.sh`目录下
- 给命令行设置一个`acme.sh`的 alias 别名
- 最后注册一个 cron 定时任务来自动更新证书。

安装完成后要**自行重启命令行**，或者**重新加载一下`.bashrc`文件**（`source ~/.bashrc`）。

然后看一下有没有生效。

```bash
$ acme.sh -h
https://github.com/acmesh-official/acme.sh
v2.8.6
Usage: acme.sh  command ...[parameters]....
Commands:
  --help, -h               Show this help message.
  --version, -v            Show version info.
  --install                Install acme.sh to your system.
  --uninstall              Uninstall acme.sh, and uninstall the cron job.
  --upgrade                Upgrade acme.sh to the latest code from https://github.com/acmesh-official/acme.sh.
  --issue                  Issue a cert.
  --signcsr                Issue a cert from an existing csr.
  --deploy                 Deploy the cert to your server.
  --install-cert           Install the issued cert to apache/nginx or any other server.
# ......
```

## 0x02 签发 SSL 证书

签发 SSL 证书需要证明这个域名是属于你的，即**域名所有权**，一般有两种方式验证：http 和 dns 验证。

通过 acme.sh 可以签发单域名、多域名、泛域名证书，还可以签发 ECC 证书。为了简单起见，这里以**单域名证书**为例，后面再拓展一下好了。

**下面任意一种方式只要安装成功了就行！**

注意：这一步**只是生成了证书，并没有进行配置**，因此访问网站当然上不了 https。

### 2.1 HTTP 验证

这种方式 `acme.sh` 会自动在你的网站根目录下放置一个文件，来验证你的域名所有权，验证之后就签发证书，最后会自动删除验证文件。

**前提是要绑定的域名已经绑定到了所在服务器上，且可以通过公网进行访问！**

#### 2.1.1 Webroot mode

假设服务器在运行着的，网站域名为 `example.com`，根目录为 `/home/wwwroot/example.com`。那么只需要执行下面这条语句就行。

```bash
acme.sh  --issue  -d example.com  -w /home/wwwroot/example.com
```

#### 2.1.2 Apache / Nginx mode

如果用的是 Apache 或者 Nginx 服务器，可以自动寻找配置文件来进行签发。

```bash
acme.sh  --issue  -d example.com  --apache  # Apache
acme.sh  --issue  -d example.com  --nginx   # Nginx
```

如果找不到配置文件的话可以自行配置。

```bash
acme.sh  --issue  -d example.com  --nginx /etc/nginx/nginx.conf  # 指定nginx的conf
acme.sh  --issue  -d example.com  --nginx /etc/nginx/conf.d/example.com.conf  # 指定网站的conf
```

#### 2.1.3 Standalone mode

这种方式下，acme.sh 会自己建立一个服务器来完成签发。主要适合的是没有建立服务器的情况，不过其实有服务器的话只要暂时关闭，不造成端口冲突就能使用。

http 模式，80端口：

```bash
acme.sh  --issue  -d example.com  --standalone
```

如果用了反代之类的不是 80 端口，则可以手动指定。

```bash
acme.sh  --issue  -d example.com  --standalone --httpport 88
```

当然它还支持 tls 模式，不是 443 端口的话也可以自行指定。

```bash
acme.sh  --issue  -d example.com  --alpn
acme.sh  --issue  -d example.com  --alpn --tlsport 8443  # 自行指定tls端口
```

### 2.2 DNS 验证

这种方式下，不需要任何服务器，不需要任何公网 ip，只需要 dns 的解析记录即可完成验证。

比如说服务器不能直接公网访问，以及某些 VPS 直接绑定域名没备案的话是上不去的，就需要采用这种方案了。

当然，手里有域名只是想生成一个证书而已也可以这么用。

#### 2.2.1 DNS API mode

这种方式贼强大，直接可以利用域名服务商提供的 API 就可以自动帮你添加 TXT 记录完成验证和证书签发。而且60天后还可以自动完成续期。（我就是用这种方式实现哒~

比如说 CloudFlare 的，在[这里](https://dash.cloudflare.com/profile)获取你的API Key。可以用全局 API Key，将参数导入到命令行。

```bash
export CF_Token="sdfsdfsdfljlbjkljlkjsdfoiwje"
export CF_Account_ID="xxxxxxxxxxxxx"
```

为了限制权限，可以新建一个区域的 API Key. 这里只需要 Zone.DNS 的编辑权限（restrict the API Token only for write access to Zone.DNS for a single domain）就行。

```bash
export CF_Token="sdfsdfsdfljlbjkljlkjsdfoiwje"
export CF_Account_ID="xxxxxxxxxxxxx"
export CF_Zone_ID="xxxxxxxxxxxxx"
```

`Account_ID` 和 `Zone_ID` 在域名的管理页面右下方可以得到。

而后再签发证书。

```bash
acme.sh --issue --dns dns_cf -d example.com 
```

之后这些配置信息会保存到 `~/.acme.sh/account.conf` 这个文件里，在证书续期或者其他利用 CF 进行验证的时候会自动调用。

当然，国内一般用的是 DNSPod，也提供了 API，类似配置就好了。

```bash
export DP_Id="1234"
export DP_Key="sADDsdasdgdsf"
acme.sh --issue --dns dns_dp -d example.com -d www.example.com
```

*（这个其实就是多域名签发了）*

更多例子参考官方 Wiki 好了→ [How to use DNS API](https://github.com/acmesh-official/acme.sh/wiki/dnsapi)

#### 2.2.2 DNS manual mode

适合域名服务商没有提供 API 的情况，需要自己在域名配置一个 TXT 记录，且不能自动续期，每次都需要重新配置。

```bash
acme.sh  --issue  -d example.com  --dns  -d www.example.com
```

更多请参考官方教程 [DNS manual mode](https://github.com/acmesh-official/acme.sh/wiki/DNS-manual-mode)。

#### 2.2.3 DNS alias mode

如果域名服务商没有提供 API，或者是一个挺重要的域名，为了安全不希望或者不方便直接配置这个域名的解析记录，可以通过另一个没那么重要的域名（可能是专门用来签发证书的）间接进行配置。

实际上还是需要有一个域名来验证所有权啦！

更多参考官方教程 [DNS alias mode](https://github.com/acmesh-official/acme.sh/wiki/DNS-alias-mode) 好了。

### 2.3 多域名配置

多个域名签发同一张证书。只需要在验证方式之后添加多个 `-d <YourDomainHere>` 参数就行。

```bash
acme.sh --issue -d example.com -w /home/wwwroot/example.com -d www.example.com

acme.sh  --issue  -d example.com  --standalone  -d www.example.com 

acme.sh  --issue  -d example.com  --dns  -d www.example.com
```

也可以多个域名指定不同的验证方式，例如

```bash
acme.sh  --issue  \
-d aa.com  -w /home/wwwroot/aa.com \
-d bb.com  --dns dns_cf \
-d cc.com  --apache \
-d dd.com  -w /home/wwwroot/dd.com
```

### 2.4 泛域名配置

Wildcard certificates

同理，**只需要加个`\*`\**就好。不过好像**只适用于 DNS 验证**的方式。

```bash
acme.sh  --issue -d example.com  -d '*.example.com'  --dns dns_cf
```

### 2.5 签发 ECC 证书

默认签发的都是基于 RSA 密钥加密的证书，而 **ECC** (Elliptic Curve Cryptography, 椭圆曲线密码) 密钥的保密性比 RSA 更好，密钥长度更短，更能对抗量子解密等，目前现代的操作系统和浏览器都支持 ECC 证书了（Windows XP 及其之前的就算了）。

`Let's Encrypt` 提供了 ECDSA 证书的签发，且 acme.sh 也支持。

我看网上的教程基本没讲 ECC 证书的签发，这里就来整一下呗！

其实只需要加上一个以 `ec-` 为前缀的 `--keylength` 参数（或 `-k`）就可以了。理论上上面的各种验证方式都适用。

比如

```bash
acme.sh --issue -w /home/wwwroot/example.com -d example.com --keylength ec-256  # 单域名
acme.sh --issue -w /home/wwwroot/example.com -d example.com -d www.example.com --keylength ec-256  # 多域名
```

支持以下长度的证书，一般就用 `ec-256` 就行了。

> 1. **ec-256 (prime256v1, “ECDSA P-256”)**
> 2. **ec-384 (secp384r1, “ECDSA P-384”)**
> 3. **ec-521 (secp521r1, “ECDSA P-521”, which is not supported by Let’s Encrypt yet.)**

## 0x03 安装(copy)证书

签发证书成功后，需要把证书安装或者复制到真正需要的地方，如 nginx / apache 的目录下。

官方说**必须**用下面的命令来安装证书，**不能**直接用 `~/.acme.sh/`目录下的证书文件，因为那只能内部使用，且未来目录结构可能会更改。

我们只需要使用 `--installcert` 命令，指定目标位置，然后证书文件就会被 copy 到相应的位置了。

其中域名是必须的，其他参数是可选的。

有必要的话，可能需要对证书文件的所属权限进行一些设置。（我这边没有问题呢，就略了吧）

### 3.1 Nginx

```bash
acme.sh --installcert -d example.com \
--key-file       /path/to/keyfile/in/nginx/key.pem  \
--fullchain-file /path/to/fullchain/nginx/cert.pem \
--reloadcmd     "service nginx force-reload"
```

比如你可以在 nginx 的目录下新建一个 `ssl` 目录，然后把证书安装 / copy 过去。

```bash
acme.sh --installcert -d example.com \
        --key-file   /etc/nginx/ssl/example.com.key \
        --fullchain-file /etc/nginx/ssl/example.com.fullchain.cer \
        --reloadcmd  "service nginx force-reload"
```

> 这里用的是 `service nginx force-reload`, 不是 `service nginx reload`, 据测试, `reload` 并不会重新加载证书, 所以用的 `force-reload`。
>
> Nginx 的配置 `ssl_certificate` 使用 `/etc/nginx/ssl/fullchain.cer` ，而非 `/etc/nginx/ssl/.cer` ，否则 [SSL Labs](https://www.ssllabs.com/ssltest/) 的测试会报 `Chain issues Incomplete` 错误。

### 3.2 Apache

```bash
acme.sh --install-cert -d example.com \
--cert-file      /path/to/certfile/in/apache/cert.pem  \
--key-file       /path/to/keyfile/in/apache/key.pem  \
--fullchain-file /path/to/fullchain/certfile/apache/fullchain.pem \
--reloadcmd     "service apache2 force-reload"
```

在命令中的 **`reloadcmd` 参数很重要！** 这个用来指定证书更新（Renew）后执行的命令，从而使续期后的证书生效。

默认 60 天就会续期一次，上面这些参数会记录下来并自动执行。（几好，贼方便

## 0x04 生成 dhparam.pem 文件（可选）

```bash
openssl dhparam -out /etc/nginx/ssl/dhparam.pem 2048
```

这一步是为了增强 SSL 的安全性。这里生成一个更强壮的 DHE 参数。

> **前向安全性（Forward Secrecy）**的概念很简单：客户端和服务器协商一个永不重用的密钥，并在会话结束时销毁它。服务器上的 RSA 私钥用于客户端和服务器之间的 Diffie-Hellman 密钥交换签名。从 Diffie-Hellman 握手中获取的预主密钥会用于之后的编码。因为预主密钥是特定于客户端和服务器之间建立的某个连接，并且只用在一个限定的时间内，所以称作短暂模式（Ephemeral）。
>
> 使用了前向安全性，如果一个攻击者取得了一个服务器的私钥，他是不能解码之前的通讯信息的。这个私钥仅用于 Diffie Hellman 握手签名，并不会泄露预主密钥。Diffie Hellman 算法会确保预主密钥绝不会离开客户端和服务器，而且不能被中间人攻击所拦截。
>
> nginx 依赖于 OpenSSL 给 Diffie-Hellman （DH）的输入参数。不幸的是，这意味着 Diffie-Hellman Ephemeral（DHE）将使用 OpenSSL 的默认设置，包括一个用于密钥交换的1024位密钥。因为我们正在使用2048位证书，DHE 客户端就会使用一个要比非 DHE 客户端更弱的密钥交换。

更多参考[这里 Guide to Deploying Diffie-Hellman for TLS](https://weakdh.org/sysadmin.html) 吧。

## 0x05 配置 Nginx

没用过 Apache，这里只说 Nginx 好了。（其实也不怎么会用（小声bb

修改网站的 conf 配置文件，加入 SSL 的相关配置。

```nginx
server {
    server_name example.com;
    listen       443 ssl http2 default_server;
    listen       [::]:443 ssl http2 default_server;
    # ...    
    # ssl 相关配置
    ssl_certificate /etc/nginx/ssl/example.com.fullchain.cer;
    ssl_certificate_key /etc/nginx/ssl/example.com.key;
    ssl_dhparam /etc/nginx/ssl/dhparam.pem;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    ssl_ciphers '[ECDHE-ECDSA-AES128-GCM-SHA256|ECDHE-ECDSA-CHACHA20-POLY1305|ECDHE-RSA-AES128-GCM-SHA256|ECDHE-RSA-CHACHA20-POLY1305]:ECDHE+AES128:RSA+AES128:ECDHE+AES256:RSA+AES256:ECDHE+3DES:RSA+3DES';
    ssl_prefer_server_ciphers on;

    # ...
}
```

`ssl_dhparam /etc/nginx/ssl/dhparam.pem;` 是在 0x04 步骤中生成的，可选。

`ssl_ciphers `用于指定加密套件，这里采用的是 [CloudFlare 家的](https://github.com/cloudflare/sslconfig/blob/master/conf)，具体也不是很清楚 emmm。可以参考一下 Mozilla 的 Wiki：[Security/Server Side TLS](https://wiki.mozilla.org/Security/Server_Side_TLS)

更多参数可以参考 nginx 的文档：[Module ngx_http_ssl_module](https://nginx.org/en/docs/http/ngx_http_ssl_module.html)

### TLSv1.3（可选）

现在很多网站都上 **TLSv1.3** 了，证书检测的网站对于 TLSv1、TLSv1.1 都认为不安全了，Firefox 自 74.0 版本开始也完全放弃对加密协议 *TLS 1.0* 和 *TLS 1.1* 的支持了。

对于 TLSv1.3 的配置，需要安装最新版的 openssl（OpenSSL 1.1.1 built with TLSv1.3或更高），而后重新编译 nginx，是有点麻烦这里懒得弄了，后面需要再折腾吧。

### 开启 HSTS（可选）

当然，为了更加安全，**可以选择开启 HSTS**（HTTP Strict Transport Security，HTTP严格传输安全协议），强制浏览器通过 https 进行访问。需要在 location 下的设置中加入一个 header。

```nginx
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload";
```

接下来的一年（即31536000秒）中，浏览器看到 header 中包含 `Strict-Transport-Security` 的话就会自动切换到 https。

但是在首次访问网站时如果被劫持了，浏览器还是可能会通过 HTTP 明文传递信息。为此，Chrome 维护了一个 HSTS preload list，内置在浏览器中，对于 Chrome, Firefox, Opera, Safari, IE 11 and Edge 等主流浏览器也适用。可以[在这里](https://hstspreload.org/)提交你的域名到这个列表里。（不过提交之前要考虑好，全站上 https 噢

如果 TLS 证书无效或不可信，用户不能忽略浏览器警告继续访问网站。这就是前几天访问 GitHub (Pages) 等网站被拦下来的原因了。

------

上面的配置完成后检查一下配置是否正确，而后重启 nginx。

```bash
nginx -t
systemctl restart nginx
```

之后就可以试一下能不能通过 https 来访问自己的网站啦！

**到这里 SSL 配置就告一段落了，下面是一些 acme.sh 的维护相关的了。**

## 0x06 更新证书

证书的有效期为 90 天，acme.sh 会 60 天更新（Renew）一次。

在安装 acme.sh 的时候就自动配置了一条 cron 任务了，会每天检查证书的情况。当然可以到 crontab 里看一下。

```bash
# crontab -l
43 0 * * * "/root/.acme.sh"/acme.sh --cron --home "/root/.acme.sh" > /dev/null
```

也可以试着用上面这条命令执行看一下相关的配置是否正确。

强制更新可以这样。

```bash
acme.sh --renew -d example.com --force  
acme.sh --renew -d example.com --force --ecc  # 如果用的是ECC证书
```

## 0x07 停止更新证书

查看证书列表

```bash
acme.sh --list
```

停止 Renew

```bash
acme.sh --remove -d example.com [--ecc]
```

之后手动把目录下的证书移除就行。

## 0x08 升级 acme.sh

```bash
acme.sh --upgrade    # 手动升级
acme.sh --upgrade --auto-upgrade    # 自动升级
acme.sh --upgrade --auto-upgrade 0  # 停止自动升级
```

## 0xFF 小结

除了上面这些配置之外，acme.sh 还提供了通知提醒，可以调用其他 API 来推送提醒，具体参考官方Wiki：[notify](https://github.com/acmesh-official/acme.sh/wiki/notify)。


```
来源: MiaoTony's小窝
文章作者: MiaoTony
文章链接: https://miaotony.xyz/2020/03/28/Server_IssueACertWithACME/
本文章著作权归作者MiaoTony所有，任何形式的转载都请注明出处。
```