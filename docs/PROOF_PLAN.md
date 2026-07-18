# 7-Day GitHub Proof-Building Plan

| Day | Focus | Files to commit | Commit message | Screenshot/proof to capture |
|---|---|---|---|---|
| 1 | Project setup + basic server | `src/server/ChatServer.java` (accept loop only), folder structure, `.gitignore`, `README.md` skeleton | `feat: initial project setup with basic ServerSocket accept loop` | Project folder structure in IDE/explorer |
| 2 | Client connection | `src/client/ChatClient.java` (connect only, no send/receive yet) | `feat: add basic client socket connection to server` | Server console showing "New connection from ..." |
| 3 | Message exchange | Add send/receive loop to `ChatClient.java`, basic echo in `ChatServer.java` | `feat: implement message sending and receiving over socket` | Terminal screenshot of a message going client → server |
| 4 | Multithreading + multiple clients | `src/server/ClientHandler.java`, update `ChatServer.java` to spawn threads | `feat: add ClientHandler thread so server supports multiple clients` | Two client terminals connected simultaneously |
| 5 | Usernames + broadcasting | Username handshake, `common/Message.java`, broadcast logic | `feat: add username handshake and real-time message broadcasting` | Three-client chat screenshot with usernames visible |
| 6 | Private messaging + logs | `/w` command, `/list` command, `common/ChatLogger.java` | `feat: add private messaging, online user list, and chat logging` | Private message screenshot + `logs/chat_log.txt` contents |
| 7 | Testing + documentation | `docs/TESTING.md`, final `README.md`, GUI client, `docs/INTERVIEW_PREP.md` | `docs: add full README, testing notes, and optional Swing GUI client` | Full README preview on GitHub, GUI screenshot, repo overview page |

**Tip:** even if you actually build the whole thing in one sitting (totally
normal), you can still stage and commit in this order over a few real
commits — it's honest (all commits are your own work, just organized
logically) and it gives your repository a readable, believable history that
recruiters and reviewers can follow.
