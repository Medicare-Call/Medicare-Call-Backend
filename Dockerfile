FROM amazoncorretto:17
ENV TZ=Asia/Seoul
EXPOSE 8080
COPY ./build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]