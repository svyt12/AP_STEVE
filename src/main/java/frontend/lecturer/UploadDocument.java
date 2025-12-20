package frontend.lecturer;

import frontend.services.FileUploadService;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class UploadDocument {

    private final String moduleName;
    private Label statusLabel;
    private Button uploadButton;

    public UploadDocument(String moduleName) {
        this.moduleName = moduleName;
    }

    public void show(Stage stage) {

        // TOP BAR
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            LecturerHome home = new LecturerHome("Lecturer");
            home.show(stage);
        });

        Label moduleLabel = new Label(moduleName);
        moduleLabel.setFont(Font.font(14));
        moduleLabel.setTextAlignment(TextAlignment.CENTER);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(backButton);
        topBar.setCenter(moduleLabel);
        topBar.setPadding(new Insets(10));

        // Avatar
        ImageView avatar = new ImageView(
                new Image(getClass().getResource("/images/STEVE.png").toExternalForm())
        );
        avatar.setFitWidth(200);
        avatar.setPreserveRatio(true);

        Label message = new Label(
                "S.T.E.V.E: Upload your documents here to supply me with knowledge!"
        );
        message.setWrapText(true);
        message.setMaxWidth(450);

        statusLabel = new Label();

        uploadButton = new Button("Upload");
        uploadButton.setOnAction(e -> onUploadClicked(stage));

        VBox center = new VBox(15, avatar, message, uploadButton, statusLabel);
        center.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Upload - " + moduleName);
        stage.show();
    }

    private void onUploadClicked(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        statusLabel.setText("Uploading...");
        uploadButton.setDisable(true);

        new Thread(() -> {
            try {
                FileUploadService service = new FileUploadService();
                service.uploadDocument(file);

                Platform.runLater(() -> {
                    statusLabel.setText("Upload successful");
                    uploadButton.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Upload failed");
                    uploadButton.setDisable(false);
                });
            }
        }).start();
    }
}
