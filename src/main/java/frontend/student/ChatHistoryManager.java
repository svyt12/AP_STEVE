package frontend.student;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryManager {

    private static final String FILE_PATH = "chat_history.json"; // saved in app working directory
    private Map<String, List<String>> history;
    private final ObjectMapper mapper;

    public ChatHistoryManager() {
        mapper = new ObjectMapper();
        loadHistory();
    }

    private void loadHistory() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                // Read JSON as Map<String, List<String>>
                history = mapper.readValue(file, HashMap.class);
            } else {
                history = new HashMap<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            history = new HashMap<>();
        }
    }

    public void saveHistory() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), history);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Add or overwrite a chat session **/
    public void addChat(String query, List<String> messages) {
        history.put(query, messages);
        saveHistory();
    }

    /** Get full conversation by query **/
    public List<String> getChat(String query) {
        return history.get(query);
    }

    /** Get all chat keys (used to populate ListView) **/
    public Map<String, List<String>> getAllChats() {
        return history;
    }
}
