# syntax=docker/dockerfile:1
# =========================================================================
# Build stage — install deps and produce the optimized Angular bundle.
# Real Node 24.15 runs the Angular CLI (Bun's node-compat version is too low
# for Angular 22's engine check); Bun is used only for fast installs.
# =========================================================================
FROM node:24.15.0-slim AS build
WORKDIR /app
COPY --from=oven/bun:1.3.14 /usr/local/bin/bun /usr/local/bin/bun

# Lockfile-first install for cache-friendly, reproducible dependencies.
COPY package.json bun.lock ./
RUN --mount=type=cache,target=/root/.bun/install/cache \
    bun install --frozen-lockfile

COPY . .
RUN node node_modules/@angular/cli/bin/ng.js build

# =========================================================================
# Runtime stage — serve the static SPA with Bun, no nginx
# =========================================================================
FROM oven/bun:1.3.14-slim AS runtime
WORKDIR /app
ENV PORT=4000

# Built browser assets + the tiny static server.
COPY --from=build /app/dist/eval-suite-frontend/browser ./browser
COPY --from=build /app/server.ts ./server.ts

# oven/bun images ship a non-root "bun" user.
USER bun
EXPOSE 4000

ENTRYPOINT ["bun", "run", "server.ts"]
