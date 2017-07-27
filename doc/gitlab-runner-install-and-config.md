## GitLab Runner 的安装和配置
> GitLab Runner 是 GitLab自家开发的CI工具,使用GO语音编写,能够在所有操作系统上运行,虽然GitLab也支持其他一些知名的CI系统集成,但是GitLab Runner 肯定是集成效果最好的.

在CI执行过程中,GitLab Runner扮演的是代理角色,它负责与GitLab通讯,接收服务器的指令,然后在本机执行相关的动作(命令)来完成远程构建任务.
在同一个系统中可以注册多个Runner,注册的时候需要选择Runner 以哪种方式来执行构建任务(称之为executor),一般使用shell或者docker:
- shell: 这是最简单的,你在构建脚本里面写构建命令(比如 mvn package),然后Runner 就在自己的环境下执行这些命令.不过这种executor不是很灵活,比如它运行在windows上,构建脚本里面就不能写linux命令.
- docker: 这是推荐的方式,构建指令在docker容器中执行.由于构建脚本里面需要先声明自己需要使用的docker镜像,因此构建命令使用非常灵活,尤其实用于那些需要在各自平台下进行构建的场景.


### 安装 GitLab Runner 
> 安装方式有多种,见 [官方文档](https://docs.gitlab.com/runner/install/) ,这里只说Ubuntu 16 下的安装

要搭建一个GitLab Runner的环境,有两种方式
- 直接运行GitLab Runner 原生应用(gitlab-ci-multi-runner)
- 在docker容器中运行GitLab Runner

安装好GitLab Runner软件后,还需要注册

> 注册的含义是
> - 在宿主环境(运行GitLab Runner的环境,如docker容器)中保存相关运行参数.
> - 在GitLab 服务器中登记(这是通过url和token实现连通和合法性检查)


#### 使用GitLab Runner 原生应用

1. 添加软件包仓库
```
# For Debian/Ubuntu
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-ci-multi-runner/script.deb.sh | sudo bash
```

2. 安装
```
# For Debian/Ubuntu
sudo apt-get install gitlab-ci-multi-runner
```
3. 注册
```
# 加sudo可以让gitlab-runner 以服务方式运行
sudo gitlab-runner register
```

#### 使用GitLab Runner 的 Docker 容器

> 首先需要安装docker,参考[Docker 安装与配置](docker-install-and-config.md)

1. 安装
```
#1）拉取gitlab-runner镜像
sudo docker pull gitlab/gitlab-runner:latest
#2）添加gitlab-runner container
sudo docker run -d --name gitlab-runner --restart always -v /srv/gitlab-runner/config:/etc/gitlab-runner -v /var/run/docker.sock:/var/run/docker.sock  gitlab/gitlab-runner:latest
```
> - 使用 -v 标记来创建一个数据卷并挂载到容器里 ,格式是 [主机路径:容器中的路径 ]
> - see https://docs.gitlab.com/runner/install/docker.html

2. 注册
```
sudo docker exec -it gitlab-runner gitlab-ci-multi-runner register
```

### GitLab Runner注册信息说明

有以下信息需要特别注意
- url: GitLab服务器的url,正确设置才能与服务器通讯
- token : GitLab 服务器产生的token,在GitLab管理员界面或者项目管理界面可以找到,但是用途不同,前置是用于公共Runner的,后者是项目私有的Runner.
- tag:标签,构建脚本通过设置标签`tags`来选择那个Runner执行构建任务,因此最好定义清晰,明确的标签
- executor: 此Runner运行方式,见本文档前面的说明. 注意,这和此Runner在物理机运行还是Docker容器中运行没有关系. 


> 参考 https://docs.gitlab.com/runner/register/index.html

### GitLab Runner 配置文件
GitLab Runner 的配置文件是`config.toml`,位于`/etc/gitlab-runner/`.
> 如果是使用GitLab Runner Docker容器,根据该容器的运行参数`-v /srv/gitlab-runner/config:/etc/gitlab-runner` 可知,容器中的`/etc/gitlab-runner`是挂载到物理机的`/srv/gitlab-runner/config`,因此,在`/srv/gitlab-runner/config`下可以找到`config.toml`文件.

**典型的config.toml文件内容(docker executor)**
```
concurrent = 1
check_interval = 0

[[runners]]
  name = "ubuntu-docker-exector"
  url = "http://192.168.0.221/"
  token = "3dde4b695b914b50f37256093fc924"
  executor = "docker"
  [runners.docker]
    tls_verify = false
    image = "docker:latest"
    privileged = true
    disable_cache = false
    cache_dir = "cache"
    volumes = ["/var/run/docker.sock:/var/run/docker.sock","/cache","/home/cj/.m2:/root/.m2:rw"]
    extra_hosts = ["scm-server:192.168.0.221"]
    shm_size = 0
  [runners.cache]

```

> 利用配置文件,可以解决一些实际的问题,见`常见问题`部分

### 常见问题

#### 无法解析主机名(docker executor)
容器的运行环境不能解析主机名,需要在在Runner的配置文件中添加extra_hosts参数(主机名:IP)来解决,见前面的配置文件样本.

#### 报错 Cannot connect to the Docker daemon(docker executor)
出现以下错误信息
```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```

这个问题一般发生在使用DinD镜像时(DinD = Docker in Docker,即在docker中运行docker) ,也就是ci runner 是在docker中运行,又要使用docker 命令.参考 [issue 1986](https://gitlab.com/gitlab-org/gitlab-ci-multi-runner/issues/1986).

关键是:
- 修改gitlab-runner的配置文件: privileged = true,挂载/var/run/docker.sock
- 修改docker 的运行参数,使用 overlay或者overlay2 驱动,[见Docker 手册](https://docs.docker.com/engine/userguide/storagedriver/overlayfs-driver/#configure-docker-with-the-overlay-or-overlay2-storage-driver).注意,这会丢失所有已经创建的容器.

#### 使用私有docker仓库(docker executor)
如果要使用私有仓库,有两种手段:
- 在构建脚本中自己执行docker login 命令,但这样密码就泄漏了.
- 在项目中设置DOCKER_AUTH_CONFIG变量,这个变量包含登录凭据,具体参考[define-an-image-from-a-private-container-registry](https://docs.gitlab.com/ce/ci/docker/using_docker_images.html#define-an-image-from-a-private-container-registry)

#### docker login 报错:https请求收到的是http响应(docker executor)
有时候构建脚本需要中执行docker login命令登录私有仓库,而私有仓库不是https就会出现这个问题,解决办法是修改 docker 配置文件(/etc/docker/daemon.json) ,加上: "insecure-registries":["服务器地址:端口"]
