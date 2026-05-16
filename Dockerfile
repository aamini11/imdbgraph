# syntax=docker/dockerfile:1.7

ARG APP_ENV=production
ARG NODE_VERSION=24

FROM node:${NODE_VERSION}-bookworm-slim AS base
LABEL org.opencontainers.image.source="https://github.com/aamini-stack/projects"
ENV TURBO_TELEMETRY_DISABLED=1
ENV COREPACK_ENABLE_DOWNLOAD_PROMPT=0
ENV PNPM_HOME=/tmp/pnpm
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable && corepack install -g pnpm@10.33.0

FROM base AS pruner
WORKDIR /repo
COPY . .
RUN --mount=type=cache,id=s/8bd6805c-dd00-4311-912b-307c21466cc4-/pnpm/store,target=/pnpm/store \
	pnpm dlx turbo@^2 prune imdbgraph --docker

FROM base AS builder
WORKDIR /app
COPY --from=pruner /repo/out/pnpm-lock.yaml ./pnpm-lock.yaml
COPY --from=pruner /repo/out/pnpm-workspace.yaml ./pnpm-workspace.yaml
RUN --mount=type=cache,id=s/8bd6805c-dd00-4311-912b-307c21466cc4-/pnpm/store,target=/pnpm/store \
	pnpm fetch --frozen-lockfile
COPY --from=pruner /repo/out/json/ ./
RUN --mount=type=cache,id=s/8bd6805c-dd00-4311-912b-307c21466cc4-/pnpm/store,target=/pnpm/store \
	pnpm install --frozen-lockfile --offline
COPY --from=pruner /repo/out/full/ ./
RUN pnpm turbo run build --filter=imdbgraph

FROM base AS runtime
ENV PORT=3000
RUN groupadd --system --gid 1001 nodejs \
	&& useradd --system --uid 1001 --gid nodejs app
WORKDIR /app
COPY --from=builder --chown=app:nodejs /app/apps/imdbgraph/.output ./.output
USER app
EXPOSE ${PORT}
CMD ["node", ".output/server/index.mjs"]
