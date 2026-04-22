# AGENTS.md

This file provides high-level project context and cross-stack integration notes for agents working in this repository.

## Project Overview

- **Fullstack**: React/TypeScript frontend (`app/`) + Spring Boot backend (`backend/`)
- **Backend**: Runs on port `9090` with SSL and HTTP/2 enabled
- **Frontend dev server**: HTTPS on `https://127.0.0.1:8080` (proxy to backend)

## Build & Development

- Frontend HTTPS dev server at `https://127.0.0.1:8080` (cert.pem/key.pem), proxy to `https://localhost:9090`
- Tests use **real PostgreSQL** (not H2), scheduler crons set to non-executing dates

## Cross-Stack Integration

- Frontend calls `/api/*` REST endpoints and `/api/risk-alerts/stream` SSE
- Backend SSE pushes to frontend via `RiskAlertSSEController`
- Auth `userId` passed via query param in SSE URL, not header
