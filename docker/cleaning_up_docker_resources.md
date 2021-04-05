# 清理 Docker 资源

docker prune 命令

 ```bash
docker system prune

WARNING! This will remove:
        - all stopped containers
        - all networks not used by at least one container
        - all dangling images
        - all build cache
 ```



```bash
docker system prune --all --force --volumes

WARNING! This will remove:
        - all stopped containers
        - all networks not used by at least one container
        - all volumes not used by at least one container
        - all images without at least one container associated to them
        - all build cache
```



```bash
docker container prune # Remove all stopped containers
docker volume prune # Remove all unused volumes
docker image prune # Remove unused images
```



```bash
docker container stop $(docker container ls -a -q) && docker system prune --all --force --volumes
```



```bash
docker container rm $(docker container ls -a -q) # Containers 
docker image rm $(docker image ls -a -q) # Images 
docker volume rm $(docker volume ls -q) # Volumes
docker network rm $(docker network ls -q) # Networks
```



```bash
alias docker-clean-unused='docker system prune --all --force --volumes'
alias docker-clean-all='docker stop $(docker container ls -a -q) && docker system prune --all --force --volumes'
alias docker-clean-containers='docker container stop $(docker container ls -a -q) && docker container rm $(docker container ls -a -q)'
```



列出 Docker常用资源

```bash
docker container ls # list containers, also can be shown with docker ps
docker image ls # list images, there is also the command docker images
docker volume ls # list volumes
docker network ls # lists networks
docker info # lists the number of containers and image, as well as system wide information regarding the Docker installation
```



参考文档

https://hackernoon.com/clean-out-your-docker-images-containers-and-volumes-with-single-commands-b8e38253c271