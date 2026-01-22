# Logistics Management System - Project Summary & Interview Guide

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [System Architecture](#system-architecture)
4. [Data Model & Relationships](#data-model--relationships)
5. [API Implementation](#api-implementation)
6. [Business Logic & Features](#business-logic--features)
7. [Request Flow](#request-flow)
8. [Key Design Decisions](#key-design-decisions)
9. [Interview Preparation](#interview-preparation)

---

## 🎯 Project Overview

### What is this project?
A **Multi-tenant Commerce Ingestion API** - a RESTful microservice for managing logistics operations including organizations, websites, orders, fulfillments, and tracking information.

### Core Purpose
- Ingest and manage e-commerce order data from multiple platforms (Shopify, Magento, NetSuite, etc.)
- Track order fulfillment and shipping status
- Provide real-time tracking updates with event history
- Support multi-tenant architecture (multiple organizations)

### Key Capabilities
- CRUD operations for all entities
- Advanced search and filtering (date ranges, status, pagination)
- Automatic status updates (order fulfillment status based on fulfillment states)
- Tracking event ingestion with automatic status rollup
- Idempotent operations (upsert patterns)

---

## 🛠 Technology Stack

### Backend Framework
- **Spring Boot 3.x** - Main framework
- **Java 17** - Programming language
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM framework
- **MySQL** - Database

### Key Libraries
- **Lombok** - Reduces boilerplate code (@Getter, @Setter, @Data)
- **ModelMapper** - Object-to-object mapping
- **Jakarta Validation** - Request validation
- **Spring Web** - REST API implementation

### Architecture Patterns
- **Layered Architecture**: Controller → Service → Repository
- **DTO Pattern**: Separate request/response DTOs from entities
- **Repository Pattern**: Data access abstraction
- **Specification Pattern**: Dynamic query building for complex filters

---

## 🏗 System Architecture

### Layer Structure

```
┌─────────────────────────────────────┐
│      Controllers (REST API)         │
│  - OrderController                  │
│  - FulfillmentController            │
│  - TrackingController               │
│  - OrganizationController           │
│  - WebsiteController                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Services (Business Logic)       │
│  - OrderService                      │
│  - FulfillmentService                │
│  - TrackingService                   │
│  - OrganizationService               │
│  - WebsiteService                    │
│  - TrackingEventService              │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Repositories (Data Access)         │
│  - JpaRepository                     │
│  - JpaSpecificationExecutor          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Database (MySQL)              │
│  - organizations                     │
│  - websites                          │
│  - orders                            │
│  - order_items                       │
│  - fulfillments                      │
│  - tracking                          │
│  - tracking_events                   │
└──────────────────────────────────────┘
```

### Request Flow

```
Client Request
    ↓
Controller (Validates Request, Maps to DTO)
    ↓
Service (Business Logic, Validation)
    ↓
Repository (Database Operations)
    ↓
Database
    ↓
Response (DTO → JSON)
```

---

## 📊 Data Model & Relationships

### Entity Hierarchy

```
Organization (1) ──→ (N) Website
    │
    └──→ (N) Order
            │
            ├──→ (N) OrderItem
            │
            └──→ (N) Fulfillment
                    │
                    └──→ (N) Tracking
                            │
                            └──→ (N) TrackingEvent
```

### Entity Details

#### 1. Organization
- **Purpose**: Top-level tenant entity
- **Key Fields**: id, name, externalId, status, createdAt, updatedAt
- **Relationships**: One-to-Many with Websites

#### 2. Website
- **Purpose**: E-commerce store/platform instance
- **Key Fields**: id, orgId, code, name, platform, domain, status
- **Relationships**: 
  - Many-to-One with Organization
  - One-to-Many with Orders

#### 3. Order
- **Purpose**: Customer order from e-commerce platform
- **Key Fields**: id, orgId, websiteId, externalOrderId, status, financialStatus, fulfillmentStatus, orderTotal
- **Relationships**:
  - Many-to-One with Organization & Website
  - One-to-Many with OrderItems & Fulfillments

#### 4. OrderItem
- **Purpose**: Individual line items in an order
- **Key Fields**: id, orderId, externalLineItemId, sku, name, quantity, price
- **Relationships**: Many-to-One with Order

#### 5. Fulfillment
- **Purpose**: Shipping/fulfillment record for an order
- **Key Fields**: id, orderId, externalFulfillmentId, status, carrier, shippedAt, deliveredAt
- **Relationships**:
  - Many-to-One with Order
  - One-to-Many with Tracking

#### 6. Tracking
- **Purpose**: Shipping tracking information
- **Key Fields**: id, fulfillmentId, trackingNumber, carrier, status, isPrimary
- **Relationships**:
  - Many-to-One with Fulfillment
  - One-to-Many with TrackingEvents

#### 7. TrackingEvent
- **Purpose**: Individual tracking events (status updates from carrier)
- **Key Fields**: id, trackingId, eventTime, eventCode, eventDescription, source, eventHash
- **Relationships**: Many-to-One with Tracking

---

## 🔌 API Implementation

### API Design Principles

1. **RESTful Conventions**
   - Resource-based URLs
   - HTTP methods: GET, POST, PUT, PATCH, DELETE
   - Proper HTTP status codes (200, 201, 204, 400, 404)

2. **Pagination**
   - 0-based page index
   - Configurable page size (default: 50, max: 500)
   - Response includes: data, page, size, totalElements, totalPages, hasNext

3. **Filtering & Search**
   - Date range filtering (from/to parameters)
   - Status filtering
   - Text search (case-insensitive)
   - Multiple filter combinations

4. **Idempotency**
   - Order creation: Upsert by (orgId, websiteId, externalOrderId)
   - Tracking creation: Upsert by trackingNumber
   - Tracking events: Deduplication by eventHash

### Key Endpoints

#### Organizations
- `POST /organizations` - Create organization
- `GET /organizations` - List with filters (date, status, name)
- `GET /organizations/{id}` - Get by ID
- `GET /organizations/search?externalId=...` - Search by external ID
- `PUT /organizations/{id}` - Full update
- `PATCH /organizations/{id}` - Partial update
- `DELETE /organizations/{id}` - Delete

#### Websites
- `POST /organizations/{orgId}/websites` - Create website
- `GET /organizations/{orgId}/websites` - List with filters
- `GET /organizations/{orgId}/websites/search` - Search by code/domain
- `GET /organizations/{orgId}/websites/{websiteId}` - Get by ID
- `PUT /organizations/{orgId}/websites/{websiteId}` - Full update
- `PATCH /organizations/{orgId}/websites/{websiteId}` - Partial update
- `DELETE /organizations/{orgId}/websites/{websiteId}` - Delete

#### Orders
- `POST /orders` - Create/upsert order (with items)
- `GET /orders?orgId=...&websiteId=...` - List with filters
- `GET /orders/search?orgId=...&externalOrderId=...` - Search
- `GET /orders/{orderId}` - Get by ID
- `PUT /orders/{orderId}` - Full update
- `PATCH /orders/{orderId}` - Partial update
- `DELETE /orders/{orderId}` - Delete

#### Fulfillments
- `POST /orders/{orderId}/fulfillments` - Create fulfillment
- `GET /orders/{orderId}/fulfillments` - List with filters
- `GET /orders/{orderId}/fulfillments/search?externalFulfillmentId=...` - Search
- `GET /orders/{orderId}/fulfillments/{fulfillmentId}` - Get by ID
- `PUT /orders/{orderId}/fulfillments/{fulfillmentId}` - Full update
- `PATCH /orders/{orderId}/fulfillments/{fulfillmentId}` - Partial update
- `DELETE /orders/{orderId}/fulfillments/{fulfillmentId}` - Delete

#### Tracking
- `POST /fulfillments/{fulfillmentId}/tracking` - Create tracking
- `GET /fulfillments/{fulfillmentId}/tracking` - List with filters
- `GET /fulfillments/{fulfillmentId}/tracking/search?trackingNumber=...` - Search
- `GET /fulfillments/{fulfillmentId}/tracking/{trackingId}` - Get by ID
- `PUT /fulfillments/{fulfillmentId}/tracking/{trackingId}` - Full update
- `PATCH /fulfillments/{fulfillmentId}/tracking/{trackingId}` - Partial update
- `POST /fulfillments/{fulfillmentId}/tracking/{trackingId}/events` - Create tracking event
- `DELETE /fulfillments/{fulfillmentId}/tracking/{trackingId}` - Delete

---

## 💼 Business Logic & Features

### 1. Order Fulfillment Status Auto-Update

**Location**: `FulfillmentService.updateOrderFulfillmentStatus()`

**Logic**:
- When fulfillment is created/updated/deleted, order fulfillment status is recalculated
- Rules:
  - No fulfillments → UNFULFILLED
  - All fulfillments DELIVERED → FULFILLED
  - All fulfillments CANCELLED → CANCELLED
  - Any fulfillment SHIPPED/DELIVERED → PARTIAL
  - Otherwise → UNFULFILLED

**Why it matters**: Ensures order status always reflects current fulfillment state

### 2. Tracking Status Auto-Update

**Location**: `TrackingEventService.updateTrackingRollup()`

**Logic**:
- When tracking event is created, tracking status is updated based on event code
- Rules:
  - "DELIVERED" → DELIVERED
  - "OUT_FOR_DELIVERY" → OUT_FOR_DELIVERY
  - "IN_TRANSIT" or "SHIPPED" → IN_TRANSIT
  - "EXCEPTION" or "FAIL" → EXCEPTION
  - "LABEL_CREATED" or "PICKUP" → LABEL_CREATED
- Updates `lastEventAt` to most recent event time

**Why it matters**: Real-time tracking status without manual updates

### 3. Idempotent Operations

**Order Creation**:
- Checks if order exists by (orgId, websiteId, externalOrderId)
- If exists → updates existing order
- If not → creates new order

**Tracking Creation**:
- Checks if tracking exists by trackingNumber
- If exists → updates existing tracking
- If not → creates new tracking

**Tracking Events**:
- Generates SHA-256 hash from (orgId + trackingId + eventTime + eventCode)
- Checks if event with same hash exists
- Prevents duplicate events

**Why it matters**: Safe to retry operations, prevents duplicate data

### 4. Dynamic Query Building

**Implementation**: Using JPA Specifications

**Purpose**: Build complex queries dynamically based on provided filters

**Example**:
```java
Specification<Order> spec = (root, query, cb) -> {
    var predicates = new ArrayList<Predicate>();
    predicates.add(cb.equal(root.get("orgId"), orgId));
    if (from != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("ingestedAt"), from));
    }
    if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
    }
    return cb.and(predicates.toArray(new Predicate[0]));
};
```

**Why it matters**: Flexible filtering without multiple repository methods

### 5. Partial Updates (PATCH)

**Implementation**: Only updates non-null fields in request

**Example** (Fulfillment PATCH):
```java
if (request.getStatus() != null) fulfillment.setStatus(request.getStatus());
if (request.getCarrier() != null) fulfillment.setCarrier(request.getCarrier());
// Only specified fields are updated
```

**Why it matters**: Allows selective updates without sending entire object

---

## 🔄 Request Flow Examples

### Flow 1: Create Order with Items

```
1. POST /orders
   ↓
2. OrderController.createOrder()
   - Validates request
   - Maps OrderRequest to Order entity
   ↓
3. OrderService.createOrder()
   - Validates orgId and websiteId exist
   - Checks for existing order (upsert)
   - Generates IDs for order and items
   - Sets default statuses if not provided
   ↓
4. OrderRepository.save()
   - Saves order and items (cascade)
   ↓
5. Returns OrderResponse with items
```

### Flow 2: Create Fulfillment and Update Order Status

```
1. POST /orders/{orderId}/fulfillments
   ↓
2. FulfillmentController.createFulfillment()
   ↓
3. FulfillmentService.createFulfillment()
   - Validates order exists
   - Creates fulfillment
   - Calls updateOrderFulfillmentStatus()
   ↓
4. updateOrderFulfillmentStatus()
   - Fetches all fulfillments for order
   - Calculates new fulfillment status
   - Updates order.fulfillmentStatus
   ↓
5. Returns FulfillmentResponse
```

### Flow 3: Create Tracking Event and Update Tracking Status

```
1. POST /fulfillments/{fulfillmentId}/tracking/{trackingId}/events
   ↓
2. TrackingController.createTrackingEvent()
   ↓
3. TrackingEventService.ingestEvent()
   - Generates event hash for idempotency
   - Checks for duplicate event
   - Creates event
   - Calls updateTrackingRollup()
   ↓
4. updateTrackingRollup()
   - Updates tracking.lastEventAt
   - Updates tracking.status based on event code
   ↓
5. Returns TrackingEventResponse
```

---

## 🎨 Key Design Decisions

### 1. Why DTOs?
- **Separation of Concerns**: Entities are for persistence, DTOs for API
- **Security**: Prevents exposing internal fields
- **Flexibility**: Can transform data between layers
- **Versioning**: Can change DTOs without affecting entities

### 2. Why Specifications?
- **Flexibility**: Dynamic query building
- **Maintainability**: Single method handles multiple filter combinations
- **Performance**: Only adds predicates that are needed

### 3. Why Cascade Operations?
- **Data Integrity**: OrderItems deleted when Order deleted
- **Convenience**: Single save operation for parent and children
- **Consistency**: Ensures related data stays in sync

### 4. Why Upsert Pattern?
- **Idempotency**: Safe to retry operations
- **Real-world**: External systems may send same data multiple times
- **Data Quality**: Prevents duplicates

### 5. Why Auto Status Updates?
- **Data Consistency**: Status always reflects current state
- **User Experience**: Real-time accurate information
- **Reduced Errors**: No manual status updates needed

---

## 📚 Interview Preparation

### Technical Questions You Should Be Ready For

#### 1. Architecture & Design

**Q: Why did you choose Spring Boot?**
- Rapid development with auto-configuration
- Production-ready features (actuators, metrics)
- Large ecosystem and community support
- Easy integration with Spring Data JPA

**Q: Explain your layered architecture.**
- **Controller Layer**: Handles HTTP requests/responses, validation
- **Service Layer**: Business logic, transactions, orchestration
- **Repository Layer**: Data access, database operations
- **Model Layer**: Entities representing database tables

**Q: Why use DTOs instead of returning entities directly?**
- Prevents lazy loading issues
- Hides internal implementation
- Allows data transformation
- Better API versioning

#### 2. Database & JPA

**Q: How do you handle relationships?**
- One-to-Many: `@OneToMany` with `@ManyToOne` on child
- Cascade operations for data integrity
- Lazy loading to avoid N+1 problems
- Bidirectional relationships where needed

**Q: Explain JPA Specifications.**
- Programmatic way to build queries
- Type-safe query building
- Composable predicates
- Alternative to QueryDSL or Criteria API

**Q: How do you handle pagination?**
- Spring Data's `Pageable` interface
- 0-based page index
- Configurable page size
- Returns `Page<T>` with metadata

**Q: What's the difference between PUT and PATCH?**
- **PUT**: Full replacement, all fields required
- **PATCH**: Partial update, only specified fields updated
- PUT is idempotent, PATCH can be idempotent

#### 3. Business Logic

**Q: How does order fulfillment status get updated?**
- Triggered when fulfillment is created/updated/deleted
- Fetches all fulfillments for the order
- Applies business rules:
  - All delivered → FULFILLED
  - All cancelled → CANCELLED
  - Any shipped → PARTIAL
  - None → UNFULFILLED

**Q: How do you prevent duplicate tracking events?**
- Generate SHA-256 hash from (orgId + trackingId + eventTime + eventCode)
- Check if event with same hash exists
- If exists, return existing event (idempotent)
- If not, create new event

**Q: Explain the upsert pattern for orders.**
- Check if order exists by natural key: (orgId, websiteId, externalOrderId)
- If exists: Update existing order
- If not: Create new order
- Prevents duplicates from retries

#### 4. API Design

**Q: Why RESTful design?**
- Standard HTTP methods map to operations
- Resource-based URLs are intuitive
- Stateless and scalable
- Easy to understand and consume

**Q: How do you handle filtering and search?**
- Query parameters for filters
- JPA Specifications for dynamic queries
- Date range filtering with `from` and `to`
- Case-insensitive text search
- Multiple filter combinations

**Q: How do you handle errors?**
- Custom exception: `ResourceNotFoundException`
- Spring's `@ExceptionHandler` (if implemented)
- Proper HTTP status codes (404, 400, 500)
- Error response with message and path

#### 5. Performance & Scalability

**Q: How would you optimize this for high traffic?**
- **Caching**: Redis for frequently accessed data
- **Database Indexing**: Index on foreign keys and search fields
- **Connection Pooling**: HikariCP (default in Spring Boot)
- **Async Processing**: For non-critical updates
- **Read Replicas**: For read-heavy operations

**Q: How do you handle database transactions?**
- `@Transactional` on service methods
- Automatic rollback on exceptions
- Read-only transactions for queries
- Propagation settings for nested transactions

**Q: What about N+1 query problems?**
- Use `@EntityGraph` or `JOIN FETCH` for eager loading
- Lazy loading with proper session management
- Batch fetching for collections
- DTO projections to avoid loading unnecessary data

#### 6. Testing

**Q: How would you test this application?**
- **Unit Tests**: Service layer with mocked repositories
- **Integration Tests**: TestContainers for database
- **API Tests**: MockMvc or REST Assured
- **Repository Tests**: @DataJpaTest with in-memory database

**Q: What's your testing strategy?**
- Test business logic in services
- Test API contracts in controllers
- Test data access in repositories
- Test error scenarios and edge cases

### Behavioral Questions

**Q: Tell me about a challenging problem you solved.**
- **Example**: Implementing idempotent operations
- **Challenge**: Preventing duplicate data from retries
- **Solution**: Hash-based deduplication for events, natural key lookup for orders
- **Result**: Safe to retry operations, no duplicates

**Q: How do you ensure code quality?**
- Code reviews
- Unit tests
- Following SOLID principles
- Consistent coding style (Lombok, naming conventions)

**Q: How do you handle changing requirements?**
- Flexible design (Specifications for dynamic queries)
- DTOs allow API changes without entity changes
- Service layer abstraction allows business logic changes

### Code Walkthrough Preparation

Be ready to explain:

1. **OrderService.createOrder()**
   - Upsert logic
   - ID generation
   - Default value setting
   - Item handling

2. **FulfillmentService.updateOrderFulfillmentStatus()**
   - Status calculation logic
   - Business rules
   - Why it's called after CRUD operations

3. **TrackingEventService.ingestEvent()**
   - Hash generation
   - Idempotency check
   - Status rollup logic

4. **Any Controller method**
   - Request validation
   - Parameter binding
   - Response mapping
   - Error handling

### System Design Questions

**Q: How would you scale this system?**
- **Horizontal Scaling**: Multiple application instances
- **Load Balancer**: Distribute requests
- **Database**: Read replicas, sharding by orgId
- **Caching**: Redis for hot data
- **Message Queue**: For async processing

**Q: How would you handle high write volume?**
- **Batch Inserts**: For bulk operations
- **Async Processing**: For non-critical updates
- **Database Optimization**: Indexes, partitioning
- **Connection Pooling**: Tune pool size

**Q: How would you ensure data consistency?**
- **Transactions**: ACID properties
- **Optimistic Locking**: Version fields
- **Eventual Consistency**: For distributed systems
- **Saga Pattern**: For distributed transactions

### Common Follow-up Questions

1. **What would you improve?**
   - Add caching layer
   - Implement comprehensive error handling
   - Add API versioning
   - Implement audit logging
   - Add rate limiting
   - Implement authentication/authorization

2. **How would you monitor this?**
   - Spring Boot Actuator for metrics
   - Logging with structured logs
   - APM tools (New Relic, Datadog)
   - Database query monitoring
   - Error tracking (Sentry)

3. **Security considerations?**
   - Authentication (JWT, OAuth2)
   - Authorization (RBAC)
   - Input validation
   - SQL injection prevention (JPA handles this)
   - Rate limiting
   - HTTPS only

---

## 🎯 Key Takeaways for Interview

### Strengths to Highlight

1. **Clean Architecture**: Well-separated layers
2. **RESTful Design**: Follows best practices
3. **Business Logic**: Auto-status updates show understanding of domain
4. **Idempotency**: Production-ready patterns
5. **Flexibility**: Dynamic queries, partial updates
6. **Data Integrity**: Proper relationships and cascades

### Areas to Discuss

1. **Error Handling**: Could be more comprehensive
2. **Caching**: Not implemented but aware of need
3. **Security**: Authentication/authorization not implemented
4. **Testing**: Mention testing strategy even if not fully implemented
5. **Monitoring**: Discuss observability needs

### Be Confident About

- Understanding of Spring Boot ecosystem
- REST API design principles
- Database relationships and JPA
- Business logic implementation
- Code organization and structure

---

## 📝 Quick Reference

### Entity Relationships
```
Organization 1:N Website
Website 1:N Order
Order 1:N OrderItem
Order 1:N Fulfillment
Fulfillment 1:N Tracking
Tracking 1:N TrackingEvent
```

### Status Enums
- **OrderStatus**: CREATED, CANCELLED, CLOSED
- **FinancialStatus**: UNKNOWN, PENDING, PAID, PARTIALLY_PAID, REFUNDED, PARTIALLY_REFUNDED, VOIDED
- **FulfillmentOverallStatus**: UNFULFILLED, PARTIAL, FULFILLED, CANCELLED, UNKNOWN
- **FulfillmentStatus**: CREATED, SHIPPED, DELIVERED, CANCELLED, FAILED, UNKNOWN
- **TrackingStatus**: LABEL_CREATED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION, UNKNOWN

### Key Patterns Used
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: API layer separation
- **Specification Pattern**: Dynamic queries
- **Upsert Pattern**: Idempotent operations
- **Cascade Pattern**: Related data management

---

Good luck with your interview! 🚀
