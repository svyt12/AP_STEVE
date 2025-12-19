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


    //Getter
    public static Stage getPrimaryStage() {
        return primaryStage;
    }


    private void showLoginScreen() {


        //Main Container
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));


        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);


        //Taylor's UNI Image
        Image Taylors = new Image(getClass().getResource("/images/Logo_of_Taylor's_University.png").toExternalForm());
        ImageView TaylorsLogo = new ImageView(Taylors);
        TaylorsLogo.setFitWidth(300);
        TaylorsLogo.setPreserveRatio(true);
        topBar.getChildren().add(TaylorsLogo);


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().add(spacer);


        //MyTIMeS Image
        Image myTimes = new Image(getClass().getResource("/images/MyTIMeS logo.png").toExternalForm());
        ImageView myTimesLogo = new ImageView(myTimes);
        myTimesLogo.setFitWidth(300);
        myTimesLogo.setPreserveRatio(true);
        topBar.getChildren().add(myTimesLogo);


        root.setTop(topBar);


        //Login Stuff
        VBox loginContent = new VBox(15);
        loginContent.setAlignment(Pos.CENTER);
        loginContent.setPadding(new Insets(40));
        loginContent.setMaxWidth(400);


        //STEVE Image
        Image avatar = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView ImgSteve = new ImageView(avatar);
        ImgSteve.setFitWidth(200);
        ImgSteve.setPreserveRatio(true);
        loginContent.getChildren().add(ImgSteve);


        //Labeling
        Label Greeting = new Label("Hello Lecturer!");
        Greeting.setFont(Font.font(16));
        Greeting.setPadding(new Insets(0,0,10,0));
        Label LoginGuide = new Label("Please login with your MyTIMeS Account. ");
        LoginGuide.setFont(Font.font(16));
        LoginGuide.setPadding(new Insets(0,0,20,0));


        //StudentID Box
        SIDField = new TextField();
        SIDField.setPromptText("Staff ID");
        SIDField.setMaxWidth(200);
        SIDField.setPadding(new Insets(10));


        //PasswordBox
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);
        passwordField.setPadding(new Insets(10));


        //Login Button
        Button loginbutton = new Button("Login");
        loginbutton.setMaxWidth(100);
        loginbutton.setOnAction(e -> handleLogin());

        //Hyperlink for Staff to Student Page
        Hyperlink switchToStudentLink = new Hyperlink("If you're a student, click here.");
        switchToStudentLink.setStyle("-fx-font-size: 20;");
        switchToStudentLink.setOnAction(e -> {
            StudentLogIn studentApp = new StudentLogIn();
            studentApp.start(primaryStage);
        });

        //Main Components
        loginContent.getChildren().addAll(Greeting, LoginGuide, SIDField, passwordField);

        VBox buttonwithLinkContainer = new VBox(15);
        buttonwithLinkContainer.setAlignment(Pos.CENTER);
        buttonwithLinkContainer.setPadding(new Insets(20,0,0,0));
        loginContent.getChildren().addAll(loginbutton, switchToStudentLink);

        loginContent.getChildren().addAll(buttonwithLinkContainer);

        StackPane centreContainer = new StackPane(loginContent);
        centreContainer.setAlignment(Pos.CENTER);



        // Scene and Stage
        root.setCenter(centreContainer);


        Scene scene = new Scene(root, 1000, 800);
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
