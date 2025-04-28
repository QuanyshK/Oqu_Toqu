from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from firebase_admin import auth as firebase_auth
from .models import User
from .serializers import UserSerializer
import jwt
from django.conf import settings
from rest_framework.permissions import AllowAny
import os
from django.utils.timezone import now
from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from PyPDF2 import PdfReader
from docx import Document
import re
import requests
from bs4 import BeautifulSoup
from .models import ChatMessage
from .serializers import ChatMessageSerializer, ChatCreateSerializer
from .services import GeminiService

class GoogleLoginView(APIView):
    permission_classes = [AllowAny]
    authentication_classes = []
    def post(self, request):
        id_token = request.data.get('idToken')

        if not id_token:
            return Response({"error": "ID Token is required."}, status=status.HTTP_400_BAD_REQUEST)

        try:
            decoded_token = firebase_auth.verify_id_token(id_token)
            email = decoded_token['email']
        except Exception as e:
            return Response({"error": f"Invalid ID Token: {str(e)}"}, status=status.HTTP_401_UNAUTHORIZED)

        user, created = User.objects.get_or_create(email=email)

        token = jwt.encode({'user_id': user.id, 'email': user.email}, settings.SECRET_KEY, algorithm='HS256')

        return Response({
            'token': token,
            'user': UserSerializer(user).data
        })


def extract_text_from_pdf(path: str) -> str:
    text = ""
    with open(path, 'rb') as f:
        reader = PdfReader(f)
        for page in reader.pages:
            text += page.extract_text() or ""
    return text


def extract_text_from_docx(path: str) -> str:
    doc = Document(path)
    return "\n".join(p.text for p in doc.paragraphs)


class ChatMessageListView(generics.ListAPIView):
    serializer_class = ChatMessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user).order_by('created_at')


class ChatMessageCreateView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    def post(self, request, *args, **kwargs):
        # лимит 20 запросов в день
        today_count = ChatMessage.objects.filter(
            user=request.user,
            created_at__date=now().date()
        ).count()
        if today_count >= 20:
            return Response(
                {"error": "Daily request limit (20) reached."},
                status=status.HTTP_429_TOO_MANY_REQUESTS
            )

        serializer = ChatCreateSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        text = serializer.validated_data.get('message', '')
        file_obj = request.FILES.get('file')
        file_name = request.data.get('file_name', '')

        # если пришёл файл — сохраним, извлечём текст, удалим
        if file_obj:
            tmp_path = os.path.join('/tmp', file_obj.name)
            with open(tmp_path, 'wb+') as f:
                for chunk in file_obj.chunks():
                    f.write(chunk)
            if file_obj.name.lower().endswith('.pdf'):
                text = extract_text_from_pdf(tmp_path)
            elif file_obj.name.lower().endswith('.docx'):
                text = extract_text_from_docx(tmp_path)
            os.remove(tmp_path)
            file_name = file_obj.name

            if not text:
                return Response(
                    {"error": "Failed to extract text from file"},
                    status=status.HTTP_400_BAD_REQUEST
                )

        if not text:
            return Response(
                {"error": "Text or file is required"},
                status=status.HTTP_400_BAD_REQUEST
            )

        # вызываем Gemini
        service = GeminiService()
        summary = service.summarize(text)

        chat = ChatMessage.objects.create(
            user=request.user,
            user_message=text,
            bot_response=summary,
            file_name=file_name or None
        )
        return Response(ChatMessageSerializer(chat).data, status=status.HTTP_201_CREATED)


class ChatMessageDetailView(generics.RetrieveAPIView):
    serializer_class = ChatMessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user)


class ChatMessageDeleteView(generics.DestroyAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user)
class ScholarSearchView(APIView):
    permission_classes = [permissions.AllowAny]
    DOI_PATTERN = re.compile(r"10\.\d{4,9}/[-._;()/:A-Z0-9]+", re.IGNORECASE)

    def get(self, request):
        q = request.query_params.get("q")
        if not q:
            return Response(
                {"detail": "Query parameter `q` is required."},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            page = int(request.query_params.get("page", 1))
            if page < 1:
                raise ValueError()
        except ValueError:
            return Response(
                {"detail": "`page` must be a positive integer."},
                status=status.HTTP_400_BAD_REQUEST
            )

        page_size = 10
        start = (page - 1) * page_size

        url = "https://scholar.google.com/scholar"
        headers = {
            "User-Agent": (
                "Mozilla/5.0 (X11; Linux x86_64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/115.0 Safari/537.36"
            )
        }
        params = {
            "q": q,
            "hl": "en",
            "as_sdt": "0,5",
            "num": page_size,
            "start": start
        }

        try:
            resp = requests.get(url, headers=headers, params=params, timeout=5)
        except requests.RequestException:
            return Response(
                {"detail": "Failed to fetch Google Scholar."},
                status=status.HTTP_502_BAD_GATEWAY
            )

        if resp.status_code != 200:
            return Response(
                {"detail": "Scholar request failed."},
                status=status.HTTP_502_BAD_GATEWAY
            )

        soup = BeautifulSoup(resp.text, "html.parser")
        items = soup.find_all("div", class_="gs_r")

        results = []
        for item in items:
            # title & link
            h3 = item.find("h3", class_="gs_rt")
            a = h3.find("a") if h3 else None
            title = a.text if a else (h3.text if h3 else None)
            link  = a["href"] if a else None

            # snippet
            snippet_tag = item.find("div", class_="gs_rs")
            snippet = snippet_tag.get_text(strip=True) if snippet_tag else None

            # authors/year
            authors_tag = item.find("div", class_="gs_a")
            authors = authors_tag.get_text(strip=True) if authors_tag else None

            # doi
            doi = None
            if link:
                m = self.DOI_PATTERN.search(link)
                doi = m.group(0) if m else None

            results.append({
                "title":   title,
                "link":    link,
                "snippet": snippet,
                "authors": authors,
                "doi":     doi
            })

        return Response({
            "page":      page,
            "page_size": page_size,
            "results":   results
        }, status=status.HTTP_200_OK)