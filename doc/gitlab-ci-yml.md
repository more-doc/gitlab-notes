## 构建脚本()

### 例子(maven项目,docker构建)

```
# Official docker image.
image: maven:3.5.0-ibmjava-8

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_REGISTRY: 192.168.0.222:9081
  APP_NAME: demo-jave-app

#services:
#  - docker:dind


stages:
  - maven-build
  - maven-test
  - maven-deploy
  - docker-build
  - docker-push

before_script:
  - chmod a+x ./mvnw
#  - ping -c 4 scm-server
# see also :  https://docs.gitlab.com/ce/ci/docker/using_docker_images.html#define-an-image-from-a-private-container-registry
#  - docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" $CI_REGISTRY


mvn_build:
  stage: maven-build
  script:
    - ./mvnw clean install
  only:
    - develop
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
    - echo "deploy over..."
  tags:
    - docker-exec
  #only:
  #  - tags


build-docker-master:
  image: gitlab/dind
  stage: docker-build
  script:
    - docker info
    - docker version
    - docker build -t '$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_REF_SLUG' .
  only:
    - master
  tags:
    - docker-exec

push-docker:
  image: gitlab/dind
  stage: docker-push
  script:
    - docker info
    #- docker push <my.container.registry.io>/<my_app>:<my_tag>
    - docker build -t '$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_REF_SLUG' .
    - docker push '$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_REF_SLUG'
  except:
    - master
  tags:
    - docker-exec

```