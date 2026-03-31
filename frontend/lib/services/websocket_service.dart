import 'dart:async';
import 'dart:convert';

import 'package:web_socket_channel/web_socket_channel.dart';

import '../models/message.dart';
import 'api_service.dart';

/// WebSocket service for real-time messaging with heartbeat mechanism
/// @author AI Agent
class WebSocketService {
  /// Singleton instance
  static final WebSocketService _instance = WebSocketService._internal();

  /// Factory constructor to return singleton instance
  factory WebSocketService() => _instance;

  /// Private constructor
  WebSocketService._internal();

  /// WebSocket channel for communication
  WebSocketChannel? _channel;

  /// Stream subscription for incoming messages
  StreamSubscription? _subscription;

  /// Timer for heartbeat mechanism
  Timer? _heartbeatTimer;

  /// API service reference for session management
  final ApiService _apiService = ApiService();

  /// Stream controller for broadcasting messages to listeners
  final StreamController<Message> _messageController = StreamController<Message>.broadcast();

  /// Stream of incoming messages
  Stream<Message> get messageStream => _messageController.stream;

  /// Connection status
  bool _isConnected = false;

  /// Get current connection status
  bool get isConnected => _isConnected;

  /// Connect to WebSocket server
  /// @throws Exception if no session ID is available or connection fails
  void connect() {
    if (_isConnected && _channel != null) {
      return; // Already connected
    }

    final sessionId = _apiService.sessionId;
    if (sessionId == null || sessionId.isEmpty) {
      throw Exception('No session ID available. Please login first.');
    }

    // Build WebSocket URL with session ID
    final wsUrl = Uri.parse('ws://localhost:8080/api/ws?sessionId=$sessionId');

    try {
      // Create WebSocket channel and connect
      _channel = WebSocketChannel.connect(wsUrl);
      _isConnected = true;

      // Set up stream listener for incoming messages
      _subscription = _channel!.stream.listen(
        _handleMessage,
        onError: _handleError,
        onDone: _handleDisconnect,
        cancelOnError: false,
      );

      // Start heartbeat mechanism
      _startHeartbeat();

      print('WebSocket connected successfully');
    } catch (e) {
      _isConnected = false;
      _channel = null;
      throw Exception('WebSocket connection failed: $e');
    }
  }

  /// Handle incoming WebSocket messages
  /// @param message The raw message from WebSocket
  void _handleMessage(dynamic message) {
    try {
      final jsonData = jsonDecode(message as String) as Map<String, dynamic>;
      final messageType = jsonData['type'] as String?;

      // Handle PONG response from heartbeat
      if (messageType == 'PONG') {
        return; // Heartbeat acknowledged, no action needed
      }

      // Process regular messages
      final messageId = jsonData['messageId'] as String?;
      if (messageId != null) {
        final msg = Message.fromJson(jsonData);
        _messageController.add(msg);
      }
    } catch (e) {
      print('Error parsing WebSocket message: $e');
    }
  }

  /// Handle WebSocket errors
  /// @param error The error that occurred
  void _handleError(dynamic error) {
    print('WebSocket error: $error');
    _isConnected = false;
    _stopHeartbeat();
  }

  /// Handle WebSocket disconnection
  void _handleDisconnect() {
    print('WebSocket disconnected');
    _isConnected = false;
    _stopHeartbeat();
    _channel = null;
    _subscription = null;
  }

  /// Start heartbeat mechanism to keep connection alive
  /// Sends PING message every 15 seconds
  void _startHeartbeat() {
    _stopHeartbeat(); // Stop existing timer if any

    _heartbeatTimer = Timer.periodic(
      const Duration(seconds: 15),
      (_) {
        try {
          if (_channel != null && _isConnected) {
            final pingMessage = jsonEncode({'type': 'PING'});
            _channel!.sink.add(pingMessage);
            print('Heartbeat: PING sent');
          }
        } catch (e) {
          print('Error sending heartbeat: $e');
          _handleError(e);
        }
      },
    );

    print('Heartbeat started (15s interval)');
  }

  /// Stop heartbeat mechanism
  void _stopHeartbeat() {
    _heartbeatTimer?.cancel();
    _heartbeatTimer = null;
    print('Heartbeat stopped');
  }

  /// Disconnect from WebSocket server
  void disconnect() {
    try {
      _channel?.sink.close();
    } catch (e) {
      print('Error closing WebSocket: $e');
    } finally {
      _handleDisconnect();
    }
  }

  /// Send a message through WebSocket
  /// @param jsonData The JSON data to send
  void sendMessage(Map<String, dynamic> jsonData) {
    if (!_isConnected || _channel == null) {
      throw Exception('WebSocket is not connected');
    }

    try {
      _channel!.sink.add(jsonEncode(jsonData));
    } catch (e) {
      print('Error sending message: $e');
      throw Exception('Failed to send message: $e');
    }
  }

  /// Dispose of resources and cleanup
  void dispose() {
    _messageController.close();
    disconnect();
  }
}
