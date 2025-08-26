# 💻 Shell Command Interface

## Introduction
The Shell Command Interface is a Java-based project that replicates the functionality of a Unix-like shell, designed to work on both Windows and Unix systems. Built with Java Swing, it combines a graphical interface with command-line power, supporting 20+ commands (e.g., `ls`, `cd`, `grep`, `chmod`, `wget`).

It includes features like auto-completion, command history navigation, and syntax highlighting, making it both user-friendly and powerful.

This project was developed as part of the **CPIT260 Final Lab Project** at King Abdulaziz University, FCIT.

---

## 📌 Features

### ✅ 20+ Supported Commands

- **File operations:** `ls`, `pwd`, `mkdir`, `cd`, `cp`, `mv`, `rm`, `rmdir`, `touch`
- **File viewing:** `cat`, `less`, `head`
- **Search tools:** `grep`, `find`, `locate`
- **System info:** `ps`, `du`, `wc`
- **Permissions:** `chmod`, `chown`, `chgrp`, `accessrights`
- **User/Group management:** `addUser`, `addGroup`
- **Networking:** `wget`
- **Utilities:** `history`, `clear`

### 🎨 Syntax Highlighting
- Blue for commands, red for errors

### ⌨️ Auto-completion
- Use `Ctrl+Space`

### 🔼🔽 Command History Navigation

### 🌍 Cross-Platform Support
- Windows + Unix

### ⚡ Error Handling
- For invalid commands and permissions

---

## 🏗️ Project Structure

- `BookingSystem.java` → Main controller & dispatcher
- **Command Handlers** → Methods for each command (`handleLsCommand`, `handleGrepCommand`, etc.)
- **GUI (Swing)** → Provides output area, command input, and dropdown

---

## 🖥️ GUI Components

- **Command Dropdown** → Preloaded with supported commands for quick access
- **Arguments Field** → Enter parameters for commands
- **Execute Button** → Runs the selected command
- **Output Pane** → Displays command results with color-coded syntax
- **Current Directory Label** → Shows the active directory path

---

## ⚙️ Installation

```sh
# Clone the repository:
git clone https://github.com/your-username/your-repo.git
cd ShellCommandInterface

# Compile the Java files:
javac *.java

# Run the application:
java EnhancedShellUI
```

---

## 🚀 Usage

1. Type or select a command from the dropdown.
2. Add arguments (e.g., `grep "hello" file.txt`).
3. Press Enter or click Execute.
4. Use `Ctrl+Space` for auto-completion.
5. Use ↑ / ↓ keys to scroll through command history.

---

## 🔍 Example Commands

```
ls
pwd
mkdir testdir
cd testdir
touch file.txt
echo "Hello World" > file.txt
cat file.txt
grep "Hello" file.txt
wc file.txt
chmod 755 file.txt
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

> grep "Hello" file.txt
Hello World

> history
1. pwd
2. ls
3. grep "Hello" file.txt
```

---

## ❗ Troubleshooting

- **Permission Denied** → Some commands (`chown`, `chgrp`, `addUser`, `addGroup`) require Admin privileges.
- **Unsupported Command** → Displays "Command not supported".
- **Windows vs Unix differences** → The system adapts with equivalent commands (e.g., `dir` for `ls`).

---

## 📜 License

This project is intended for educational purposes only.
