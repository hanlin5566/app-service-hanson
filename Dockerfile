FROM registry.cn-shanghai.aliyuncs.com/hanson-basic/maven-3.6.3-jdk-8-slim:1.0 as builder
COPY ./maven/settings.xml /usr/share/maven/conf
COPY ./ /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM registry.cn-shanghai.aliyuncs.com/hanson-basic/alpine_jdk8_351:1.0
COPY --from=builder /home/app/${project_name}/target/*.jar /app.jar
ENV spring.profiles.active="dev"
EXPOSE 8080
CMD ["java","-Duser.timezone=Asia/Shanghai","-jar","-Xms512m","-Xmx512m","/app.jar"]
