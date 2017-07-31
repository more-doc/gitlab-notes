# docker build --build-arg VAR1=XX --build-arg VAR2=XX

FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
#ENV PRE_BUILD_JAR_URL ${PRE_BUILD_JAR_URL}
#RUN echo '$DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$CI_COMMIT_REF_SLUG'
# ADD http://scm-server/chenjun/ci-example-docker/builds/artifacts/docker-push-test/raw/target/example.jar?job=mvn_build /example.jar
ARG PRE_BUILD_JAR_URL
ADD $PRE_BUILD_JAR_URL  /example.jar
ENV JAVA_OPTS="-Xms50m -Xmx1024m"
EXPOSE 18081
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /example.jar --server.port=18081" ]