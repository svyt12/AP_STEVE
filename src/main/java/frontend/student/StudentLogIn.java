package frontend.student;

import frontend.lecturer.LecturerLogIn;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class StudentLogIn extends Application {

    private PasswordField passwordField;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private void showLoginScreen() {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Image Taylors = new Image(getClass().getResource("/images/TaylorUniLogo.png").toExternalForm());
        ImageView TaylorsLogo = new ImageView(Taylors);
        TaylorsLogo.setFitWidth(200);
        TaylorsLogo.setPreserveRatio(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Image myTimes = new Image(getClass().getResource("/images/MyTIMES.png").toExternalForm());
        ImageView myTimesLogo = new ImageView(myTimes);
        myTimesLogo.setFitWidth(200);
        myTimesLogo.setPreserveRatio(true);

        topBar.getChildren().addAll(TaylorsLogo, spacer, myTimesLogo);
        root.setTop(topBar);

        // ===== CENTER CONTENT =====
        VBox loginContent = new VBox(15);
        loginContent.setAlignment(Pos.CENTER);
        loginContent.setPadding(new Insets(30, 40, 30, 40));
        loginContent.setMaxWidth(400);

        Image avatar = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView ImgSteve = new ImageView(avatar);
        ImgSteve.setFitWidth(150);
        ImgSteve.setPreserveRatio(true);

        Label Greeting = new Label("Hello!");
        Greeting.setFont(Font.font(14));
        Greeting.setTextAlignment(TextAlignment.CENTER);

        Label LoginGuide = new Label("Please login with your MyTIMeS Account.");
        LoginGuide.setFont(Font.font(14));
        LoginGuide.setWrapText(true);
        LoginGuide.setTextAlignment(TextAlignment.CENTER);

        TextField SIDField = new TextField();
        SIDField.setPromptText("Student ID");
        SIDField.setMaxWidth(200);
        SIDField.setPrefHeight(35);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);
        passwordField.setPrefHeight(35);

        Button loginbutton = new Button("Login");
        loginbutton.setMaxWidth(100);
        loginbutton.setPrefHeight(35);
        loginbutton.setOnAction(e -> handleLogin());

        Hyperlink switchToLecturerLink = new Hyperlink("If you're a lecturer, click here.");
        switchToLecturerLink.setStyle("-fx-font-size: 12;");
        switchToLecturerLink.setOnAction(e -> {
            LecturerLogIn lecturerApp = new LecturerLogIn();
            lecturerApp.start(primaryStage);
        });

        loginContent.getChildren().addAll(
                ImgSteve,
                Greeting,
                LoginGuide,
                SIDField,
                passwordField,
                loginbutton,
                switchToLecturerLink
        );

        StackPane centerWrapper = new StackPane(loginContent);
        centerWrapper.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true);   // ✅ CRITICAL
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void handleLogin() {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
