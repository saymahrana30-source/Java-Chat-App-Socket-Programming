# Interview Preparation

## 1. "Explain your project."
I built a real-time chat application in core Java using TCP socket
programming. A central server accepts connections from multiple clients;
each client gets its own thread on the server so the server can handle many
users concurrently. Clients send plain-text messages over the socket, and
the server broadcasts each message to every connected client. I also added
usernames, private messaging, an online-user list, join/leave notifications,
and persistent chat logging, plus an optional Swing GUI client on top of the
same protocol.

## 2. "What is a socket, and how is it different from a port?"
A port is just a number that identifies an endpoint on a machine (like an
apartment number). A socket is the actual live, two-way connection between
a specific client and server, bound to that port — it's the combination of
IP address + port + the connection state, wrapped in a `Socket` object with
readable/writable streams.

## 3. "Why do you need multithreading in a chat server?"
Because `ServerSocket.accept()` and `BufferedReader.readLine()` are blocking
calls. If the server handled one client at a time on a single thread, it
would freeze on the first client and never accept a second connection. By
giving every client its own thread (`ClientHandler implements Runnable`),
the server can wait on many clients' input simultaneously without blocking
each other.

## 4. "How do you prevent race conditions between client threads?"
I store all connected clients in a `Set` backed by `ConcurrentHashMap` (via
`Collections.newSetFromMap`), which is safe for concurrent add/remove/iterate
without external locking. For the shared chat log file, I explicitly
`synchronize` on a private lock object so only one thread writes to the file
at a time, preventing interleaved or corrupted log lines.

## 5. "What happens if a client disconnects without saying goodbye?"
The next call to `in.readLine()` on that client's socket either returns
`null` or throws an `IOException`. Either way, the handler's loop exits, the
`finally` block runs `disconnect()`, which removes the client from the
shared set, closes its streams/socket, and broadcasts a "left the chat"
notification to everyone else.

## 6. "Why use BufferedReader and PrintWriter instead of raw streams?"
Sockets natively expose raw `InputStream`/`OutputStream` (bytes). Wrapping
them in `BufferedReader`/`PrintWriter` lets us work with whole lines of text
(`readLine()` / `println()`) instead of manually parsing byte arrays, which
is exactly the abstraction level a simple line-based chat protocol needs.

## 7. "How would you scale this beyond a single server?"
I'd move from a plain-text, single-process model toward: (1) a message
broker or pub/sub layer (e.g. Kafka/Redis) so multiple server instances can
share state, (2) a load balancer in front of multiple server processes, and
(3) persisting user/message state in a database instead of in-memory
collections, so any server instance can serve any client.

## 8. "What are the limitations of your current implementation?"
No encryption (plain TCP, fine for localhost learning but not production),
no persistence across server restarts, single chat room only, and no
authentication — anyone who knows the host/port can join with any free
username. All of these are documented as intentional scope limits and
listed as future improvements in the README.

## 9. "How does your GUI client differ from the console client, technically?"
Same underlying protocol and same socket/stream setup — the only difference
is the presentation layer (Swing components instead of `System.out`) and one
important extra rule: Swing components can only be safely updated from the
Event Dispatch Thread, so the background listener thread hops back onto the
EDT using `SwingUtilities.invokeLater()` whenever it needs to update the chat
window.

## 10. "Why is a project like this still relevant when everyone talks about AI now?"
Because AI features are built *on top of* exactly this kind of
infrastructure — reliable client-server communication, concurrency, and
backend fundamentals. Every AI-powered chat app, live agent, or real-time
system still needs a dependable networking and threading layer underneath
it; this project proves I understand that layer from first principles
rather than just calling a library.
