import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Card } from "@/components/ui/card";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import {
  getAllMediaThunk,
  uploadMediaThunk,
  deleteMediaThunk,
  restoreMediaThunk,
} from "@/store/thunks/media.thunk";
import { Upload, Search, Trash2, RotateCcw, Loader2, Image as ImageIcon, FileText, Film } from "lucide-react";
import { toast } from "sonner";

export default function MediaPage() {
  const dispatch = useAppDispatch();
  const { library, loading, uploading, pagination } = useAppSelector((state) => state.media);
  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  useEffect(() => {
    dispatch(getAllMediaThunk({ page: 0, size: 20 }));
  }, [dispatch]);

  const handleSearch = () => {
    dispatch(getAllMediaThunk({ page: 0, size: 20, keyword: searchKeyword }));
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.error("Vui lòng chọn file");
      return;
    }

    const result = await dispatch(uploadMediaThunk(selectedFile));
    if (uploadMediaThunk.fulfilled.match(result)) {
      const media = result.payload.data;
      toast.success(
        media.deduplicated
          ? "✨ Đã dùng lại file từ thư viện"
          : "✅ Tải file lên thành công"
      );
      setIsUploadOpen(false);
      setSelectedFile(null);
      setPreviewUrl(null);
      dispatch(getAllMediaThunk({ page: 0, size: 20 }));
    } else {
      toast.error("Không thể tải file lên");
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Xóa file này? (Có thể khôi phục sau)")) return;
    
    const result = await dispatch(deleteMediaThunk(id));
    if (deleteMediaThunk.fulfilled.match(result)) {
      toast.success("Đã xóa file");
      dispatch(getAllMediaThunk({ page: 0, size: 20 }));
    }
  };

  const handleRestore = async (id: number) => {
    const result = await dispatch(restoreMediaThunk(id));
    if (restoreMediaThunk.fulfilled.match(result)) {
      toast.success("Đã khôi phục file");
      dispatch(getAllMediaThunk({ page: 0, size: 20 }));
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      if (file.type.startsWith("image/")) {
        setPreviewUrl(URL.createObjectURL(file));
      }
    }
  };

  const getFileIcon = (mimeType: string) => {
    if (mimeType.startsWith("image/")) return <ImageIcon className="h-8 w-8" />;
    if (mimeType.startsWith("video/")) return <Film className="h-8 w-8" />;
    return <FileText className="h-8 w-8" />;
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  return (
    <main className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Thư viện Media</h1>
          <p className="text-muted-foreground mt-1">
            Quản lý ảnh, video và file đính kèm
          </p>
        </div>

        <Button size="lg" onClick={() => setIsUploadOpen(true)} className="w-full md:w-auto">
          <Upload className="h-5 w-5 mr-2" />
          Tải file lên
        </Button>
      </div>

      <Card className="p-4">
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm theo tên file..."
              className="pl-10"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleSearch()}
            />
          </div>
          <Button onClick={handleSearch}>Tìm kiếm</Button>
        </div>
      </Card>

      {loading && library.length === 0 ? (
        <div className="flex h-64 items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
            {library.map((media) => (
              <Card
                key={media.id}
                className={`group relative overflow-hidden hover:shadow-lg transition-shadow ${
                  media.isDeleted ? "opacity-50" : ""
                }`}
              >
                <div className="aspect-square bg-muted flex items-center justify-center overflow-hidden">
                  {media.mimeType.startsWith("image/") ? (
                    <img
                      src={media.fileUrl}
                      alt={media.fileName}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="text-muted-foreground">
                      {getFileIcon(media.mimeType)}
                    </div>
                  )}
                </div>

                <div className="p-3 space-y-2">
                  <p className="text-sm font-medium truncate" title={media.fileName}>
                    {media.fileName}
                  </p>
                  <div className="text-xs text-muted-foreground space-y-1">
                    <p>{formatFileSize(media.fileSize)}</p>
                    <p>
                      {media.uploader?.username || "Unknown"}
                    </p>
                  </div>
                </div>

                <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1">
                  {media.isDeleted ? (
                    <Button
                      size="icon"
                      variant="secondary"
                      className="h-8 w-8"
                      onClick={() => handleRestore(media.id)}
                    >
                      <RotateCcw className="h-4 w-4" />
                    </Button>
                  ) : (
                    <Button
                      size="icon"
                      variant="destructive"
                      className="h-8 w-8"
                      onClick={() => handleDelete(media.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </div>

                {media.isDeleted && (
                  <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                    <span className="text-white text-sm font-medium">Đã xóa</span>
                  </div>
                )}
              </Card>
            ))}
          </div>

          {library.length === 0 && (
            <div className="text-center py-12 text-muted-foreground">
              <ImageIcon className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>Chưa có file nào trong thư viện</p>
            </div>
          )}

          {pagination && pagination.totalPages > 1 && (
            <div className="flex items-center justify-between pt-6 border-t">
              <p className="text-sm text-muted-foreground">
                Hiển thị {pagination.currentPage * pagination.pageSize + 1} -{" "}
                {Math.min(
                  (pagination.currentPage + 1) * pagination.pageSize,
                  pagination.totalElements
                )}{" "}
                trong số {pagination.totalElements}
              </p>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={pagination.currentPage === 0}
                  onClick={() =>
                    dispatch(
                      getAllMediaThunk({
                        page: pagination.currentPage - 1,
                        size: 20,
                        keyword: searchKeyword,
                      })
                    )
                  }
                >
                  Trước
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={pagination.currentPage >= pagination.totalPages - 1}
                  onClick={() =>
                    dispatch(
                      getAllMediaThunk({
                        page: pagination.currentPage + 1,
                        size: 20,
                        keyword: searchKeyword,
                      })
                    )
                  }
                >
                  Sau
                </Button>
              </div>
            </div>
          )}
        </>
      )}

      <Dialog open={isUploadOpen} onOpenChange={setIsUploadOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Tải file lên</DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            <div className="border-2 border-dashed rounded-lg p-8 text-center">
              <input
                type="file"
                id="file-upload"
                className="hidden"
                onChange={handleFileSelect}
                accept="image/*,video/*"
              />
              <label
                htmlFor="file-upload"
                className="cursor-pointer flex flex-col items-center gap-2"
              >
                <Upload className="h-12 w-12 text-muted-foreground" />
                <p className="text-sm font-medium">
                  {selectedFile ? selectedFile.name : "Click để chọn file"}
                </p>
                <p className="text-xs text-muted-foreground">
                  Hỗ trợ: Ảnh, Video
                </p>
              </label>
            </div>

            {previewUrl && (
              <div className="rounded-lg overflow-hidden border">
                <img src={previewUrl} alt="Preview" className="w-full" />
              </div>
            )}

            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  setIsUploadOpen(false);
                  setSelectedFile(null);
                  setPreviewUrl(null);
                }}
              >
                Hủy
              </Button>
              <Button onClick={handleUpload} disabled={!selectedFile || uploading}>
                {uploading ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Đang tải...
                  </>
                ) : (
                  "Tải lên"
                )}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </main>
  );
}
