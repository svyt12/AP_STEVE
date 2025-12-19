package backend.rag;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Component
public class PgVectorStore implements VectorStore {

    private final DataSource dataSource;
    private static final String TABLE_NAME = "document_embeddings";
    private static final int EMBEDDING_DIMENSION = 1536; // Adjust based on your embedding model

    public PgVectorStore(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeDatabase();
        System.out.println("✅ PgVectorStore initialized (using REAL arrays)");
    }

    private void initializeDatabase() {
        // CHANGED: Use REAL[] instead of VECTOR
        String createTableSQL = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                id VARCHAR(255) PRIMARY KEY,
                content TEXT NOT NULL,
                embedding REAL[%d],  -- REAL array type
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """, TABLE_NAME, EMBEDDING_DIMENSION);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // REMOVED: No need for CREATE EXTENSION vector
            // Create table only
            stmt.execute(createTableSQL);

            System.out.println("✅ Database table initialized with REAL arrays: " + TABLE_NAME);

        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    @Override
    public void store(String id, String content, float[] embedding) {
        validateEmbeddingDimension(embedding);

        // CHANGED: Use ?::real[] instead of CAST(? AS vector)
        String sql = String.format("""
            INSERT INTO %s (id, content, embedding) 
            VALUES (?, ?, ?::real[])
            ON CONFLICT (id) 
            DO UPDATE SET content = EXCLUDED.content, embedding = EXCLUDED.embedding
            """, TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, content);
            pstmt.setString(3, convertArrayToPostgresArray(embedding)); // Uses PostgreSQL array format

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

        // CHANGED: Manual cosine similarity calculation for REAL arrays
        String sql = String.format("""
            WITH query_vector AS (
                SELECT ?::real[] as qvec
            )
            SELECT id, content,
                   (1 - (
                       SELECT COALESCE(
                           SUM(a*b) / NULLIF(SQRT(SUM(a*a)) * SQRT(SUM(b*b)), 0),
                           0
                       )
                       FROM unnest(embedding, (SELECT qvec FROM query_vector)) AS t(a,b)
                   )) as similarity
            FROM %s, query_vector
            WHERE embedding IS NOT NULL
            ORDER BY similarity DESC
            LIMIT ?
            """, TABLE_NAME);

        List<SearchResult> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String embeddingStr = convertArrayToPostgresArray(queryEmbedding);
            pstmt.setString(1, embeddingStr);
            pstmt.setInt(2, topK);

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
                String arrayText = rs.getString("embedding");
                float[] vector = parsePostgresArray(arrayText);
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

    // CHANGED: Convert float array to PostgreSQL array format {1.0,2.0,3.0}
    private String convertArrayToPostgresArray(float[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("}");
        return sb.toString();
    }

    // CHANGED: Parse PostgreSQL array string {1.0,2.0,3.0} back to float array
    private float[] parsePostgresArray(String arrayText) {
        // Remove curly braces and split by commas
        String cleaned = arrayText.replace("{", "").replace("}", "");
        if (cleaned.isEmpty()) {
            return new float[0];
        }

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

    // CHANGED: Create GIN index for array similarity (optional optimization)
    public void createIndex() {
        String sql = String.format("""
            CREATE INDEX IF NOT EXISTS embedding_gin_idx ON %s 
            USING GIN (embedding)
            """, TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("✅ Created GIN index for array operations");

        } catch (SQLException e) {
            System.err.println("⚠️  Could not create GIN index (not required for basic operations): " + e.getMessage());
        }
    }

    public void dropIndex() {
        String sql = "DROP INDEX IF EXISTS embedding_gin_idx";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("✅ Dropped GIN index");

        } catch (SQLException e) {
            System.err.println("❌ Failed to drop index: " + e.getMessage());
        }
    }

    // Batch store method (convenience method, not in interface)
    public void storeBatch(Map<String, String> contents, Map<String, float[]> embeddings) {
        if (contents.size() != embeddings.size()) {
            throw new IllegalArgumentException("Contents and embeddings must have the same size");
        }

        // CHANGED: Use ?::real[] for batch insert
        String sql = String.format("""
            INSERT INTO %s (id, content, embedding) 
            VALUES (?, ?, ?::real[])
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
                pstmt.setString(3, convertArrayToPostgresArray(embedding));
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