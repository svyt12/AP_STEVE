package backend.rag;

/**
 * Represents a single search result from the vector store.
 * This is a Data Transfer Object (DTO) that packages together:
 * - Document ID
 * - Content/text
 * - Similarity score
 */
public class SearchResult {
    private final String documentId;
    private final String content;
    private final float similarity;

    /**
     * Constructor to create a search result
     *
     * @param documentId The unique identifier of the document/chunk
     * @param content The text content of the document/chunk
     * @param similarity The cosine similarity score (0.0 to 1.0)
     */
    public SearchResult(String documentId, String content, float similarity) {
        this.documentId = documentId;
        this.content = content;
        this.similarity = similarity;
    }

    // ========== GETTERS ==========

    /**
     * @return The document/chunk ID
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * @return The text content of the document/chunk
     */
    public String getContent() {
        return content;
    }

    /**
     * @return The similarity score (0.0 = not similar, 1.0 = identical)
     */
    public float getSimilarity() {
        return similarity;
    }

    /**
     * @return Similarity as percentage (0% to 100%)
     */
    public float getSimilarityPercentage() {
        return similarity * 100.0f;
    }

    // ========== UTILITY METHODS ==========

    /**
     * Get a preview of the content (first N characters)
     *
     * @param maxLength Maximum length of the preview
     * @return Content preview
     */
    public String getContentPreview(int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * Check if this result is highly relevant
     *
     * @param threshold Similarity threshold (default 0.7 = 70%)
     * @return true if similarity >= threshold
     */
    public boolean isHighlyRelevant(float threshold) {
        return similarity >= threshold;
    }

    public boolean isHighlyRelevant() {
        return isHighlyRelevant(0.7f); // Default threshold
    }

    // ========== OVERRIDDEN METHODS ==========

    @Override
    public String toString() {
        return String.format("SearchResult[" +
                        "id='%s', " +
                        "similarity=%.2f%%, " +
                        "content='%s']",
                documentId,
                getSimilarityPercentage(),
                getContentPreview(50));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SearchResult that = (SearchResult) obj;

        if (Float.compare(that.similarity, similarity) != 0) return false;
        if (!documentId.equals(that.documentId)) return false;
        return content.equals(that.content);
    }

    @Override
    public int hashCode() {
        int result = documentId.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + Float.hashCode(similarity);
        return result;
    }

    // ========== BUILDER PATTERN (Optional) ==========

    /**
     * Builder for creating SearchResult objects (optional but useful)
     */
    public static class Builder {
        private String documentId;
        private String content;
        private float similarity;

        public Builder setDocumentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setSimilarity(float similarity) {
            this.similarity = similarity;
            return this;
        }

        public SearchResult build() {
            if (documentId == null) {
                throw new IllegalStateException("Document ID must be set");
            }
            if (content == null) {
                content = "";
            }
            return new SearchResult(documentId, content, similarity);
        }
    }

    /**
     * Create a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ========== COMPARATORS ==========

    /**
     * Comparator for sorting by similarity (descending - highest first)
     */
    public static final java.util.Comparator<SearchResult> BY_SIMILARITY_DESC =
            (a, b) -> Float.compare(b.similarity, a.similarity);

    /**
     * Comparator for sorting by similarity (ascending - lowest first)
     */
    public static final java.util.Comparator<SearchResult> BY_SIMILARITY_ASC =
            (a, b) -> Float.compare(a.similarity, b.similarity);

    /**
     * Comparator for sorting by document ID
     */
    public static final java.util.Comparator<SearchResult> BY_DOCUMENT_ID =
            (a, b) -> a.documentId.compareTo(b.documentId);
}
