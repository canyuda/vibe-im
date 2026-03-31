class Message {
  final String id;
  final String messageId;
  final String senderId;
  final String senderName;
  final String receiverId;
  final String content;
  final String messageType;
  final String status;
  final DateTime createTime;

  Message({
    required this.id,
    required this.messageId,
    required this.senderId,
    required this.senderName,
    required this.receiverId,
    required this.content,
    required this.messageType,
    required this.status,
    required this.createTime,
  });

  factory Message.fromJson(Map<String, dynamic> json) {
    return Message(
      id: json['id'] as String? ?? '',
      messageId: json['messageId'] as String? ?? '',
      senderId: json['senderId'] as String? ?? '',
      senderName: json['senderName'] as String? ?? '',
      receiverId: json['receiverId'] as String? ?? '',
      content: json['content'] as String? ?? '',
      messageType: json['messageType'] as String? ?? 'text',
      status: json['status'] as String? ?? 'sent',
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'] as String)
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'messageId': messageId,
      'senderId': senderId,
      'senderName': senderName,
      'receiverId': receiverId,
      'content': content,
      'messageType': messageType,
      'status': status,
      'createTime': createTime.toIso8601String(),
    };
  }

  Message copyWith({
    String? id,
    String? messageId,
    String? senderId,
    String? senderName,
    String? receiverId,
    String? content,
    String? messageType,
    String? status,
    DateTime? createTime,
  }) {
    return Message(
      id: id ?? this.id,
      messageId: messageId ?? this.messageId,
      senderId: senderId ?? this.senderId,
      senderName: senderName ?? this.senderName,
      receiverId: receiverId ?? this.receiverId,
      content: content ?? this.content,
      messageType: messageType ?? this.messageType,
      status: status ?? this.status,
      createTime: createTime ?? this.createTime,
    );
  }

  /// Check if this message is sent by the current user
  bool get isSent => false;
}
