# Project

The assignment for this project is in `eval_suite_assignment.md` in this project's root.
The project is a full-stack application using Angular 22 for the frontend, Java 25 + Springboot 4 for the backend, and a PostgreSQL database. The goal is to follow 2026 SOTA best practices for each layer, using the latest stable versions of Angular, Java, and Spring Boot.

# Capabilities

The capabilities of the project are defined in `capabilities.md` in this project's root.

# Tooling

This repo uses `mise` for tool versions and common tasks in `mise.toml`. Prefer `mise` tasks when running project-wide checks, formatting, building or testing.

- `mise run check` runs frontend and backend checks.
- `mise run apply` applies formatting and autofixable lint changes.

# Angular

You are an expert in TypeScript, Angular, and scalable web application development. You write functional, maintainable, performant, and accessible code following Angular and TypeScript best practices.

## Angular MCP

This repo uses the `angular-cli` MCP server.

Use Angular MCP for Angular-specific work when the task depends on current Angular behavior, official best practices, workspace structure, migrations, APIs, CLI behavior, or version-specific details. Do not ask the user to remind you to use it.

- Use `list_projects` when workspace/project structure matters.
- Use `get_best_practices` before generating or substantially changing Angular code.
- Use `search_documentation` for Angular API, CLI, migration, or version-specific details.
- Use `onpush_zoneless_migration` for change detection or zoneless migration planning.

For trivial edits that are fully determined by nearby code, do not call MCP unless uncertainty appears.

## TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain

## Angular 22 Defaults

- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators. It's the default in Angular v20+.
- Do NOT create new NgModules for application code unless a compatibility boundary requires it.
- Use the Angular v22 application builder (`@angular/build:application`) patterns already present in the workspace.
- Prefer the Angular CLI for generation, migrations, builds, and tests. Use `ng generate`, `ng update`, and Angular schematics rather than hand-writing generated structure when possible.
- Follow the 2025 Angular style guide naming style for new files unless nearby files use an older convention.
- Use the framework defaults for new Angular 22 code: standalone APIs, strict typing, modern build tooling, and zoneless-compatible patterns.

## Angular Best Practices

- Use signals for state management
- Use `ChangeDetectionStrategy.OnPush` for application components unless there is a specific reason not to.
- Write code that is compatible with zoneless change detection. Do not rely on ZoneJS to notice state changes.
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
    - `NgOptimizedImage` does not work for inline base64 images.
- Use `afterNextRender`/`afterEveryRender` instead of `NgZone.onStable`, `NgZone.onUnstable`, `NgZone.onMicrotaskEmpty`, or `NgZone.isStable`.
- `NgZone.run` and `NgZone.runOutsideAngular` can remain when they serve a real interop/performance purpose.

## Accessibility Requirements

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.

### Components

- Keep components small and focused on a single responsibility
- Use `input()` and `output()` functions instead of decorators
- Use signal-based inputs, outputs, view queries, and content queries instead of decorator-based APIs when available.
- Use `computed()` for derived state
- Use `model()` only for intentional two-way binding APIs.
- Prefer inline templates for small components
- Prefer Signal Forms for new form work.
- Use Reactive Forms only when Signal Forms are a poor fit, existing code already uses Reactive Forms, or an integration requires Reactive Forms.
- Do not use Template-driven forms for new code.
- Do NOT use `ngClass`, use `class` bindings instead
- Do NOT use `ngStyle`, use `style` bindings instead
- When using external templates/styles, use paths relative to the component TS file.
- Prefer component `imports` arrays with direct imports. Let the Angular Language Service/CLI manage imports where possible.

## State Management

- Use signals for local component state
- Use `computed()` for derived state
- Use `linkedSignal()` for writable state that must reset or derive from another signal while preserving user edits.
- Use `effect()` for synchronization with external systems, not for ordinary state propagation.
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals, use `update` or `set` instead
- Prefer `resource`, `rxResource`, and `httpResource` for async state and reactive data loading.
- Use `httpResource` for reactive read-only HTTP data that depends on signals.
- Avoid `httpResource` for mutations such as POST/PUT/DELETE; use `HttpClient` directly for those.
- Guard resource value reads with `hasValue()` before reading `value()`.

## Templates

- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Always provide a meaningful `track` expression in `@for` blocks. Prefer stable IDs over `$index`; use `$index` only for static collections.
- Use `@empty` for empty states in `@for` when the UI needs a fallback.
- Use `@let` for readable local template variables instead of repeating complex expressions.
- Use `@switch` with `@default never;` or `@default never(value);` for exhaustive checking of union-like states when appropriate.
- Use `@defer` for heavy, non-critical UI that can be lazy loaded. Include accessible `@placeholder`, `@loading`, and `@error` states when useful.
- Avoid deferring content visible in the initial viewport when it would harm LCP or cause layout shift.
- Avoid nested `@defer` blocks with identical triggers that create cascading loads.
- Use the async pipe to handle observables
- Do not assume globals like (`new Date()`) are available.

## Services

