import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

/// Login page with registration and login functionality
/// Provides a form for user authentication and registration
/// Supports switching between login and registration modes
/// @author AI Agent
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  /// Form key for validation
  final _formKey = GlobalKey<FormState>();

  /// Controller for username input
  final _usernameController = TextEditingController();

  /// Controller for password input
  final _passwordController = TextEditingController();

  /// Controller for nickname input (registration only)
  final _nicknameController = TextEditingController();

  /// Flag to toggle between login and registration mode
  bool _isLoginMode = true;

  /// Flag to indicate loading state during API calls
  bool _isLoading = false;

  /// Error message to display to the user
  String? _errorMessage;

  @override
  void dispose() {
    // Dispose all controllers to prevent memory leaks
    _usernameController.dispose();
    _passwordController.dispose();
    _nicknameController.dispose();
    super.dispose();
  }

  /// Validate and submit the form
  /// Calls AuthProvider register or login based on current mode
  Future<void> _handleSubmit() async {
    // Validate form before submission
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = context.read<AuthProvider>();
      bool success;

      if (_isLoginMode) {
        // Login mode
        success = await authProvider.login(
          username: _usernameController.text.trim(),
          password: _passwordController.text,
        );
      } else {
        // Registration mode
        success = await authProvider.register(
          username: _usernameController.text.trim(),
          password: _passwordController.text,
          nickname: _nicknameController.text.trim(),
        );
      }

      if (mounted) {
        if (success) {
          // TODO: Navigate to chat list or home page
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                _isLoginMode ? 'Login successful!' : 'Registration successful!',
              ),
              backgroundColor: Colors.green,
            ),
          );
        } else {
          setState(() {
            _errorMessage = _isLoginMode
                ? 'Login failed. Please check your credentials.'
                : 'Registration failed. Username may already exist.';
          });
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = 'An error occurred. Please try again.';
        });
      }
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  /// Toggle between login and registration mode
  /// Clears form fields and error message
  void _toggleMode() {
    setState(() {
      _isLoginMode = !_isLoginMode;
      _errorMessage = null;
      // Clear form fields when switching modes
      _usernameController.clear();
      _passwordController.clear();
      _nicknameController.clear();
    });
  }

  /// Validator for username field
  /// Username must be 3-20 characters and is required
  String? _validateUsername(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Username is required';
    }
    if (value.trim().length < 3) {
      return 'Username must be at least 3 characters';
    }
    if (value.trim().length > 20) {
      return 'Username must be no more than 20 characters';
    }
    return null;
  }

  /// Validator for password field
  /// Password must be 6-20 characters and is required
  String? _validatePassword(String? value) {
    if (value == null || value.isEmpty) {
      return 'Password is required';
    }
    if (value.length < 6) {
      return 'Password must be at least 6 characters';
    }
    if (value.length > 20) {
      return 'Password must be no more than 20 characters';
    }
    return null;
  }

  /// Validator for nickname field (registration only)
  /// Nickname must be no more than 50 characters and is required
  String? _validateNickname(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Nickname is required';
    }
    if (value.trim().length > 50) {
      return 'Nickname must be no more than 50 characters';
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24.0),
          child: ConstrainedBox(
            constraints: BoxConstraints(
              minHeight: MediaQuery.of(context).size.height -
                  MediaQuery.of(context).padding.top,
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // App icon
                const Icon(
                  Icons.chat_bubble_outline,
                  size: 80,
                  color: Colors.blue,
                ),
                const SizedBox(height: 24),

                // App title
                Text(
                  'Vibe Chat',
                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: Colors.blue,
                      ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 8),

                // Subtitle
                Text(
                  _isLoginMode ? 'Sign in to continue' : 'Create an account',
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        color: Colors.grey[600],
                      ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 32),

                // Form
                Form(
                  key: _formKey,
                  child: Column(
                    children: [
                      // Username field
                      TextFormField(
                        controller: _usernameController,
                        decoration: const InputDecoration(
                          labelText: 'Username',
                          hintText: 'Enter your username',
                          prefixIcon: Icon(Icons.person_outline),
                          border: OutlineInputBorder(),
                        ),
                        textInputAction: TextInputAction.next,
                        validator: _validateUsername,
                        enabled: !_isLoading,
                      ),
                      const SizedBox(height: 16),

                      // Password field
                      TextFormField(
                        controller: _passwordController,
                        decoration: const InputDecoration(
                          labelText: 'Password',
                          hintText: 'Enter your password',
                          prefixIcon: Icon(Icons.lock_outline),
                          border: OutlineInputBorder(),
                        ),
                        obscureText: true,
                        textInputAction: _isLoginMode
                            ? TextInputAction.done
                            : TextInputAction.next,
                        validator: _validatePassword,
                        enabled: !_isLoading,
                        onFieldSubmitted: _isLoginMode
                            ? (_) => _handleSubmit()
                            : null,
                      ),
                      const SizedBox(height: 16),

                      // Nickname field (registration only)
                      if (!_isLoginMode)
                        TextFormField(
                          controller: _nicknameController,
                          decoration: const InputDecoration(
                            labelText: 'Nickname',
                            hintText: 'Enter your display name',
                            prefixIcon: Icon(Icons.badge_outlined),
                            border: OutlineInputBorder(),
                          ),
                          textInputAction: TextInputAction.done,
                          validator: _validateNickname,
                          enabled: !_isLoading,
                          onFieldSubmitted: (_) => _handleSubmit(),
                        ),

                      const SizedBox(height: 16),

                      // Error message
                      if (_errorMessage != null)
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.red[50],
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: Colors.red[200]!),
                          ),
                          child: Row(
                            children: [
                              Icon(Icons.error_outline, color: Colors.red[700]),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  _errorMessage!,
                                  style: TextStyle(color: Colors.red[700]),
                                ),
                              ),
                            ],
                          ),
                        ),

                      const SizedBox(height: 24),

                      // Submit button
                      ElevatedButton(
                        onPressed: _isLoading ? null : _handleSubmit,
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                        ),
                        child: _isLoading
                            ? const SizedBox(
                                height: 20,
                                width: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  color: Colors.white,
                                ),
                              )
                            : Text(
                                _isLoginMode ? 'Sign In' : 'Sign Up',
                                style: const TextStyle(fontSize: 16),
                              ),
                      ),

                      const SizedBox(height: 16),

                      // Toggle mode button
                      TextButton(
                        onPressed: _isLoading ? null : _toggleMode,
                        child: Text(
                          _isLoginMode
                              ? "Don't have an account? Sign up"
                              : 'Already have an account? Sign in',
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
