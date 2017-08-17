## Docker 安装与配置(Ubuntu16)
> 此文档内容基本都是来自[官方的文档](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu)和网上资料.

由于历史原因,docker 旧版本比较多,安装包名称最开始是 docker.io,后来docker-engine,目前是docker-ce和docker-ee (分别是开源版和企业版)

###  安装过程
```
#  Older versions of Docker were called docker or docker-engine. If these are installed, uninstall them:
$ sudo apt-get remove docker docker-engine docker.io
$ sudo apt-get update

# Install packages to allow apt to use a repository over HTTPS:
$ sudo apt-get install  apt-transport-https  ca-certificates  curl  software-properties-common
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

# for AMD64(x64)
$ sudo add-apt-repository  "deb [arch=amd64] https://download.docker.com/linux/ubuntu  $(lsb_release -cs) stable"
$ sudo apt-get update
$ sudo apt-get install docker-ce

# test 
$ sudo docker run hello-world
```
> 以上命令的说明:
> - 为了使用最新的版本,先卸载了所有**可能存在**的旧版本
> - 安装的是64位社区版(docker-ce)

### 配置

#### 允许非root用户使用docker

docker 命令需要使用 Unix socket 与 Docker 引擎通讯.而只有 root 用户和 docker 组的用户才可以访问 Docker 引擎的 Unix socket.

```
# 给docker用户组赋予当前用户
sudo usermod -a -G docker $USER
```
> - 重新登录后生效


#### Docker镜像仓库

由于不能说的原因,第一件事情就是要设置dcoker仓库的镜像,经过测试可用的公共镜像仓库有:

- Docker 官方的国内镜像 https://registry.docker-cn.com 
- UTSC https://docker.mirrors.ustc.edu.cn 

需要在配置文件daemon.json中添加镜像仓库的信息
```
{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn","https://registry.docker-cn.com"]
}
```

> 有的网上的资料是通过修改命令行参数(`--registry-mirrors`)来设置,这是比较老的方法了,新版的 Docker 使用 /etc/docker/daemon.json(Linux)或者 %programdata%\docker\config\daemon.json(Windows) 来配置 Daemon

以下是一个典型的daemon.json文件(ubuntu 16.04 amd64)
```
{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn","https://registry.docker-cn.com"],
  "insecure-registries":["192.168.0.222:9081","192.168.0.222:9091"],
  "storage-driver": "overlay2"
}
```


### Docker 基本概念
Docker 常用属于有
- 镜像(Image)
- 容器(Container)
- 仓库(Repository)

#### 镜像(Image)
镜像是一个文件系统(可以理解为某种压缩包格式),存放的是运行时所需要的静态资源.

#### 容器(Container)
镜像跑起来就是容器.容器可以被创建,启动,停止,删除,暂停等.

#### 仓库(Repository)
仓库是对镜像进行集中存储地方,而负责提供这种仓库服务的叫`Docker Registry`.
Docker Registry 中可以包含多个仓库(Repository).每个仓库可以包含多个标签(Tag),每个标签对应一个镜像.

通常,一个仓库会包含同一个软件不同版本的镜像,而标签就常用于对应该软件的各个版本.我们可以通过 <仓库名>:<标签> 的格式来指定具体是这个软件哪个版本的镜像.如果不给出标签,将以 latest 作为默认标签.

### 常用命令

- help: 如 docker help build
- build: 从一个 Dockerfile 创建一个镜像
- exec: 在运行的容器内执行命令
- info: 显示一些相关的系统信息
- ps: 列出主机上的容器
- restart: 重启一个运行中的容器
- start: 启动一个容器
- stats: 输出(一个或多个)容器的资源使用统计信息
- stop: 终止一个运行中的容器
- top: 查看一个容器中的正在运行的进程信息
- run: 创建一个新容器，并在其中运行给定命令

### Docker 命令图

> 网上找到的Docker 命令图

![cmd_logic.png](resources/img/cmd_logic.png)

### 常用的运维命令

#### 查看日志
```
journalctl -u docker.service
```
> see [read-the-logs](https://docs.docker.com/engine/admin/#read-the-logs)

#### 镜像/容器清理
删除所有Exit状态的容器
```
docker ps -q -f status=exited | xargs --no-run-if-empty docker rm
```

删除所有没有打tag的镜像
```
docker rmi $(docker images | grep “^<none>” | awk ‘{print $3}’)
```
删除所有没有使用的镜像
```
docker images -q -f dangling=true | xargs --no-run-if-empty docker rmi
```

Docker [1.13.0](https://github.com/docker/docker/blob/master/CHANGELOG.md#1130-2016-12-08) 及其以上,可以使用以下命令
```
docker container prune   # Remove all stopped containers
docker volume prune      # Remove all unused volumes
docker image prune       # Remove unused images
docker system prune      # All of the above, in this order: containers, volumes, images
docker system df         # Show docker disk usage, including space reclaimable by pruning
```

#### 进入容器的shell

如果容器正在运行
```
docker exec -it $CONTAINER_ID /bin/bash
```
直接启动一个新的容器
```
docker run -it --entrypoint '/bin/sh' $YOUR_DOCKER_IMAGE

```

#### 加代理
创建`/etc/systemd/system/docker.service.d/http-proxy.conf`文件(如果不存在)
```
mkdir -p /etc/systemd/system/docker.service.d
# echo "" > /etc/systemd/system/docker.service.d/http-proxy.conf
```

在`/etc/systemd/system/docker.service.d/http-proxy.conf`中添加代理环境变量
```
[Service]
Environment="HTTP_PROXY=http://192.168.0.164:8388" "HTTPS_PROXY=http://192.168.0.164:8388" "NO_PROXY=192.168.0.222:9081,192.168.0.222:9091,localhost,127.0.0.1,192.168.0.220"
```
> 后面的 `NO_PROXY` 是指定哪些地址不使用代理

重启docker服务
```
sudo systemctl daemon-reload
sudo systemctl restart docker
```

验证设置是否生效
```
systemctl show --property=Environment docker
```
> 应该会显示`Environment=HTTP_PROXY=http://proxy.example.com:80/`

参考资料: [https://docs.docker.com/engine/admin/systemd/#httphttps-proxy](https://docs.docker.com/engine/admin/systemd/#httphttps-proxy)


#### Docker 引擎服务
```
service docker start
service docker stop
service docker restart
service docker status
```

### 一些有用的工具

- `ctop`: 一个类似与top命令的工具,[地址](https://github.com/bcicen/ctop)
