# Eval Suite

A lightweight platform for versioning LLM system prompts and grading their outputs automatically, inspired by Braintrust, Humanloop and LangSmith. Built as a university project at TU Wien.

LLM outputs are non-deterministic, so a normal unit test can't tell you whether a prompt change made things better. Eval Suite runs a prompt against a dataset, has a second model grade every answer, and keeps the full history so prompt versions can be compared by score.

## Features

- **Versioned system prompts and datasets.** Both are immutable once created. A new version is saved instead of editing, so every run stays reproducible.
- **Runs.** A run executes a system prompt against every dataset item (input plus expected output).
- **LLM-as-judge.** A judge model compares each output to the expected output and returns a score from 0 to 100 with feedback. It then grades the system prompt itself and suggests an improvement.
- **Automatic prompt improvement.** Optionally, an improver agent rewrites the prompt based on the results and re-runs it on the same dataset. A new version is kept only if the score improves. Otherwise the loop stops (at most 10 iterations). Every intermediate version and run is stored and shown as a timeline.
- **Live progress.** Run status is streamed to the frontend via Server-Sent Events.

## Tech stack

| Layer    | Technology                                                |
| -------- | --------------------------------------------------------- |
| Frontend | Angular 22 (standalone, signals, zoneless)                |
| Backend  | Java 25, Spring Boot 4, Spring AI (OpenAI-compatible API) |
| Database | PostgreSQL 17                                             |
| LLM      | TU Wien Aqueduct (any OpenAI-compatible endpoint works)   |
| Tooling  | Docker Compose, mise                                      |

## Getting started

1. Copy the environment file:

   ```sh
   cp eval-suite-backend/.env.example eval-suite-backend/.env
   ```

2. Fill in your environment variables in the newly created `.env` file.

   > TU Wien Aqueduct works with the model and URL already specified, only the API key is needed.

3. Start the project in one of two ways:

   ### Docker only (no mise required)

   With Docker running, build and start the whole stack in one command:

   ```sh
   docker compose up --build
   ```

   | Service  | URL                   |
   | -------- | --------------------- |
   | Frontend | http://localhost:4000 |
   | Backend  | http://localhost:8080 |
   | Postgres | localhost:5433        |

   ### mise (local dev with hot reload)

   If you have mise-en-place installed, run:

   ```sh
   mise trust && mise install && mise dev
   ```

## Development

- `mise run check` checks formatting and linting for frontend and backend.
- `mise run apply` applies formatting and autofixable lint fixes.

This project was built with AI coding agents. Their instructions live in the repository: [`AGENTS.md`](AGENTS.md) (stack and conventions), `.agents/skills` (Angular and frontend design skills) and the Angular CLI MCP server configured in `.agents/mcp_config.json` and `.codex/config.toml`.

## Known limitations

- The judge is itself an LLM. Each prompt version is evaluated once, so small score differences can be noise.
- The improvement loop compares the judge's grade of the system prompt, not the average of the per-item output scores.
- The judge's JSON format is enforced by the prompt, not by the API's structured-output feature.
