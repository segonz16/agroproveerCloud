# Etapa 1: Construcción con Gradle y JDK 17
FROM gradle:8.13-jdk17 AS build

WORKDIR /app

# Copiamos archivos esenciales primero para aprovechar la caché de Docker
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copiamos el código fuente
COPY src ./src

# Compilamos la aplicación sin ejecutar tests
RUN gradle clean bootJar --no-daemon -x test

# Etapa 2: Imagen final más liviana para ejecutar
FROM eclipse-temurin:17-jdk-alpine

# Crear usuario sin privilegios
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer el puerto de Spring Boot
EXPOSE 3000

# Comando para arrancar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
