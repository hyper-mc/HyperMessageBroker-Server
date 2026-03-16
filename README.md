# HyperMessageBroker-Server
This is our high performance messenger for minecraft servers.

## Configuration
All options live in `config.yml` (copied automatically on first run):
- `port`: TCP port used by the ProcBridge server.
- `poolSize`: thread pool size for background tasks.
- `cleanDelay`: interval (seconds) to run housekeeping tasks.
- `tokenTimeoutSeconds`: disconnect clients that stop sending `ONLINE` heartbeats.
- `messageTtlSeconds`: maximum time a message stays stored before expiring.
- `maxMessagesPerQueue`: cap per-queue backlog to avoid unbounded memory usage.
