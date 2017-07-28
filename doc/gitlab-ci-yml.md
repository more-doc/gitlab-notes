## 构建脚本

构建脚本的名字必须是 `.gitlab-ci.yml`,并且放在项目根目录下(和`.git`文件夹同目录).

### 注意事项

### 变量取值
脚本中定义的变量在runner端执行时,都是以环境变量形式存在的,因此在使用上需要注意这个问题,比如runner的构建环境是windows命令行,那么应该使用`%`符号来引用变量的值
```
script:
  - echo %MySecret%
```
如果runner的构建环境是linux命令行,那么应该使用`$`符号来引用变量的值
```
script:
  - echo $MySecret
```

关于变量的更详细说明见[官方说明](https://docs.gitlab.com/ce/ci/variables/README.html)

### 变量嵌套
如果需要定义一个变量,它的值需要引用另外一个变量的值,可以使用两个`$`符号
```
variables:
  LS_CMD: 'ls $FLAGS $$TMP_DIR'
  FLAGS: '-al'
script:
  - 'eval $LS_CMD'  # will execute 'ls -al $TMP_DIR'
```

### 制品(artifacts)
在构建脚本中的制品任务主要用于上传构建出来的对象(制品)

这个任务将当前路径下的mycv.pdf上传,保存1周.
```
pdf:
  script: xelatex mycv.tex
  artifacts:
    paths:
    - mycv.pdf
    expire_in: 1 week 
```
上传的文件可以在项目的Pipeline界面中看到.

如果需要手动下载,其URL的规则是:
- 下载整个artifacts 
```
https://example.com/<namespace>/<project>/builds/artifacts/<ref>/download?job=<job_name>
```
- 下载artifacts中的某一个文件
```
https://example.com/<namespace>/<project>/builds/artifacts/<ref>/raw/<path_to_file>?job=<job_name>
```
> 注意,artifacts 在服务器上是以压缩包形式存在的,因为一个项目构建出来的制品不一定是一个单一的文件.

### 例子(maven项目,docker构建)

[ci-sampel-maven-and-docker](ci-sampel-maven-and-docker.md)