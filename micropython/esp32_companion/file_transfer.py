"""File transfer handlers."""

class FileTransferManager:
    def __init__(self) -> None:
        self.active_transfers = {}

    def start_upload(self, path: str, transfer_id: int) -> None:
        self.active_transfers[transfer_id] = {
            "path": path,
            "offset": 0,
        }

    def handle_chunk(self, transfer_id: int, data: bytes, is_last: bool) -> None:
        transfer = self.active_transfers.get(transfer_id)
        if not transfer:
            return
        transfer["offset"] += len(data)
        if is_last:
            self.active_transfers.pop(transfer_id, None)

    def start_download(self, path: str, transfer_id: int) -> None:
        self.active_transfers[transfer_id] = {
            "path": path,
            "offset": 0,
        }
