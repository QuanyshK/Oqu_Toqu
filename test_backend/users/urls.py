from django.urls import path
from .views import *

urlpatterns = [
    path('google-login/', GoogleLoginView.as_view(), name='google-login'),
    path('chat/send/',    ChatMessageCreateView.as_view(), name='chat-send'),
    path('chat/',         ChatMessageListView.as_view(),   name='chat-list'),
    path('chat/<int:pk>/',       ChatMessageDetailView.as_view(), name='chat-detail'),
    path('chat/<int:pk>/delete/', ChatMessageDeleteView.as_view(), name='chat-delete'),
    path("scholar/search/", ScholarSearchView.as_view(), name="scholar-search"),

]
