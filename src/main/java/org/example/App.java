package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        TextArea chatArea = new TextArea();
        TextField input = new TextField();
        Button sendBtn = new Button("Ask");

        sendBtn.setOnAction(e -> {
            String question = input.getText();
            chatArea.appendText("You: " + question + "\n");

            // Later → call Python RAG backend
            chatArea.appendText("STEVE: (answer here)\n");
            input.clear();
        });

        VBox root = new VBox(10, chatArea, input, sendBtn);
        Scene scene = new Scene(root, 400, 400);

        stage.setScene(scene);
        stage.setTitle("STEVE – AI Learning Assistant");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
