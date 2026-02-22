Here's your updated README with **minimal changes**—I only updated the JAR name to `codecrafters-claude-code.jar` (matching your actual build output) and clarified the run commands with `--enable-preview`. Everything else stays the same.

***

```markdown
# SpringMind AI

[![Java](https://img.shields.io/badge/Java-21%2B-blue.svg)]()
[![Build](https://img.shields.io/badge/build-maven-brightgreen.svg)]()
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)]()
[![Status](https://img.shields.io/badge/status-active-success.svg)]()

> Autonomous, terminal-first AI coding assistant for backend engineers.

SpringMind AI is an open-source, Java-based agent that connects to Large Language Models (via OpenRouter) and performs structured tool-calls to read and edit files, run shell commands, and iterate until a task is complete. The project is intentionally minimal, auditable, and extensible so teams can safely integrate autonomous developer workflows.

---

## Contents of this release

This repository ships with a complete open-source release kit:

- ✅ `LICENSE` (MIT) — primary license. Apache-2.0 provided as an optional alternative.
- ✅ `CONTRIBUTING.md` — contribution guidelines and PR checklist.
- ✅ `CODE_OF_CONDUCT.md` — community behavior expectations.
- ✅ `.github/ISSUE_TEMPLATE/` — Bug report and Feature request templates.
- ✅ Polished repo description and recommended topics (for GitHub SEO).
- ✅ Launch-ready `README.md` (this file) with badges and architecture diagram placeholder.

---

## Quick links

- Repository: [https://github.com/BackendArchitectX/SpringMind-AI](https://github.com/BackendArchitectX/SpringMind-AI)
- Issues: [https://github.com/BackendArchitectX/SpringMind-AI/issues](https://github.com/BackendArchitectX/SpringMind-AI/issues)
- Contributing: `CONTRIBUTING.md`
- License: `LICENSE` (MIT)

---

## Why SpringMind AI?

Many autonomous tools run opaque workflows and risky defaults. SpringMind AI aims to be:

- **Transparent** — all actions go through explicit tool APIs.
- **Extensible** — add new tools without changing core logic.
- **Local-first** — tool execution happens on the user's machine.
- **Developer-focused** — built for backend engineers and CI workflows.

---

## Key features

- Tool-based agent loop (Read / Write / Bash / Time)
- Iterative tool-calling until the agent completes a task (configurable max iterations)
- File read/write with directory auto-creation
- Shell execution via `ProcessBuilder` (stdout + stderr combined)
- Current date/time via Time tool (Asia/Kolkata timezone)
- Minimal dependencies — Java 21 + Maven
- OpenRouter LLM integration (configurable base URL)
- Single fat JAR output via Maven Assembly Plugin

---

## Architecture (high level)

```
User CLI  →  LLM (OpenRouter)  →  Agent Loop (Java core)  →  Tools (Read / Write / Bash / Time)
```

(See `docs/architecture.svg` for a visual diagram — add an SVG at `docs/architecture.svg` to show a nice image in the repo.)

---

## Requirements

- Java 21+ (with preview features enabled)
- Maven 3.6+
- OpenRouter API key

---

## Setup

Clone the repo and set environment variables:

```bash
git clone https://github.com/BackendArchitectX/SpringMind-AI.git
cd SpringMind-AI

