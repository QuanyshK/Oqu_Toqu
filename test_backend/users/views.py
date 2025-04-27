from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from firebase_admin import auth as firebase_auth
from .models import User
from .serializers import UserSerializer
import jwt
from django.conf import settings

class GoogleLoginView(APIView):
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