- Design services around a single responsibility
- Use the `providedIn: 'root'` option for singleton services
- Use the `inject()` function instead of constructor injection
- Use `provideHttpClient()` and functional providers/interceptors for HTTP setup.
- Keep services framework-agnostic where practical; put UI-specific state in components or focused facades.

## Routing and Rendering

- Prefer lazy loaded route components/features.
- Use functional route guards, resolvers, and interceptors.
- For SSR/SSG/hybrid rendering, use Angular's route-level render mode configuration rather than ad hoc server checks.
- When using SSR or prerendering, account for event replay and hydration. Avoid direct DOM mutations that can break hydration.
- Use incremental hydration with `@defer` for large server-rendered pages when it improves startup behavior.

## Testing

- Prefer the project's configured test runner. Angular 22 defaults to Vitest for new projects.
- In zoneless-compatible tests, prefer `await fixture.whenStable()` and signal-driven updates over repeated manual `fixture.detectChanges()` where practical.
- For `@defer` blocks, use Angular's defer block testing APIs when asserting placeholder/loading/error/complete states.
- Add focused tests for user-visible behavior, routing, forms, and state transformations.

## Modern Angular 17-22 Summary

- Angular v17 made standalone application authoring and the Vite/esbuild application builder the default for new apps.
- Angular v17 introduced built-in control flow and deferrable views; both are stable by Angular v18.
- Angular v18 expanded stable deferrable views and introduced experimental zoneless support.
- Angular v19 advanced incremental hydration, route-level render modes, event replay, and zoneless support.
- Angular v20 made standalone the implicit component default, so `standalone: true` should not be written.
- Angular v20 stabilized core signal primitives including `signal`, `computed`, `effect`, `linkedSignal`, signal inputs, and signal queries.
- Angular v20 stabilized incremental hydration and route-level render modes.
- Angular v20.2 stabilized zoneless Angular.
- Angular v21 made zoneless the default for new apps and made Vitest the primary test runner for new projects.
- Angular v22 continues the modern defaults: standalone, signals, native control flow, deferrable views, zoneless-compatible components, Signal Forms, resources, `httpResource`, CLI AI config, and MCP-assisted tooling.

When these instructions conflict with current Angular MCP documentation for the installed framework version, trust Angular MCP and update the instructions if the change is durable.

# Java and Spring Boot

You are an expert in Java 25, Spring Boot 4, Spring Framework 7, and production-grade backend development. You write simple, maintainable, observable, and secure code that follows modern Java and Spring conventions.

For Java/Spring work, prefer current official Spring, Java, and project build information over model memory.

## Java 25

- Target Java 25 for new code unless the project build says otherwise.
- Use modern Java features where they improve clarity: records for immutable DTOs/value carriers, sealed types for closed hierarchies, pattern matching, switch expressions, text blocks, and local `var` only when the inferred type remains obvious.
- Prefer immutable data structures and explicit domain types over primitive/stringly typed APIs.
- Avoid reflection-heavy or runtime-magic designs unless required by Spring integration.
- Do not use preview features unless the build explicitly enables preview.
- Keep code friendly to GraalVM/native image where practical: avoid unnecessary dynamic proxies, reflection, and classpath scanning tricks.

## Spring Boot 4

- Assume Spring Boot 4.x and Spring Framework 7.x conventions.
- Prefer constructor injection. Do not use field injection.
- Prefer Java configuration and auto-configuration over XML.
- Use `@ConfigurationProperties` for structured configuration.
- Use validation annotations for configuration, request DTOs, and domain boundaries.
- Prefer `RestClient` or HTTP interfaces for synchronous HTTP clients.
- Prefer `WebClient` only when reactive/non-blocking behavior is intentionally needed.
- Use `RestTestClient`/Spring test slices where appropriate.
- Use Actuator, Micrometer, structured logging, health checks, and OpenTelemetry conventions for production services.
- Prefer Testcontainers for integration tests involving databases, queues, object stores, or external infrastructure.
- Use Spring Boot managed dependency versions. Do not pin versions unless necessary.

## Spring Boot 4 Migration Defaults

- Use OpenRewrite recipes for large Spring Boot migrations instead of manual bulk edits.
- For Boot 4 migrations, prefer the OpenRewrite Spring Boot 4 recipes, then review and test the result.
- Expect Spring Boot 4 to align with Spring Framework 7, Jakarta EE 11, Jackson 3, JSpecify null-safety, Kotlin 2.2, and JUnit 6.
- Use the modular Spring Boot 4 starters where applicable.
- Treat Jackson 3 migration issues carefully; do not assume Jackson 2 behavior.
- Prefer JSpecify/null-safety-aware APIs and avoid ambiguous null contracts.

## Virtual Threads

- Prefer virtual threads for blocking servlet-style workloads when the app benefits from high concurrency.
- Enable with `spring.threads.virtual.enabled=true` when appropriate.
- If virtual threads are enabled and the app relies on scheduled/background work to keep running, set `spring.main.keep-alive=true`.
- Watch for pinned virtual threads and blocking synchronized sections.
