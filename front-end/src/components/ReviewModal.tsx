import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Loader2 } from "lucide-react";

interface ReviewModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: (reason: string) => Promise<void>;
  loading: boolean;
  mode: 'REJECT' | 'REVISION';
  title?: string;
}

export function ReviewModal({ 
  open, 
  onOpenChange, 
  onConfirm, 
  loading, 
  mode,
  title 
}: ReviewModalProps) {
  const [reason, setReason] = useState("");

  useEffect(() => {
    if (open) setReason("");
  }, [open]);

  const handleConfirm = async () => {
    if (!reason.trim()) return;
    await onConfirm(reason);
  };

  const config = {
    REJECT: {
      title: title || "Từ chối bài viết",
      description: "Vui lòng cho biết lý do bài viết này bị từ chối.",
      buttonText: "Xác nhận từ chối",
      variant: "destructive" as const
    },
    REVISION: {
      title: title || "Yêu cầu sửa lại",
      description: "Vui lòng liệt kê các điểm cần tác giả chỉnh sửa.",
      buttonText: "Gửi yêu cầu",
      variant: "default" as const
    }
  };

  const currentConfig = config[mode];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{currentConfig.title}</DialogTitle>
          <DialogDescription>
            {currentConfig.description}
          </DialogDescription>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <Textarea 
            placeholder="Nhập nội dung nhận xét..." 
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            className="min-h-[120px]"
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button 
            variant={currentConfig.variant} 
            onClick={handleConfirm} 
            disabled={loading || !reason.trim()}
          >
            {loading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
            {currentConfig.buttonText}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
