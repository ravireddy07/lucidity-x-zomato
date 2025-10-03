# sample-cart-offer

Spring Boot service implementing the cart-offer APIs.

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker for MockServer

## MockServer

Start MockServer on port 1080:

```bash
cd mockserver
docker compose up
# service available at http://localhost:1080
```

## Build

```bash
mvn clean package -DskipTests
```

This gives a runnable jar: `target/simple-springboot-app-0.0.1-SNAPSHOT.jar`

## Run the Application

```bash
java -jar target/simple-springboot-app-0.0.1-SNAPSHOT.jar
# Service runs at http://localhost:9001
```

## Run Tests

- Requires the app to start on port 9001
- Requires MockServer running on port 1080

Tests are split by concern into multiple files:

- Happy path tests → CartOfferHappyPathTests
- Business rules / edge cases → CartOfferBusinessRulesTests
- Validation tests → CartOfferValidationTests
- Resilience tests → CartOfferResilienceTests

```bash
mvn test # Run all tests
# or
mvn -Dtest=CartOfferBusinessRulesTests test
mvn -Dtest=CartOfferHappyPathTests test
mvn -Dtest=CartOfferValidationTests test
mvn -Dtest=CartOfferResilienceTests test
```

### What the tests do:

- Seed offers via /api/v1/offer
- Program expectations in MockServer
- Call /api/v1/cart/apply_offer and verify results (final cart_value)

> JUnit reports are generated in: target/surefire-reports/

## Reports

Make sure to run the test(s) atleast once :)

```bash
mvn surefire-report:report # This generates: 'target/reports/**'
# Preview the surefire.html file or open in Chrome to visualise the test results
```

## Run with MockServer

Once MockServer is up, you can set **expectations** so it returns segments for specific users. For example, to always return `p1` for user `101`:

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

## APIs

### Seed Offer

Stored inside the Spring Boot app (in-memory). Add them by calling `POST /api/v1/offer`.

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

### Apply Offer

Uses both the app’s in-memory offers **and** the segment from MockServer.

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

```
Client (curl/Postman)
   │
   ▼
Spring Boot app (localhost:9001)
   │  (calls user segment service)
   ▼
MockServer (localhost:1080)
```
