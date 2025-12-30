"""REPL command processing."""

class ReplManager:
    def __init__(self) -> None:
        self.history = []

    def execute(self, command: str) -> str:
        """Execute a command and return output."""
        self.history.append(command)
        # Placeholder for actual REPL execution.
        return "OK"
