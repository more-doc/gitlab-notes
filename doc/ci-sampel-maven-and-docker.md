## 构建脚本例子-构建maven项目和Docker镜像

### 项目描述
标准的spring boot 项目(由[Spring Initializr](http://start.spring.io/)生成的maven项目)



### 构建脚本的任务 
构建任务主要分两部分
- maven 项目的构建
- 处理docker 镜像(将maven项目构建出来的内容打包成docker 镜像,上传到私有docker 仓库)

### 脚本内容

`Dockerfile`
```
# docker build --build-arg VAR1=XX --build-arg VAR2=XX

FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ARG PRE_BUILD_JAR_URL
ADD $PRE_BUILD_JAR_URL  /example.jar
ENV JAVA_OPTS="-Xms50m -Xmx1024m"
EXPOSE 18081
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /example.jar --server.port=18081" ]
```

`.gitlab-ci.yml`
```
image: maven:3.5.0-ibmjava-8

variables:
  DOCKER_DRIVER: "overlay2"
  DOCKER_REGISTRY: "192.168.0.222:9081"
  DOCKER_REGISTRY_USER: "xd-developer"
  DOCKER_REGISTRY_PASS: "123456"
  DOCKER_IMAGE_NAME: "demo-jave-app"
  JAR_FILE_NAME: "example.jar"
  UPLOADED_JAR: "http://scm-server/chenjun/ci-example-docker/builds/artifacts/${CI_COMMIT_REF_NAME}/raw/target/example.jar?job=mvn_build"

#services:
#  - mysql:latest


stages:
  - maven-build
  - maven-test
  - maven-deploy
  - docker-build
  - docker-push

before_script:
  - chmod a+x ./mvnw
  - echo ${UPLOADED_JAR}
#  - echo "$DOCKER_AUTH_CONFIG"
#  - ping -c 4 scm-server
# see also :  https://docs.gitlab.com/ce/ci/docker/using_docker_images.html#define-an-image-from-a-private-container-registry
#  - docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" $CI_REGISTRY

# artifacts url is  https://example.com/<namespace>/<project>/builds/artifacts/<ref>/download?job=<job_name>
mvn_build:
  stage: maven-build
  script:
    - ./mvnw clean package
  artifacts: 
    name: "${CI_JOB_NAME}_${CI_COMMIT_REF_NAME}"
    paths:
      - target/*.jar
    expire_in: 1 week 
  tags:
    - docker-exec

mvn_test:
  stage: maven-test
  script:
    - ./mvnw test
  tags:
    - docker-exec

mvn_deploy:
  stage: maven-deploy
  when: manual
  script:
    - echo "TODO - run mvnw deploy"
  tags:
    - docker-exec
  #only:
  #  - tags


build-docker-master:
  image: gitlab/dind
  stage: docker-build
  when: manual
  script:
    - docker version
    #- docker info
    #- docker build -t <my.container.registry.io>/<my_app>:<my_tag> .
    - docker build --build-arg PRE_BUILD_JAR_URL=$UPLOADED_JAR -t $DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$CI_COMMIT_REF_SLUG  .
  only:
    - master
  tags:
    - docker-exec

push-docker:
  image: gitlab/dind
  stage: docker-push
  when: manual
  script:
    - docker info
    - docker login -u $DOCKER_REGISTRY_USER -p $DOCKER_REGISTRY_PASS $DOCKER_REGISTRY
    - docker build --build-arg PRE_BUILD_JAR_URL=$UPLOADED_JAR -t $DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$CI_COMMIT_REF_SLUG .
    - docker push $DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$CI_COMMIT_REF_SLUG
  except:
    - master
  tags:
    - docker-exec



```

> `docker-exec` 是gitlab runner 注册时使用的tag

docker 镜像构建成功后,就可以从私有仓库中拉取出来运行了
```
docker run -p 18083:18080 192.168.0.222:9081/demo-jave-app:docker-push-test
```
或者在后台运行docker 容器

```
docker run -d -p 18083:18080 192.168.0.222:9081/demo-jave-app:docker-push-test
```

> 此处通过-p host_port:container_port 来将容器中的18080端口映射到主机的18083端口,因此通过浏览器访问时,端口应该填 18083 . 