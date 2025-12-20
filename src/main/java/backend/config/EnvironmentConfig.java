package backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

    private static Dotenv dotenv;

    // Static initializer runs when class is loaded
    static {
        try {
            dotenv = Dotenv.configure()
                    .directory(System.getProperty("user.dir"))
                    .ignoreIfMissing()
                    .load();

            System.out.println("🌍 EnvironmentConfig loaded .env file");

            // Log what was loaded (for debugging, but hide sensitive values)
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();

                // Mask sensitive keys
                if (key.toLowerCase().contains("key") ||
                        key.toLowerCase().contains("secret") ||
                        key.toLowerCase().contains("password") ||
                        key.toLowerCase().contains("api")) {
                    System.out.println("   " + key + " = [MASKED]");
                } else {
                    System.out.println("   " + key + " = " + value);
                }

                // Load into System properties
                System.setProperty(key, value);
            });

        } catch (Exception e) {
            System.out.println("⚠️ Could not load .env file: " + e.getMessage());
            dotenv = null;
        }
    }

    public static String get(String key) {
        if (dotenv != null) {
            String value = dotenv.get(key);
            if (value != null) {
                return value;
            }
        }

        // Fall back to system environment
        String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }

        // Fall back to system property
        return System.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public static boolean has(String key) {
        if (dotenv != null && dotenv.get(key) != null) {
            return true;
        }
        return System.getenv(key) != null || System.getProperty(key) != null;
    }
}