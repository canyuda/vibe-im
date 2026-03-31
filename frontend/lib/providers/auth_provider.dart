import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user.dart';
import '../services/api_service.dart';

/// Authentication provider for managing user authentication state
/// Uses ChangeNotifier to notify UI when authentication state changes
/// @author AI Agent
class AuthProvider with ChangeNotifier {
  final ApiService _apiService;
  User? _currentUser;

  AuthProvider({required ApiService apiService}) : _apiService = apiService;

  /// Get the current logged-in user
  User? get currentUser => _currentUser;

  /// Check if user is logged in
  bool get isLoggedIn => _currentUser != null;

  /// Register a new user
  /// @param username The username for registration
  /// @param password The password for registration
  /// @param nickname The user's nickname
  /// @return true if registration successful, false otherwise
  Future<bool> register({
    required String username,
    required String password,
    required String nickname,
  }) async {
    try {
      final user = await _apiService.register(
        username: username,
        password: password,
        nickname: nickname,
      );
      _currentUser = user;
      notifyListeners();
      return true;
    } catch (e) {
      if (kDebugMode) {
        print('Registration error: $e');
      }
      return false;
    }
  }

  /// Login with username and password
  /// @param username The username
  /// @param password The password
  /// @return true if login successful, false otherwise
  Future<bool> login({
    required String username,
    required String password,
  }) async {
    try {
      final responseData = await _apiService.login(
        username: username,
        password: password,
      );

      // Create User object from response data
      final user = User.fromJson(responseData);
      _currentUser = user;

      // Save session to SharedPreferences
      final prefs = await SharedPreferences.getInstance();
      final sessionId = responseData['sessionId'] as String?;
      final userId = responseData['userId'] as String?;

      if (sessionId != null && userId != null) {
        await prefs.setString('sessionId', sessionId);
        await prefs.setString('userId', userId);
      }

      notifyListeners();
      return true;
    } catch (e) {
      if (kDebugMode) {
        print('Login error: $e');
      }
      return false;
    }
  }

  /// Logout current user
  /// Clears session from SharedPreferences and resets user state
  Future<void> logout() async {
    try {
      await _apiService.logout();
    } catch (e) {
      if (kDebugMode) {
        print('Logout error: $e');
      }
    } finally {
      // Clear session from SharedPreferences
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove('sessionId');
      await prefs.remove('userId');

      // Reset user state
      _currentUser = null;
      notifyListeners();
    }
  }

  /// Load saved session from SharedPreferences
  /// Attempts to restore session if sessionId and userId are saved
  Future<void> loadSavedSession() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final sessionId = prefs.getString('sessionId');
      final userId = prefs.getString('userId');

      if (sessionId != null && userId != null) {
        _apiService.setSession(sessionId, userId);
        // Note: We don't restore User object here as it would require
        // an additional API call to fetch user details
      }
    } catch (e) {
      if (kDebugMode) {
        print('Load session error: $e');
      }
    }
  }
}
