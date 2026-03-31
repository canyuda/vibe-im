import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user.dart';
import '../models/message.dart';

/// API service for handling all HTTP requests to the backend
/// @author AI Agent
class ApiService {
  /// Base URL for the backend API
  static const String baseUrl = 'http://localhost:8080/api';

  /// Session ID for authentication
  String? _sessionId;

  /// Current user ID
  String? _currentUserId;

  /// Getter for session ID
  String? get sessionId => _sessionId;

  /// Getter for current user ID
  String? get currentUserId => _currentUserId;

  /// Set session information after successful login
  /// @param sessionId The session ID from the backend
  /// @param userId The current user ID
  void setSession(String sessionId, String userId) {
    _sessionId = sessionId;
    _currentUserId = userId;
  }

  /// Clear session information (e.g., after logout)
  void clearSession() {
    _sessionId = null;
    _currentUserId = null;
  }

  /// Get HTTP headers including session ID if available
  /// @return Map of HTTP headers
  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        if (_sessionId != null) 'X-Session-Id': _sessionId!,
      };

  /// Register a new user
  /// @param username The username for registration
  /// @param password The password for registration
  /// @param nickname The user's nickname
  /// @return User object containing the registered user information
  /// @throws Exception if registration fails
  Future<User> register({
    required String username,
    required String password,
    required String nickname,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/register'),
      headers: _headers,
      body: jsonEncode({
        'username': username,
        'password': password,
        'nickname': nickname,
      }),
    );

    if (response.statusCode != 200) {
      throw Exception('Registration failed: ${response.body}');
    }

    final Map<String, dynamic> responseData = jsonDecode(response.body);
    return User.fromJson(responseData);
  }

  /// Login with username and password
  /// @param username The username
  /// @param password The password
  /// @return Map containing session information and user data
  /// @throws Exception if login fails
  Future<Map<String, dynamic>> login({
    required String username,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: _headers,
      body: jsonEncode({
        'username': username,
        'password': password,
      }),
    );

    if (response.statusCode != 200) {
      throw Exception('Login failed: ${response.body}');
    }

    final Map<String, dynamic> responseData = jsonDecode(response.body);

    // Set session information
    final sessionId = responseData['sessionId'] as String?;
    final userId = responseData['userId'] as String?;

    if (sessionId != null && userId != null) {
      setSession(sessionId, userId);
    }

    return responseData;
  }

  /// Logout current user
  /// @throws Exception if logout fails
  Future<void> logout() async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/logout'),
      headers: _headers,
    );

    if (response.statusCode != 200) {
      throw Exception('Logout failed: ${response.body}');
    }

    // Clear session regardless of response status
    clearSession();
  }

  /// Send a message to another user
  /// @param receiverId The ID of the message recipient
  /// @param content The message content
  /// @return Message object containing the sent message information
  /// @throws Exception if sending message fails
  Future<Message> sendMessage({
    required String receiverId,
    required String content,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/chat/send'),
      headers: _headers,
      body: jsonEncode({
        'receiverId': receiverId,
        'content': content,
        'messageType': 'TEXT',
      }),
    );

    if (response.statusCode != 200) {
      throw Exception('Send message failed: ${response.body}');
    }

    final Map<String, dynamic> responseData = jsonDecode(response.body);
    return Message.fromJson(responseData);
  }

  /// Get chat messages between current user and another user
  /// @param otherUserId The ID of the other user in the conversation
  /// @param limit Maximum number of messages to retrieve (default: 50)
  /// @param offset Offset for pagination (default: 0)
  /// @return List of Message objects
  /// @throws Exception if retrieving messages fails
  Future<List<Message>> getChatMessages({
    required String otherUserId,
    int limit = 50,
    int offset = 0,
  }) async {
    final uri = Uri.parse('$baseUrl/chat/messages').replace(
          queryParameters: {
            'otherUserId': otherUserId,
            'limit': limit.toString(),
            'offset': offset.toString(),
          },
        );

    final response = await http.get(
      uri,
      headers: _headers,
    );

    if (response.statusCode != 200) {
      throw Exception('Get messages failed: ${response.body}');
    }

    final Map<String, dynamic> responseData = jsonDecode(response.body);
    final List<dynamic> messagesJson = responseData['messages'] as List<dynamic>;

    return messagesJson
        .map((json) => Message.fromJson(json as Map<String, dynamic>))
        .toList();
  }
}
