# Sporty Kislin System - Startup Instructions

This document provides step-by-step instructions to start the Sporty Kislin ticket management system.

## Prerequisites

- Docker and Docker Compose installed
- Java 24 or higher
- Gradle (or use the included Gradle wrapper)

## System Components

- **RabbitMQ**: Message broker running in Docker container
- **Gateway**: Spring Boot application for ticket creation API
- **Consumer**: Spring Boot application for processing tickets from RabbitMQ

## Startup Sequence

### Step 1: Start RabbitMQ

Start the RabbitMQ container using Docker Compose:

```bash
docker-compose up -d
```

Wait for RabbitMQ to be fully started (check health status):

```bash
docker-compose ps
```

RabbitMQ Management UI will be available at: http://localhost:15673
- Username: `guest`
- Password: `guest`

### Step 2: Start Gateway Application

Navigate to the gateway directory and start the application:

```bash
cd gateway
./gradlew bootRun
```

The Gateway application will start on port 8011.

### Step 3: Start Consumer Application

Open a new terminal, navigate to the consumer directory and start the application:

```bash
cd consumer
./gradlew bootRun
```

The Consumer application will start on port 8012 and connect to RabbitMQ to listen for messages on the `support-tickets` queue.

## Verification

1. **RabbitMQ**: Check that the container is running and healthy
2. **Gateway**: Verify the application starts without errors and is ready to accept requests
3. **Consumer**: Verify the application connects to RabbitMQ and is ready to process messages

## API Usage

### Create a new support ticket

```bash
curl -X POST http://localhost:8011/api/v1/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "subject": "Test ticket",
    "description": "This is a test ticket"
  }'
```

### Assign a ticket to a user

```bash
curl -X POST http://localhost:8011/api/v1/tickets/assign \
  -H "Content-Type: application/json" \
  -d '{
    "ticketId": "550e8400-e29b-41d4-a716-446655440000",
    "assigneeId": "admin123"
  }'
```

### Update ticket status

```bash
curl -X PUT http://localhost:8011/api/v1/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "ticketId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "IN_PROGRESS"
  }'
```

## Ports Configuration

- RabbitMQ AMQP: `5673` (non-standard port to avoid conflicts)
- RabbitMQ Management UI: `15673` (non-standard port to avoid conflicts)
- Gateway: `8011`
- Consumer: `8012`

## Shutdown

To stop all services:

1. Stop Consumer application (Ctrl+C)
2. Stop Gateway application (Ctrl+C)
3. Stop RabbitMQ container:

```bash
docker-compose down
```

## Troubleshooting

- If RabbitMQ fails to start, check if ports 5673 and 15673 are available
- If applications fail to connect to RabbitMQ, verify the container is running and healthy
- Check application logs for detailed error messages
