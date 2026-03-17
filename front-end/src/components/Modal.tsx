import { useEffect } from "react";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  message: string;
  type: "success" | "error";
  autoClose?: boolean;
  autoCloseDelay?: number;
}

export default function Modal({
  isOpen,
  onClose,
  title,
  message,
  type,
  autoClose = false,
  autoCloseDelay = 3000,
}: ModalProps) {
  useEffect(() => {
    if (isOpen && autoClose) {
      const timer = setTimeout(() => {
        onClose();
      }, autoCloseDelay);
      return () => clearTimeout(timer);
    }
  }, [isOpen, autoClose, autoCloseDelay, onClose]);

  if (!isOpen) return null;

  const bgColor = type === "success" ? "bg-green-50" : "bg-red-50";
  const borderColor = type === "success" ? "border-green-200" : "border-red-200";
  const textColor = type === "success" ? "text-green-800" : "text-red-800";
  const iconColor = type === "success" ? "text-green-400" : "text-red-400";
  const buttonColor = type === "success" 
    ? "bg-green-600 hover:bg-green-700" 
    : "bg-red-600 hover:bg-red-700";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className={`${bgColor} ${borderColor} border-2 rounded-lg shadow-xl p-6 max-w-md w-full mx-4`}>
        <div className="flex items-start">
          <div className={`flex-shrink-0 ${iconColor}`}>
            {type === "success" ? (
              <svg
                className="h-6 w-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            ) : (
              <svg
                className="h-6 w-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            )}
          </div>
          <div className="ml-4 flex-1">
            <h3 className={`text-lg font-semibold ${textColor} mb-2`}>
              {title}
            </h3>
            <p className={`text-sm ${textColor}`}>{message}</p>
          </div>
        </div>
        <div className="mt-4 flex justify-end">
          <button
            onClick={onClose}
            className={`px-4 py-2 rounded-lg text-white font-medium ${buttonColor} focus:outline-none focus:ring-2 focus:ring-offset-2 ${
              type === "success" ? "focus:ring-green-500" : "focus:ring-red-500"
            }`}
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}

