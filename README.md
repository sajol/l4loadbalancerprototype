# L4 Load Balancer Prototype

This is a **Level 4 (L4) Load Balancer Prototype** implemented in **Kotlin**, showcasing the use of:
- **Asynchronous I/O** for efficient client-server communication.
- **Kotlin Coroutines** for high-performance concurrency.
- **Core Spring** for dependency injection and modular configuration.

The prototype supports multiple load balancing strategies:
- **Round Robin**
- **Weighted Round Robin**
- **Least Active Connection**

This is a minimal implementation to demonstrate the key concepts of load balancing and asynchronous server-client communication.

---

## Features
1. **Asynchronous Non-Blocking I/O**:
    - Uses `AsynchronousSocketChannel` for efficient handling of multiple connections.

2. **Kotlin Coroutines**:
    - Fully coroutine-based design for managing client-backend communication.

3. **Core Spring Configuration**:
    - Lightweight use of Spring for dependency injection and modularity.

4. **Custom Load Balancing Strategies**:
    - Easily extendable strategies implemented via the `LBStrategy` interface.

---

## Prerequisites
- Docker
- Java 17+
- Kotlin 1.9+
- Gradle

---

## Running the Prototype

### 1. Start Backend Services
Use the provided `docker-compose.yml` file to start the backend servers:
```bash
docker-compose up -d
```
The following services will be started:
- Backend 1: localhost:8081
- Backend 2: localhost:8082
- Backend 3: localhost:8083
- Backend 4: localhost:8084

### 2. Start the Load Balancer
Run the load balancer locally:
```bash
./gradlew run
```
The load balancer listens on localhost:8080.

### 3. Test the Load Balancer
Send requests to the load balancer:
```bash
curl localhost:8080
```
You should see responses from different backends, such as:
```csharp
Hello from Backend 1
Hello from Backend 2
Hello from Backend 3
Hello from Backend 4
```
---
### Changing the Load Balancing Strategy
To change the load balancing strategy, update the BackendConfigService class. Currently, the strategies are hardcoded to simulate behavior, but this can be made dynamic in the future.

## Example
In the `BackendConfigService`, modify the `getBackendConfig()` method:
```kotlin
private fun getBackendConfig(): BackendConfig {
    return roundRobinBackendConfig // Change to weightedRoundRobinBackendConfig or leastActiveConnectionBackendConfig
}
```
The available strategies are:
- RoundRobinStrategy
- WeightedRoundRobinStrategy
- LeastActiveConnectionStrategy

---
### How It Works
- The load balancer proxies requests between clients and backend servers.

- Backend selection is based on the configured load balancing strategy:
  - Round Robin: Cycles through backends sequentially.
  - Weighted Round Robin: Prioritizes backends with higher weights.
  - Least Active Connection: Selects the backend with the fewest active connections.
- The communication between the load balancer and backends is fully asynchronous, leveraging Kotlin coroutines for concurrency.

---
### Testing
Run the included tests to verify the load balancer functionality:

```bash
./gradlew test
```

The tests cover:
- Correct backend selection for each strategy.
- Concurrent request handling.

---
### Extending Prototype
- New Strategies: Implement the LBStrategy interface to add custom load balancing logic.
- Dynamic Backend Configuration: Replace the static backend list with a database-driven configuration.
- Health Checks: Add logic to exclude unhealthy backends.