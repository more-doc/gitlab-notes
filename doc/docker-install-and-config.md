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