# Telephone Number Inventory Management System

A scalable Java Spring Boot application for managing telephone number inventory, including loading, searching, allocation, and lifecycle management.

## Features

1. **Telephone Number Loading in Inventory**
   - Efficient processing of large files using batch processing and multi-threading
   - Data consistency handling with transaction management
   - Idempotent load process to prevent duplicates during retries
   - Scalable architecture with Kafka for asynchronous processing

2. **Inventory Management and Search**
   - Multi-criteria search algorithm powered by Elasticsearch
   - Performance-optimized queries with indexing strategies
   - RESTful API for flexible querying

3. **Number Allocation to Users**
   - Reservation system based on search results
   - Optimistic locking for concurrency control to prevent conflicts
   - Transactional integrity for allocation operations

4. **Lifecycle Management**
   - State management (Available, Reserved, Allocated, Activated, Deactivated)
   - Event-driven state transitions
   - Comprehensive audit logging with user tracking

## Technology Stack

- **Backend**: Java 11, Spring Boot 2.7
- **Database**: PostgreSQL for transactional data
- **Search Engine**: Elasticsearch for optimized multi-criteria searches
- **Message Broker**: Apache Kafka for asynchronous processing
- **Build Tool**: Maven
- **API Documentation**: Swagger/OpenAPI
- **Testing**: JUnit, Mockito, TestContainers

## Architecture

The system uses a microservices-inspired architecture with:

- **API Layer**: RESTful endpoints for client interaction
- **Service Layer**: Business logic implementation
- **Repository Layer**: Data access and persistence
- **Messaging Layer**: Asynchronous communication between components
- **Search Layer**: Optimized search capabilities

## Project Structure

```
/src
├── /main
│   ├── /java/com/telecom/inventory
│   │   ├── /config          # Configuration classes
│   │   ├── /controller      # REST API controllers
│   │   ├── /dto             # Data Transfer Objects
│   │   ├── /exception       # Custom exceptions and handlers
│   │   ├── /model           # Domain entities
│   │   ├── /repository      # Data access layer
│   │   ├── /service         # Business logic
│   │   │   ├── /file        # File processing services
│   │   │   ├── /search      # Search services
│   │   │   ├── /allocation  # Number allocation services
│   │   │   └── /lifecycle   # Lifecycle management services
│   │   ├── /util            # Utility classes
│   │   └── Application.java # Main application class
│   └── /resources│      
│       ├── application.yml  # Application configuration
│       └── logback.xml      # Logging configuration
└── /test                    # Test classes
```

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Elasticsearch 7.x
- Apache Kafka 2.x

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/telephone-inventory-system.git
   cd telephone-inventory-system
   ```

2. **Configure the application**
   
   Edit `src/main/resources/application.yml` to set up your database, Elasticsearch, and Kafka connections.

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Alternatively, you can run the JAR file directly:
   ```bash
   java -jar target/number-inventory-0.0.1-SNAPSHOT.jar
   ```

5. **Access the API documentation**
   
   Open your browser and navigate to `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Telephone Number Loading
- `POST /api/v1/numbers/upload` - Upload telephone numbers from a file

### Inventory Management and Search
- `GET /api/v1/numbers/search` - Search for telephone numbers based on criteria
- `GET /api/v1/numbers/{id}` - Get a specific telephone number by ID

### Number Allocation
- `POST /api/v1/numbers/{id}/reserve` - Reserve a telephone number
- `POST /api/v1/numbers/{id}/allocate` - Allocate a telephone number to a user

### Lifecycle Management
- `PUT /api/v1/numbers/{id}/status` - Update the status of a telephone number
- `GET /api/v1/numbers/{id}/history` - Get the history of a telephone number

## Performance Considerations

- **File Processing**: Uses chunked processing and parallel streams for efficient handling of large files
- **Search Optimization**: Leverages Elasticsearch for fast multi-criteria searches
- **Concurrency Control**: Implements optimistic locking to prevent conflicts during allocation
- **Scalability**: Uses Kafka for asynchronous processing to handle high loads

## Development

### Running Tests
```bash
mvn test
```

### Adding New Features
1. Create appropriate model classes
2. Implement repository interfaces
3. Add service layer logic
4. Expose functionality through REST controllers
5. Update documentation

