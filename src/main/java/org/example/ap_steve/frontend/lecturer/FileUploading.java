package org.example.ap_steve.frontend.lecturer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class FileUploading extends Application {
    private String moduleName;

    public FileUploading(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void start(Stage stage) {


        //TOP BAR
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> onBackClicked(stage));


        Label moduleLabel = new Label("(Module Name)");
        moduleLabel.setFont(Font.font(14));
        moduleLabel.setTextAlignment(TextAlignment.CENTER);


        BorderPane topBar = new BorderPane();
        topBar.setLeft(backButton);
        topBar.setCenter(moduleLabel);
        BorderPane.setAlignment(moduleLabel, Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");


        //STEVE
        Image avatarImage = new Image(
                getClass().getResource("/images/STEVE.png").toExternalForm()
        );


        ImageView avatar = new ImageView(avatarImage);
        avatar.setFitWidth(200);
        avatar.setPreserveRatio(true);


        //UPLOAD DOCUMENT
        Label message = new Label("S.T.E.V.E: Upload your documents here to supply me with knowledge!");
        message.setWrapText(true);
        message.setFont(Font.font(14));
        message.setTextAlignment(TextAlignment.CENTER);
        message.setMaxWidth(450);


        //the button
        Button uploadButton = new Button("Upload");
        uploadButton.setPrefSize(100, 100);
        uploadButton.setOnAction(e -> onUploadClicked());
        uploadButton.setStyle("""
               -fx-background-color: white;
               -fx-border-color: lightgray;
               -fx-border-radius: 8;
               -fx-background-radius: 8;
               """);


        //center
        VBox centerBox = new VBox(15);
        centerBox.getChildren().addAll(avatar, message, uploadButton);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(40));


        //roots
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centerBox);


        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("S.T.E.V.E");
        stage.setScene(scene);
        stage.show();
    }


    //combination
    private void onBackClicked(Stage currentStage) {
        //when back is clicked type shiot
        LecturerHome homePage = new LecturerHome(getUserAgentStylesheet());
        homePage.show(currentStage);
    }


    private void onUploadClicked() {
        // when upload files is clicked TYPE SHIT
    }

}


