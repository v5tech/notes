# 安装你想安装的工具
sudo yum install -y git vim gcc glibc-static telnet bridge-utils

# 安装docker
curl -fsSL get.docker.com -o get-docker.sh

sh get-docker.sh

# 启动docker服务
sudo systemctl start docker

#移除安装包
rm -rf get-docker.sh