import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/message.dart';
import '../services/api_service.dart';
import '../services/websocket_service.dart';

/// Chat provider for managing chat messages and history state
/// Uses ChangeNotifier to notify UI when message state changes
/// @author AI Agent
class ChatProvider with ChangeNotifier {
  final ApiService _apiService;
  final WebSocketService _webSocketService = WebSocketService();

  /// Stream subscription for WebSocket messages
  StreamSubscription<Message>? _subscription;

  /// List of all messages (current chat)
  final List<Message> _messages = [];

  /// Map to store chat history per user (userId -> List<Message>)
  final Map<String, List<Message>> _chatHistory = {};

  /// Get all messages for the current chat
  List<Message> get messages => _messages;

  ChatProvider({required ApiService apiService}) : _apiService = apiService {
    _initializeWebSocket();
  }

  /// Initialize WebSocket connection and listen for incoming messages
  void _initializeWebSocket() {
    try {
      _subscription = _webSocketService.messageStream.listen(
        _addMessage,
        onError: (error) {
          if (kDebugMode) {
            print('WebSocket stream error: $error');
          }
        },
      );
    } catch (e) {
      if (kDebugMode) {
        print('Failed to initialize WebSocket: $e');
      }
    }
  }

  /// Connect to WebSocket server
  /// @throws Exception if connection fails
  Future<void> connectWebSocket() async {
    try {
      _webSocketService.connect();
    } catch (e) {
      if (kDebugMode) {
        print('WebSocket connection error: $e');
      }
      rethrow;
    }
  }

  /// Send a message to another user
  /// @param receiverId The ID of the message recipient
  /// @param content The message content
  /// @return Message object containing the sent message information
  /// @throws Exception if sending message fails
  Future<void> sendMessage({
    required String receiverId,
    required String content,
  }) async {
    try {
      final message = await _apiService.sendMessage(
        receiverId: receiverId,
        content: content,
      );
      _addMessage(message);
    } catch (e) {
      if (kDebugMode) {
        print('Send message error: $e');
      }
      rethrow;
    }
  }

  /// Load chat history for a specific user
  /// @param otherUserId The ID of the other user in the conversation
  /// @throws Exception if loading history fails
  Future<void> loadChatHistory(String otherUserId) async {
    try {
      final messages = await _apiService.getChatMessages(
        otherUserId: otherUserId,
      );

      // Reverse messages to show oldest first
      final reversedMessages = messages.reversed.toList();

      // Store in chat history map
      _chatHistory[otherUserId] = reversedMessages;

      // Update current messages list
      _messages
        ..clear()
        ..addAll(reversedMessages);

      notifyListeners();
    } catch (e) {
      if (kDebugMode) {
        print('Load chat history error: $e');
      }
      rethrow;
    }
  }

  /// Add a message to the appropriate chat history
  /// Automatically determines which user's chat history to add to
  /// @param message The message to add
  void _addMessage(Message message) {
    // Determine the user ID for chat history
    // If current user sent the message, store it under receiver's ID
    // If current user received the message, store it under sender's ID
    final currentUserId = _apiService.currentUserId;
    final userId = (message.senderId == currentUserId)
        ? message.receiverId
        : message.senderId;

    // Initialize chat history for this user if not exists
    if (!_chatHistory.containsKey(userId)) {
      _chatHistory[userId] = [];
    }

    // Check for duplicate messages using messageId
    final messages = _chatHistory[userId]!;
    final isDuplicate = messages.any((msg) => msg.messageId == message.messageId);

    // Add message only if not duplicate
    if (!isDuplicate) {
      messages.add(message);

      // If this is the current active chat, update _messages list
      if (_messages.isNotEmpty) {
        final currentChatUserId = (_messages.first.senderId == currentUserId)
            ? _messages.first.receiverId
            : _messages.first.senderId;

        if (currentChatUserId == userId) {
          _messages.add(message);
        }
      }

      notifyListeners();
    }
  }

  /// Get all messages for a specific user
  /// @param userId The user ID to get messages for
  /// @return List of messages for the specified user, or empty list if no messages
  List<Message> getMessagesForUser(String userId) {
    return _chatHistory[userId] ?? [];
  }

  /// Dispose of resources and cleanup
  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }
}
