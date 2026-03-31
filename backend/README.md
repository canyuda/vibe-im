# Vibe IM Backend

Real-time instant messaging backend service with WebSocket support.

## Tech Stack

- **Java 21** - Modern Java with Virtual Threads support
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - ORM and database access
- **Spring WebSocket** - Real-time bidirectional communication
- **MySQL 8.0** - Primary database
- **Redis 7.x** - Session management and caching
- **Maven** - Build and dependency management
- **Lombok** - Reduce boilerplate code
- **TestContainers** - Integration testing with real containers

## Prerequisites

Before running the application, ensure you have the following installed:

- **JDK 21+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)

## Quick Start with Docker Compose

The fastest way to get started is using Docker Compose, which will spin up MySQL, Redis, and the backend service automatically.

### 1. Build the project

```bash
mvn clean package
```

### 2. Start all services

```bash
docker-compose up -d
```

This will start:
- MySQL on port 3306
- Redis on port 6379
- Backend service on port 8080

### 3. View logs

```bash
docker-compose logs -f backend
```

### 4. Stop services

```bash
docker-compose down
```

**Note:** If you need to start only the infrastructure services first (MySQL and Redis):

```bash
docker-compose up -d mysql redis
# Wait for services to be healthy
docker-compose up backend
```

## Local Development

If you prefer to run the backend service locally while using Docker for infrastructure:

### 1. Start infrastructure services

```bash
docker-compose up -d mysql redis
```

### 2. Verify services are healthy

```bash
docker-compose ps
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

All API endpoints are prefixed with `/api`. Authentication is required for all endpoints except registration.

### Authentication Endpoints

#### Register a new user

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "nickname": "Test User"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "Test User",
  "avatar": null,
  "status": "OFFLINE",
  "createTime": "2026-03-30T10:00:00"
}
```

#### User login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "Test User",
  "avatar": null,
  "status": "ONLINE",
  "createTime": "2026-03-30T10:00:00",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Save the `sessionId` from the response - you'll need it for subsequent requests.

#### User logout

```http
POST /api/auth/logout
Session-Id: 550e8400-e29b-41d4-a716-446655440000
User-Id: 1
```

**Response (200 OK):** Empty body

**Note:** `Session-Id` and `User-Id` headers are optional.

### Chat Endpoints

All chat endpoints require the `Session-Id` header for authentication.

#### Send a message

```http
POST /api/chat/send
Session-Id: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json

{
  "receiverId": 2,
  "content": "Hello, World!",
  "messageType": "TEXT"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "senderId": 1,
  "receiverId": 2,
  "content": "Hello, World!",
  "messageType": "TEXT",
  "status": "DELIVERED",
  "sendTime": "2026-03-30T10:00:00",
  "createTime": "2026-03-30T10:00:00",
  "updateTime": "2026-03-30T10:00:00",
  "senderName": "testuser"
}
```

#### Get chat history

```http
GET /api/chat/messages?friendId=2&page=0&size=20
Session-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Query Parameters:**
- `friendId` (required): The ID of the user you're chatting with
- `page` (optional, default=0): Page number (0-indexed)
- `size` (optional, default=20): Number of messages per page

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "senderId": 1,
      "receiverId": 2,
      "content": "Hello, World!",
      "messageType": "TEXT",
      "status": "DELIVERED",
      "sendTime": "2026-03-30T10:00:00",
      "createTime": "2026-03-30T10:00:00",
      "updateTime": "2026-03-30T10:00:00",
      "senderName": "testuser"
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 1,
  "pageSize": 20
}
```

Messages are sorted by creation time in ascending order (oldest first).

## WebSocket Connection

WebSocket provides real-time message delivery. Connect to receive messages instantly without polling.

### Connection URL

```
ws://localhost:8080/api/ws?sessionId={sessionId}
```

Replace `{sessionId}` with the session ID received from the login endpoint.

### WebSocket Protocol

#### Heartbeat Mechanism

To keep the connection alive, send a periodic PING message:

**Client sends:**
```json
{
  "type": "PING"
}
```

**Server responds:**
```json
{
  "type": "PONG"
}
```

Recommended heartbeat interval: 30-60 seconds.

#### New Message Notification

When a new message arrives, the server pushes it automatically:

```json
{
  "id": 1,
  "senderId": 1,
  "receiverId": 2,
  "content": "Hello, World!",
  "messageType": "TEXT",
  "status": "DELIVERED",
  "sendTime": "2026-03-30T10:00:00",
  "createTime": "2026-03-30T10:00:00",
  "updateTime": "2026-03-30T10:00:00",
  "senderName": "testuser"
}
```

#### Offline Messages

When establishing a WebSocket connection, the server automatically pushes all undelivered messages (messages with status `SENT`) and updates their status to `DELIVERED`.

### WebSocket Lifecycle

1. **Connection Established:**
   - Server validates the `sessionId` query parameter
   - If valid, the connection is accepted
   - Offline messages are pushed automatically

2. **Message Handling:**
   - Client can send PING messages
   - Server responds with PONG
   - Server pushes new messages as they arrive

