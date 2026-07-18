# Java Chat App — Socket Programming

A real-time, multi-client chat application built in core Java using **Socket
Programming** and **Multithreading** — no external frameworks, no internet
required. Runs entirely on `localhost`, includes a console client and an
optional Swing GUI client, and is designed as a portfolio-ready project for
students to showcase Java networking fundamentals on GitHub.

---

## 1. Overview

This project simulates the backbone of every real chat/messaging product:
a central server that many clients connect to, each client running on its
own thread on the server, exchanging messages in real time.

**Problem it solves:** demonstrates how independent programs (clients) can
communicate with each other through a shared server using nothing but TCP
sockets — the same foundational mechanism used by WhatsApp, Slack, Discord,
and multiplayer game servers under the hood.

### Simple explanation
Think of the server as a group phone operator. Every client "calls in"
(connects). Whatever one caller says, the operator repeats to everyone else
on the line, instantly.

### Technical explanation
- A `ServerSocket` binds to a TCP port and blocks on `accept()`, waiting for
  incoming connections.
- Each incoming connection produces a `Socket` — a byte-stream endpoint
  between server and one client.
- Because `accept()` and `readLine()` are **blocking calls**, the server
  spins up a **new thread per client** (`ClientHandler implements Runnable`)
  so it can serve many clients concurrently instead of one at a time.
- Every message a `ClientHandler` receives is **broadcast** to a thread-safe
  collection of all connected handlers, so every client sees it in real time.

### Workflow
```
Client 1 ─┐
Client 2 ─┼─► Socket Connection ─► Java Server ─► Client Handler Threads ─► Message Broadcasting ─► Real-Time Chat Output
Client 3 ─┘
```

---

## 2. Industry Relevance

The exact same client-server + socket + multithreading pattern underpins:

| Domain | How it's used |
|---|---|
| Messaging systems | WhatsApp/Telegram-style servers route messages between connected clients |
| Live support / helpdesk | Support agents and customers connect to a shared session server |
| Multiplayer games | Game servers broadcast player state to all connected clients in real time |
| Collaboration tools | Google Docs / Figma-style presence & edit-event broadcasting |
| Customer service platforms | Live chat widgets connect to a routing server |
| Real-time notifications | Push/notification servers hold open connections and broadcast events |
| Distributed systems | Node-to-node communication over sockets is the base layer of most distributed protocols |

**Technical value:** understanding blocking I/O, threading, and shared-state
synchronization is foundational for backend, distributed systems, and even
AI infrastructure engineering roles.

**Business value:** every real-time product (chat, live scores, trading
tickers, collaborative editing) needs this exact pattern at its core —
knowing it end-to-end is a strong signal in interviews.

**Why Java, even in an AI-driven era:** Java remains dominant in enterprise
backend systems, integrates cleanly with AI/ML APIs and cloud services, and
still needs the same reliable networking, threading, and backend skills this
project teaches — AI features sit *on top of* systems like this, they don't
replace them.

---

## 3. Tech Stack Used

This project implements **Option B (Recommended)**: Java sockets +
multithreading + usernames + broadcasting + private messaging + chat
logging — plus an **optional Swing GUI** from Option C, without requiring a
database or login system. This is the best balance of depth vs. beginner
friendliness for a student portfolio project. No internet connection is
required — everything runs on `localhost`.

| Piece | Purpose |
|---|---|
| `java.net.ServerSocket` / `Socket` | TCP connection endpoints |
| `java.io.BufferedReader` / `PrintWriter` | Line-based text I/O over the socket stream |
| `java.lang.Thread` / `Runnable` | One thread per connected client |
| `java.util.concurrent.ConcurrentHashMap`-backed `Set` | Thread-safe list of connected clients |
| `javax.swing` | Optional GUI client |
| Plain text file logging | Persists chat history to `logs/chat_log.txt` |

---

## 4. Java Concepts Used

| Concept | Role in this project |
|---|---|
| `Socket` / `ServerSocket` | Establish and accept TCP connections |
| `InputStream` / `OutputStream` | Raw byte streams underlying all socket I/O |
| `BufferedReader` | Efficiently reads line-by-line text from a socket |
| `PrintWriter` | Sends line-based text to a socket, auto-flushing |
| `Thread` / `Runnable` | Lets the server handle many clients concurrently |
| `ConcurrentHashMap` / `Set` | Thread-safe storage of active client handlers |
| `synchronized` | Guards the chat log file from concurrent writes |
| Exception handling (`try-with-resources`, `catch IOException`) | Graceful handling of disconnects and I/O errors |
| OOP (classes, encapsulation) | `Message`, `ClientHandler`, `ChatServer`, `ChatClient` are all cleanly separated responsibilities |
| Swing GUI (optional) | Demonstrates a real client on top of the same protocol |

---

## 5. Architecture

**Input:** username, chat text, `/w user msg` (private message), `/list`,
`/nick newname`, `/help`, `/quit`

**Processing:** server accepts a connection → spawns a `ClientHandler`
thread → reads lines from that client → broadcasts / routes them →
handles disconnect → logs everything to disk.

**Output:** real-time broadcast messages, join/leave notifications, private
message delivery, `chat_log.txt` history file.

```
                       ┌─────────────────────┐
                       │     ChatServer      │
                       │  (ServerSocket:5000) │
                       └──────────┬──────────┘
                                  │ accept()
              ┌───────────────────┼───────────────────┐
              ▼                   ▼                   ▼
     ClientHandler Thread  ClientHandler Thread  ClientHandler Thread
       (Client 1 - Sam)      (Client 2 - Riya)     (Client 3 - Dev)
              │                   │                   │
              ▼                   ▼                   ▼
        broadcast() ───────► shared Set<ClientHandler> ◄─────── broadcast()
                                  │
                                  ▼
                          logs/chat_log.txt
```

