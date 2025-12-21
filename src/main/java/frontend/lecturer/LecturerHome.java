package frontend.lecturer;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LecturerHome {


    private String userName;


    public LecturerHome(String userName) {
        this.userName = userName;
    }


    public void show(Stage stage) {
        //Main container
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));


        //Header with logo and title
        HBox header  = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10,  20, 20, 20));


        //STEVE Image
        Image avatar = new Image(getClass().getResource("/images/STEVE.png").toExternalForm());
        ImageView ImgSteve = new ImageView(avatar);
        ImgSteve.setFitWidth(200);
        ImgSteve.setPreserveRatio(true);
        header.getChildren().add(ImgSteve);


        //WelcomePage
        VBox WelBox = new VBox(30);
        WelBox.setAlignment(Pos.CENTER_LEFT);
        Label welcomeLabel = new Label("S.T.E.V.E: ");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));


        Label messageLabel = new Label("Hello there " + userName + "! Click on a module and lets get straight to teaching!");
        messageLabel.setFont(Font.font("Arial", 16));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);


        WelBox.getChildren().addAll(welcomeLabel, messageLabel);
        header.getChildren().add(WelBox);


        root.setTop(header);


        //6 Module Buttons
        VBox ModVBox = new VBox(30);
        ModVBox.setAlignment(Pos.CENTER);
        ModVBox.setPadding(new Insets(40));


        //Modules 1-3
        HBox row1 = new HBox(30);
        row1.setAlignment(Pos.CENTER);
        for (int i = 1; i <= 3; i++) {
            row1.getChildren().add(createModuleButton("Module " + i));
        }


        //Modules 4-6
        HBox row2 = new HBox(30);
        row2.setAlignment(Pos.CENTER);
        for (int i = 4; i <= 6; i++) {
            row2.getChildren().add(createModuleButton("Module " + i));
        }


        ModVBox.getChildren().addAll(row1, row2);
        root.setCenter(ModVBox);


        //Scene
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Home Page-Student");
        stage.show();
    }


    private Button createModuleButton(String moduleName) {
        Button button = new Button(moduleName);
        button.setPrefSize(300, 100);
        button.setOnAction(e -> {System.out.println(moduleName + " selected!");
            UploadDocument uploadPage = new UploadDocument(moduleName);
            Stage currentStage = (Stage) button.getScene().getWindow();
            uploadPage.show(currentStage);});
        return button;
    }
}
