package frontend.lecturer;

import frontend.student.StudentLogIn;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LecturerLogIn extends Application {

    private TextField SIDField;
    private PasswordField passwordField;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScreen();
    }

    // Getter
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private void showLoginScreen() {
        // Main Container
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top Bar with Logos
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Taylor's UNI Image
        Image taylors = new Image(getClass().getResource("/images/TaylorUniLogo.png").toExternalForm());
        ImageView taylorsLogo = new ImageView(taylors);
        taylorsLogo.setFitWidth(250);
        taylorsLogo.setPreserveRatio(true);
        topBar.getChildren().add(taylorsLogo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().add(spacer);

        // MyTIMeS Image
        Image myTimes = new Image(getClass().getResource("/images/myTIMES.png").toExternalForm());
        ImageView myTimesLogo = new ImageView(myTimes);
        myTimesLogo.setFitWidth(250);
        myTimesLogo.setPreserveRatio(true);
        topBar.getChildren().add(myTimesLogo);

        root.setTop(topBar);

        // Login Stuff
        VBox loginContent = new VBox(15);
        loginContent.setAlignment(Pos.CENTER);
        loginContent.setPadding(new Insets(20));
        loginContent.setMaxWidth(350);

        // STEVE Image
        Image avatar = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView ImgSteve = new ImageView(avatar);
        ImgSteve.setFitWidth(150);
        ImgSteve.setPreserveRatio(true);

        // Labels
        Label Greeting = new Label("Hello Lecturer!");
        Greeting.setFont(Font.font(16));
        Label LoginGuide = new Label("Please login with your MyTIMeS Account.");
        LoginGuide.setFont(Font.font(14));
        LoginGuide.setWrapText(true);
        LoginGuide.setMaxWidth(300);

        // Text fields
        SIDField = new TextField();
        SIDField.setPromptText("Staff ID");
        SIDField.setMaxWidth(250);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        // Buttons
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(100);
        loginButton.setOnAction(e -> handleLogin());

        Hyperlink switchToStudentLink = new Hyperlink("If you're a student, click here.");
        switchToStudentLink.setStyle("-fx-font-size: 12;");
        // FIX: Added back the action handler
        switchToStudentLink.setOnAction(e -> {
            StudentLogIn studentApp = new StudentLogIn();
            studentApp.start(primaryStage);
        });

        loginContent.getChildren().addAll(ImgSteve, Greeting, LoginGuide, SIDField, passwordField, loginButton, switchToStudentLink);

        StackPane centreContainer = new StackPane(loginContent);
        centreContainer.setAlignment(Pos.CENTER);

        root.setCenter(centreContainer);

        // Scene and Stage
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin() {
        String username = SIDField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter both username and password.");
            alert.showAndWait();
        }
        else {
            LecturerHome homePageLecturer = new LecturerHome(username);
            homePageLecturer.show(getPrimaryStage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}