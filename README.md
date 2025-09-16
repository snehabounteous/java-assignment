# 📦 OrderUp - Safe Ordering System

OrderUp is a **Spring Boot REST API** designed to process product orders safely and consistently.  
It guarantees **thread-safe stock updates** to prevent overselling and offers **centralized exception handling** for predictable, clean error responses.  
The project includes comprehensive **unit and integration tests** with **JaCoCo coverage reports** (currently at 86%, exceeding the 80%+ requirement for business logic and exception handling).

---

## 🚀 Features
- ✅ Place an order for a product with concurrency safety.
- ✅ Thread-safe stock updates preventing race conditions.
- ✅ Centralized exception handling returning meaningful JSON errors.
- ✅ DTO-based request/response for API clean separation.
- ✅ Aspect-Oriented Programming (AOP) for logging cross-cutting concerns.
- ✅ Robust unit and integration tests with detailed coverage reports.

---

## 🏗 Design Overview

### 1. **Controller Layer**
- `OrderController` handles HTTP requests for placing orders.
- Accepts JSON input (`OrderRequest`), delegates to `OrderService`.
- Returns structured JSON response (`OrderResponse`).

### 2. **Service Layer**
- `OrderService` implements business logic:
  - Validates product existence.
  - Checks stock availability.
  - Deducts stock atomically using **synchronized blocks** to ensure **thread-safety** during concurrent orders.
- **Why synchronized?**  
  This simple yet effective mechanism prevents two threads from simultaneously reducing the stock, avoiding overselling without introducing complex distributed locks.

### 3. **Entity & DTOs**
- **Entities:** Represent database tables (`Product`, `Order`).
- **DTOs:** Data Transfer Objects separate persistence from API layer.
  - `OrderRequest`: Input payload with product ID, quantity, customer name.
  - `OrderResponse`: Output with order ID, status, and messages.

### 4. **Global Exception Handling**
- Implemented via `GlobalExceptionHandler` annotated with `@RestControllerAdvice`.
- Intercepts exceptions globally using Spring AOP under the hood.
- Maps domain exceptions to HTTP status codes:
  - `ProductNotFoundException` → 404 Not Found.
  - `InsufficientStockException` → 400 Bad Request.
  - Other exceptions → 500 Internal Server Error.
- Returns consistent JSON error responses with timestamps for easier debugging.

### 5. **Aspect-Oriented Programming (AOP)**
- Logging aspect intercepts service methods.
- Logs method entry, exit, and execution time.
- Separates cross-cutting concerns cleanly from business logic, enhancing code readability and maintainability.

---

## 🔄 Flow of an Order
Client → OrderController → OrderService → Product Repository → DB → Response

Client sends order request:

POST /orders
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2,
  "customerName": "Alice"
}


OrderController receives request and calls OrderService.

OrderService:

Validates product exists.

Checks and deducts stock inside a thread-safe block.

Creates order record.

Returns OrderResponse.

Successful response:

{
  "orderId": 101,
  "productId": 1,
  "quantity": 2,
  "status": "CONFIRMED",
  "message": "Order placed successfully"
}


Errors handled centrally and return structured JSON, e.g.:

{
  "error": "Product Not Found",
  "message": "Product not found with ID: 9999",
  "timestamp": 1694947200000
}

## 📚 API Endpoints

### Orders

| HTTP Method | Endpoint          | Description            | Request Body Example                  | Response Example                                              |
|-------------|-------------------|------------------------|-------------------------------------|--------------------------------------------------------------|
| POST        | `/api/orders`     | Place a new order      | `{ "productId": 1, "quantity": 2 }` | `{ "orderId": 101, "productId": 1, "quantity": 2, "status": "CONFIRMED" }` |
| GET         | `/api/orders/{id}` | Get order details by ID | N/A                                 | `{ "orderId": 101, "productId": 1, "quantity": 2, "status": "CONFIRMED" }` |
| GET         | `/api/orders`     | List all orders        | N/A                                 | List of order objects                                        |

---

### Products

| HTTP Method | Endpoint               | Description                | Request Body Example                     | Response Example                                                         |
|-------------|------------------------|----------------------------|----------------------------------------|-------------------------------------------------------------------------|
| POST        | `/api/products`        | Create a new product       | `{ "name": "New Product", "stock": 15 }` | `{ "id": 5, "name": "New Product", "stock": 15, "message": "Product created successfully" }` |
| GET         | `/api/products/{id}`   | Retrieve product by ID     | N/A                                    | `{ "id": 1, "name": "Test Product", "stock": 10, "message": "Product retrieved successfully" }` |
| GET         | `/api/products`        | Retrieve all products      | N/A                                    | List of product objects                                                 |
| PUT         | `/api/products/{id}`   | Update existing product    | `{ "name": "Updated Name", "stock": 20 }` | `{ "id": 5, "name": "Updated Name", "stock": 20, "message": "Product updated successfully" }` |
| DELETE      | `/api/products/{id}`   | Delete a product           | N/A                                    | HTTP 204 No Content                                                     |
| GET         | `/api/products/{id}/stock` | Get current stock of product | N/A                                  | `10` (integer representing current stock)                             |


🧪 Testing
✅ Unit Tests

Test business logic of OrderService and ProductService.

Validate exception handling through GlobalExceptionHandler.

Includes concurrency tests to simulate simultaneous orders.

✅ Integration Tests

Use MockMvc to simulate HTTP requests/responses.

Validate controller layer, successful flows, and error scenarios.

✅ JaCoCo Coverage

Run tests and generate coverage report:

mvn clean test
mvn jacoco:report


Open report at:

target/site/jacoco/index.html


Current Coverage: 86% overall, exceeding the 80% threshold on key classes like OrderService and exception handlers.

📌 How to Run

Clone and build the project:

git clone <your-repo-url>
cd orderup
mvn clean spring-boot:run


The API will be available at:

http://localhost:8080

🔥 How to Test with curl
✅ Place a Successful Order
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"productId": 1, "quantity": 2, "customerName": "Alice"}'


Expected Response:

{
  "orderId": 101,
  "productId": 1,
  "quantity": 2,
  "status": "CONFIRMED",
  "message": "Order placed successfully"
}

❌ Trigger Product Not Found Error
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"productId": 9999, "quantity": 1, "customerName": "Bob"}'


Expected Response:

{
  "error": "Product Not Found",
  "message": "Product not found with ID: 9999",
  "timestamp": 1694947200000
}

❌ Trigger Insufficient Stock Error
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"productId": 1, "quantity": 100, "customerName": "Charlie"}'


Expected Response:

{
  "error": "Insufficient Stock",
  "message": "Not enough stock available for product ID: 1",
  "timestamp": 1694947200000
}

🛠 Technologies Used

Java 17+

Spring Boot 3.x (Spring Web, Spring Data JPA)

H2 Database (in-memory for tests)

JUnit 5 & Mockito for unit testing

MockMvc for integration testing

JaCoCo for code coverage reporting

Lombok for boilerplate code reduction

Maven for build and dependency management

📊 Summary

OrderUp is a clean, robust, and scalable order management system that:

Separates concerns cleanly across Controller, Service, Repository, and Exception Handling.

Ensures thread-safe stock updates to prevent overselling.

Uses centralized AOP-powered exception handling for consistent error responses.

Delivers strong automated test coverage verified by JaCoCo.

Supports real-world concurrency scenarios with dedicated tests.
