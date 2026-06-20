import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main extends Application {
    private Stage window;
    private String currentUser = "";
    private final Path usersFile = Paths.get("users.txt");
    private final Path notesFile = Paths.get("notes.txt");
    private final ListView<String> notesList = new ListView<>();
    private final TextArea noteArea = new TextArea();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("Student Notes App");
        showLogin();
        window.show();
    }

    private void showLogin() {
        TextField username = new TextField();
        PasswordField password = new PasswordField();

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        Label message = new Label();

        loginBtn.setOnAction(e -> {
            if (login(username.getText(), password.getText())) {
                currentUser = username.getText();
                showNotesScreen();
            } else {
                message.setText("Invalid username or password.");
            }
        });

        registerBtn.setOnAction(e -> {
            if (register(username.getText(), password.getText())) {
                message.setText("Registration successful. You can login now.");
            } else {
                message.setText("User already exists or fields are empty.");
            }
        });

        VBox root = new VBox(10,
                new Label("Student Notes App"),
                new Label("Username:"), username,
                new Label("Password:"), password,
                loginBtn, registerBtn, message
        );
        root.setStyle("-fx-padding: 20;");
        window.setScene(new Scene(root, 350, 300));
    }

    private void showNotesScreen() {
        Button addBtn = new Button("Add Note");
        Button updateBtn = new Button("Update Note");
        Button deleteBtn = new Button("Delete Note");
        Button logoutBtn = new Button("Logout");

        noteArea.setPromptText("Write your note here...");
        loadNotes();

        notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                noteArea.setText(newVal);
            }
        });

        addBtn.setOnAction(e -> {
            if (!noteArea.getText().trim().isEmpty()) {
                notesList.getItems().add(noteArea.getText().trim());
                saveNotes();
                noteArea.clear();
            }
        });

        updateBtn.setOnAction(e -> {
            int index = notesList.getSelectionModel().getSelectedIndex();
            if (index >= 0 && !noteArea.getText().trim().isEmpty()) {
                notesList.getItems().set(index, noteArea.getText().trim());
                saveNotes();
                noteArea.clear();
            }
        });

        deleteBtn.setOnAction(e -> {
            int index = notesList.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                notesList.getItems().remove(index);
                saveNotes();
                noteArea.clear();
            }
        });

        logoutBtn.setOnAction(e -> {
            currentUser = "";
            notesList.getItems().clear();
            noteArea.clear();
            showLogin();
        });

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn, logoutBtn);
        VBox root = new VBox(10,
                new Label("Welcome, " + currentUser),
                notesList,
                noteArea,
                buttons
        );
        root.setStyle("-fx-padding: 20;");
        window.setScene(new Scene(root, 600, 500));
    }

    private boolean register(String username, String password) {
        if (username.isBlank() || password.isBlank()) return false;
        try {
            if (!Files.exists(usersFile)) Files.createFile(usersFile);
            List<String> users = Files.readAllLines(usersFile);
            for (String user : users) {
                if (user.split(",")[0].equals(username)) return false;
            }
            Files.writeString(usersFile, username + "," + password + System.lineSeparator(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean login(String username, String password) {
        try {
            if (!Files.exists(usersFile)) return false;
            List<String> users = Files.readAllLines(usersFile);
            for (String user : users) {
                String[] parts = user.split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private void loadNotes() {
        notesList.getItems().clear();
        try {
            if (!Files.exists(notesFile)) Files.createFile(notesFile);
            List<String> notes = Files.readAllLines(notesFile);
            for (String line : notes) {
                String[] parts = line.split("::", 2);
                if (parts.length == 2 && parts[0].equals(currentUser)) {
                    notesList.getItems().add(parts[1]);
                }
            }
        } catch (IOException ignored) {}
    }

    private void saveNotes() {
        try {
            List<String> allNotes = new ArrayList<>();
            if (Files.exists(notesFile)) {
                for (String line : Files.readAllLines(notesFile)) {
                    if (!line.startsWith(currentUser + "::")) {
                        allNotes.add(line);
                    }
                }
            }
            for (String note : notesList.getItems()) {
                allNotes.add(currentUser + "::" + note);
            }
            Files.write(notesFile, allNotes);
        } catch (IOException ignored) {}
    }
}
