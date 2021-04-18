# Vagrant使用国内镜像安装插件和box镜像

## 前言

[Vagrant](https://www.vagrantup.com/)是非常优秀的本地虚拟化管理工具。无奈国内访问速度实在感人。本文分享一些如何使用国内镜像加速的经验，让 Vagrant 的使用更加爽快。

## 插件镜像

Vagrant 的插件主要托管在[RubyGems](https://rubygems.org/)仓库，在国内几乎无法访问。万幸的是国内已经有许多 RubyGems 镜像。不过 Vagrant 使用这个镜像安装插件的方法有些特殊：

```bash
vagrant plugin install --plugin-clean-sources --plugin-source https://mirrors.aliyun.com/rubygems/ <plugin>...
```

有两个地方特别需要注意：

1. `--plugin-clean-sources`这个参数容易被忽略，官方文档写的也比较模糊，必须在`DEBUG`模式下才能发现这个参数的作用，就是清理掉 Vagrant 默认使用的 Gems 仓库
2. `--plugin-clean-sources`和`--plugin-source`参数的顺序特别需要注意，必须`--plugin-clean-sources`在前，`--plugin-source`在后，才能保证先清理掉默认的 Vagrant 使用的 Gems 仓库，然后添加 RubyChina 镜像仓库。否则顺序反了的话就会把所有仓库全清掉，导致找不到插件仓库

体验一下速度，装个`vagrant-disksize`插件试试:

```bash
vagrant plugin install --plugin-clean-sources --plugin-source https://mirrors.aliyun.com/rubygems/ vagrant-disksize
```

如果使用`bash`/`zsh`之类的 shell 环境，可以考虑使用`alias`简化命令:

```bash
alias vagrant-plugin-install='vagrant plugin install --plugin-clean-sources --plugin-source https://mirrors.aliyun.com/rubygems/'
```

将以上命令添加到`~/.bashrc`(bash 环境)或`~/.zshrc`(zsh 环境)，下次打开终端即可生效。

这样以后想从镜像站安装插件只需要使用命令:

```bash
vagrant-plugin-install <plugin>...
```

方便太多了，以后可以畅快的安装 Vagrant 插件了。

## Vagrant Box 镜像

并没有统一的 Vagrant Box 镜像地址，需要独立查找。

使用 Vagrant Box 镜像的方法如下：

- 在空目录下通过命令直接初始化:

```bash
vagrant init name url
```

其中`name`为期望的虚拟机的别名，`url`指向一个`box`文件的镜像 URL。

- 已有`Vagrantfile`的情况下，编辑或添加配置项`config.vm.box_url = "box文件的url"`

我这边整理了几个常见的 box 镜像以供参考

### Ubuntu

[清华大学镜像站](https://mirrors.tuna.tsinghua.edu.cn/ubuntu-cloud-images/)，如: `https://mirrors.tuna.tsinghua.edu.cn/ubuntu-cloud-images/bionic/current/bionic-server-cloudimg-amd64-vagrant.box`

启动一个 Ubuntu 18.04 的虚拟机:

```bash
vagrant init ubuntu-bionic https://mirrors.tuna.tsinghua.edu.cn/ubuntu-cloud-images/bionic/current/bionic-server-cloudimg-amd64-vagrant.box
```

其他版本在各自的开发代号目录下都可以找到。

### CentOS

[中科大镜像站](https://mirrors.ustc.edu.cn/centos-cloud/)，如: `https://mirrors.ustc.edu.cn/centos-cloud/centos/7/vagrant/x86_64/images/CentOS-7.box`

启动一个 CentOS 7 的虚拟机:

```bash
vagrant init centos7 https://mirrors.ustc.edu.cn/centos-cloud/centos/7/vagrant/x86_64/images/CentOS-7.box
```

其他版本的镜像也可以在该目录下找到。

## 其他可能会用到的镜像

如果启用了`vagrant-vbguest`插件，可能希望通过镜像下载 Virtualbox 扩展，编辑`Vagrantfile`:

```ini
config.vbguest.iso_path = "https://mirrors.tuna.tsinghua.edu.cn/virtualbox/%{version}/VBoxGuestAdditions_%{version}.iso"
```

## 小结

本文总结了 Vagrant 可能会用到的国内镜像，通过国内镜像加速，大大提升 Vagrant 使用体验。

https://kiwenlau.com/tags/Vagrant/