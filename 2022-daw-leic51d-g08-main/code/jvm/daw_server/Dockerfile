FROM openjdk:17
COPY ./build/libs/daw_server-1.0-SNAPSHOT.jar /usr/app
WORKDIR /usr/app/daw_server
CMD ["java", "-jar", "daw_server-1.0-SNAPSHOT.jar"]

FROM postgres
ENV postgres postgresURL
COPY ./src/main/kotlin/BattleShipApp/sql/restartSql.sql /docker-entrypoint-initdb.d/
RUN mkdir /docker-entrypoint-initdb.d