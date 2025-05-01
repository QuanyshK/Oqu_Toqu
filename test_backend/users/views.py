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
from rest_framework.authentication import BaseAuthentication
from rest_framework import exceptions


from rest_framework.generics import ListAPIView


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


class ChatMessageListView(ListAPIView):
    serializer_class = ChatMessageSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return ChatMessage.objects.filter(user=self.request.user).order_by('created_at')

    def list(self, request, *args, **kwargs):
        queryset = self.get_queryset()
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            "messages": serializer.data
        })



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
        file_name = file_obj.name if file_obj else ''

        # если пришёл файл — сохраним, извлечём текст, удалим
        if file_obj:
            print(f"Received file: {file_obj.name} size: {file_obj.size}")
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
