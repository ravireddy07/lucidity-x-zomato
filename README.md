# sample-cart-offer

Spring Boot service implementing the cart-offer APIs.

## Prerequisities

- Java 11+
- Maven 3.8+
- Docker for MockServer

## Mockserver

```bash
cd mockserver
docker compose up
# the mock server will start at port 1080
```

## Build

```bash
mvn -DskipTests clean package
java -jar target/simple-springboot-app-0.0.1-SNAPSHOT.jar
# Service: http://localhost:9001
```

# Run

```bash
mvn test
```
