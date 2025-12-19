package backend.rag;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PgVectorStore implements VectorStore {

    private final DataSource dataSource;
    private static final String TABLE_NAME = "document_embeddings";
    private static final int EMBEDDING_DIMENSION = 1536; // Adjust based on your embedding model

    public PgVectorStore(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeDatabase();
        System.out.println("✅ PgVectorStore initialized");
    }

    private void initializeDatabase() {
        String createTableSQL = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                id VARCHAR(255) PRIMARY KEY,
                content TEXT NOT NULL,
                embedding VECTOR(%d),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """, TABLE_NAME, EMBEDDING_DIMENSION);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable pgvector extension
            stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");

            // Create table
            stmt.execute(createTableSQL);

            System.out.println("✅ Database table initialized: " + TABLE_NAME);

        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    @Override
    public void store(String id, String content, float[] embedding) {
        validateEmbeddingDimension(embedding);

        String sql = String.format("""
        INSERT INTO %s (id, content, embedding) 
        VALUES (?, ?, CAST(? AS vector))
        ON CONFLICT (id) 
        DO UPDATE SET content = EXCLUDED.content, embedding = EXCLUDED.embedding
        """, TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, content);
            pstmt.setString(3, convertArrayToString(embedding)); // Keep as string

            pstmt.executeUpdate();
            System.out.println("✅ Stored document with ID: " + id);

        } catch (SQLException e) {
            System.err.println("❌ Failed to store document: " + e.getMessage());
            throw new RuntimeException("Failed to store document: " + id, e);
        }
    }

    @Override
    public List<SearchResult> searchSimilar(float[] queryEmbedding, int topK) {
        validateEmbeddingDimension(queryEmbedding);

        String sql = String.format("""
        SELECT id, content, 
               (1 - (embedding <=> CAST(? AS vector))) as similarity
        FROM %s
        ORDER BY embedding <=> CAST(? AS vector)
        LIMIT ?
        """, TABLE_NAME);

        List<SearchResult> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String embeddingStr = convertArrayToString(queryEmbedding);
            pstmt.setString(1, embeddingStr);
            pstmt.setString(2, embeddingStr);
            pstmt.setInt(3, topK);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String content = rs.getString("content");
                    float similarity = rs.getFloat("similarity");

                    results.add(new SearchResult(id, content, similarity));
                }
            }

            System.out.println("🔍 Found " + results.size() + " similar documents (topK=" + topK + ")");

        } catch (SQLException e) {
            System.err.println("❌ Failed to search documents: " + e.getMessage());
            throw new RuntimeException("Failed to search documents", e);
        }

        return results;
    }

    @Override
    public Map<String, String> getContents() {
        String sql = String.format("""
            SELECT id, content 
            FROM %s 
            ORDER BY created_at
            """, TABLE_NAME);

        Map<String, String> contents = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String content = rs.getString("content");
                contents.put(id, content);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to get contents: " + e.getMessage());
            throw new RuntimeException("Failed to get contents", e);
        }

        return contents;
    }

    @Override
    public Map<String, float[]> getVectors() {
        String sql = String.format("""
            SELECT id, embedding::text 
            FROM %s 
            ORDER BY created_at
            """, TABLE_NAME);

        Map<String, float[]> vectors = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String vectorText = rs.getString("embedding");
                float[] vector = parseVectorString(vectorText);
                vectors.put(id, vector);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to get vectors: " + e.getMessage());
            throw new RuntimeException("Failed to get vectors", e);
        }

        return vectors;
    }

    @Override
    public void clear() {
        String sql = String.format("DELETE FROM %s", TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            int deleted = stmt.executeUpdate(sql);
            System.out.println("🧹 Cleared " + deleted + " documents from vector store");

        } catch (SQLException e) {
            System.err.println("❌ Failed to clear vector store: " + e.getMessage());
            throw new RuntimeException("Failed to clear vector store", e);
        }
    }

    // Helper method to convert float array to PostgreSQL vector string format
    private String convertArrayToString(float[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    // Helper method to parse PostgreSQL vector string back to float array
    private float[] parseVectorString(String vectorText) {
        // Remove brackets and split by commas
        String cleaned = vectorText.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(",");

        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }

        return vector;
    }

    // Validate embedding dimension matches expected dimension
    private void validateEmbeddingDimension(float[] embedding) {
        if (embedding.length != EMBEDDING_DIMENSION) {
            String error = String.format(
                    "Embedding dimension mismatch. Expected: %d, Got: %d",
                    EMBEDDING_DIMENSION, embedding.length
            );
            throw new IllegalArgumentException(error);
        }
    }

    // Optional utility methods

    public int getDocumentCount() {
        String sql = String.format("SELECT COUNT(*) as count FROM %s", TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to get document count: " + e.getMessage());
        }

        return 0;
    }

    public void createIndex() {
        String sql = String.format("""
            CREATE INDEX IF NOT EXISTS embedding_idx ON %s 
            USING ivfflat (embedding vector_cosine_ops)
            """, TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("✅ Created vector index for faster similarity search");

        } catch (SQLException e) {
            System.err.println("❌ Failed to create index: " + e.getMessage());
        }
    }

    public void dropIndex() {
        String sql = String.format("DROP INDEX IF EXISTS embedding_idx");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("✅ Dropped vector index");

        } catch (SQLException e) {
            System.err.println("❌ Failed to drop index: " + e.getMessage());
        }
    }

    // Batch store method (convenience method, not in interface)
    public void storeBatch(Map<String, String> contents, Map<String, float[]> embeddings) {
        if (contents.size() != embeddings.size()) {
            throw new IllegalArgumentException("Contents and embeddings must have the same size");
        }

        String sql = String.format("""
            INSERT INTO %s (id, content, embedding) 
            VALUES (?, ?, ?::vector)
            ON CONFLICT (id) 
            DO UPDATE SET content = EXCLUDED.content, embedding = EXCLUDED.embedding
            """, TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Map.Entry<String, String> entry : contents.entrySet()) {
                String id = entry.getKey();
                String content = entry.getValue();
                float[] embedding = embeddings.get(id);

                if (embedding == null) {
                    throw new IllegalArgumentException("No embedding found for ID: " + id);
                }

                validateEmbeddingDimension(embedding);

                pstmt.setString(1, id);
                pstmt.setString(2, content);
                pstmt.setString(3, convertArrayToString(embedding));
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);

            System.out.println("✅ Batch stored " + contents.size() + " documents");

        } catch (SQLException e) {
            System.err.println("❌ Failed to batch store documents: " + e.getMessage());
            throw new RuntimeException("Failed to batch store documents", e);
        }
    }
}