# Java Environment Test (Contest Setup)

This repository is a **minimal cross-platform Java setup** to verify that both coders are ready for a programming contest (Linux + Windows, different editors).

The goal is to ensure that:

* Java compiles and runs correctly
* Git workflow works between teammates
* Line endings and OS differences cause no issues
* Editors (LazyVim / VS Code) are correctly configured

---

## Requirements

* **OpenJDK 21** installed on all machines
* Git
* Linux /  Windows

Check Java version:

```bash
java -version
javac -version
```

---

## Project Structure

```
env-test/
├── src/
│   └── Main.java      # Sample program to test Java setup
├── run.sh             # Run script for Linux/macOS
├── run.bat            # Run script for Windows
├── .gitignore         # Ignore build + IDE files
├── .gitattributes     # Normalize line endings (LF)
└── README.md
```

---

## How to Run

### Linux / macOS

```bash
chmod +x run.sh
./run.sh
```

### Windows (PowerShell or CMD)

```powershell
run.bat
```

Expected output:

```
Environment test OK ✅
Sum = 15
Random = 0.xxx
Branch A or B
```

---

## What This Tests

The sample program verifies:

* Java compiler (`javac`) and runtime (`java`)
* Standard library access
* Loops, conditionals, collections
* Random number generation
* Console output

The repository setup verifies:

* Cross-platform compilation
* Git push / pull between Linux and Windows
* Correct handling of line endings

---

## Git Workflow Test (Recommended)

1. One teammate pushes this repository to GitHub
2. Second teammate clones it
3. Second teammate edits `Main.java` (e.g. changes a print line)
4. Commit and push the change
5. First teammate pulls and runs the program

If both can run the program successfully → the environment is ready.

---

## Editor Notes

### LazyVim (Linux)

* Java LSP (`jdtls`) should start automatically
* Compilation can be tested via:

```vim
:!javac src/Main.java
```

### VS Code (Windows)

* Install **Extension Pack for Java**
* Ensure no red errors before running

---

## Contest Readiness Criteria

You are ready if:

* The program compiles and runs on both machines
* Git sync works without conflicts
* No OS-specific issues appear

Once this is confirmed, you can safely move on to the actual contest task.

Good luck 🚀
