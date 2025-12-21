package backend.service;

import backend.rag.EmbeddingService;
import backend.rag.SearchResult;
import backend.rag.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RAGQueryService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final GPTService gptService;

    public RAGQueryService(VectorStore vectorStore, EmbeddingService embeddingService, GPTService gptService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.gptService = gptService;
    }

    // search for relevant documents
    public List<SearchResult> searchDocuments(String query) {
        try {
            System.out.println("🔍 Searching for: " + query);

            // Convert query to embedding
            float[] queryEmbedding = embeddingService.embed(query);

            // Search vector store
            List<SearchResult> results = new ArrayList<>(vectorStore.searchSimilar(queryEmbedding, 10));

            System.out.println("   Found " + results.size() + " potential matches");

            // Filter only highly relevant results (>50% similarity)
            results.removeIf(result -> !result.isHighlyRelevant(0.5f));

            System.out.println("   After filtering: " + results.size() + " highly relevant");

            return results;
        } catch (Exception e) {
            System.err.println("❌ Search failed: " + e.getMessage());
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    // generate answer using GPT based on the documents
    public String generateAnswer(String question, List<SearchResult> relevantDocs) {
        if (relevantDocs.isEmpty()) {
            return "I couldn't find relevant information in the uploaded documents about '" + question + "'.";
        }

        // Merge top 5 chunks for context
        List<SearchResult> topChunks = relevantDocs.subList(0, Math.min(5, relevantDocs.size()));
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < topChunks.size(); i++) {
            SearchResult doc = topChunks.get(i);
            contextBuilder.append(i + 1).append(". [")
                    .append(String.format("%.2f", doc.getSimilarityPercentage()))
                    .append("% match] From ")
                    .append(doc.getDocumentId())
                    .append(":\n")
                    .append(doc.getContent())
                    .append("\n\n");
        }

        String mergedContext = contextBuilder.toString();

        String prompt = """
                You are a C++ tutor. Use ONLY the context from the uploaded documents.
                - If the context contains code, you may infer variable values or pointer relationships from the code.
                - Answer clearly and concisely.

                Context:
                %s

                Question:
                %s

                Answer:
                """.formatted(mergedContext, question);

        System.out.println("📝 Prompt prepared for GPT:\n" + prompt);

        try {
            return gptService.chatCompletion(prompt);
        } catch (Exception e) {
            System.err.println("❌ GPT call failed: " + e.getMessage());
            e.printStackTrace();
            return "Based on the uploaded documents, here's what I found:\n\n" +
                    topChunks.get(0).getContentPreview(300) +
                    "\n\n💡 Answer summary: The documents contain information about this topic.";
        }
    }

    /**
     * Generate quiz questions based on a topic
     * @param topic the topic for the quiz
     * @param format "plain" for sentence questions, "mcq" for multiple-choice
     * @param numberOfQuestions number of questions for plain format (ignored for MCQ)
     */
    public List<String> generateQuiz(String topic, String format, int numberOfQuestions) {
        List<SearchResult> topicDocs = searchDocuments(topic);

        if (topicDocs.isEmpty()) {
            throw new RuntimeException("No documents found about: " + topic);
        }

        System.out.println("🎯 Found " + topicDocs.size() + " documents for quiz on: " + topic);

        List<String> quizQuestions = new ArrayList<>();

        for (SearchResult doc : topicDocs) {
            String prompt;
            if ("mcq".equalsIgnoreCase(format)) {
                prompt = """
                        You are an exam question generator.
                        Based on the following text, create **ONE multiple choice question** with:
                        - 4 options labeled A, B, C, D
                        - Indicate which option is correct
                        - Keep the question and options clear and concise

                        Text:
                        %s

                        Output format:
                        Question: ...
                        A: ...
                        B: ...
                        C: ...
                        D: ...
                        Answer: ...
                        """.formatted(doc.getContent());
            } else {
                prompt = """
                        You are an exam question generator.
                        Based on the following text, create **ONE multiple choice question** with:
                        - 4 options labeled A, B, C, D
                        - Indicate which option is correct
                        - Keep the question and options clear and concise

                        Text:
                        %s

                        Output format:
                        Question: ...
                        A: ...
                        B: ...
                        C: ...
                        D: ...
                        Answer: ...
                        """.formatted(doc.getContent());
            }

            try {
                String generated = gptService.chatCompletion(prompt);
                quizQuestions.add(generated);
            } catch (Exception e) {
                System.err.println("❌ GPT quiz generation failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return quizQuestions;
    }
}
