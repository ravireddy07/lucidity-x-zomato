# sample-cart-offer

Spring Boot service implementing the cart-offer APIs.

## Prerequisities

- Java 11+
- Maven 3.8+
- Docker for MockServer (optional)

### ⚠️ Why is Docker/MockServer optional?

- For **Tests**: not needed (tests use in-memory [WireMock](https://wiremock.org/docs/configuration/)).
- For **Manual Testing(Curl/Postman)**: required (app calls user segment API on port 1080, so MockServer must respond).

## Mockserver

```bash
cd mockserver
docker compose up
# the mock server will start at port 1080
```

## Build

```bash
mvn clean package -DskipTests
```

This gives a runnable jar: `target/simple-springboot-app-0.0.1-SNAPSHOT.jar`

## Run the Application

```bash
java -jar target/simple-springboot-app-0.0.1-SNAPSHOT.jar
# Service: http://localhost:9001
```

# Run with MockServer

Start MockServer (default port 1080):

```bash
cd mockserver
docker compose up
```

Sets expectation(s) in MockServer to return segment(s) for specific user(s) (example: user 101 → p1):

```bash
curl -X PUT http://localhost:1080/mockserver/expectation \
  -H "Content-Type: application/json" \
  -d '{
    "httpRequest": {
      "method": "GET",
      "path": "/api/v1/user_segment",
      "queryStringParameters": [{"name":"user_id","values":["101"]}]
    },
    "httpResponse": {
      "statusCode": 200,
      "headers": { "Content-Type": ["application/json"] },
      "body": { "json": {"segment":"p1"} }
    }
  }'

```

# APIs

## Seed Offer

Stored inside the Spring Boot app. Add them by calling `POST /api/v1/offer`. Rules like “p1 users at restaurant 1 get ₹10 off”. Think of this as seeding the app’s in-memory offers database.

```bash
POST http://localhost:9001/api/v1/offer
{
  "restaurant_id": 1,
  "offer_type": "FLATX",
  "offer_value": 10,
  "customer_segment": ["p1"]
}
```

> Response: `{"response_msg":"success"}`

## Apply Offer

Stored inside MockServer. Add them by calling `PUT /mockserver/expectation`. Think of this as mocking the external segment database/service

```bash
POST http://localhost:9001/api/v1/cart/apply_offer
{
  "cart_value": 200,
  "user_id": 101,
  "restaurant_id": 1
}
```

> Response: `{"cart_value":190}`

## Flow

```bash
Client (curl/Postman)
   │
   ▼
Spring Boot app (localhost:9001)
   │  (calls user segment service)
   ▼
MockServer (localhost:1080)
```

## Run Tests

```bash
mvn test
# mvn clean test
```
