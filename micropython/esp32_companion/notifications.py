"""Notification system."""

import time

class NotificationManager:
    def __init__(self) -> None:
        self.queue = []

    def push(self, message: str, level: int = 0) -> dict:
        notification = {
            "timestamp_ms": int(time.time() * 1000),
            "level": level,
            "message": message,
        }
        self.queue.append(notification)
        return notification

    def pop_all(self) -> list:
        notifications = list(self.queue)
        self.queue.clear()
        return notifications
