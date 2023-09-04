# api.py

# Import required libraries
from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
import scipy
import Levenshtein

app = Flask(__name__)

# Load the BERT model
model = SentenceTransformer('bert-base-nli-mean-tokens')

@app.route("/", methods=['GET'])
def home():
    return "Hello World!"

@app.route("/compare", methods=['POST'])
def compare_answers():
    data = request.get_json()
    user_answer = data.get('user_answer', '')
    true_answer = data.get('true_answer', '')

    if not user_answer or not true_answer:
        return jsonify({'error': 'User answer and true answer not provided'}), 400

    user_embedding = model.encode([user_answer])[0]  # Get the first element from the list
    true_embedding = model.encode([true_answer])[0]  # Get the first element from the list

    similarity_score = 1 - scipy.spatial.distance.cosine(user_embedding, true_embedding)

    max_cosine_similarity = 0.9
    max_levenshtein_distance = 1

    if similarity_score >= max_cosine_similarity:
        is_similar = True
    else:
        levenshtein_distance = Levenshtein.distance(user_answer, true_answer)
        is_similar = levenshtein_distance <= max_levenshtein_distance

    response = {'user_answer': user_answer, 'true_answer': true_answer, 'similarity_score': similarity_score, 'is_similar': is_similar}

    return jsonify(response)

if __name__ == "__main__":
    app.run()
