# 💻 Shell Command Interface

## 📖 Introduction
The Shell Command Interface is a Java-based project that mimics the functionality of a Unix-like shell while working seamlessly on both Windows and Unix systems.  
It supports 20+ commands, integrates syntax highlighting, auto-completion, and a GUI (Swing) for user-friendly interaction.  
Developed as part of the **CPIT260 Final Lab Project** at King Abdulaziz University, FCIT.

---

## ✨ Features

### ✅ 20+ Supported Commands
- **File management:** `ls`, `pwd`, `mkdir`, `cd`, `cp`, `mv`, `rm`, `rmdir`, `touch`
- **File viewing:** `cat`, `less`, `head`
- **Search:** `grep`, `find`, `locate`
- **System info:** `ps`, `du`, `wc`
- **Permissions:** `chmod`, `chown`, `chgrp`, `accessrights`
- **User management:** `addUser`, `addGroup`
- **Networking:** `wget`
- **Utilities:** `history`, `clear`

- 🎨 **Syntax Highlighting** → Commands (blue), Errors (red)
- ⌨️ **Auto-completion** (`Ctrl+Space`)
- 🔼🔽 **Command history navigation**
- 🌍 **Cross-platform** → Works on Windows + Unix
- ⚡ **Error handling** → Invalid commands, I/O issues, permissions

---

## 🏗️ Project Structure

- `EnhancedShellUI.java` → Swing GUI (main window, input/output pane, dropdown)
- **Command Dispatcher** → Routes commands (`executeSystemCommand`)
- **Command Handlers** → Implement functionality for each command
- **ProcessBuilder Integration** → Executes commands in current directory

---

## 🖥️ GUI Components

- **Command Dropdown** → Choose from supported commands
- **Arguments Field** → Enter parameters
- **Execute Button** → Run the command
- **Output Pane** → Displays results with syntax highlighting
- **Current Directory Label** → Shows active path

---

## ⚙️ Installation

Clone this repository:
```sh
git clone https://github.com/your-username/ShellCommandInterface.git
cd ShellCommandInterface
```
Compile the project:
```sh
javac *.java
```
Run the shell:
```sh
java EnhancedShellUI
```

---

## 🚀 Usage

1. Select a command from the dropdown (or type it in).
2. Add arguments (e.g., `grep "hello" file.txt`).
3. Press Execute or hit Enter.
4. Use `Ctrl+Space` → auto-completion.
5. Use ↑ / ↓ → scroll through command history.

---

## 🔍 Example Commands

```
ls
pwd
mkdir projects
cd projects
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
Documents/  Downloads/  projects/

> grep "hello" notes.txt
hello world

> history
1. pwd
2. ls
3. grep "hello" notes.txt
```

---

## ❗ Troubleshooting

- **Admin Privileges Required** → Some commands (`chown`, `chgrp`, `addUser`, `addGroup`) may require elevated rights.
- **Windows vs Unix Differences** → Uses equivalent commands (`dir` for `ls`, PowerShell `Invoke-WebRequest` for `wget`).
- **Unsupported Command** → Displays:  
  `Command not supported`

---

## 📜 License

This project is for educational purposes only.
