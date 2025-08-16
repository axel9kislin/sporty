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

## Design Decisions

### Gateway Service Architecture

In the Gateway service, 3 methods were implemented, 2 of which are located at the same address but with different HTTP methods. This was done because logically the ticket creation method (POST) and ticket update method (PUT) work on the same entity. The assign method is separated because it doesn't change the ticket state, but only changes the responsible employee.

In the creation method response, I decided to return only the ID of the created ticket for the convenience of the calling code, and since the gateway is a stateless service, it doesn't store anything internally. Other methods return void and 200 OK status, essentially serving as indicators of successful process initiation.

Minimal business logic is encapsulated at the service level, while all RabbitMQ interaction is encapsulated in the Sender class.

### Consumer Service Architecture

In the Consumer service, I created 3 separate listeners for each queue. Business logic is also encapsulated at the service level, and for working with the in-memory H2 database, I used JPA as it's the most concise option for this task. We don't require a flexible approach to database work or any optimizations, so the choice was made towards the most declarative approach.

For database schema updates, I used Flyway, since this technology is widely used in enterprise development and allows doing everything necessary for this task.

### Testing Approach

Tests in both services are written from a minimalistic approach without using external libraries. Test data is placed in JSON files for better code clarity in tests, since when working with large models, readability would be significantly reduced.

In the Gateway service, I decided to go with honest controller testing but mocked RabbitMQ sending, because for this task it's sufficient to verify the fact of sending itself.

In the Consumer service, I used an end-to-end approach in tests, calling listener methods in tests, sometimes providing pre-saved test data, and then checking the result from the database. I also considered using the testContainers library to ensure full black-box testing and verification of all interactions with infrastructure elements (RabbitMQ), but decided that for this task it would be too heavyweight a solution.

## AI-tools

This entire project was developed using **WindSurf IDE** with **Claude Sonnet 4** AI assistant. All code, configurations and documentation were generated by AI, with human oversight to limited creating the architecture, performing code reviews and making changes in a declarative manner.

### Development Statistics (August 16, 2025)
- **Total Cascade conversations**: 6
- **Total Cascade messages sent**: 37
- **Total lines of code written by Cascade**: 1,445
- **New code written by WindSurf**: 98%
- **Total credits used**: 74
- **Memories used**: 2
- **Terminal messages sent**: 74

The AI-driven development approach demonstrated high efficiency in:
- Rapid prototyping and implementation
- Consistent code quality and patterns
- Comprehensive documentation generation
- Best practices adherence across the entire codebase

## Troubleshooting

- If RabbitMQ fails to start, check if ports 5673 and 15673 are available
- If applications fail to connect to RabbitMQ, verify the container is running and healthy
- Check application logs for detailed error messages
