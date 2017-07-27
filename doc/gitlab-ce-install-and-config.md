## GitLab CE 的安装和配置

> GitLab 分社区版(免费的开源版本)和企业版,社区版叫`GitLab CE`,企业版叫`GitLab EE`

### 安装
根据不同的平台(操作系统),安装方法有细微差别,以下以Ubuntu 16.04 为例,步骤如下

1. 安装依赖包和添加GitLab 软件包仓库
```
sudo apt-get install curl openssh-server ca-certificates postfix -y
curl -sS https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.deb.sh | sudo bash
```

2. 安装GitLab CE
```
sudo apt-get install gitlab-ce
```

3. 启动
```
# 因为是新安装,需要执行gitlab-ctl reconfigure来应用配置参数,并启动
sudo gitlab-ctl reconfigure
# 启动完成后,就可以通过浏览器访问了.
```

> - 其他平台的安装参考: https://about.gitlab.com/installation/
> - GltLab 不支持Windows
> - 默认的管理员账号是root,第一次访问时,系统会要求重置密码,在界面上输入新的密码即可.

### 配置和维护

#### 常用的命令
```
#重新配置
sudo gitlab-ctl reconfigure
#重启(包含所有模块)
sudo gitlab-ctl restart
#重启某一个模块(如nginx)
sudo gitlab-ctl restart nginx
#查看状态
sudo gitlab-ctl status
```
> 关于重新配置(reconfigure)命令:一般用于参数配置文件发生变化的时候,使用这个命令来使得参数的变化生效.

#### 更新

直接使用安装命令,此命令会检查旧版本,并且更新到新版本
```
sudo apt-get install gitlab-ce
```


### 常见问题

1. root 用户密码忘记
可以直接登录控制台来修改密码(root 用户的id是1)
```
gitlab-rails console production
# 也可以 user = User.where(id: 1).first
user = User.find_by(email: 'admin@local.host')
user.password = 'secret_pass'
user.password_confirmation = 'secret_pass'
user.save!
```

2. 访问gitlab 报 502 错误
有可能是因为gitlab的默认端口被占用了,典型的就是8080端口被占用.

