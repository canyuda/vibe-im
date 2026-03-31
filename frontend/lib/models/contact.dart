/// Contact model representing a user contact with online status
/// Used for displaying contacts in the chat list
/// @author AI Agent
class Contact {
  final String id;
  final String username;
  final String nickname;
  final String avatar;
  final String status;
  final DateTime createTime;

  Contact({
    required this.id,
    required this.username,
    required this.nickname,
    required this.avatar,
    required this.status,
    required this.createTime,
  });

  /// Check if the contact is online based on status
  bool get isOnline => status.toLowerCase() == 'online';

  factory Contact.fromJson(Map<String, dynamic> json) {
    return Contact(
      id: json['id'] as String? ?? '',
      username: json['username'] as String? ?? '',
      nickname: json['nickname'] as String? ?? '',
      avatar: json['avatar'] as String? ?? '',
      status: json['status'] as String? ?? 'offline',
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'] as String)
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'nickname': nickname,
      'avatar': avatar,
      'status': status,
      'createTime': createTime.toIso8601String(),
    };
  }

  Contact copyWith({
    String? id,
    String? username,
    String? nickname,
    String? avatar,
    String? status,
    DateTime? createTime,
  }) {
    return Contact(
      id: id ?? this.id,
      username: username ?? this.username,
      nickname: nickname ?? this.nickname,
      avatar: avatar ?? this.avatar,
      status: status ?? this.status,
      createTime: createTime ?? this.createTime,
    );
  }
}
