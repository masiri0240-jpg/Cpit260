/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package shellui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class ShellUI extends JFrame {

    // UI Components
    private JComboBox<String> commandComboBox;
    private JTextField argumentsField;
    private JTextPane outputArea;
    private JButton executeButton;
    private JButton clearButton;
    private JLabel currentDirLabel;

    // Application state
    private File currentDirectory;
    private boolean isWindows;
    private List<String> commandHistory;
    private int historyIndex = -1;
    private StyleContext styleContext;
    private StyledDocument document;

    // Supported commands
    private static final String[] SUPPORTED_COMMANDS = {
        "ls", "pwd", "mkdir", "cd", "man", "touch", "cp",
        "mv", "rm", "rmdir", "cat", "less", "head", "grep",
        "wc", "chmod", "chown", "chgrp", "addUser", "addGroup",
        "ps", "quotacheck", "du", "gzip", "file", "find",
        "locate", "wget", "accessrights", "history", "clear"
    };

    public ShellUI() {
        super("Shell Interface - CPIT260 Final Project");
        initializeApplication();
        setupUIComponents();
        configureStyles();
        setupEventHandlers();
    }

    private void initializeApplication() {
        isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        currentDirectory = new File(System.getProperty("user.home"));
        commandHistory = new ArrayList<>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
    }

    private void setupUIComponents() {
        // Command selection combo box
        commandComboBox = new JComboBox<>(SUPPORTED_COMMANDS);
        commandComboBox.setEditable(true);
        ((JTextComponent) commandComboBox.getEditor().getEditorComponent()).setText("");

        // Arguments input field
        argumentsField = new JTextField(30);

        // Output area with styling support
        outputArea = new JTextPane();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Action buttons
        executeButton = new JButton("Execute");
        clearButton = new JButton("Clear Output");

        // Current directory display
        currentDirLabel = new JLabel("Current Directory: " + currentDirectory.getAbsolutePath());

        // Layout the components
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Command:"));
        topPanel.add(commandComboBox);
        topPanel.add(new JLabel("Arguments:"));
        topPanel.add(argumentsField);
        topPanel.add(executeButton);
        topPanel.add(clearButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        mainPanel.add(currentDirLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void configureStyles() {
        styleContext = new StyleContext();
        document = outputArea.getStyledDocument();

        // Default style
        Style defaultStyle = styleContext.addStyle("default", null);
        StyleConstants.setFontFamily(defaultStyle, "Monospaced");
        StyleConstants.setFontSize(defaultStyle, 12);

        // Command style (blue and bold)
        Style commandStyle = styleContext.addStyle("command", defaultStyle);
        StyleConstants.setForeground(commandStyle, new Color(0, 0, 200));
        StyleConstants.setBold(commandStyle, true);

        // Output style (black)
        Style outputStyle = styleContext.addStyle("output", defaultStyle);
        StyleConstants.setForeground(outputStyle, Color.BLACK);

        // Error style (red and bold)
        Style errorStyle = styleContext.addStyle("error", defaultStyle);
        StyleConstants.setForeground(errorStyle, new Color(200, 0, 0));
        StyleConstants.setBold(errorStyle, true);

        // Directory style (green)
        Style directoryStyle = styleContext.addStyle("directory", defaultStyle);
        StyleConstants.setForeground(directoryStyle, new Color(0, 128, 0));
    }

    private void setupEventHandlers() {
        // Command combo box key listener
        commandComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyEvents(e, commandComboBox);
            }
        });

        // Arguments field key listener
        argumentsField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Use Ctrl+Space for auto-completion 
                if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown()) {
                    autoCompletePath();
                    e.consume(); // Prevent the space from being typed
                } // Keep other key bindings (e.g., Up/Down for history)
                else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    navigateHistory(e.getKeyCode() == KeyEvent.VK_UP);
                    e.consume();
                }
            }
        });

        // Execute button action
        executeButton.addActionListener(e -> executeCommand());

        // Clear button action
        clearButton.addActionListener(e -> clearOutput());

        // Command selection change
        commandComboBox.addActionListener(e -> updateCommandHint());
    }

    private void handleKeyEvents(KeyEvent e, JComponent source) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                executeCommand();
                break;
            case KeyEvent.VK_TAB:
                if (source == commandComboBox) {
                    autoCompleteCommand();
                } else {
                    autoCompletePath();
                }
                e.consume();
                break;
            case KeyEvent.VK_UP:
                navigateHistory(true);
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                navigateHistory(false);
                e.consume();
                break;
        }
    }

    private void updateCommandHint() {
        String selectedCommand = (String) commandComboBox.getSelectedItem();
        if (selectedCommand == null) {
            return;
        }

        String hint = switch (selectedCommand) {
            case "ls" ->
                "[directory] (lists directory contents)";
            case "pwd" ->
                "(prints working directory)";
            case "mkdir" ->
                "directory_name (creates a directory)";
            case "cd" ->
                "directory_path (changes directory)";
            case "man" ->
                "command (shows manual for command)";
            case "touch" ->
                "file_name (creates empty file)";
            case "cp" ->
                "source_file target_file (copies file)";
            case "mv" ->
                "source target (moves/renames file)";
            case "rm" ->
                "file_name (removes file)";
            case "rmdir" ->
                "directory_name (removes empty directory)";
            case "cat" ->
                "file_name (displays file content)";
            case "less" ->
                "file_name (views file content page by page)";
            case "head" ->
                "[-n lines] file_name (shows first lines of file)";
            case "grep" ->
                "pattern file_name (searches for pattern in file)";
            case "wc" ->
                "file_name (counts lines, words, characters)";
            case "chmod" ->
                "permissions file (change file permissions)\n"
                + "Examples:\n"
                + "  chmod 755 script.sh\n"
                + "  chmod +x executable\n"
                + "Permissions: 4=read, 2=write, 1=execute";

            case "chown" ->
                "owner[:group] file (change file owner)\n"
                + "Examples:\n"
                + "  chown user file.txt\n"
                + "  chown user:group file.txt\n"
                + "Note: Requires admin/sudo on most systems";

            case "chgrp" ->
                "group file (change file group)\n"
                + "Examples:\n"
                + "  chgrp developers app.jar\n"
                + "Note: Requires admin/sudo on most systems";
            case "addUser" ->
                "username (adds a new user - requires admin)";
            case "addGroup" ->
                "groupname (adds a new group - requires admin)";
            case "ps" ->
                "(displays running processes)";
            case "quotacheck" ->
                "[drive:] (check filesystem quotas)";
            case "du" ->
                "[directory] (shows disk usage)";
            case "gzip" ->
                "file_name (compresses file)";
            case "file" ->
                "file_name (determines file type)";
            case "find" ->
                "directory -name pattern (finds files)";
            case "locate" ->
                "pattern (finds files in database)";
            case "wget" ->
                "URL (downloads file from internet)";
            case "accessrights" ->
                "file_or_directory (displays access rights)";
            case "history" ->
                "(shows command history)";
            case "clear" ->
                "(clears the output screen)";
            default ->
                "(enter command arguments)";
        };

        argumentsField.setToolTipText(hint);
    }

    private void executeCommand() {
        String command = (String) commandComboBox.getSelectedItem();
        String arguments = argumentsField.getText().trim();
        String fullCommand = command + (arguments.isEmpty() ? "" : " " + arguments);

        // Add to command history (except for history command itself)
        if (!"history".equals(command)) {
            commandHistory.add(fullCommand);
        }
        historyIndex = commandHistory.size();

        try {
            // Display the command in output
            appendToOutput("$ " + fullCommand + "\n", "command");

            // Handle special commands
            switch (command) {
                case "cd":
                    appendToOutput(changeDirectory(arguments) + "\n\n", "output");
                    break;
                case "pwd":
                    appendToOutput(currentDirectory.getAbsolutePath() + "\n\n", "output");
                    break;
                case "accessrights":
                    appendToOutput(displayAccessRights(arguments) + "\n\n", "output");
                    break;
                case "history":
                    showCommandHistory();
                    break;
                case "clear":
                    clearOutput();
                    break;
                case "man":
                    String manual = getManualPage(arguments);

                    appendToOutput(manual + "\n\n", "output");
                    break;
                default:
                    // Execute system command
                    String output = executeSystemCommand(command, arguments);
                    highlightOutput(output);
                    appendToOutput("\n", "default");
            }
        } catch (Exception ex) {
            appendToOutput("Error: " + ex.getMessage() + "\n\n", "error");
        }

        // Clear arguments field after execution
        argumentsField.setText("");
    }

    private String changeDirectory(String dirPath) {
        File newDir;
        if (dirPath.isEmpty()) {
            // Change to home directory
            newDir = new File(System.getProperty("user.home"));
        } else if (dirPath.equals("..")) {
            // Move up one directory
            newDir = currentDirectory.getParentFile();
        } else if (dirPath.startsWith("/") || dirPath.startsWith("\\")
                || (dirPath.length() > 1 && dirPath.charAt(1) == ':')) {
            // Absolute path
            newDir = new File(dirPath);
        } else {
            // Relative path
            newDir = new File(currentDirectory, dirPath);
        }

        if (newDir.exists() && newDir.isDirectory()) {
            currentDirectory = newDir;
            currentDirLabel.setText("Current Directory: " + currentDirectory.getAbsolutePath());
            return "Changed directory to: " + currentDirectory.getAbsolutePath();
        } else {
            throw new RuntimeException("Directory not found: " + newDir.getAbsolutePath());
        }
    }

    private String displayAccessRights(String path) {
        File file;
        if (path.isEmpty()) {
            file = currentDirectory;
        } else if (path.startsWith("/") || path.startsWith("\\")
                || (path.length() > 1 && path.charAt(1) == ':')) {
            file = new File(path);
        } else {
            file = new File(currentDirectory, path);
        }

        if (!file.exists()) {
            throw new RuntimeException("File/directory not found: " + file.getAbsolutePath());
        }

        StringBuilder rights = new StringBuilder();
        rights.append("Access rights for: ").append(file.getAbsolutePath()).append("\n");

        if (isWindows) {
            rights.append("Readable: ").append(file.canRead()).append("\n");
            rights.append("Writable: ").append(file.canWrite()).append("\n");
            rights.append("Executable: ").append(file.canExecute()).append("\n");
            rights.append("Hidden: ").append(file.isHidden()).append("\n");
        } else {
            try {
                Process process = new ProcessBuilder("ls", "-ld", file.getAbsolutePath())
                        .directory(currentDirectory)
                        .start();

                String output = readProcessOutput(process);
                rights.append(output);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to get access rights: " + ex.getMessage());
            }
        }

        return rights.toString();
    }

    private String executeSystemCommand(String command, String arguments) throws IOException, InterruptedException {
        List<String> commandParts = new ArrayList<>();
        // Handle commands specially
        switch (command) {
            case "cp":
                return handleCopyCommand(arguments);
            case "mv":
                return handleMoveCommand(arguments);
            case "cat":
                return handleCatCommand(arguments);
            case "less":
                return handleLessCommand(arguments);
            case "head":
                return handleHeadCommand(arguments);
            case "grep":
                return handleGrepCommand(arguments);
            case "wc":
                return handleWcCommand(arguments);
            case "chmod":
                return handleChmodCommand(arguments);
            case "chown":
                return handleChownCommand(arguments);
            case "chgrp":
                return handleChgrpCommand(arguments);
            case "addUser":
                return handleAddUserCommand(arguments);
            case "addGroup":
                return handleAddGroupCommand(arguments);
            case "ps":
                return handlePsCommand(arguments);
            case "quotacheck":
                return handleQuotacheckCommand(arguments);
            case "du":
                return handleDuCommand(arguments);
            case "gzip":
                return handleGzipCommand(arguments);
            case "file":
                return handleFileCommand(arguments);
            case "find":
                return handleFindCommand(arguments);
            case "locate":
                return handleLocateCommand(arguments);
            case "wget":
                return handleWgetCommand(arguments);
            case "accessrights":
                return handleAccessRightsCommand(arguments);
            default:
            // Default command handling
        }
        if (isWindows) {
            commandParts.add("cmd.exe");
            commandParts.add("/c");

            // Special handling for Windows commands
            switch (command) {
                case "cp":
                    commandParts.add("copy");
                    break;
                case "ls":
                    commandParts.add("dir");
                    break;
                case "mkdir":
                    commandParts.add("mkdir");
                    break;
                case "touch":
                    // Special handling for touch command
                    if (arguments.isEmpty()) {
                        throw new RuntimeException("touch requires a filename argument");
                    }
                    File touchFile = arguments.startsWith("/") || arguments.startsWith("\\")
                            || (arguments.length() > 1 && arguments.charAt(1) == ':')
                            ? new File(arguments) : new File(currentDirectory, arguments);
                    if (!touchFile.exists()) {
                        try {
                            if (!touchFile.createNewFile()) {
                                throw new RuntimeException("Failed to create file: " + touchFile.getAbsolutePath());
                            }
                            return "Created empty file: " + touchFile.getAbsolutePath();
                        } catch (IOException e) {
                            throw new RuntimeException("Error creating file: " + e.getMessage());
                        }
                    } else {
                        return "File already exists: " + touchFile.getAbsolutePath();
                    }
                case "rm":
                    commandParts.add("del");
                    break;
                case "rmdir":
                    commandParts.add("rmdir");
                    break;
                default:
                    // For commands that are the same in Windows
                    commandParts.add(command);
            }

            if (!arguments.isEmpty() && !command.equals("touch")) {
                commandParts.add(arguments);
            }
        } else {
            // Unix-like systems - just use the command directly
            commandParts.add(command);
            if (!arguments.isEmpty()) {
                commandParts.addAll(Arrays.asList(arguments.split(" ")));
            }
            // Handle arguments for copy command
            if (command.equals("cp") && !arguments.isEmpty()) {
                // Split arguments while preserving paths with spaces
                String[] args = arguments.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String arg : args) {
                    commandParts.add(arg.replace("\"", ""));
                }
            } else if (!arguments.isEmpty()) {
                commandParts.add(arguments);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(commandParts);
        pb.directory(currentDirectory);
        pb.redirectErrorStream(true); // Merge error stream with output

        Process process = pb.start();
        String output = readProcessOutput(process);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }

        return output;
    }

    private String handleCopyCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("cp requires source and destination arguments");
        }

        // Parse source and destination paths
        String[] paths = parsePaths(arguments);
        if (paths.length < 2) {
            throw new RuntimeException("cp requires both source and destination arguments");
        }

        String source = paths[0];
        String destination = paths[1];

        // Verify source exists
        File sourceFile = resolvePath(source);
        if (!sourceFile.exists()) {
            throw new RuntimeException("Source file/directory does not exist: " + source);
        }

        // Prepare command based on OS
        List<String> commandParts = new ArrayList<>();
        if (isWindows) {
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("copy");
            commandParts.add("\"" + sourceFile.getAbsolutePath() + "\"");

            File destFile = resolvePath(destination);
            if (destination.endsWith("\\") || destination.endsWith("/")) {
                // Destination is a directory
                if (!destFile.exists()) {
                    destFile.mkdirs();  // Create directory if it doesn't exist
                }
                commandParts.add("\"" + destFile.getAbsolutePath() + "\"");
            } else {
                // Destination is a file path
                commandParts.add("\"" + destFile.getAbsolutePath() + "\"");
            }
        } else {
            // Unix-like systems
            commandParts.add("cp");
            commandParts.add(source);
            commandParts.add(destination);
        }

        ProcessBuilder pb = new ProcessBuilder(commandParts);
        pb.directory(currentDirectory);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = readProcessOutput(process);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Copy failed. Verify paths and permissions.\n"
                    + "Source: " + source + "\n"
                    + "Destination: " + destination + "\n"
                    + "System message: " + output);
        }

        return "Copied successfully: " + source + " → " + destination;
    }

    private File resolvePath(String path) {
        if (path.startsWith("/") || path.startsWith("\\")
                || (path.length() > 1 && path.charAt(1) == ':')) {
            return new File(path);
        }
        return new File(currentDirectory, path);
    }

    private String[] parsePaths(String arguments) {
        // Handle quoted paths containing spaces
        List<String> paths = new ArrayList<>();
        StringBuilder currentPath = new StringBuilder();
        boolean inQuotes = false;

        for (char c : arguments.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (currentPath.length() > 0) {
                    paths.add(currentPath.toString());
                    currentPath.setLength(0);
                }
            } else {
                currentPath.append(c);
            }
        }

        if (currentPath.length() > 0) {
            paths.add(currentPath.toString());
        }

        return paths.toArray(new String[0]);
    }

    private String handleMoveCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("mv requires source and destination arguments");
        }

        // Parse source and destination paths
        String[] paths = parsePaths(arguments);
        if (paths.length < 2) {
            throw new RuntimeException("mv requires both source and destination arguments");
        }

        String source = paths[0];
        String destination = paths[1];

        // Verify source exists
        File sourceFile = resolvePath(source);
        if (!sourceFile.exists()) {
            throw new RuntimeException("Source file/directory does not exist: " + source);
        }

        // Prepare command based on OS
        List<String> commandParts = new ArrayList<>();
        if (isWindows) {
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("move");
            commandParts.add("\"" + sourceFile.getAbsolutePath() + "\"");

            File destFile = resolvePath(destination);
            if (destination.endsWith("\\") || destination.endsWith("/")) {
                // Destination is a directory
                if (!destFile.exists()) {
                    destFile.mkdirs();  // Create directory if it doesn't exist
                }
                commandParts.add("\"" + destFile.getAbsolutePath() + "\"");
            } else {
                // Destination is a file path
                commandParts.add("\"" + destFile.getAbsolutePath() + "\"");
            }
        } else {
            // Unix-like systems
            commandParts.add("mv");
            commandParts.add(source);
            commandParts.add(destination);
        }

        ProcessBuilder pb = new ProcessBuilder(commandParts);
        pb.directory(currentDirectory);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = readProcessOutput(process);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Move failed. Verify paths and permissions.\n"
                    + "Source: " + source + "\n"
                    + "Destination: " + destination + "\n"
                    + "System message: " + output);
        }

        return "Moved successfully: " + source + " → " + destination;
    }

    private String handleCatCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("cat requires at least one file argument");
        }

        // Parse file paths (handling spaces in filenames)
        String[] filePaths = parsePaths(arguments);

        StringBuilder content = new StringBuilder();

        if (isWindows) {
            // Windows implementation using 'type' command
            for (String filePath : filePaths) {
                File file = resolvePath(filePath);
                if (!file.exists()) {
                    throw new RuntimeException("File not found: " + filePath);
                }

                List<String> commandParts = new ArrayList<>();
                commandParts.add("cmd.exe");
                commandParts.add("/c");
                commandParts.add("type");
                commandParts.add("\"" + file.getAbsolutePath() + "\"");

                ProcessBuilder pb = new ProcessBuilder(commandParts);
                pb.directory(currentDirectory);
                pb.redirectErrorStream(true);

                Process process = pb.start();
                content.append(readProcessOutput(process));

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("Failed to read file: " + filePath);
                }
            }
        } else {
            // Unix implementation using native 'cat'
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cat");
            for (String filePath : filePaths) {
                File file = resolvePath(filePath);
                if (!file.exists()) {
                    throw new RuntimeException("File not found: " + filePath);
                }
                commandParts.add(file.getAbsolutePath());
            }

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            content.append(readProcessOutput(process));

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to read files");
            }
        }

        return content.toString();
    }

    private String handleLessCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("less requires a file argument");
        }

        String[] parts = parsePaths(arguments);
        if (parts.length > 1) {
            throw new RuntimeException("less only supports one file at a time");
        }

        File file = resolvePath(parts[0]);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + parts[0]);
        }

        if (isWindows) {
            // Windows implementation using more (basic paging)
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("more");
            commandParts.add("\"" + file.getAbsolutePath() + "\"");

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            Process process = pb.start();

            // For Windows, we'll let more handle the paging
            return "Opening file in pager... Use spacebar to page, Q to quit";
        } else {
            // Unix implementation using less
            List<String> commandParts = new ArrayList<>();
            commandParts.add("less");
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            Process process = pb.start();
            return readProcessOutput(process);
            // For Unix, less will handle the paging directly in terminal
//            return "Opening file in less pager... Use arrow keys, Q to quit";
        }
    }

    private String handleHeadCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("head requires a file argument");
        }

        // Parse arguments for -n option
        int lines = 10; // default
        String[] parts = arguments.split(" ");
        String filePath = arguments;

        if (parts.length >= 2 && parts[0].equals("-n")) {
            try {
                lines = Integer.parseInt(parts[1]);
                filePath = arguments.substring(parts[0].length() + parts[1].length() + 2).trim();
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid line count: " + parts[1]);
            }
        }

        filePath = filePath.replace("\"", ""); // Remove quotes if present
        File file = resolvePath(filePath);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (isWindows) {
            // Windows implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("powershell");
            commandParts.add("-Command");
            commandParts.add("Get-Content");
            commandParts.add("\"" + file.getAbsolutePath() + "\"");
            commandParts.add("-Head");
            commandParts.add(String.valueOf(lines));

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("head");
            commandParts.add("-n");
            commandParts.add(String.valueOf(lines));
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        }
    }

    private String handleGrepCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("grep requires a pattern and file argument");
        }

        // Parse pattern and file path (handling quoted strings)
        String[] parts = parsePaths(arguments);
        if (parts.length < 2) {
            throw new RuntimeException("grep requires both pattern and file arguments");
        }

        String pattern = parts[0];
        String filePath = parts[1];
        File file = resolvePath(filePath);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (isWindows) {
            // Windows implementation using findstr
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("findstr");
            commandParts.add("/n"); // show line numbers
            commandParts.add("\"" + pattern + "\"");
            commandParts.add("\"" + file.getAbsolutePath() + "\"");

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("grep");
            commandParts.add("-n"); // show line numbers
            commandParts.add("--color=always"); // colored output if supported
            commandParts.add(pattern);
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        }
    }

    private String handleWcCommand(String arguments) throws IOException, InterruptedException {
        if (arguments == null || arguments.trim().isEmpty()) {
            // If no arguments, read from standard input (not implemented here)
            throw new RuntimeException("wc requires file arguments or standard input");
        }

        String[] filePaths = parsePaths(arguments);
        StringBuilder result = new StringBuilder();
        int totalLines = 0;
        int totalWords = 0;
        int totalChars = 0;

        for (String filePath : filePaths) {
            File file = resolvePath(filePath);
            if (!file.exists() || !file.isFile()) {
                throw new RuntimeException("File not found: " + filePath);
            }

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            // Line count (split by various line endings)
            int lineCount = content.split("\r\n|\r|\n").length;
            // Handle empty last line
            if (content.endsWith("\r") || content.endsWith("\n") || content.endsWith("\r\n")) {
                lineCount++;
            }

            // Word count (split by whitespace)
            String trimmed = content.trim();
            int wordCount = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;

            // Character count
            int charCount = content.length();

            // Format output like Unix wc
            result.append(String.format("%7d %7d %7d %s%n", lineCount, wordCount, charCount, filePath));

            // Update totals
            totalLines += lineCount;
            totalWords += wordCount;
            totalChars += charCount;
        }

        // Add totals line if multiple files
        if (filePaths.length > 1) {
            result.append(String.format("%7d %7d %7d %s%n", totalLines, totalWords, totalChars, "total"));
        }

        return result.toString();
    }

    private String handleChmodCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("chmod requires permissions and file arguments");
        }

        String[] parts = arguments.split(" ", 2);
        if (parts.length < 2) {
            throw new RuntimeException("chmod requires both permissions and file arguments");
        }

        String permissions = parts[0];
        String filePath = parts[1].replace("\"", "");
        File file = resolvePath(filePath);

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (isWindows) {
            // Windows implementation (basic read-only attribute)
            if (permissions.matches("[0-7]+")) {
                boolean readable = Integer.parseInt(permissions.substring(0, 1)) >= 4;
                boolean writable = Integer.parseInt(permissions.substring(1, 2)) >= 2;
                boolean executable = Integer.parseInt(permissions.substring(2, 3)) >= 1;

                file.setReadable(readable);
                file.setWritable(writable);
                file.setExecutable(executable);
                return "Set permissions for " + filePath;
            } else {
                throw new RuntimeException("Windows only supports numeric permissions (e.g., 755)");
            }
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("chmod");
            commandParts.add(permissions);
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to change permissions: " + output);
            }
            return "Changed permissions for " + filePath;
        }
    }

    private String handleChownCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("chown requires owner and file arguments");
        }

        String[] parts = arguments.split(" ", 2);
        if (parts.length < 2) {
            throw new RuntimeException("chown requires both owner and file arguments");
        }

        String owner = parts[0];
        String filePath = parts[1].replace("\"", "");
        File file = resolvePath(filePath);

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (isWindows) {
            // Windows implementation (requires admin privileges)
            // Step 1: Take ownership first (required before we can change owner)
            List<String> takeOwnCommand = new ArrayList<>();
            takeOwnCommand.add("cmd.exe");
            takeOwnCommand.add("/c");
            takeOwnCommand.add("takeown");
            takeOwnCommand.add("/f");
            takeOwnCommand.add(file.getAbsolutePath());

            ProcessBuilder takeOwnPb = new ProcessBuilder(takeOwnCommand);
            takeOwnPb.directory(currentDirectory);
            takeOwnPb.redirectErrorStream(true);
            Process takeOwnProcess = takeOwnPb.start();

            String takeOwnOutput = readProcessOutput(takeOwnProcess);
            int takeOwnExitCode = takeOwnProcess.waitFor();

            if (takeOwnExitCode != 0) {
                throw new RuntimeException("Failed to take ownership (Admin required): " + takeOwnOutput);
            }

            // Step 2: Now change the owner using icacls
            List<String> icaclsCommand = new ArrayList<>();
            icaclsCommand.add("cmd.exe");
            icaclsCommand.add("/c");
            icaclsCommand.add("icacls");
            icaclsCommand.add(file.getAbsolutePath());
            icaclsCommand.add("/setowner");
            icaclsCommand.add(owner);
            icaclsCommand.add("/t");  // Recursive for directories
            icaclsCommand.add("/c");  // Continue on errors
            icaclsCommand.add("/l");  // Process symbolic links
            icaclsCommand.add("/q");   // Quiet mode

            ProcessBuilder icaclsPb = new ProcessBuilder(icaclsCommand);
            icaclsPb.directory(currentDirectory);
            icaclsPb.redirectErrorStream(true);
            Process icaclsProcess = icaclsPb.start();

            String icaclsOutput = readProcessOutput(icaclsProcess);
            int icaclsExitCode = icaclsProcess.waitFor();

            if (icaclsExitCode != 0) {
                throw new RuntimeException("Failed to change owner (Admin required): " + icaclsOutput);
            }

            return "Changed owner of " + filePath + " to " + owner;
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("chown");
            commandParts.add(owner);
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to change owner: " + output);
            }
            return "Changed owner of " + filePath + " to " + owner;
        }
    }

    private String handleChgrpCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("chgrp requires group and file arguments");
        }

        String[] parts = arguments.split(" ", 2);
        if (parts.length < 2) {
            throw new RuntimeException("chgrp requires both group and file arguments");
        }

        String group = parts[0];
        String filePath = parts[1].replace("\"", "");
        File file = resolvePath(filePath);

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (isWindows) {
            // Windows implementation using icacls
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("icacls");
            commandParts.add("\"" + file.getAbsolutePath() + "\"");
            commandParts.add("/grant:r");
            commandParts.add(group + ":(R,W,Rc)");
            commandParts.add("/T"); // Recursive
            commandParts.add("/C"); // Continue on errors
            commandParts.add("/Q"); // Quiet mode

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to change group (Run as Administrator): " + output);
            }
            return "Modified permissions for group " + group + " on " + filePath;
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("chgrp");
            commandParts.add(group);
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to change group: " + output);
            }
            return "Changed group of " + filePath + " to " + group;
        }
    }

    private String handleAddUserCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("addUser requires a username argument");
        }

        String username = arguments.split(" ")[0].replace("\"", "");

        if (isWindows) {
            // Windows implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("net");
            commandParts.add("user");
            commandParts.add(username);
            commandParts.add("/add");

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to add user (Admin required): " + output);
            }
            return "Added user: " + username;
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("sudo");
            commandParts.add("useradd");
            commandParts.add("-m"); // Create home directory
            commandParts.add(username);

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to add user (Need sudo): " + output);
            }
            return "Added user: " + username;
        }
    }

    private String handleAddGroupCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("addGroup requires a groupname argument");
        }

        String groupname = arguments.split(" ")[0].replace("\"", "");

        if (isWindows) {
            // Windows implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("net");
            commandParts.add("localgroup");
            commandParts.add(groupname);
            commandParts.add("/add");

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to add group (Admin required): " + output);
            }
            return "Added group: " + groupname;
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("sudo");
            commandParts.add("groupadd");
            commandParts.add(groupname);

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Failed to add group (Need sudo): " + output);
            }
            return "Added group: " + groupname;
        }
    }

    private String handlePsCommand(String arguments) throws IOException, InterruptedException {
        if (isWindows) {
            // Windows implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("tasklist");

            if (!arguments.isEmpty()) {
                commandParts.add(arguments);
            }

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("ps");
            commandParts.add("aux");

            if (!arguments.isEmpty()) {
                commandParts.addAll(Arrays.asList(arguments.split(" ")));
            }

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        }
    }

    private String handleQuotacheckCommand(String arguments) throws IOException, InterruptedException {
        if (isWindows) {
            // Improved Windows implementation with actual quota checking
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("fsutil");
            commandParts.add("quota");
            commandParts.add("query");

            // Add drive letter if specified
            if (!arguments.isEmpty()) {
                String drive = arguments.split(" ")[0];
                if (drive.matches("^[A-Za-z]:$")) {
                    commandParts.add(drive);
                }
            }

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                if (output.contains("This system does not support quotas")) {
                    return "Quotas are not enabled on this system";
                }
                throw new RuntimeException("Failed to check quotas: " + output);
            }

            if (output.trim().isEmpty()) {
                return "No quota information available (quotas may be disabled)";
            }
            return output;
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("sudo");
            commandParts.add("quotacheck");

            if (!arguments.isEmpty()) {
                commandParts.addAll(Arrays.asList(arguments.split(" ")));
            } else {
                commandParts.add("-avug"); // Default options
            }

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("quotacheck failed (Need sudo): " + output);
            }
            return output;
        }
    }

    private String handleDuCommand(String arguments) throws IOException, InterruptedException {
        if (isWindows) {
            List<String> commandParts = new ArrayList<>();
            commandParts.add("powershell.exe");
            commandParts.add("-Command");
            commandParts.add("function Get-DirectorySize {");
            commandParts.add("  param ([string]$folder = '.')");
            commandParts.add("  Get-ChildItem $folder -Recurse -File | ");
            commandParts.add("  Measure-Object -Property Length -Sum | ");
            commandParts.add("  Select-Object @{Name='Size (MB)';Expression={[math]::Round($_.Sum / 1MB, 2)}},");
            commandParts.add("          @{Name='Size (GB)';Expression={[math]::Round($_.Sum / 1GB, 2)}}");
            commandParts.add("}");
            commandParts.add("Get-DirectorySize " + (arguments.isEmpty() ? "'" + currentDirectory.getAbsolutePath() + "'" : arguments));

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        } else {
            List<String> commandParts = new ArrayList<>();
            commandParts.add("du");
            commandParts.add("-h"); // Human readable
            if (!arguments.isEmpty()) {
                commandParts.addAll(Arrays.asList(arguments.split(" ")));
            } else {
                commandParts.add(".");
            }

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        }
    }

    private String handleGzipCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("gzip requires a file argument");
        }

        String filePath = arguments.split(" ")[0].replace("\"", "");
        File file = resolvePath(filePath);

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (isWindows) {
            // Using built-in compact command
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("compact");
            commandParts.add("/C");
            commandParts.add("\"" + file.getAbsolutePath() + "\"");

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Compression failed: " + output);
            }
            return "Compressed: " + filePath;
        } else {
            List<String> commandParts = new ArrayList<>();
            commandParts.add("gzip");
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("gzip failed: " + output);
            }
            return "Compressed: " + filePath + ".gz";
        }
    }

    private String handleFileCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("file requires a file argument");
        }

        String filePath = arguments.split(" ")[0].replace("\"", "");
        File file = resolvePath(filePath);

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (isWindows) {
            // Basic file type detection
            String type = "unknown";
            if (file.isDirectory()) {
                type = "directory";
            } else {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".exe")) {
                    type = "executable";
                } else if (name.endsWith(".txt")) {
                    type = "text";
                } else if (name.endsWith(".jpg") || name.endsWith(".png")) {
                    type = "image";
                }
                // Add more types as needed
            }
            return filePath + ": " + type;
        } else {
            List<String> commandParts = new ArrayList<>();
            commandParts.add("file");
            commandParts.add(file.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        }
    }

    private String handleFindCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("find requires a search pattern");
        }

        if (isWindows) {
            // Improved Windows implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("dir");
            commandParts.add("/s/b"); // Recursive, bare format
            commandParts.add("*" + arguments + "*"); // Wildcard search

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);

            if (output.contains("File Not Found")) {
                return "No files matching '" + arguments + "' found in " + currentDirectory;
            }
            return output;
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("find");
            commandParts.add(".");
            commandParts.add("-name");
            commandParts.add("*" + arguments + "*");

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        }
    }

    private String handleLocateCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("locate requires a search pattern");
        }

        if (isWindows) {
            // Windows implementation using dir and findstr
            List<String> commandParts = new ArrayList<>();
            commandParts.add("cmd.exe");
            commandParts.add("/c");
            commandParts.add("dir");
            commandParts.add("/s/b"); // Recursive search, bare format
            commandParts.add("*");    // All files
            commandParts.add("|");
            commandParts.add("findstr");
            commandParts.add("/i");    // Case insensitive
            commandParts.add("\"" + arguments + "\"");

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.directory(currentDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);

            if (output.trim().isEmpty()) {
                return "No files matching '" + arguments + "' found";
            }
            return output;
        } else {
            // Unix implementation
            List<String> commandParts = new ArrayList<>();
            commandParts.add("locate");
            commandParts.add(arguments);

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            return readProcessOutput(process);
        }
    }

    private String handleWgetCommand(String arguments) throws IOException, InterruptedException {
        if (arguments.isEmpty()) {
            throw new RuntimeException("wget requires a URL argument");
        }

        String url = arguments.split(" ")[0].replace("\"", "");

        if (isWindows) {
            List<String> commandParts = new ArrayList<>();
            commandParts.add("powershell");
            commandParts.add("-Command");
            commandParts.add("Invoke-WebRequest");
            commandParts.add("-Uri");
            commandParts.add(url);
            commandParts.add("-OutFile");
            commandParts.add(url.substring(url.lastIndexOf('/') + 1));

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Download failed: " + output);
            }
            return "Downloaded: " + url;
        } else {
            List<String> commandParts = new ArrayList<>();
            commandParts.add("wget");
            commandParts.add(url);

            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("wget failed: " + output);
            }
            return "Downloaded: " + url;
        }
    }

    private String handleAccessRightsCommand(String arguments) throws IOException {
        String path = arguments.isEmpty() ? currentDirectory.getAbsolutePath() : arguments;
        File file = resolvePath(path);

        if (!file.exists()) {
            throw new RuntimeException("File/directory not found: " + path);
        }

        StringBuilder rights = new StringBuilder();
        rights.append("Access rights for: ").append(file.getAbsolutePath()).append("\n");

        if (isWindows) {
            rights.append("Readable: ").append(file.canRead()).append("\n");
            rights.append("Writable: ").append(file.canWrite()).append("\n");
            rights.append("Executable: ").append(file.canExecute()).append("\n");
            rights.append("Hidden: ").append(file.isHidden()).append("\n");

            try {
                // Get more detailed ACL info
                Process process = new ProcessBuilder("icacls", file.getAbsolutePath())
                        .redirectErrorStream(true)
                        .start();
                rights.append("\nDetailed permissions:\n")
                        .append(readProcessOutput(process));
            } catch (IOException e) {
                rights.append("\nCould not retrieve detailed permissions (Admin required)");
            }
        } else {
            try {
                Process process = new ProcessBuilder("ls", "-ld", file.getAbsolutePath())
                        .redirectErrorStream(true)
                        .start();
                rights.append(readProcessOutput(process));

                // Get ACL info if available
                try {
                    Process aclProcess = new ProcessBuilder("getfacl", file.getAbsolutePath())
                            .redirectErrorStream(true)
                            .start();
                    rights.append("\nACL details:\n")
                            .append(readProcessOutput(aclProcess));
                } catch (IOException e) {
                    // getfacl not available
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to get access rights: " + e.getMessage());
            }
        }

        return rights.toString();
    }

    private String getManualPage(String command) {
        Map<String, String> manualPages = new HashMap<>();

        // Common manual pages
        manualPages.put("pwd", "pwd - Print Working Directory\n"
                + "Displays the full path of the current directory.\n"
                + "Usage: pwd");

        manualPages.put("ls", "ls - List Directory Contents\n"
                + "Displays files and directories in the current directory.\n"
                + "Options:\n"
                + "  -l: Long format listing\n"
                + "  -a: Include hidden files\n"
                + "Usage: ls [options] [directory]");

        manualPages.put("cd", "cd - Change Directory\n"
                + "Changes the current working directory.\n"
                + "Usage: cd [directory]\n"
                + "Special paths:\n"
                + "  .. : Parent directory\n"
                + "  ~  : Home directory");

        manualPages.put("mkdir", "mkdir - Make Directory\n"
                + "Creates a new directory.\n"
                + "Usage: mkdir directory_name");

        manualPages.put("man", "man - Manual Pages\n"
                + "Displays documentation for commands.\n"
                + "Usage: man command");

        // Additional commands
        manualPages.put("echo", "echo - Display Message\n"
                + "Prints text or variables to the terminal.\n"
                + "Usage: echo [text]");

        manualPages.put("cat", "cat - Concatenate Files\n"
                + "Displays file contents or combines files.\n"
                + "Usage: cat [file1] [file2]...");

        manualPages.put("grep", "grep - Global Regular Expression Print\n"
                + "Searches for patterns in files.\n"
                + "Options:\n"
                + "  -i: Case insensitive search\n"
                + "  -r: Recursive search\n"
                + "Usage: grep [options] pattern [file]");

        manualPages.put("cp", "cp - Copy Files\n"
                + "Copies files or directories.\n"
                + "Usage: cp [source] [destination]");

        manualPages.put("mv", "mv - Move Files\n"
                + "Moves or renames files/directories.\n"
                + "Usage: mv [source] [destination]");

        manualPages.put("rm", "rm - Remove Files\n"
                + "Deletes files or directories.\n"
                + "Options:\n"
                + "  -r: Recursive delete (for directories)\n"
                + "  -f: Force delete\n"
                + "Usage: rm [options] [file/directory]");

        System.out.println("Requested manual for: " + command);

        String manualEntry = manualPages.get(command);
        return manualEntry != null ? manualEntry : "We will provide description of '" + command + "' soon.";
    }

    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    private void appendToOutput(String text, String style) {
        try {
            document.insertString(document.getLength(), text, styleContext.getStyle(style));
            outputArea.setCaretPosition(document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void clearOutput() {
        outputArea.setText("");
    }

    private void highlightOutput(String output) {
        String[] lines = output.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            if (line.startsWith("total") || line.matches("^\\d+.*")) {
                // Disk usage or similar numerical output
                appendToOutput(line + "\n", "output");
            } else if (line.matches("^[drwx-]+\\s+\\d+.*")) {
                // Unix file listing
                appendToOutput(line + "\n", "output");
            } else if (line.matches("^\\d+/\\d+/\\d+\\s+\\d+:\\d+\\s+[AP]M.*")) {
                // Windows dir output
                appendToOutput(line + "\n", "output");
            } else if (line.toLowerCase().contains("error") || line.toLowerCase().contains("fail")) {
                // Error messages
                appendToOutput(line + "\n", "error");
            } else if (line.matches("^[A-Za-z]:\\.*")) {
                // Directory paths
                appendToOutput(line + "\n", "directory");
            } else {
                // Regular output
                appendToOutput(line + "\n", "output");
            }
        }
    }

    private void autoCompleteCommand() {
        String partial = ((JTextComponent) commandComboBox.getEditor().getEditorComponent()).getText().toLowerCase();
        if (partial.isEmpty()) {
            return;
        }

        List<String> matches = new ArrayList<>();
        for (String cmd : SUPPORTED_COMMANDS) {
            if (cmd.toLowerCase().startsWith(partial)) {
                matches.add(cmd);
            }
        }

        if (matches.size() == 1) {
            commandComboBox.setSelectedItem(matches.get(0));
        } else if (matches.size() > 1) {
            // Show possible completions
            appendToOutput("Possible completions:\n", "output");
            for (String match : matches) {
                appendToOutput("  " + match + "\n", "output");
            }
            appendToOutput("\n", "default");

            // Find common prefix
            String commonPrefix = findCommonPrefix(matches);
            if (commonPrefix.length() > partial.length()) {
                commandComboBox.setSelectedItem(commonPrefix);
            }
        }
    }

    private void autoCompletePath() {
        String text = argumentsField.getText();
        if (text.isEmpty()) {
            return;
        }

        String partialPath;
        String prefix = "";
        int lastSpace = text.lastIndexOf(' ');
        if (lastSpace >= 0) {
            prefix = text.substring(0, lastSpace + 1);
            partialPath = text.substring(lastSpace + 1);
        } else {
            partialPath = text;
        }

        File dir;
        String filePattern;

        if (partialPath.contains("/") || partialPath.contains("\\")) {
            // Path with directories
            int lastSep = Math.max(partialPath.lastIndexOf('/'), partialPath.lastIndexOf('\\'));
            String dirPath = partialPath.substring(0, lastSep + 1);
            filePattern = partialPath.substring(lastSep + 1);

            if (dirPath.startsWith("/") || dirPath.startsWith("\\")
                    || (dirPath.length() > 1 && dirPath.charAt(1) == ':')) {
                // Absolute path
                dir = new File(dirPath);
            } else {
                // Relative path
                dir = new File(currentDirectory, dirPath);
            }
        } else {
            // Just a filename in current directory
            dir = currentDirectory;
            filePattern = partialPath;
        }

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        List<String> matches = new ArrayList<>();
        for (File file : files) {
            if (file.getName().toLowerCase().startsWith(filePattern.toLowerCase())) {
                matches.add(file.getName() + (file.isDirectory() ? (isWindows ? "\\" : "/") : ""));
            }
        }

        if (matches.size() == 1) {
            String completed = prefix + partialPath.substring(0,
                    partialPath.lastIndexOf('/') + partialPath.lastIndexOf('\\') + 1) + matches.get(0);
            argumentsField.setText(completed);
        } else if (matches.size() > 1) {
            // Find common prefix
            String commonPrefix = findCommonPrefix(matches);
            if (commonPrefix.length() > filePattern.length()) {
                String completed = prefix + partialPath.substring(0,
                        partialPath.lastIndexOf('/') + partialPath.lastIndexOf('\\') + 1) + commonPrefix;
                argumentsField.setText(completed);
            }

            // Show possible completions
            appendToOutput("Possible completions:\n", "output");
            for (String match : matches) {
                appendToOutput("  " + match + "\n", "output");
            }
            appendToOutput("\n", "default");
        }
    }

    private String findCommonPrefix(List<String> strings) {
        if (strings.isEmpty()) {
            return "";
        }

        String prefix = strings.get(0);
        for (int i = 1; i < strings.size(); i++) {
            while (!strings.get(i).startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) {
                    return "";
                }
            }
        }
        return prefix;
    }

    private void navigateHistory(boolean up) {
        if (commandHistory.isEmpty()) {
            return;
        }

        if (up) {
            if (historyIndex > 0) {
                historyIndex--;
            }
        } else {
            if (historyIndex < commandHistory.size() - 1) {
                historyIndex++;
            } else if (historyIndex == commandHistory.size() - 1) {
                historyIndex++;
                // Clear fields when going past the most recent command
                commandComboBox.setSelectedItem("");
                argumentsField.setText("");
                return;
            }
        }

        if (historyIndex >= 0 && historyIndex < commandHistory.size()) {
            String historyCommand = commandHistory.get(historyIndex);
            // Split into command and arguments
            int firstSpace = historyCommand.indexOf(' ');
            if (firstSpace > 0) {
                commandComboBox.setSelectedItem(historyCommand.substring(0, firstSpace));
                argumentsField.setText(historyCommand.substring(firstSpace + 1));
            } else {
                commandComboBox.setSelectedItem(historyCommand);
                argumentsField.setText("");
            }
        }
    }

    private void showCommandHistory() {
        if (commandHistory.isEmpty()) {
            appendToOutput("No commands in history\n\n", "output");
            return;
        }

        appendToOutput("Command history:\n", "output");
        for (int i = 0; i < commandHistory.size(); i++) {
            appendToOutput(String.format("%3d: %s\n", i + 1, commandHistory.get(i)), "output");
        }
        appendToOutput("\n", "default");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ShellUI shell = new ShellUI();
            shell.setVisible(true);
        });
    }
}
