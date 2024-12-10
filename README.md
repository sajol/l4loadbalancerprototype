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

---
## Load Testing Results

### Overview
The L4 Load Balancer prototype was subjected to load testing to validate its performance and scalability. Here are the highlights from the testing results:

### Results Summary
- **Concurrency**: Successfully handled **700 concurrent requests**.
- **Throughput**: Achieved **2686.5 requests/sec**.
- **Latency Distribution**:
    - **Fastest**: 49 ms
    - **Average**: 127.7 ms
    - **Slowest**: 259 ms
    - **95% of requests** completed in under **237 ms**.
- **Status Code**: 100% success with **700 HTTP 200 responses**.

### Observations
1. **Backend Utilization**:
    - The load balancer distributed requests evenly across 4 backend servers.
    - Each backend has a capacity of **170 concurrent connections**.

2. **System Stability**:
    - Proper tuning of the connection backlog and file descriptor limits ensured stable performance under high load.
    - No dropped connections or `connection reset by peer` errors were observed.

3. **Potential for Scaling**:
    - The system performed exceptionally well at its theoretical limit of **700 concurrent requests**.
    - With additional backend servers, the load balancer can scale further.

### Benchmark Details
The load testing was conducted using the `hey` tool:
```bash
hey -n 1000 -c 700 http://localhost:8080/
```
```text
Summary:
  Total:	0.2606 secs
  Slowest:	0.2593 secs
  Fastest:	0.0490 secs
  Average:	0.1277 secs
  Requests/sec:	2686.4969

Latency distribution:
  10% in 0.0600 secs
  25% in 0.0666 secs
  50% in 0.1170 secs
  75% in 0.1788 secs
  90% in 0.2175 secs
  95% in 0.2374 secs
  99% in 0.2567 secs
```

### Machine Specifications

The load testing was performed on the following setup:

- **CPU**: Intel Core i7-9750H (6 cores, 12 threads)
- **Memory**: 16GB DDR4
- **Operating System**: macOS Ventura 13.4
- **JVM Version**: OpenJDK 17.0.6
- **Network**: Localhost testing (requests routed directly to the load balancer and backends on the same machine)
- **Backend Servers**:
    - 4 backend servers running as **Docker containers** using the `hashicorp/http-echo` image.

#### Docker Configuration
- Docker Desktop version: `4.x` (for macOS)
- Resource Limits:
    - **CPU**: Default allocation (shared with host machine)
    - **Memory**: Default allocation (shared with host machine)

This configuration demonstrates how the load balancer performs when backends are hosted in Docker containers on the same machine. Resource contention between the load balancer and backend servers, as well as Docker’s virtualization overhead, should be considered when interpreting the results.