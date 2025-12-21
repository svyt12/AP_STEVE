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

import java.util.List;
import java.util.stream.Collectors;

public class ChatBot extends Application {

    private Stage primaryStage;
    private String moduleName;
    private String username;
    private TextArea chatDisplay;
    private ListView<String> chatHistoryList;
    private ChatService chatService;
    private ChatHistoryManager chatHistoryManager;

    public ChatBot() {
        this.chatService = new ChatService();
        this.chatHistoryManager = new ChatHistoryManager(); // JSON-based persistent storage
    }

    public ChatBot(String moduleName, String username) {
        this.moduleName = moduleName;
        this.username = username;
        this.chatService = new ChatService();
        this.chatHistoryManager = new ChatHistoryManager();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // --- Top bar ---
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBackClicked(stage));

        Label moduleLabel = new Label(moduleName != null ? moduleName : "General Chat");
        moduleLabel.setFont(Font.font(14));
        moduleLabel.setTextAlignment(TextAlignment.CENTER);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(backButton);
        topBar.setCenter(moduleLabel);
        BorderPane.setAlignment(moduleLabel, Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");

        // --- Chat history section ---
        TextField searchField = new TextField();
        searchField.setPromptText("Search for a chat...");
        searchField.setMaxWidth(200);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> onSearchClicked(searchField.getText()));

        VBox searchBoxContainer = new VBox(5, searchField, searchButton);
        searchBoxContainer.setAlignment(Pos.TOP_CENTER);

        chatHistoryList = new ListView<>();
        refreshChatHistoryList();

        chatHistoryList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        onChatHistorySelected(newVal);
                    }
                }
        );

        VBox chatHistoryBox = new VBox(10);
        chatHistoryBox.setPadding(new Insets(10));
        Label historyLabel = new Label("Chat History");
        historyLabel.setFont(Font.font(16));
        chatHistoryBox.getChildren().addAll(historyLabel, chatHistoryList, searchBoxContainer);
        chatHistoryBox.setPrefWidth(250);

        // --- Main chat section ---
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
        userInputField.setPromptText("Ask a question");
        userInputField.setMaxWidth(400);

        Button askButton = new Button("Ask S.T.E.V.E");
        askButton.setStyle("-fx-font-size: 14; -fx-padding: 8 20;");
        askButton.setOnAction(e -> {
            String question = userInputField.getText().trim();
            if (!question.isEmpty()) {
                onAskClicked(question);
                userInputField.clear();
            }
        });

        Button clearButton = new Button("Clear Chat");
        clearButton.setOnAction(e -> chatDisplay.clear());

        VBox inputPanel = new VBox(10, userInputField, askButton, clearButton);
        inputPanel.setAlignment(Pos.CENTER);

        VBox chatBox = new VBox(15, avatar, steveMessage, chatDisplay, inputPanel);
        chatBox.setPadding(new Insets(20));
        chatBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(chatHistoryBox);
        root.setCenter(chatBox);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("S.T.E.V.E Chat - " + moduleName);
        stage.setScene(scene);
        stage.show();
    }

    private void refreshChatHistoryList() {
        chatHistoryList.getItems().clear();
        chatHistoryList.getItems().addAll(chatHistoryManager.getAllChats().keySet());
    }

    private void onBackClicked(Stage primaryStage) {
        StudentHome homePage = new StudentHome(username);
        homePage.show(primaryStage);
    }

    private void onSearchClicked(String query) {
        if (query == null || query.trim().isEmpty()) return;

        String searchTerm = query.trim().toLowerCase();
        chatHistoryList.getItems().clear();

        List<String> filtered = chatHistoryManager.getAllChats().keySet().stream()
                .filter(q -> q.toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            chatHistoryList.getItems().add("No results found for: " + query);
        } else {
            chatHistoryList.getItems().addAll(filtered);
        }
    }

    private void onChatHistorySelected(String selectedHistory) {
        chatDisplay.clear();
        List<String> conversation = chatHistoryManager.getChat(selectedHistory);
        if (conversation != null) {
            conversation.forEach(line -> chatDisplay.appendText(line + "\n"));
        }
    }

    private void onAskClicked(String question) {
        appendToChat("User: " + question);

        appendToChat("S.T.E.V.E is thinking...\n");

        new Thread(() -> {
            try {
                ChatService.ChatResponse response = chatService.askQuestion(question);

                Platform.runLater(() -> {
                    // Remove "thinking" line
                    String currentText = chatDisplay.getText();
                    if (currentText.endsWith("S.T.E.V.E is thinking...\n")) {
                        chatDisplay.setText(currentText.substring(0,
                                currentText.length() - "S.T.E.V.E is thinking...\n".length()));
                    }

                    String answer = response.getAnswer();
                    appendToChat("S.T.E.V.E: " + answer);

                    // Save conversation to persistent history
                    List<String> conversation = chatHistoryManager.getChat(question);
                    if (conversation == null) conversation = new java.util.ArrayList<>();
                    conversation.add("User: " + question);
                    conversation.add("S.T.E.V.E: " + answer);
                    chatHistoryManager.addChat(question, conversation);

                    // Refresh ListView if new
                    if (!chatHistoryList.getItems().contains(question)) {
                        chatHistoryList.getItems().add(0, question);
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
            chatDisplay.setScrollTop(Double.MAX_VALUE);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
