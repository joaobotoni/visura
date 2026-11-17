# Imagem da API: compila com o Maven Wrapper e roda num JRE 17.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /build

# Baixa as dependências antes de copiar o código, para o cache do Docker reaproveitar essa camada.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests && cp target/*.jar app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --uid 1001 visura && mkdir -p /data/evidencias /data/anexos && chown -R visura /data
USER visura

COPY --from=build /build/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
