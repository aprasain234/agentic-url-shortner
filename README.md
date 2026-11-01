# Agentic URL Shortener & Software Engineering Orchestrator

An autonomous, LLM-powered URL shortening service built for the Charles Schwab technical assessment. This application utilizes Java 21, Spring Boot 3.3+, and LangChain4j to orchestrate an AI-driven Software Development Life Cycle (SDLC) that safely interprets requirements, designs solutions, and generates implementation code under strict human governance.

## Prerequisites
- **Java 21**
- **Maven 3.8+**
- **OpenAI API Key** (Funded account required for `gpt-4o-mini`)

## Environment Setup & Execution
The application requires an OpenAI API key exposed as an environment variable to bypass rate limits and ensure stable agent execution.

1. **Configure Environment Variable**:
   In your terminal or IntelliJ IDEA Run/Debug Configurations, add:
   `export OPENAI_API_KEY="your_actual_api_key_here"`

2. **Build the Project**:
   To compile the project and bypass the OpenAI context initialization during automated testing, use the provided dummy key override:
   `mvn clean install -DargLine="-DOPENAI_API_KEY=dummy-test-key"`
   *(Alternatively, skip tests entirely via `mvn clean install -DskipTests`)*

3. **Run the Application**:
   `mvn spring-boot:run`
   The service will start on `http://localhost:8080`.

## Core API Endpoints (Baseline URL Shortener)
- **POST** `/api/url/shorten`: Accepts a JSON payload `{"originalUrl": "https://..."}` and returns a short key.
- **GET** `/api/url/{shortKey}`: Redirects to the original URL and records analytics (IP, User-Agent).
- **GET** `/api/url/{shortKey}/stats`: Returns aggregate click analytics.

## Agentic Orchestrator Execution Scenarios

### 1. Greenfield Requirement
Test the system's ability to build a feature from scratch.
- **POST** `http://localhost:8080/api/orchestrator/run`
- **Body**: `{"requirement": "Build a REST API to shorten a long URL and return a 6-character alphanumeric code."}`

### 2. Brownfield Enhancement
Test the system's ability to modify existing domain logic.
- **POST** `http://localhost:8080/api/orchestrator/run`
- **Body**: `{"requirement": "Enhance existing URL Shortener by introducing rate-limiting per IP (max 10 redirects per minute) and soft-deletion for expired links."}`

### 3. Ambiguous Request
Test the Requirements Agent's ability to identify missing constraints.
- **POST** `http://localhost:8080/api/orchestrator/run`
- **Body**: `{"requirement": "Make the URL shortener highly secure and fast."}`
- **Expected Result**: The agent will pause at `PENDING_HUMAN_APPROVAL` and output a Markdown list of clarifying questions regarding definitions of "secure" (e.g., Auth? Hashing?) and "fast" (e.g., Caching? Latency SLA?).

## Human-in-the-Loop (HITL) Governance
Once a workflow reaches `PENDING_HUMAN_APPROVAL`, human review is enforced. You can either approve the architectural analysis to generate code or provide feedback to dynamically re-plan.

- **Approve**:
  **POST** `http://localhost:8080/api/orchestrator/run/{id}/approve` (Empty Body)
  Advances the workflow to `RELEASE_READY` and triggers the Coder Agent to generate implementation code.
- **Provide Feedback**:
  **POST** `http://localhost:8080/api/orchestrator/run/{id}/feedback`
  **Body**: `{"feedback": "Use Redis for caching instead of in-memory."}`
  Rejects the current analysis, dynamically re-plans the task incorporating the feedback, and pushes the state back to `REQUIREMENT_ANALYSIS`.

## Database & Observability

### H2 In-Memory Database
The application uses an in-memory H2 database for zero-configuration testing. Access the console via:
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:urldb`
- **Username**: `sa` (No password)
  *Query `EXECUTION_CONTEXT` to view agent memory/generated code, and `CLICK_EVENT` for URL redirection analytics.*

### Audit-Grade Observability
Audit-grade observability is implemented via Spring AOP. A custom interceptor (`OrchestratorMetricsAspect`) wraps all orchestrator operations. Monitor the terminal console during execution to view exact latency metrics and success rates tagged with `[AUDIT METRIC]`.

---

## Final Engineering Summary

### Architecture Overview
The system is divided into two strict boundaries to isolate deterministic application logic from non-deterministic LLM operations. The core URL shortener operates synchronously, prioritizing low-latency redirection and lightweight analytics capture via JPA `ClickEvent` entities. The AI Orchestrator operates as an independent state machine (`WorkflowState`), tracking execution contexts, agent memory, and requirement iterations in the database.

### Addressing Core Rubric Requirements
* **Human-in-the-Loop Governance**: The orchestrator enforces strict state transitions. A `GlobalExceptionHandler` enforces governance by returning HTTP 400 Bad Request if an out-of-sequence transition is attempted. The bidirectional feedback loop allows reviewers to inject human feedback, forcing the LLM to discard the current plan, re-analyze the requirement, and generate a new context memory.
* **Audit-Grade Observability**: Relying on external LLM APIs introduces unpredictable latency. To provide enterprise-grade telemetry, a Spring AOP interceptor automatically measures execution time and success/failure rates, logging them to standard output. This decouples observability from business logic while providing the raw data necessary for downstream APM ingestion.

### Design Decisions & Trade-offs
* **Synchronous Retries vs. Asynchronous Message Brokers**: The orchestrator utilizes synchronous execution with exponential backoff (`Thread.sleep`) to handle LLM API rate limits. While a robust enterprise system would offload this to a message broker (Kafka) with dead-letter queues, synchronous blocking was selected for this prototype to minimize infrastructure dependencies and ensure a zero-config, single-command executable for reviewers.
* **Database Persistence vs. Local File Execution**: Generated implementation code is stored as text blocks inside the `EXECUTION_CONTEXT` table rather than written to the local file system. Executing generated code dynamically or writing to the local host machine presents severe security vulnerabilities. Storing the code in the database maintains a strict sandbox boundary, proving the orchestrator's generative capabilities while adhering to zero-trust security principles.
* **State Machine Implementation**: A custom enum-based state machine was chosen over a heavy orchestration framework (like Temporal or Spring State Machine). This provides a lightweight, highly readable implementation that directly maps to JPA entity states and ensures transaction safety without external overhead.