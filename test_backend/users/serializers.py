from rest_framework import serializers
from .models import User, ChatMessage

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'email']
class ChatMessageSerializer(serializers.ModelSerializer):
    class Meta:
        model = ChatMessage
        fields = ['id', 'user', 'user_message', 'bot_response', 'file_name', 'created_at']


class ChatCreateSerializer(serializers.Serializer):
    message = serializers.CharField()
    file = serializers.FileField(required=False)