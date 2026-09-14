# Player messaging task

A pure-Java implementation of two players exchanging messages.

The project supports both required deployment modes:

- **Same process:** two `Player` instances run on separate executor threads and communicate through in-memory blocking queues.
- **Separate processes:** each `Player` runs in its own JVM/PID and communicates over a loopback TCP socket.

The initiator sends ten application messages and receives ten replies. Every reply contains the received message followed by the sender's application-message counter. A separate control message then shuts down both players gracefully.

## Requirements

- JDK 17+
- Maven 3.8+
- POSIX-compatible shell

## Run

```sh
./run.sh same-process
./run.sh multi-process
```

An optional TCP port may be supplied:

```sh
./run.sh multi-process 5050
```

Run tests:

```sh
mvn test
```

## Class responsibilities

| Class | Responsibility |
| --- | --- |
| `Message` | Immutable protocol value carrying either application data or a stop signal. |
| `MessageTransport` | Small transport abstraction used by a player to send and receive messages. |
| `Player` | Owns the conversation rules and counters; it is independent of threads, sockets, and process topology. |
| `InMemoryTransport` | Provides paired endpoints backed by blocking queues for same-process communication. |
| `SocketTransport` | Sends framed messages over a TCP connection for cross-process communication. |
| `SameProcessMain` | Wires two players in one JVM and coordinates orderly executor shutdown. |
| `PlayerProcessMain` | Starts one player as either TCP server/responder or TCP client/initiator. |

## Design notes

The conversation policy is centralized in `Player`, while communication details are behind `MessageTransport`. Therefore the same player implementation is used in both deployment modes. There is no framework, serialization library, or external runtime service.

A stop message is deliberately not included in the application-message counter. The initiator sends it only after its tenth reply, so the required condition is exact: ten application messages sent and ten replies received.

The source archive for submission should exclude generated files:

```sh
git archive --format=zip --output=player-messaging-task.zip HEAD
```
