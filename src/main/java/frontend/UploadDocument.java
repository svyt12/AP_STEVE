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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import frontend.services.FileUploadService;

import java.io.File;

public class UploadDocument extends Application {
    private FileUploadService uploadService;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        uploadService = new FileUploadService();

        // TOP BAR
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBackClicked(stage));

        Label moduleLabel = new Label("Document Upload");
        moduleLabel.setFont(Font.font(14));
        moduleLabel.setTextAlignment(TextAlignment.CENTER);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(backButton);
        topBar.setCenter(moduleLabel);
        BorderPane.setAlignment(moduleLabel, Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");

        // STEVE
        Image avatarImage = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(200);
        avatar.setPreserveRatio(true);

        // UPLOAD DOCUMENT
        Label message = new Label("S.T.E.V.E: Upload your documents here to supply me with knowledge!");
        message.setWrapText(true);
        message.setFont(Font.font(14));
        message.setTextAlignment(TextAlignment.CENTER);
        message.setMaxWidth(450);

        // Status label
        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(400);

        // Upload button
        Button uploadButton = new Button("Upload PDF");
        uploadButton.setPrefSize(150, 50);
        uploadButton.setOnAction(e -> onUploadClicked(stage));
        uploadButton.setStyle("""
               -fx-background-color: #4CAF50;
               -fx-text-fill: white;
               -fx-font-size: 14px;
               -fx-border-radius: 8;
               -fx-background-radius: 8;
               """);

        // Center layout
        VBox centerBox = new VBox(20);
        centerBox.getChildren().addAll(avatar, message, uploadButton, statusLabel);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(40));

        // Root layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("S.T.E.V.E - Document Upload");
        stage.setScene(scene);
        stage.show();
    }

    private void onBackClicked(Stage stage) {
        // Go back to chat interface
        try {
            ChatBot chatBot = new ChatBot();
            chatBot.start(new Stage());
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onUploadClicked(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF Document");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            // Show progress
            statusLabel.setText("Uploading " + selectedFile.getName() + "...");
            statusLabel.setStyle("-fx-text-fill: blue;");

            // Upload in background thread
            new Thread(() -> {
                try {
                    String result = uploadService.uploadDocument(selectedFile);

                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText(result);
                        statusLabel.setStyle("-fx-text-fill: green;");

                        // Show success dialog
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Upload Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Document uploaded and processed successfully!");
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Error: " + e.getMessage());
                        statusLabel.setStyle("-fx-text-fill: red;");

                        // Show error dialog
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Upload Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to upload document: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}