3. **Connection Closed:**
   - Server cleans up the session
   - Resources are released

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/vibe/im/
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── RedisConfig.java        # Redis configuration
│   │   │   │   └── WebSocketConfig.java   # WebSocket configuration
│   │   │   ├── controller/          # REST API controllers
│   │   │   │   ├── AuthController.java    # Authentication endpoints
│   │   │   │   └── ChatController.java    # Chat endpoints
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── request/               # Request DTOs
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   └── SendMessageRequest.java
│   │   │   │   └── response/              # Response DTOs
│   │   │   │       ├── LoginResponse.java
│   │   │   │       ├── UserResponse.java
│   │   │   │       ├── MessageResponse.java
│   │   │   │       └── PageResponse.java
│   │   │   ├── entity/              # JPA entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Message.java
│   │   │   │   └── enums/                 # Enum types
│   │   │   │       ├── UserStatus.java
│   │   │   │       ├── MessageType.java
│   │   │   │       └── MessageStatus.java
│   │   │   ├── exception/           # Exception handling
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── repository/          # JPA repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── MessageRepository.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── AuthService.java
│   │   │   │   └── ChatService.java
│   │   │   ├── util/                # Utility classes
│   │   │   │   └── SnowflakeIdGenerator.java
│   │   │   ├── websocket/           # WebSocket handlers
│   │   │   │   ├── WebSocketHandler.java
│   │   │   │   └── ConnectionManager.java
│   │   │   └── VibeImApplication.java    # Main application class
│   │   └── resources/
│   │       └── application.yml     # Application configuration
│   └── test/
│       └── java/com/vibe/im/
│           ├── controller/         # Controller tests
│           ├── service/            # Service tests
│           ├── entity/             # Entity tests
│           └── integration/        # Integration tests
├── Dockerfile                      # Docker image definition
├── docker-compose.yml              # Docker Compose configuration
└── pom.xml                         # Maven dependencies
```

## Testing

### Unit Tests

Run all unit tests:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=AuthServiceTest
```

### Integration Tests

Integration tests use TestContainers to spin up real MySQL and Redis instances:

```bash
mvn verify
```

Or run specific integration test:

```bash
mvn verify -Dtest=ChatFlowIntegrationTest
```

### Test Coverage

Generate test coverage report:

```bash
mvn jacoco:report
```

The report will be generated in `target/site/jacoco/index.html`.

## Configuration

### Application Configuration

The main configuration is in `src/main/resources/application.yml`:

- Server port: 8080
- Context path: /api
- MySQL: localhost:3306/vibe_im
- Redis: localhost:6379

### Environment Variables

When using Docker Compose, the following environment variables are used:

- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_DATA_REDIS_HOST` - Redis host
- `SPRING_DATA_REDIS_PORT` - Redis port

## Architecture Highlights

### Layered Architecture

- **Controller Layer:** REST API endpoints, handles HTTP requests/responses
- **Service Layer:** Business logic, transaction management
- **Repository Layer:** Data access using Spring Data JPA
- **Domain Layer:** Entities, value objects, and domain events

### WebSocket Architecture

- **ConnectionManager:** Manages active WebSocket connections using ConcurrentHashMap for thread safety
- **WebSocketHandler:** Handles connection lifecycle, heartbeats, and message delivery
- **Offline Message Push:** Automatically delivers undelivered messages when a user connects

### Session Management

- Session IDs are generated using UUID
- Sessions are stored in Redis with TTL
- Session validation is performed on every authenticated request

### Message Status Flow

1. **SENT** - Message created and stored
2. **DELIVERED** - Message delivered via WebSocket (or pulled via API)
3. **READ** - Message marked as read by recipient (future enhancement)

## Next Steps

The current implementation provides a solid foundation. Here are planned enhancements:

### Message Reliability
- Message acknowledgment mechanism
- Retry logic for failed deliveries
- Message persistence queue

### High Concurrency Optimization
- Virtual Threads (Java 21) for better scalability
- Connection pooling optimization
- Redis cluster support for session storage

### Search and Analytics
- Elasticsearch integration for message search
- Message analytics and statistics
- Chat history export

### Security Enhancements
- JWT token support
- Rate limiting
- Input sanitization
- SQL injection prevention

### Performance Monitoring
- Metrics collection (Micrometer)
- Distributed tracing (OpenTelemetry)
- Health checks and diagnostics

## Troubleshooting

### Database Connection Issues

If you see connection errors to MySQL:

```bash
# Check MySQL container status
docker-compose ps mysql

# View MySQL logs
docker-compose logs mysql

# Restart MySQL
docker-compose restart mysql
```

### Redis Connection Issues

If you see connection errors to Redis:

```bash
# Check Redis container status
docker-compose ps redis

# View Redis logs
docker-compose logs redis

# Restart Redis
docker-compose restart redis
```

### WebSocket Connection Failures

Ensure you're passing a valid `sessionId` in the query string:

```
ws://localhost:8080/api/ws?sessionId=your-valid-session-id
```

Check the backend logs for detailed error messages:

```bash
docker-compose logs -f backend
```

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please open an issue in the repository.
