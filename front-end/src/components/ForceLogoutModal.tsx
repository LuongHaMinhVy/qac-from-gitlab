interface ForceLogoutModalProps {
  open: boolean;
  message: string;
  onConfirm: () => void;
}

export default function ForceLogoutModal({ open, message, onConfirm }: ForceLogoutModalProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-[100]">
      <div className="bg-white p-6 rounded-lg shadow-lg text-center max-w-sm">
        <h2 className="text-xl font-bold mb-2">Thông báo</h2>
        <p className="mb-4">{message}</p>
        <button
          onClick={onConfirm}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
        >
          Đăng nhập lại
        </button>
      </div>
    </div>
  );
}
