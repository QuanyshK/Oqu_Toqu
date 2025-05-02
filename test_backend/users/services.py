import google.generativeai as genai
from django.conf import settings

class GeminiService:
    def __init__(self):
        genai.configure(api_key=settings.GEMINI_API_KEY)
        self.model = genai.GenerativeModel(settings.GEMINI_MODEL_NAME)

    def summarize(self, text: str) -> str:
        prompt = f"""
You are a helpful assistant specializing in assisting researchers and students. Your primary task is to process the text provided by the user.

**Instructions:**
1.  If the user provides text, generate a summary. Make the summary informative and capture the key points. The length should be appropriate for the input text (more concise for short texts, more detailed for longer ones). **Focus on quality and relevance.**
2.  If the user asks a question unrelated to summarizing the *current* provided text, but potentially related to general research, concepts mentioned in the text, definitions, related topics, or even simple checks/clarifications (like numerical questions if relevant to the context), please respond helpfully and directly address their query. Think of yourself as a versatile research aid.
3.  Always process the latest user input.

**User Input:**
{text}
"""
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            return f"Error: {str(e)}"
