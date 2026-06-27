# Architecture Decision Records — ZipRun AI Reassignment Engine

---

## ADR-1: Where does routing logic live?

**Context**

The system needs to route orders to agents using multiple interchangeable strategies (rule-based, AI-powered, and a forthcoming zone-affinity strategy in Sprint 2). Routing is called from two distinct entry points: an HTTP endpoint for on-demand suggestions, and an async event listener during the agentic re-planning loop. The logic must be independently testable without starting the full application context.

**Options considered**

(a) Embed routing logic directly in the service layer (`ReassignmentService`) — fast to build, but creates a monolithic service that mixes routing decisions, persistence, and event handling.

(b) Place routing logic in domain entities (`Order.findBestAgent(...)`) — violates single responsibility; entities should not depend on repositories or external services.

(c) Dedicated routing layer with a `RoutingStrategy` interface and a `RoutingStrategyRegistry` — isolates routing logic behind a clean contract, leaving service classes responsible only for orchestration and persistence.

**Decision**

Chose option (c). The `RoutingStrategy` interface defines the contract (`recommend(RoutingContext) -> RoutingRecommendation`). Implementations (`RuleBasedRoutingStrategy`, `AIRoutingStrategy`) are Spring beans registered by key (`@Component("RULE")`, `@Component("AI")`). The `RoutingStrategyRegistry` selects and delegates to the active strategy. `ReassignmentService` calls the registry — it does not know which strategy is active.

**Tradeoffs accepted**

The additional layer adds indirection. A reader must understand that Spring auto-populates `Map<String, RoutingStrategy>` by bean name. The tradeoff is deliberate: both the HTTP path and the async event listener share the same registry call without any code duplication, and adding Sprint 2's `ZoneAffinityStrategy` requires implementing the interface and annotating the bean — nothing else changes.

---

## ADR-2: How does runtime strategy switching work?

**Context**

The active routing strategy must be switchable without a code change or application restart. Both call paths — the HTTP endpoint and the async event listener — must use the same active strategy. Sprint 2 adds a third strategy (`ZoneAffinityStrategy`). The switching mechanism must accommodate new strategies without modifying existing code.

**Options considered**

(a) `@Qualifier` with a hardcoded bean name — no runtime flexibility; requires a restart to switch.

(b) Manual factory with a switch statement — explicit but requires modifying the factory every time a new strategy is added. Violates open/closed principle.

(c) Auto-wired `Map<String, RoutingStrategy>` bean map, with the active key read from `application.properties` — Spring populates the map automatically at startup. Strategy selection is a single `map.get(activeKey)` call at runtime.

**Decision**

Chose option (c). The `routing.strategy` property (default: `AI`) drives selection. Both `ReassignmentService` (HTTP path) and `ReplanningEventListener` (async path) inject `RoutingStrategyRegistry`, which reads the config on every call. Adding `ZoneAffinityStrategy` in Sprint 2 means: implement the interface, annotate with `@Component("ZONE")`, set `routing.strategy=ZONE`. No existing file changes needed.

A `StartupValidator` validates at boot that the configured key resolves to a real strategy, surfacing misconfiguration immediately rather than on the first request.

**Tradeoffs accepted**

The bean-map approach is less explicit than a factory. The failure mode if `routing.strategy` is misconfigured shifts from compile time to startup time — mitigated by `StartupValidator`. Thread safety is not a concern here since strategy instances are stateless Spring singletons.

---

## ADR-3: How does the system stay resilient when the LLM is unavailable?

**Context**

`AIRoutingStrategy` calls Gemini Flash over HTTP. LLM calls can fail in several distinct ways: network timeouts, HTTP 429 quota exhaustion, malformed or non-JSON responses, and hallucinated agent IDs that don't exist in the roster. In the async re-planning path especially, a silent failure would leave stranded orders without any suggestion in the ops queue — which is worse than a rule-based suggestion.

**Options considered**

(a) Propagate exceptions to the caller — simple, but causes the re-planning loop to abort and leaves ops with no suggestion at all.

(b) Return a null/empty recommendation and let the caller decide — ambiguous; callers must check null which is error-prone.

(c) `AIRoutingStrategy` catches all failure modes internally and falls back to `RuleBasedRoutingStrategy`, setting `fallbackUsed = true` and recording `failureReason` on the recommendation — callers always receive a valid recommendation with a real agent.

**Decision**

Chose option (c). `AIRoutingStrategy.recommend()` wraps the entire LLM call in a try/catch. On any exception, it delegates to `RuleBasedRoutingStrategy` and marks `fallbackUsed = true`. Agent ID validation happens before accepting the AI response — if the model hallucinates an ID not in the available roster, it's treated as a parse failure and falls back. The `failureReason` field is persisted on the suggestion so ops can see that a fallback occurred and why.

