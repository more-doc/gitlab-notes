## 构建脚本()

### 例子(maven项目,docker构建)

```
# Official docker image.
image: docker:latest

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_REGISTRY: 192.168.0.222:9081
  APP_NAME: demo-jave-app

services:
  - docker:dind
  - maven:3-jdk-8

stages:
  - maven-build
  - maven-test
  - maven-deploy
  - docker-build
  - docker-push

#before_script:
#  - ping -c 4 mygit-servre.com
# see also :  https://docs.gitlab.com/ce/ci/docker/using_docker_images.html#define-an-image-from-a-private-container-registry
#  - docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" $CI_REGISTRY


mvn_build:
  stage: maven-build
  script:
    - mvnw clean install
  only:
    - develop
  tags:
    - docker-exec

mvn_test:
  stage: maven-test
  script:
    - mvnw test
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


build-master:
  stage: docker-build
  script:
    - docker info
    - docker version
    - chkconfig docker on
    - docker build -t '$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_REF_SLUG' .
  only:
    - master
  tags:
    - docker-exec

build:
  stage: docker-push
  script:
    - docker build -t '$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_REF_SLUG' .
    - docker push '$DOCKER_REGISTRY/$APP_NAME:$CI_COMMIT_REF_SLUG'
  except:
    - master
  tags:
    - docker-exec

```