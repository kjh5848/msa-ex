FROM gradle:8.5-jdk21
WORKDIR /app
COPY . .
RUN gradle bootJar -x test
ENTRYPOINT ["java", "-jar", "build/libs/app.jar"]

COPY init.sql /docker-entrypoint-initdb.d

ENV MYSQL_ROOT_PASSWORD=root1234
ENV MYSQL_DATABASE=metadb

CMD ["--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]
