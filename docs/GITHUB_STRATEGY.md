# GitHub Upload Strategy

## Repository setup
- **Repository name:** `java-chat-app-socket-programming`
- **Description:** "Real-time multi-client chat application in core Java using TCP sockets and multithreading — console + Swing GUI clients, localhost server, no external dependencies."
- **Tags/topics:** `java`, `socket-programming`, `multithreading`, `networking`, `chat-application`, `client-server`, `swing`, `student-project`, `tcp-ip`, `java-networking`

## Folder organization on GitHub
Push the folder structure exactly as laid out in the README (`src/`, `docs/`,
`logs/`, `outputs/`, `screenshots/`). Keep `logs/chat_log.txt` out of git by
default (see `.gitignore`) but commit one **sample** log snippet into
`outputs/sample_chat_log.txt` so visitors can see real output without running it.

## .gitignore essentials
Already included in this project — makes sure you don't upload:
- Compiled `.class` files / `build/` or `out/` directories
- IDE-specific files (`.idea/`, `*.iml`, `.classpath`, `.project`, `.settings/`)
- OS files (`.DS_Store`, `Thumbs.db`)
- Live runtime logs (`logs/*.txt` — commit a sample copy separately instead)
- Any future database credentials or `.env` files if you add authentication later

## Commit strategy
Don't do one giant "initial commit" — commit in the same phases you built
the project, so your GitHub history itself is proof of incremental, real
work. See `PROOF_PLAN.md` for the exact day-by-day breakdown and commit
messages to use.

## Meaningful commit message examples
```
feat: add ServerSocket and basic accept loop
feat: implement ClientHandler with per-client thread
feat: add multithreaded broadcast to all connected clients
feat: add username handshake with duplicate-name protection
feat: add private messaging (/w) and online user list (/list)
feat: add thread-safe chat logging to file
feat: add optional Swing GUI client
docs: add README with architecture and setup instructions
test: verify multi-client broadcast and disconnect handling
```

## Security notes
- Never commit real passwords, API keys, or `.env` files — this project has
  none currently, but keep this in mind if you add the optional database/
  login system later.
- If you do add authentication in the future, store password **hashes**
  (e.g. bcrypt), never plain text, and add your credentials file to
  `.gitignore` immediately.
