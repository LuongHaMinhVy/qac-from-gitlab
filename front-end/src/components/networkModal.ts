
let networkModalShown = false;

export function resetNetworkModalFlag() {
  networkModalShown = false;
}

export function showNetworkModal(options: {
  title?: string;
  message: string;
  confirmText?: string;
  onConfirm: () => void;
}) {
  if (networkModalShown) return;
  networkModalShown = true;

  const {
    title = "Mất kết nối máy chủ",
    message,
    confirmText = "OK",
    onConfirm,
  } = options;

  const overlay = document.createElement("div");
  overlay.style.position = "fixed";
  overlay.style.inset = "0";
  overlay.style.background = "rgba(0,0,0,0.45)";
  overlay.style.display = "flex";
  overlay.style.alignItems = "center";
  overlay.style.justifyContent = "center";
  overlay.style.zIndex = "99999";

  const box = document.createElement("div");
  box.style.width = "min(520px, 92vw)";
  box.style.background = "#fff";
  box.style.borderRadius = "14px";
  box.style.padding = "18px 18px 14px";
  box.style.boxShadow = "0 10px 30px rgba(0,0,0,0.25)";
  box.style.fontFamily =
    "system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif";

  const titleEl = document.createElement("div");
  titleEl.textContent = title;
  titleEl.style.fontSize = "18px";
  titleEl.style.fontWeight = "700";
  titleEl.style.marginBottom = "8px";

  const desc = document.createElement("div");
  desc.textContent = message;
  desc.style.fontSize = "14px";
  desc.style.lineHeight = "1.45";
  desc.style.marginBottom = "14px";

  const btn = document.createElement("button");
  btn.textContent = confirmText;
  btn.style.width = "100%";
  btn.style.border = "none";
  btn.style.borderRadius = "10px";
  btn.style.padding = "10px 12px";
  btn.style.cursor = "pointer";
  btn.style.fontWeight = "700";
  btn.style.background = "#111";
  btn.style.color = "#fff";

  btn.onclick = () => {
    overlay.remove();
    try {
      onConfirm();
    } finally {
      // Nếu bạn muốn sau khi confirm mà modal có thể hiện lại trong tương lai
      // thì bật dòng dưới (không bắt buộc)
      // resetNetworkModalFlag();
    }
  };

  // Optional: click ngoài cũng không tắt để user đọc rõ
  // overlay.onclick = () => {}

  box.appendChild(titleEl);
  box.appendChild(desc);
  box.appendChild(btn);
  overlay.appendChild(box);
  document.body.appendChild(overlay);
}
