import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../models/contact.dart';

/// Chat list page showing contacts with online status
/// Allows navigation to individual chat conversations
/// Provides logout functionality and add contact feature (TODO)
/// @author AI Agent
class ChatListPage extends StatefulWidget {
  const ChatListPage({super.key});

  @override
  State<ChatListPage> createState() => _ChatListPageState();
}

class _ChatListPageState extends State<ChatListPage> {
  /// List of contacts - TODO: Replace with actual API call
  final List<Contact> _contacts = [
    Contact(
      id: '2',
      username: 'user2',
      nickname: 'User 2',
      avatar: '',
      status: 'online',
      createTime: DateTime.now(),
    ),
    Contact(
      id: '3',
      username: 'user3',
      nickname: 'User 3',
      avatar: '',
      status: 'offline',
      createTime: DateTime.now(),
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('消息'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: _showLogoutDialog,
            tooltip: '退出登录',
          ),
        ],
      ),
      body: _contacts.isEmpty
          ? _buildEmptyState()
          : ListView.builder(
              itemCount: _contacts.length,
              itemBuilder: (context, index) {
                return _buildContactTile(_contacts[index]);
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddContactDialog,
        tooltip: '添加好友',
        child: const Icon(Icons.add),
      ),
    );
  }

  /// Build empty state when no contacts are available
  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.person_outline,
            size: 64,
            color: Colors.grey[400],
          ),
          const SizedBox(height: 16),
          Text(
            '暂无联系人',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '点击右下角按钮添加好友',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[500],
            ),
          ),
        ],
      ),
    );
  }

  /// Build a contact list tile with online status indicator
  /// @param contact The contact to display
  Widget _buildContactTile(Contact contact) {
    final isOnline = contact.isOnline;

    return ListTile(
      leading: CircleAvatar(
        backgroundColor: isOnline ? Colors.green : Colors.grey,
        child: Text(
          contact.nickname.isNotEmpty
              ? contact.nickname[0].toUpperCase()
              : contact.username[0].toUpperCase(),
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      title: Text(
        contact.nickname,
        style: const TextStyle(
          fontWeight: FontWeight.w500,
        ),
      ),
      subtitle: Text(
        isOnline ? '在线' : '离线',
        style: TextStyle(
          color: isOnline ? Colors.green : Colors.grey,
        ),
      ),
      trailing: const Icon(Icons.chevron_right),
      onTap: () => _navigateToChat(contact),
    );
  }

  /// Navigate to chat page with contact information
  /// @param contact The contact to chat with
  void _navigateToChat(Contact contact) {
    // TODO: Navigate to ChatPage with userId, userName, userNickname
    // Navigator.push(
    //   context,
    //   MaterialPageRoute(
    //     builder: (context) => ChatPage(
    //       userId: contact.id,
    //       userName: contact.username,
    //       userNickname: contact.nickname,
    //     ),
    //   ),
    // );
  }

  /// Show logout confirmation dialog
  void _showLogoutDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.of(context).pop();
              await _performLogout();
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  /// Perform logout operation
  /// Calls AuthProvider.logout() and navigates to login page
  Future<void> _performLogout() async {
    final authProvider = context.read<AuthProvider>();
    await authProvider.logout();

    if (mounted) {
      // Navigate to login page
      Navigator.of(context).pushReplacementNamed('/');
    }
  }

  /// Show add contact dialog
  /// TODO: Implement actual add contact functionality
  void _showAddContactDialog() {
    // TODO: Implement add contact dialog with form
    // For now, show a SnackBar indicating feature not implemented
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('添加好友功能待实现'),
        backgroundColor: Colors.orange,
      ),
    );
  }
}
