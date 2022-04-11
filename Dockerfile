FROM gradle:7.4.2-jdk17-alpine@sha256:dbed44df17c4931060e6ab5e3d823eadd3128ce1f023ab4e3e78dd4e9f81a012  AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
RUN gradle build

FROM eclipse-temurin:17-jre-alpine@sha256:898c28d9082a3453504234ef0f28055f944ca9e69c1b6f324a3d3129a054d25a
RUN apk add dumb-init
RUN mkdir /app
RUN mkdir /searchspace
RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser

COPY --from=build ./project/build/libs/henv-lint-0.1.0-all.jar /app/henv-lint.jar
#COPY ./mount /searchspace

WORKDIR /app
RUN chown -R javauser:javauser /app
USER javauser
CMD "dumb-init" "java" "-jar" "henv-lint.jar"