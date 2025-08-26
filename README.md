# 💻 Shell Command Interface

## 📖 Introduction

The Shell Command Interface is a Java-based project that mimics the functionality of a Unix-like shell, designed to work seamlessly on both Windows and Unix systems.  
It supports 20+ commands, provides auto-completion, command history, syntax highlighting, and comes with a clean Java Swing GUI.  
Developed as part of the CPIT260 Final Lab Project at King Abdulaziz University, FCIT.

---

## ✨ Features

- ✅ **20+ Supported Commands**
  - **File management:** `ls`, `pwd`, `mkdir`, `cd`, `cp`, `mv`, `rm`, `rmdir`, `touch`
  - **File viewing:** `cat`, `less`, `head`
  - **Search:** `grep`, `find`, `locate`
  - **System info:** `ps`, `du`, `wc`
  - **Permissions:** `chmod`, `chown`, `chgrp`, `accessrights`
  - **User management:** `addUser`, `addGroup`
  - **Networking:** `wget`
  - **Utilities:** `history`, `clear`
- 🎨 Syntax Highlighting (commands in blue, errors in red)
- ⌨️ Auto-completion with <kbd>Ctrl</kbd>+<kbd>Space</kbd>
- 🔼🔽 Command history navigation
- 🌍 Cross-platform support (Windows + Unix)
- ⚡ Error handling for invalid commands & permissions

---

## 🏗️ Project Structure

- `EnhancedShellUI.java` → Swing GUI (main window, dropdown, input/output)
- **Command Dispatcher** → Routes commands (`executeSystemCommand`)
- **Command Handlers** → Implement each command logic (`handleLsCommand`, `handleGrepCommand`, etc.)
- **ProcessBuilder Integration** → Executes commands on the underlying OS

---

## 🖥️ GUI Components

- **Command Dropdown:** Preloaded with supported commands
- **Arguments Field:** Enter command parameters
- **Execute Button:** Runs the command
- **Output Pane:** Displays results with syntax highlighting
- **Current Directory Label:** Shows working directory dynamically

---

## ⚙️ Installation

```sh
git clone https://github.com/your-username/ShellCommandInterface.git
cd ShellCommandInterface
javac *.java
java EnhancedShellUI
```

---

## 🚀 Usage

1. Select or type a command.
2. Add arguments (e.g., `grep "hello" file.txt`).
3. Hit <kbd>Enter</kbd> or click **Execute**.
4. Use <kbd>Ctrl</kbd>+<kbd>Space</kbd> for auto-completion.
5. Use <kbd>↑</kbd> / <kbd>↓</kbd> to navigate history.

---

## 🔍 Example Commands

```
ls
pwd
mkdir testdir
cd testdir
touch notes.txt
cat notes.txt
grep "hello" notes.txt
wc notes.txt
chmod 755 notes.txt
wget https://example.com/file.zip
history
clear
```

---

## 📊 Example Output

```
> pwd
C:\Users\student\ShellProject

> ls
Documents/  Downloads/  testdir/

> grep "hello" notes.txt
hello world

> history
1. pwd
2. ls
3. grep "hello" notes.txt
```

---

## ❗ Troubleshooting

- **Permission Denied:** Some commands (`chown`, `chgrp`, `addUser`, `addGroup`) require admin rights.
- **Windows vs Unix:** Uses equivalent system calls (`dir` for `ls`, PowerShell `Invoke-WebRequest` for `wget`).
- **Unsupported Command:** Shows `Command not supported`.

---

## 📜 License

This project is for educational purposes only.