In the async re-planning path (`ReassignmentService.replanForOfflineAgent`), even a failure of the registry call itself is caught — a second attempt with the `RULE` strategy is made before giving up on that order.

**Tradeoffs accepted**

Ops always sees a suggestion, but cannot always distinguish a high-confidence AI pick from a rule-based fallback at a glance — the `fallbackUsed` boolean and `strategy` field on `SuggestionResponse` address this. The fallback is intentionally not retried (no retry loop) to keep the async path simple and avoid hammering a quota-exhausted API.

---

## ADR-4: How is the agentic loop triggered and kept off the request path?

**Context**

When `PATCH /agents/{id}/status` sets an agent to `OFFLINE`, the system must identify all stranded orders and queue reassignment suggestions automatically. This re-planning can involve multiple LLM calls and database writes and must not block the HTTP response. The trigger must be event-driven — firing because something changed — not a polling timer.

**Options considered**

(a) Synchronous execution inside the PATCH handler — blocks the response until all LLM calls complete. Unacceptable for 200ms SLA expectations.

(b) Scheduled poller checking for offline agents every N seconds — introduces latency proportional to poll interval, and does not fire because something changed.

(c) `ApplicationEventPublisher` + `@EventListener` + `@Async` on the dedicated `replanningExecutor` thread pool — the PATCH handler publishes `AgentOfflineEvent` after saving the agent, then returns immediately. The listener picks it up on a separate thread pool.

**Decision**

Chose option (c). `AgentService.updateStatus()` calls `eventPublisher.publishEvent(new AgentOfflineEvent(agentId))` after the transaction commits. `ReplanningEventListener` is annotated `@Async("replanningExecutor")` and `@EventListener`, ensuring it runs on the dedicated `replanning-*` thread pool. The PATCH endpoint returns the updated agent immediately — the re-planning is fully invisible to the HTTP caller.

Idempotency is enforced in `ReassignmentService.processStrandedOrder()`: before creating a suggestion, it checks `existsByOrderIdAndTriggerReasonAndStatus(orderId, AGENT_OFFLINE, PENDING)`. Repeated offline events for the same agent do not produce duplicate suggestions.

**Extension seam for Sprint 3**: the `RoutingContext.triggerReason` field is already a typed enum (`INITIAL`, `AGENT_OFFLINE`). Adding SLA-breach detection in Sprint 3 means publishing a new event type, adding a listener, and adding `SLA_BREACH` to the enum. The routing contract, strategy implementations, and suggestion persistence code do not change.

**Tradeoffs accepted**

Spring's `ApplicationEventPublisher` does not offer retry or dead-letter semantics out of the box. A re-planning failure is logged at ERROR level (`[AGENTIC LOOP] Re-planning failed`) but does not retry. This is acceptable for a hackathon scope — production would warrant a durable event bus (Kafka, SQS) with retry and DLQ. This tradeoff is called out explicitly.

---

## ADR-5: What did we design to extend, and what did we deliberately leave for later?

**Context**

Five hours is not enough to build everything — but it is enough to build the right things. Explicit prioritisation decisions were made throughout.

**Extension seam: Sprint 2 ZoneAffinityStrategy**

The `Agent` entity already carries `currentZone` and the `Order` entity carries `pickupZone` and `dropoffZone` (all nullable). These fields are persisted in the schema today. Adding `ZoneAffinityStrategy` in Sprint 2 means: read these fields from the already-populated `RoutingContext`, implement the interface, register the bean as `@Component("ZONE")`, and set `routing.strategy=ZONE`. No migration needed for the domain model. The extension seam is the `RoutingStrategy` interface — it was designed with a third strategy in mind from the start.

**Deliberate exclusions**

*Full dispatch board (UI ceiling)*: deferred in favour of a functional ops floor showing the agentic re-plan path end to end. The re-plan badge appearing after an agent goes offline is the core correctness signal. A full board with SLA countdowns and zone maps is a visibility enhancement — valuable, but not a correctness requirement for Sprint 1.

*SSE streaming (`POST /orders/{id}/suggest/stream`)*: deferred. Token-by-token streaming is a UX enhancement. The non-streaming path is fully functional and easier to reason about under time pressure. Streaming can be added to `AIRoutingStrategy` without changing any other component — the extension point is clear.

*Persistent event log / retry for async failures*: the current `@Async` + `@EventListener` approach does not retry failed re-plans. A durable event bus would be the production answer. Deferred because it would require Kafka or a job table, both of which carry significant setup cost relative to their value in a hackathon scope. The failure mode is logged clearly enough for manual recovery.