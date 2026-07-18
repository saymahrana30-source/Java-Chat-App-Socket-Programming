# Testing Strategy

## Manual test cases

| # | Test case | Steps | Expected result |
|---|---|---|---|
| 1 | Single client connects | Start server, start 1 client, enter username | Client sees "joined" broadcast, server console logs the connection |
| 2 | Multiple clients | Start server + 3 clients with different usernames | All 3 can message each other in real time |
| 3 | Duplicate username | Connect Client A as "Sam", connect Client B also as "Sam" | Client B is prompted `NAMEINUSE` and must pick another name |
| 4 | Empty message | Press Enter with no text typed | No crash; server simply broadcasts an empty line (acceptable) or client can be extended to ignore blank input |
| 5 | Sudden client disconnect | Close a client's terminal window (kill process) without `/quit` | Server catches `IOException` on next read, removes the client, broadcasts a leave notification |
| 6 | Server shutdown | Kill the server process while clients are connected | Clients' listener threads catch `IOException` and print "Disconnected from server." |
| 7 | Invalid private-message user | Send `/w Ghost hello` where "Ghost" isn't online | Sender receives "User 'Ghost' is not online." — no crash |
| 8 | Long messages | Send a very long line of text (500+ characters) | Delivered and displayed correctly, no truncation |
| 9 | Rapid messaging | Send 20+ messages back-to-back quickly from 2 clients | All messages arrive, correctly interleaved, no lost lines (verified in `logs/chat_log.txt`) |
| 10 | Exception handling | Try starting two servers on the same port | Second server prints a clear "port already in use" message and exits without crashing the JVM |

## Expected result summary table

| Scenario | Pass criteria |
|---|---|
| Broadcast | Every connected client (except none — including sender) receives every public message |
| Private message | Only sender + target receive the `/w` message |
| Join/Leave | Every connected client is notified when someone joins or leaves |
| Logging | Every event appears with a timestamp in `logs/chat_log.txt` |
| Disconnect handling | No server crash or thread leak when a client vanishes ungracefully |

## Optional JUnit test ideas

Because most of the logic here is I/O-driven (sockets, threads), full unit
testing requires either mocking streams or spinning up a real server on an
ephemeral port inside the test. Ideas if you want to add `src/test`:

- `MessageFormatTest` — construct a `Message` with each `MessageType` and
  assert `toString()` produces the expected formatted string.
- `ChatServerIntegrationTest` — start `ChatServer` on port `0` (OS picks a
  free port), open 2 real `Socket`s from the test, send/receive lines, and
  assert broadcast behavior — closest to the manual simulation above, just
  automated.
- `ChatLoggerTest` — call `ChatLogger.log(...)` from multiple threads
  concurrently and assert the resulting file has the correct number of
  lines with no interleaved/corrupted text.

This project's server logic was verified end-to-end with a scripted socket
client (username handshake, broadcast, private message, `/list`, and leave
notification) before being finalized — see the simulation walkthrough in
the main `README.md`.
