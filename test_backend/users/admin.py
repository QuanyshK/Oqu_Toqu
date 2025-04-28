from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from .models import User, ChatMessage

@admin.register(User)
class UserAdmin(BaseUserAdmin):
    list_display = ('id', 'email', 'is_active', 'is_staff')
    list_filter = ('is_active', 'is_staff')
    ordering = ('id',)
    search_fields = ('email',)

    fieldsets = (
        (None, {'fields': ('email', 'password')}),
        ('Permissions', {'fields': ('is_active', 'is_staff', 'is_superuser', 'groups', 'user_permissions')}),
        ('Important dates', {'fields': ('last_login',)}),
    )

    add_fieldsets = (
        (None, {
            'classes': ('wide',),
            'fields': ('email', 'password1', 'password2'),
        }),
    )

@admin.register(ChatMessage)
class ChatMessageAdmin(admin.ModelAdmin):
    list_display = ('id', 'user', 'short_user_message', 'short_bot_response', 'file_name', 'created_at')
    search_fields = ('user__username', 'user_message')
    list_filter = ('created_at',)
    ordering = ('-created_at',)
    list_per_page = 50

    def short_user_message(self, obj):
        return obj.user_message[:50] + ('...' if len(obj.user_message) > 50 else '')
    short_user_message.short_description = "Message"

    def short_bot_response(self, obj):
        return obj.bot_response[:50] + ('...' if len(obj.bot_response) > 50 else '')
    short_bot_response.short_description = "Response"