**Thread flow:** main thread only ever runs the `accept()` loop. Every other
line of chat is handled entirely inside per-client threads — the main thread
never blocks on chat traffic.

---

## 6. Folder Structure

```
Java-Chat-App-Socket-Programming/
│
├── src/
│   ├── server/
│   │   ├── ChatServer.java      # accepts connections, owns the broadcast loop
│   │   └── ClientHandler.java   # one thread per connected client
│   ├── client/
│   │   └── ChatClient.java      # console client (2 threads: input + listener)
│   ├── common/
│   │   ├── Message.java         # shared message model + formatting
│   │   └── ChatLogger.java      # thread-safe file logging
│   └── gui/
│       └── ChatClientGUI.java   # optional Swing GUI client
├── logs/                        # chat_log.txt is written here at runtime
├── outputs/                     # place sample console output captures here
├── screenshots/                 # place your proof screenshots here
├── docs/
│   ├── TESTING.md
│   ├── GITHUB_STRATEGY.md
│   ├── PROOF_PLAN.md
│   ├── SCREENSHOTS_CHECKLIST.md
│   └── INTERVIEW_PREP.md
├── README.md
└── .gitignore
```

---

## 7. Features

**Implemented:**
- Start chat server, connect multiple clients
- Username entry with duplicate-name protection
- Real-time message broadcasting to all connected clients
- Join / leave notifications
- Graceful disconnect (`/quit`) and handling of sudden disconnects
- Private messaging (`/w <username> <message>`)
- Online user list (`/list`)
- Change username on the fly (`/nick <newname>`)
- In-chat command help (`/help`)
- Timestamped chat history logging to `logs/chat_log.txt`
- Robust exception handling (bad ports, dropped connections, etc.)
- Optional Swing GUI client

**Not implemented (documented as future improvements):** multiple chat
rooms, file sharing, login/registration with a database, online/offline
status indicators beyond join/leave messages.

---

## 8. How to Run

### A. Command line (from the project root)

```bash
# 1. Compile everything into a build/ folder
javac -d build $(find src -name "*.java")

# 2. Start the server (default port 5000; optional custom port as arg)
java -cp build server.ChatServer 5000

# 3. In separate terminals, start as many clients as you like
java -cp build client.ChatClient localhost 5000
java -cp build client.ChatClient localhost 5000

# Optional: start the GUI client instead of the console client
java -cp build gui.ChatClientGUI localhost 5000
```

Default host: `localhost`. Default port: `5000`.

**Port already in use?** Either stop whatever is using that port, or start
the server on a different port (`java -cp build server.ChatServer 5050`) and
pass the same port to each client.

### B. IntelliJ IDEA
1. `File → New → Project from Existing Sources` → select this folder.
2. Mark `src` as the Sources Root (right-click → `Mark Directory as → Sources Root`).
3. Run `server/ChatServer.java` first (right-click → Run).
4. Run `client/ChatClient.java` — you can launch it multiple times
   (`Run → Edit Configurations → Allow Multiple Instances`) to simulate
   several users.

### C. Eclipse
1. `File → New → Java Project`, then import the `src` folder.
2. Right-click `ChatServer.java → Run As → Java Application`.
3. Right-click `ChatClient.java → Run As → Java Application` (repeat for
   more simulated clients — Eclipse allows multiple run instances by default).

### Sample output
```
=========================================
 Java Chat Server - Socket Programming
=========================================
Starting server on port 5000 ...
Server is up and listening on port 5000
Waiting for clients to connect...

New connection from /127.0.0.1
Sam has joined the chat.
New connection from /127.0.0.1
Riya has joined the chat.
```

---

## 9. Virtual Multi-Client Simulation (no second machine needed)

Since this all runs on `localhost`, you can fully simulate a multi-user chat
on a single laptop:

1. Open **4 terminals** total.
2. Terminal 1: start the server.
3. Terminal 2, 3, 4: start a client in each, entering usernames `Sam`,
   `Riya`, `Dev`.
4. From Sam's terminal, type a message → confirm it appears in Riya's and
   Dev's terminals instantly.
5. From Riya's terminal, type `/w Sam hey, just you` → confirm only Sam sees it.
6. From Dev's terminal, type `/list` → confirm all three usernames are listed.
7. Close Riya's terminal (or type `/quit`) → confirm Sam and Dev see a leave
   notification.
8. Open `logs/chat_log.txt` → confirm every event was recorded with a
   timestamp.

This exact flow was tested against the included source using a scripted
socket simulation before this README was written — join, broadcast, private
messaging, `/list`, and leave notifications all behave as described above.

---

## 10. Limitations

- No database — usernames and history are not persisted between server restarts.
- Single chat room only (no room/channel separation).
- No authentication — anyone who knows the host/port can join with any free username.
- Not encrypted (plain TCP) — fine for a localhost learning project, not for production.

## 11. Future Improvements

- Multiple chat rooms / channels
- File sharing over the socket
- Login system backed by a database (with hashed passwords)
- Online/offline status indicators
- TLS-encrypted sockets (`SSLSocket`) for production use
- Move to an object/JSON-based protocol instead of plain text lines

## 12. Learning Outcomes

Building this project demonstrates practical understanding of: TCP socket
programming, blocking I/O, multithreaded server design, thread-safe shared
state, basic protocol design over a text stream, Swing GUI event threading,
and structuring a multi-package Java project for real-world readability.

## 13. Author

Built by Sam as a GTU Java course project and GitHub portfolio piece.

---

See `docs/` for the testing strategy, GitHub upload plan, screenshot
checklist, and interview preparation notes.
