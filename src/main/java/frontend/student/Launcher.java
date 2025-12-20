package frontend.student;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class Launcher {
    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        System.out.println("🚀 Starting S.T.E.V.E Application...");

        // Start Spring Boot in a separate thread
        Thread springThread = new Thread(() -> {
            System.out.println("🌐 Starting Spring Boot backend...");
            try {
                springContext = SpringApplication.run(backend.Application.class, args);
                System.out.println("✅ Spring Boot backend started on http://localhost:8080");
            } catch (Exception e) {
                System.err.println("❌ Failed to start Spring Boot: " + e.getMessage());
                e.printStackTrace();
            }
        });
        springThread.setDaemon(true);
        springThread.start();

        // Wait for Spring to start
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start JavaFX - ChatBot is in the same package, so no import needed
        System.out.println("🖥️ Starting JavaFX frontend...");
        Application.launch(ChatBot.class, args);
    }
}