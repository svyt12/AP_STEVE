from flask import Flask, request, jsonify
from langchain.embeddings import OpenAIEmbeddings
from langchain.vectorstores import Chroma
from langchain.chains import RetrievalQA
from langchain.chat_models import ChatOpenAI
import os
x
# ----------------------------
# 1️⃣ Configure OpenAI API key
# ----------------------------
# Make sure you set environment variable OPENAI_API_KEY
# e.g., in terminal: setx OPENAI_API_KEY "your_key_here"
openai_api_key = os.environ.get("OPENAI_API_KEY")
if not openai_api_key:
    raise ValueError("Please set your OPENAI_API_KEY environment variable")

# ----------------------------
# 2️⃣ Setup Flask
# ----------------------------
app = Flask(__name__)

# ----------------------------
# 3️⃣ Setup LangChain + ChromaDB
# ----------------------------
# For simplicity, create a local ChromaDB collection
embedding_model = OpenAIEmbeddings(openai_api_key=openai_api_key)
vectorstore = Chroma(persist_directory="chroma_db", embedding_function=embedding_model)

# QA chain
qa_chain = RetrievalQA.from_chain_type(
    llm=ChatOpenAI(openai_api_key=openai_api_key, temperature=0),
    retriever=vectorstore.as_retriever(),
    return_source_documents=True
)

# ----------------------------
# 4️⃣ API endpoint
# ----------------------------
@app.route("/ask", methods=["POST"])
def ask_question():
    data = request.get_json()
    question = data.get("question", "")
    if not question:
        return jsonify({"error": "No question provided"}), 400

    # Run RAG
    result = qa_chain.run(question)

    # For now, return just the answer
    return jsonify({"answer": result})

# ----------------------------
# 5️⃣ Run server
# ----------------------------
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
