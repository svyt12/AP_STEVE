package org.example.ap_steve;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppTest {
    public static void main(String[] args) {
        System.out.println("Testing dependencies...");

        // Test Jackson
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("Jackson loaded: " + mapper);

        // Test HttpClient (Java 11+)
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        System.out.println("HttpClient loaded: " + client);

        System.out.println("All good!");
    }
}