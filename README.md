# **Variate JDBC API**

![variate](https://github.com/user-attachments/assets/5309b3ad-4cef-41ca-bf11-70d5e441f00f)

**Variate JDBC API** is a Spring Boot-based e-commerce backend application that provides a RESTful API for managing categories, products, orders, order items, payments, and reviews. The application integrates with PostgreSQL as its primary database and supports JWT-based authentication for secure access.

## **Features**

- **Category Management**: Create, read, update, patch, and delete product categories.
- **Product Management**: Handle products under different categories, including information such as price, description, and stock availability.
- **Order Management**: Create, update, and manage customer orders.
- **Order Item Management**: Handle individual items within customer orders.
- **Payment Management**: Manage payment details related to customer orders.
- **Review Management**: Customers can add, update, and manage reviews for products.
- **JWT Authentication**: Secure API access using JSON Web Tokens (JWT).
- **Swagger Documentation**: Explore and interact with the API using Swagger UI.

## **Technologies Used**

- **Java 22**
- **Spring Boot 3.3.3**
- **JDBC** for direct database access and SQL queries
- **Spring Security** with JWT for authentication and authorization
- **PostgreSQL** as the primary database
- **H2** for in-memory testing
- **ModelMapper** for DTO conversions
- **Swagger/OpenAPI** for API documentation
- **Maven** for dependency management and build automation

## **Database Setup**

This project uses PostgreSQL as the database. The default configuration assumes a database named `variatespring`. Ensure PostgreSQL is installed and running in your local environment, and update the credentials in the `application.properties` file as needed.

Example `application.properties` configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/variatespring
spring.datasource.username=yourUsername
spring.datasource.password=yourPassword
```

## **Endpoints Overview**

### **Category Endpoints**

- `POST /api/categories`: Create a new category
- `GET /api/categories/{id}`: Retrieve a category by ID
- `GET /api/categories`: List all categories
- `PUT /api/categories/{id}`: Update a category by ID
- `PATCH /api/categories/{id}`: Partially update a category
- `DELETE /api/categories/{id}`: Delete a category

### **Product Endpoints**

- `POST /api/products`: Create a new product
- `GET /api/products/{id}`: Retrieve a product by ID
- `GET /api/products`: List all products
- `PUT /api/products/{id}`: Update a product
- `PATCH /api/products/{id}`: Partially update a product
- `DELETE /api/products/{id}`: Delete a product

### **Order Endpoints**

- `POST /api/orders`: Create a new order
- `GET /api/orders/{id}`: Retrieve an order by ID
- `GET /api/orders`: List all orders
- `PUT /api/orders/{id}`: Update an order
- `PATCH /api/orders/{id}`: Partially update an order
- `DELETE /api/orders/{id}`: Delete an order

### **Order Item Endpoints**

- `POST /api/order-items`: Create a new order item
- `GET /api/order-items/{id}`: Retrieve an order item by ID
- `GET /api/order-items`: List all order items
- `PUT /api/order-items/{id}`: Update an order item
- `PATCH /api/order-items/{id}`: Partially update an order item
- `DELETE /api/order-items/{id}`: Delete an order item

### **Payment Endpoints**

- `POST /api/payments`: Create a new payment
- `GET /api/payments/{id}`: Retrieve a payment by ID
- `GET /api/payments`: List all payments
- `PUT /api/payments/{id}`: Update a payment
- `PATCH /api/payments/{id}`: Partially update a payment
- `DELETE /api/payments/{id}`: Delete a payment

### **Review Endpoints**

- `POST /api/reviews`: Create a new review
- `GET /api/reviews/{id}`: Retrieve a review by ID
- `GET /api/reviews`: List all reviews
- `PUT /api/reviews/{id}`: Update a review
- `PATCH /api/reviews/{id}`: Partially update a review
- `DELETE /api/reviews/{id}`: Delete a review

## **Security**

JWT-based authentication is used for securing the API. Publicly accessible endpoints include:

- `/api/auth/login`
- `/api/auth/register`
- `/swagger-ui/**`
- `/v3/api-docs/**`

All other endpoints require a valid JWT token to access.

## **API Documentation**

Explore the API using Swagger UI. You can access the interactive API documentation via the following links:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Docs**: `http://localhost:8080/v3/api-docs`

## **Running the Application**

To run the application locally:

1. Clone the repository:
   ```bash
   git clone https://github.com/7irelo/variate-jdbc-api.git
   ```

2. Navigate to the project directory:
   ```bash
   cd variate-jdbc-api
   ```

3. Configure your PostgreSQL database credentials in `application.properties`.

4. Run the application using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

## **Development**

For local development, the project utilizes Maven for managing dependencies. You can take advantage of tools like `spring-boot-devtools` for live reloading during development.

### **Key Dependencies**

- `spring-boot-starter-web`: Provides RESTful web services
- `spring-boot-starter-jdbc`: For database access using JDBC
- `spring-boot-starter-security`: To handle security features
- `postgresql`: PostgreSQL JDBC Driver
- `modelmapper`: For mapping entities to DTOs and vice versa
- `jjwt`: For JWT-based authentication
- `springdoc-openapi-ui`: For auto-generated Swagger documentation

## **Contributing**

We welcome contributions to improve the project! To contribute:

1. Fork the repository.
2. Create your feature branch: `git checkout -b feature/new-feature`.
3. Commit your changes: `git commit -m 'Add a new feature'`.
4. Push to the branch: `git push origin feature/new-feature`.
5. Open a pull request and provide a description of your changes.

## **License**

This project is licensed under the MIT License.
