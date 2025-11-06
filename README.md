# KotlinAndSwiftScriptRunner
# Script Runner GUI

A lightweight desktop tool that lets you **edit, run and debug Swift or Kotlin scripts** with live output and syntax highlighting.  
Built with **Java + Swing**, designed for cross-platform use (macOS, Windows, Linux).

---

## Features

**Script Editor** — type or paste Swift/Kotlin code in a syntax-highlighted editor.  
**Live Output** — see output from the script in real time (as it executes).
**Execution Indicator** — shows when the script is currently running.  
**Exit Code Indicator** — displays whether the last run ended successfully.  
**Lightweight Keyword Highlighting** — highlights up to 10 core language keywords efficiently.  
**Language Switching** — switch between Swift and Kotlin at runtime.

---

## Technologies

- **Java 17+**
- **Swing (GUI)**
- **ProcessBuilder** for script execution
- **StyledDocument** for efficient text highlighting
- **Timer (debounced)** for keyword coloring

---

## Building and Running

### Build
IntelliJ IDEA:
1. Open the project
2. Mark `src/` as **Sources Root**
3. Mark `resources/` as **Resources Root**
4. Build → *Rebuild Project*

### Example Swift Script

import Darwin

for i in 1...5 {
print("Step \(i)")
fflush(stdout)
sleep(1)
}
print("Done!")

### Output (live updating):

Step 1
Step 2
Step 3
Step 4
Step 5
Done!

### Example Kotlin Script
for (i in 1..5) {
println("Step $i")
Thread.sleep(1000)
}
println("Done!")

### Output (live updating):

Step 1
Step 2
Step 3
Step 4
Step 5
Done!

### Created by Konrad Drewnowski
