# Salesforce Pub/Sub API Spring Boot Client

A robust Spring Boot application demonstrating how to integrate with the [Salesforce Pub/Sub API](https://developer.salesforce.com/docs/platform/pub-sub-api/guide/intro.html) using gRPC. This project handles authentication, schema management, event publishing, and subscription with built-in resilience and observability.

## Features

- **gRPC Integration:** Direct communication with Salesforce Pub/Sub API using Google's gRPC framework.
- **Authentication:** Implements OAuth 2.0 JWT Bearer Token flow for secure server-to-server communication.
- **Schema Management:**
  - Automatic fetching of Avro schemas for topics.
  - **Caching:** In-memory caching of parsed schemas to optimize performance and reduce network calls.
- **Resilience:**
  - **Retries:** Built-in retry logic (using Spring Retry) for critical network operations (Publish, GetTopic, GetSchema) to handle transient failures.
  - **Health Checks:** Periodic channel health checks.
- **Observability:** Integrated with Micrometer and Prometheus for metrics collection.

## Prerequisites

- **Java 21** or later
- **Maven 3.8+**
- **Salesforce Org:**
  - Pub/Sub API enabled.
  - A [Connected App](https://help.salesforce.com/s/articleView?id=sf.connected_app_create.htm) configured with:
    - "Enable OAuth Settings" checked.
    - "Use digital signatures" checked (upload your public key).
    - Selected OAuth Scopes: `api`, `refresh_token`, `offline_access`.
  - A private key file (RSA) matching the public key uploaded to the Connected App.

### Certificate Generation (Windows/PowerShell)

A helper script `generate-cert.ps1` is included to generate a self-signed certificate and private key in the correct format (PKCS#8 PEM) for this application.

**Usage:**

1. Open PowerShell in the project root.
2. Run:

   ```powershell
   .\generate-cert.ps1
   ```

3. The script will generate:
   - `public-key.crt`: Upload this to your Salesforce Connected App ("Use digital signatures").
   - `private-key.pem`: Place this in `src/main/resources/` (or configure `salesforce.jwt.private-key-path`).

## Configuration

The application is configured via `application.properties` or `application.yml`. You can also use environment variables.

### Key Properties

| Property | Description | Default |
| :--- | :--- | :--- |
| `salesforce.jwt.login-url` | Salesforce Login URL (e.g., `https://login.salesforce.com`) | N/A |
| `salesforce.jwt.client-id` | Connected App Consumer Key | N/A |
| `salesforce.jwt.username` | Salesforce Username | N/A |
| `salesforce.jwt.private-key-path` | Private Key. Supports: File Path, Classpath (`classpath:`), or Raw Content (`-----BEGIN...`) | N/A |
| `pubsub.grpc.host` | Pub/Sub API Host | `api.pubsub.salesforce.com` |
| `pubsub.grpc.port` | Pub/Sub API Port | `7443` |
| `pubsub.schema.cache-ttl-minutes` | Schema cache TTL in minutes | `60` |

### Example `application.properties`

```properties
# Salesforce Authentication
salesforce.jwt.login-url=https://login.salesforce.com
salesforce.jwt.client-id=YOUR_CONNECTED_APP_CLIENT_ID
salesforce.jwt.username=your-username@example.com
salesforce.jwt.private-key-path=classpath:keys/private-key.pem

# Pub/Sub API Configuration
pubsub.grpc.host=api.pubsub.salesforce.com
pubsub.grpc.port=7443

# Resilience & Performance
pubsub.schema.cache-ttl-minutes=60
pubsub.event-processing.thread-pool-size=10
```

## Building and Running

1. **Clone the repository:**

   ```bash
   git clone https://github.com/your-repo/salesforce-pub-sub-api-springboot.git
   cd salesforce-pub-sub-api-springboot
   ```

2. **Place your private key:**
   Put your `private-key.pem` in `src/main/resources/keys/` (or update the path in config).

3. **Build the project:**

   ```bash
   mvn clean install
   ```

4. **Run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Architecture

### Key Components

- **`PubSubService`**: The core service wrapper around the gRPC stub. It handles the low-level gRPC calls for publishing, subscribing, and fetching schemas. It includes `@Retryable` annotations for fault tolerance.
- **`TopicSchema`**: Manages Avro schemas. It fetches schema IDs from topics, retrieves the schema JSON, parses it, and caches the result to avoid redundant lookups.
- **`SalesforceSessionTokenService`**: Handles the OAuth 2.0 JWT Bearer flow. It exchanges a signed JWT for a Salesforce session token, which is then used as authentication credentials for gRPC calls.
- **`SalesforceJwtTokenService`**: Generates and signs the JWT using the configured private key.

## Observability

The application exposes metrics at `/actuator/prometheus` (if configured). Key metrics include:

- gRPC channel state.
- Retry attempts (via Spring Retry metrics if enabled).
- Application health.

## License

[MIT](LICENSE)
