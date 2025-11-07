# KotlinAndSwiftScriptRunner
# Script Runner GUI

A lightweight desktop tool that lets you **edit, run and debug Swift or Kotlin scripts** with live output, errors(clickable error lines in output) and syntax highlighting.  
Built with **Java + Swing**, designed for cross-platform use (macOS, Windows, Linux).


## Requirements
**Java** : 17 or higher

**Kotlin** : Installed and available via `kotlinc` (make sure it is in `$PATH`)

**Swift** : Installed and available via `/usr/bin/env swift`

## Building and Running

### IntelliJ IDEA:
1. Open the project
2. Mark `src/` as **Sources Root**
3. Mark `resources/` as **Resources Root**
4. Open ->  `src/main/java/ScriptRunner.java`
5. Click -> **Run** in the top-right corner

The KotlinAndSwiftScriptRunner window should appear.

Select the language (Kotlin/Swift), type in the script, and click **Run**

### Example Kotlin Script
for (i in 1..5) {

println("Step $i")

Thread.sleep(1000)

}

println("Done!")

**Output (live updating):**

Step 1

Step 2

Step 3

Step 4

Step 5

Done!

### Example Swift Script

import Darwin

for i in 1...5 {

print("Step \\(i)")

fflush(stdout)

sleep(1)

}

print("Done!")

**Output (live updating):**

Step 1

Step 2

Step 3

Step 4

Step 5

Done!




### Created by Konrad Drewnowski
