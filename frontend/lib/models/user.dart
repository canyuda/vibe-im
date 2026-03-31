class User {
  final String id;
  final String username;
  final String nickname;
  final String avatar;
  final String status;
  final DateTime createTime;

  User({
    required this.id,
    required this.username,
    required this.nickname,
    required this.avatar,
    required this.status,
    required this.createTime,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
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

  /// Check if this is the current user (for later use in message display)
  bool get isSent => false;

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

  User copyWith({
    String? id,
    String? username,
    String? nickname,
    String? avatar,
    String? status,
    DateTime? createTime,
  }) {
    return User(
      id: id ?? this.id,
      username: username ?? this.username,
      nickname: nickname ?? this.nickname,
      avatar: avatar ?? this.avatar,
      status: status ?? this.status,
      createTime: createTime ?? this.createTime,
    );
  }
}