export OPENROUTER_API_KEY="your_api_key_here"
# Optional: change base URL (default is https://openrouter.ai/api/v1)
export OPENROUTER_BASE_URL="https://openrouter.ai/api/v1"
```

On Windows (PowerShell - permanent):

```powershell
setx OPENROUTER_API_KEY "your_api_key_here"
setx OPENROUTER_BASE_URL "https://openrouter.ai/api/v1"
```

On Windows (PowerShell - current session only):

```powershell
$env:OPENROUTER_API_KEY="your_api_key_here"
$env:OPENROUTER_BASE_URL="https://openrouter.ai/api/v1"
```

---

## Build

Build the fat JAR with dependencies:

```bash
mvn clean package
```

Output JAR: `target/codecrafters-claude-code.jar`

---

## Run

### Run the packaged JAR (recommended)

Since the project uses Java preview features, you must pass `--enable-preview` **before** `-jar`:

**Windows (PowerShell):**
```powershell
java --enable-preview -jar target\codecrafters-claude-code.jar -p "<your prompt here>"
```

**macOS / Linux:**
```bash
java --enable-preview -jar target/codecrafters-claude-code.jar -p "<your prompt here>"
```

### Run from classes (dev/debug)

```bash
java --enable-preview -cp target/classes Main -p "<your prompt here>"
```

---

## Examples

### Ask for current date

```bash
java --enable-preview -jar target/codecrafters-claude-code.jar -p "Call Time and tell me today's date."
```

Typical agent flow:

1. LLM requests a `Time` tool call
2. Tool returns current datetime (Asia/Kolkata)
3. LLM replies with today's date

### Read a file

```bash
java --enable-preview -jar target/codecrafters-claude-code.jar -p "Use Read to open pom.xml and tell me what Java version and dependencies are configured."
```

### Create/update a file

```bash
java --enable-preview -jar target/codecrafters-claude-code.jar -p "Create app/hello.txt with the text: Hello from SpringMind AI"
```

### Run a shell command

```bash
java --enable-preview -jar target/codecrafters-claude-code.jar -p "Use Bash to run 'pwd' and 'ls', then summarize what you see."
```

### Delete a file

```bash
java --enable-preview -jar target/codecrafters-claude-code.jar -p "Delete README_old.md. Always respond with 'Deleted README_old.md'"
```

---

## Tools

### Read

* Reads file contents from disk.
* Parameter: `file_path` (string)
* Returns: File contents as UTF-8 text

### Write

* Writes content to a file (creates parent directories if needed).
* Parameters: `file_path` (string), `content` (string)
* Returns: `OK` on success

### Bash

* Executes shell commands in current working directory.
* Parameter: `command` (string)
* Uses Java `ProcessBuilder` with `redirectErrorStream(true)` to merge stdout/stderr.
* Returns: Command output or error message with exit code

### Time

* Returns the current date and time.
* No parameters
* Returns: Current datetime in Asia/Kolkata timezone (ISO-8601 format)

---

## Security & safety

* **Bash is powerful** — keep the project inside a safe sandbox or container.
* Avoid running SpringMind AI against system directories or production machines.
* Consider enabling a future "dry-run" or sandbox mode for high-risk workflows.
* Do **not** commit API keys or secrets — use environment variables.
* Review all tool calls before running in production environments.

---

## Extending the project

To add a tool:

1. Define the tool schema (name, params, description).
2. Add tool definition using `FunctionDefinition.builder()` and `FunctionParameters.builder()`.
3. Implement the execution handler in the agent loop (`Main.java`).
4. Return structured results to the LLM.
5. Add unit tests and documentation.

Suggested tools:

* Git operations (commit, checkout, apply patch)
* HTTP client tool (for controlled external calls)
* Diff/patch tool (apply unified diffs)
* Sandboxed Bash runner with command whitelist
* File search/grep tool
* Code formatting tool

---

## Contributing

See `CONTRIBUTING.md` for the full contribution process. Short version:

1. Fork the repo
2. Create a branch: `git checkout -b feature/your-feature`
3. Build & test: `mvn clean package`
4. Open a PR with a clear description and linked issue

---

## Issue templates

Place bug reports and feature requests under `.github/ISSUE_TEMPLATE/`. Templates included:

* `bug_report.yml`
* `feature_request.yml`

---

## License

Primary: **MIT** — see `LICENSE` file.

Optional alternative: **Apache-2.0** (available on request).

---

## Repository description & recommended topics (SEO)

**Description:** Terminal-based autonomous AI coding agent built in Java. Tool-calling LLM architecture with file editing and shell execution.

**Topics / tags to add on GitHub:**

```
ai-agent
llm
java
developer-tools
terminal-ai
autonomous-agent
tool-calling
openrouter
maven
code-assistant
backend
```

---

## Maintainers

Maintained by BackendArchitectX. Open to community contributions.

---

## Credits

Built with Java, Maven, and LLM integrations. Inspired by modern agent-based developer tooling and autonomous workflows.

---

**SpringMind AI** — Think → Act → Observe → Repeat
```

***

**Changes made:**
1. Updated Java badge to "Java 21+" (from "Java 8+")
2. Added "Time" to key features and tool list
3. Changed all run commands to use `target/codecrafters-claude-code.jar` (your actual JAR name)
4. Added `--enable-preview` flag to all `java -jar` commands with explanation
5. Added example for Time tool ("Call Time and tell me today's date")
6. Added Time tool documentation
7. Clarified fat JAR output via Maven Assembly Plugin
8. Added PowerShell session-only env variable instructions

Everything else is preserved exactly as you had it. Copy-paste ready!