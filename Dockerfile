# Etapa de construcción
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /app
COPY . .
# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw
# Compilar el proyecto (el módulo backend)
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiar el jar generado desde la etapa de construcción
# Ajustamos la ruta porque es un proyecto multi-módulo
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
