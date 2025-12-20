package frontend.student;

import frontend.services.ChatService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class ChatBot extends Application {
    private Stage primaryStage;
    private String moduleName;
    private String username;
    private TextArea chatDisplay;
    private ListView<String> chatHistoryList;
    private ChatService chatService;

    public ChatBot() {
        this.chatService = new ChatService();
    }

    // Constructor
    public ChatBot(String moduleName, String username) {
        this.moduleName = moduleName;
        this.username = username;
        this.chatService = new ChatService();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // BACK button and topbar
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBackClicked(stage));

        // Module name label
        Label moduleLabel = new Label(moduleName != null ? moduleName : "General Chat");
        moduleLabel.setFont(Font.font(14));
        moduleLabel.setTextAlignment(TextAlignment.CENTER);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(backButton);
        topBar.setCenter(moduleLabel);
        BorderPane.setAlignment(moduleLabel, Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");

        // CHAT HISTORY SECTION

        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search for a chat...");
        searchField.setMaxWidth(200);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> onSearchClicked(searchField.getText()));

        // VBox for search bar and button
        VBox searchBoxContainer = new VBox(5);
        searchBoxContainer.setAlignment(Pos.TOP_CENTER);
        searchBoxContainer.getChildren().addAll(searchField, searchButton);

        // Chat History List
        chatHistoryList = new ListView<>();
        // Initialize with some sample history
        chatHistoryList.getItems().addAll(
                "RAG Discussion - Dec 20"
        );

        // Set selection handler
        chatHistoryList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        onChatHistorySelected(newValue);
                    }
                }
        );

        VBox chatHistoryBox = new VBox(10);
        chatHistoryBox.setPadding(new Insets(10));
        Label historyLabel = new Label("Chat History");
        historyLabel.setFont(Font.font(16));
        chatHistoryBox.getChildren().addAll(historyLabel, chatHistoryList, searchBoxContainer);

        chatHistoryBox.setPrefWidth(250);

        // MAIN CHAT SECTION

        // Chat display area
        chatDisplay = new TextArea();
        chatDisplay.setEditable(false);
        chatDisplay.setWrapText(true);
        chatDisplay.setPrefHeight(300);
        chatDisplay.setPromptText("Conversation will appear here...");
        chatDisplay.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12;");

        Image avatarImage = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(150);
        avatar.setPreserveRatio(true);

        Label steveMessage = new Label("S.T.E.V.E: How may I assist you today?");
        steveMessage.setFont(Font.font(16));
        steveMessage.setTextAlignment(TextAlignment.CENTER);

        TextField userInputField = new TextField();
        userInputField.setPromptText("Ask a question or type 'quiz about [topic]'...");
        userInputField.setMaxWidth(400);

        // Ask button
        Button askButton = new Button("Ask S.T.E.V.E");
        askButton.setStyle("-fx-font-size: 14; -fx-padding: 8 20;");
        askButton.setOnAction(e -> {
            String question = userInputField.getText().trim();
            if (!question.isEmpty()) {
                onAskClicked(question);
                userInputField.clear();
            }
        });

        // Clear button
        Button clearButton = new Button("Clear Chat");
        clearButton.setOnAction(e -> chatDisplay.clear());

        VBox inputPanel = new VBox(10);
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.getChildren().addAll(userInputField, askButton, clearButton);

        VBox chatBox = new VBox(15);
        chatBox.setPadding(new Insets(20));
        chatBox.setAlignment(Pos.CENTER);
        chatBox.getChildren().addAll(avatar, steveMessage, chatDisplay, inputPanel);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(chatHistoryBox);
        root.setCenter(chatBox);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("S.T.E.V.E Chat - " + moduleName);
        stage.setScene(scene);
        stage.show();
    }

    private void onBackClicked(Stage primaryStage) {
        StudentHome homePage = new StudentHome(username);
        homePage.show(primaryStage);
    }

    private void onSearchClicked(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String searchTerm = query.trim().toLowerCase();
        chatHistoryList.getItems().clear();


        java.util.List<String> allHistory = java.util.Arrays.asList(

        );

        for (String history : allHistory) {
            if (history.toLowerCase().contains(searchTerm)) {
                chatHistoryList.getItems().add(history);
            }
        }

        if (chatHistoryList.getItems().isEmpty()) {
            chatHistoryList.getItems().add("No results found for: " + query);
        }
    }

    private void onChatHistorySelected(String selectedHistory) {
        // Simulate loading chat history
        appendToChat("\nLoading chat: " + selectedHistory + "\n");

    }

    private void onAskClicked(String question) {
        // Add user question to chat
        appendToChat("Question: " + question);

        // Add to chat history if it's a new conversation starter
        if (!chatHistoryList.getItems().contains("New Chat - " +
                java.time.LocalDate.now().toString())) {
            chatHistoryList.getItems().add(0, "New Chat - " +
                    java.time.LocalDate.now().toString());
        }

        appendToChat("S.T.E.V.E is thinking...\n");

        // Process in background thread (keep UI responsive)
        new Thread(() -> {
            try {
                ChatService.ChatResponse response = chatService.askQuestion(question);

                Platform.runLater(() -> {
                    // Clear "thinking" message
                    String currentText = chatDisplay.getText();
                    if (currentText.endsWith("S.T.E.V.E is thinking...\n")) {
                        chatDisplay.setText(currentText.substring(0,
                                currentText.length() - "S.T.E.V.E is thinking...\n".length()));
                    }

                    String answer = response.getAnswer();
                    appendToChat("S.T.E.V.E: " + answer);

                    if (question.toLowerCase().contains("quiz")) {
                        appendToChat("\nQuiz Mode Activated!");
                        appendToChat("   Topic: " + extractTopic(question));
                        appendToChat("   [Quiz would be displayed here in a real implementation]\n");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    appendToChat("❌ Error: " + e.getMessage());
                    System.err.println("Chat error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void appendToChat(String text) {
        Platform.runLater(() -> {
            chatDisplay.appendText(text + "\n");
            // Auto-scroll to bottom
            chatDisplay.setScrollTop(Double.MAX_VALUE);
        });
    }

    private String extractTopic(String question) {
        // Extract topic from quiz question
        String q = question.toLowerCase();
        if (q.contains("about")) {
            return q.substring(q.indexOf("about") + 5).trim();
        } else if (q.contains("on")) {
            return q.substring(q.indexOf("on") + 2).trim();
        }
        return "General Knowledge";
    }

    public static void main(String[] args) {
        launch(args);
    }
}