package frontend;

import frontend.student.StudentHome;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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

    public ChatBot() {
        //empty constructor
    }

    //Constructor
    public ChatBot(String moduleName, String username){
        this.moduleName = moduleName;
        this.username = username;
    }

    @Override
    public void start(Stage stage) {


        // BACK button and topbar
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBackClicked(stage)); // Placeholder method for bskc


        // Module name label
        Label moduleLabel = new Label(moduleName);
        moduleLabel.setFont(Font.font(14));
        moduleLabel.setTextAlignment(TextAlignment.CENTER);


        BorderPane topBar = new BorderPane();
        topBar.setLeft(backButton);
        topBar.setCenter(moduleLabel);
        BorderPane.setAlignment(moduleLabel, Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");


        // Chat history and search components
        TextField searchField = new TextField();
        searchField.setPromptText("Search for a chat...");
        searchField.setMaxWidth(200);


        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> onSearchClicked(searchField.getText()));


        // VBox for search bar and button, aligned to the top
        VBox searchBoxContainer = new VBox(5);
        searchBoxContainer.setAlignment(Pos.TOP_CENTER);
        searchBoxContainer.getChildren().addAll(searchField, searchButton);


        // Chat History
        ListView<String> chatHistoryList = new ListView<>();
        chatHistoryList.getItems().addAll("History 1", "History 2", "History 3");


        // VBox for the Chat History section
        VBox chatHistoryBox = new VBox(10);
        chatHistoryBox.setPadding(new Insets(10));
        Label historyLabel = new Label("Chat History");
        historyLabel.setFont(Font.font(16));
        chatHistoryBox.getChildren().addAll(historyLabel, chatHistoryList, searchBoxContainer);


        // STEVE
        Image avatarImage = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(150);
        avatar.setPreserveRatio(true);


        Label steveMessage = new Label("S.T.E.V.E: How may I assist you today?");
        steveMessage.setFont(Font.font(16));
        steveMessage.setTextAlignment(TextAlignment.CENTER);


        TextField userInputField = new TextField();
        userInputField.setPromptText("Ask a question...");
        userInputField.setMaxWidth(300);


        Button askButton = new Button("Ask");
        askButton.setOnAction(e -> onAskClicked(userInputField.getText()));


        // VBox for STEVE "I AM STEVE"
        VBox chatBox = new VBox(15);
        chatBox.setPadding(new Insets(40));
        chatBox.setAlignment(Pos.CENTER);
        chatBox.getChildren().addAll(avatar, steveMessage, userInputField, askButton);


        // beetroot
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(chatHistoryBox);
        root.setCenter(chatBox);


        // Scene
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("S.T.E.V.E Chat");
        stage.setScene(scene);
        stage.show();
    }


    // Placeholder for Back
    private void onBackClicked(Stage primaryStage) {
        // Implement the back button functionality here
        StudentHome homePage = new StudentHome(username);
        homePage.show(primaryStage);
    }


    // Placeholder for Search
    private void onSearchClicked(String query) {
        // Handle the search functionality here
        System.out.println("Search query: " + query);
    }


    // Placeholder for Ask button action (counting or not counting quizes)
    private void onAskClicked(String question) {
        // If the question contains quiz= quiz
        if (question.toLowerCase().contains("quiz")) {
            System.out.println("Quiz triggered: Generate quiz prototype.");
            // quiz willy wonkay here
        } else {
            System.out.println("User question: " + question);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
