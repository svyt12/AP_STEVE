package frontend;

import javafx.application.Application;
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
import frontend.services.ChatService;
import javafx.scene.layout.HBox;

public class ChatBot extends Application {
    private ChatService chatService;
    private ListView<String> chatHistoryList;
    private Label steveMessage;
    private TextArea chatArea;

    @Override
    public void start(Stage stage) {
        chatService = new ChatService();

        // BACK button and topbar
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBackClicked());

        Button uploadButton = new Button("Upload Documents");
        uploadButton.setOnAction(e -> openUploadWindow());

        // Module name label
        Label moduleLabel = new Label("S.T.E.V.E Chat Assistant");
        moduleLabel.setFont(Font.font(16));
        moduleLabel.setTextAlignment(TextAlignment.CENTER);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(backButton);
        topBar.setCenter(moduleLabel);
        topBar.setRight(uploadButton);
        BorderPane.setAlignment(moduleLabel, Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");

        // Chat history and search
        TextField searchField = new TextField();
        searchField.setPromptText("Search chat history...");
        searchField.setMaxWidth(200);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> onSearchClicked(searchField.getText()));

        // Chat History List
        chatHistoryList = new ListView<>();
        chatHistoryList.getItems().addAll(
                "Welcome to S.T.E.V.E!",
                "Ask me questions about your uploaded documents"
        );

        // History panel
        VBox chatHistoryBox = new VBox(10);
        chatHistoryBox.setPadding(new Insets(10));
        Label historyLabel = new Label("Chat History");
        historyLabel.setFont(Font.font(16));

        VBox searchBox = new VBox(5, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        chatHistoryBox.getChildren().addAll(historyLabel, chatHistoryList, searchBox);

        // STEVE Chat Interface
        Image avatarImage = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(150);
        avatar.setPreserveRatio(true);

        steveMessage = new Label("S.T.E.V.E: How may I assist you today?");
        steveMessage.setFont(Font.font(16));
        steveMessage.setTextAlignment(TextAlignment.CENTER);
        steveMessage.setWrapText(true);
        steveMessage.setMaxWidth(500);

        // Chat display area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(200);
        chatArea.setPrefWidth(500);
        chatArea.setStyle("-fx-control-inner-background: #f5f5f5;");

        // User input
        TextField userInputField = new TextField();
        userInputField.setPromptText("Ask a question about your documents...");
        userInputField.setPrefWidth(400);

        Button askButton = new Button("Ask");
        askButton.setOnAction(e -> onAskClicked(userInputField));

        // Chat controls
        VBox inputBox = new VBox(10);
        HBox inputControls = new HBox(10, userInputField, askButton);
        inputControls.setAlignment(Pos.CENTER);
        inputBox.getChildren().addAll(inputControls);
        inputBox.setAlignment(Pos.CENTER);

        // Main chat box
        VBox chatBox = new VBox(20);
        chatBox.setPadding(new Insets(20));
        chatBox.setAlignment(Pos.CENTER);
        chatBox.getChildren().addAll(avatar, steveMessage, chatArea, inputBox);

        // Root layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(chatHistoryBox);
        root.setCenter(chatBox);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("S.T.E.V.E Chat - RAG Assistant");
        stage.setScene(scene);
        stage.show();
    }

    private void onBackClicked() {
        System.out.println("Back to main menu");
        // Add navigation logic here
    }

    private void openUploadWindow() {
        try {
            UploadDocument uploadWindow = new UploadDocument();
            uploadWindow.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSearchClicked(String query) {
        if (!query.isEmpty()) {
            // Filter chat history
            chatHistoryList.getItems().add("Search: " + query);
            System.out.println("Searching for: " + query);
        }
    }

    private void onAskClicked(TextField userInputField) {
        String question = userInputField.getText().trim();
        if (question.isEmpty()) return;

        // Add to chat history
        chatHistoryList.getItems().add("You: " + question);

        // Update chat display
        chatArea.appendText("You: " + question + "\n");
        steveMessage.setText("S.T.E.V.E: Thinking...");
        userInputField.clear();

        // Handle quiz trigger
        if (question.toLowerCase().contains("quiz")) {
            chatArea.appendText("S.T.E.V.E: Generating quiz based on your documents...\n");
            steveMessage.setText("S.T.E.V.E: Quiz generated! Check the chat area.");
            // Add quiz logic here
            return;
        }

        // Send question to backend in background thread
        new Thread(() -> {
            try {
                String answer = chatService.askQuestion(question);

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    chatArea.appendText("S.T.E.V.E: " + answer + "\n\n");
                    steveMessage.setText("S.T.E.V.E: Ready for your next question!");
                    chatHistoryList.getItems().add("STEVE: " +
                            (answer.length() > 50 ? answer.substring(0, 50) + "..." : answer));
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    chatArea.appendText("S.T.E.V.E: Sorry, I encountered an error: "
                            + e.getMessage() + "\n");
                    steveMessage.setText("S.T.E.V.E: Error processing request");
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}