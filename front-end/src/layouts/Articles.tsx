import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ArticlesTable } from "@/components/ArticlesTable";
import { ArticleForm } from "@/components/ArticleForm";
import type { ArticleListItem } from "@/interfaces/article.interface";
import { Info } from "lucide-react";
import { ArticlePreviewModal } from "@/components/ArticlePreviewModal";
import { ReviewModal } from "@/components/ReviewModal";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import { Alert, AlertTitle, AlertDescription } from "@/components/ui/alert";
import { approveArticleThunk, rejectArticleThunk, requestRevisionThunk, getAllArticlesThunk, deleteArticleThunk, submitForReviewThunk, publishArticleThunk } from "@/store/thunks/article.thunk";
import { useSearchParams } from "react-router";
import type { ArticleStatus } from "@/interfaces/article.interface";

export default function ArticlesPage() {
  const dispatch = useAppDispatch();
  const [searchParams] = useSearchParams();
  const filterStatus = searchParams.get('status') as ArticleStatus | null;
  
  const isReviewPage = filterStatus === 'pending_review';

  const [selectedArticle, setSelectedArticle] = useState<ArticleListItem | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [previewArticleId, setPreviewArticleId] = useState<number | null>(null);
  const [reviewModal, setReviewModal] = useState<{ 
    open: boolean; 
    mode: 'REJECT' | 'REVISION'; 
    articleId: number | null;
  }>({
    open: false,
    mode: 'REJECT',
    articleId: null
  });

  const { loading } = useAppSelector((state) => state.articles);



  const handleEdit = (article: ArticleListItem) => {
    setSelectedArticle(article);
    setIsFormOpen(true);
  };

  const handleView = (article: ArticleListItem) => {
    setPreviewArticleId(article.id);
  };

  const handleSuccess = () => {
    setIsFormOpen(false);
    setSelectedArticle(null);
  };

  const handleCancel = () => {
    setIsFormOpen(false);
    setSelectedArticle(null);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm("Bạn có chắc chắn muốn xóa bài viết này?")) {
      await dispatch(deleteArticleThunk(id));
      refreshData();
    }
  };

  const handleApprove = async (id: number) => {
    if (window.confirm("Bạn có chắc chắn muốn duyệt bài viết này?")) {
      const result = await dispatch(approveArticleThunk(id));
      if (approveArticleThunk.fulfilled.match(result)) {
        setPreviewArticleId(null);
        refreshData();
      }
    }
  };

  const handleReviewConfirm = async (reason: string) => {
    if (!reviewModal.articleId) return;

    let result;
    if (reviewModal.mode === 'REJECT') {
      result = await dispatch(rejectArticleThunk({ id: reviewModal.articleId, reason }));
    } else {
      result = await dispatch(requestRevisionThunk({ id: reviewModal.articleId, reason }));
    }

    if (rejectArticleThunk.fulfilled.match(result) || requestRevisionThunk.fulfilled.match(result)) {
      setReviewModal({ ...reviewModal, open: false });
      setPreviewArticleId(null);
      refreshData();
    }
  };

  const handleSubmitForReview = async (id: number) => {
    if (window.confirm("Bạn có chắc chắn muốn gửi bài viết này để duyệt?")) {
      const result = await dispatch(submitForReviewThunk(id));
      if (submitForReviewThunk.fulfilled.match(result)) {
        refreshData();
      }
    }
  };

  const handlePublish = async (id: number) => {
    if (window.confirm("Bạn có chắc chắn muốn công khai bài viết này?")) {
      const result = await dispatch(publishArticleThunk(id));
      if (publishArticleThunk.fulfilled.match(result)) {
        setPreviewArticleId(null);
        refreshData();
      }
    }
  };

  const refreshData = () => {
    dispatch(getAllArticlesThunk({ 
      page: 0, 
      size: 10, 
      status: filterStatus || undefined,
      sort: 'createdAt',
      direction: 'desc'
    }));
  };

  return (
    <main className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            {isReviewPage ? "Duyệt bài viết" : "Quản lý bài viết"}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isReviewPage 
              ? <span className="flex items-center gap-2 italic">
                  <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800 uppercase tracking-wider animate-pulse">
                    Chế độ kiểm duyệt
                  </span>
                  "Xem xét và phê duyệt các bài báo khoa học đang chờ xử lý."
                </span>
              : "Đăng ký, chỉnh sửa và theo dõi trạng thái các bài báo khoa học."}
          </p>
        </div>

      </div>

      {isReviewPage && (
        <Alert className="bg-amber-50 text-amber-900 border-amber-200">
          <Info className="h-4 w-4 !text-amber-900" />
          <AlertTitle className="font-bold">Bạn đang ở chế độ duyệt bài viết!</AlertTitle>
          <AlertDescription>
            Trong chế độ này, bạn có thể xem xét, phê duyệt hoặc yêu cầu chỉnh sửa các bài viết đang chờ duyệt.
          </AlertDescription>
        </Alert>
      )}

      <ArticlesTable 
        onEdit={handleEdit} 
        onView={handleView} 
        onDelete={handleDelete}
        onApprove={handleApprove}
        onReject={(id) => setReviewModal({ open: true, mode: 'REJECT', articleId: id })}
        onRevision={(id) => setReviewModal({ open: true, mode: 'REVISION', articleId: id })}
        onSubmitForReview={handleSubmitForReview}
        onPublish={handlePublish}
        initialStatus={filterStatus || undefined} 
      />

      {/* Post Editor Modal */}
      <Dialog open={isFormOpen} onOpenChange={setIsFormOpen}>
        <DialogContent className="max-w-6xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {selectedArticle ? "Cập nhật bài viết" : "Tạo bài viết mới"}
            </DialogTitle>
          </DialogHeader>

          <ArticleForm
            article={selectedArticle ?? undefined}
            onSuccess={handleSuccess}
            onCancel={handleCancel}
          />
        </DialogContent>
      </Dialog>

      {/* Article Preview Modal */}
      <ArticlePreviewModal 
        open={!!previewArticleId}
        onOpenChange={(open) => !open && setPreviewArticleId(null)}
        articleId={previewArticleId}
        onApprove={handleApprove}
        onReject={(id) => setReviewModal({ open: true, mode: 'REJECT', articleId: id })}
        onRevision={(id) => setReviewModal({ open: true, mode: 'REVISION', articleId: id })}
        onSubmitForReview={handleSubmitForReview}
        onPublish={handlePublish}
      />

      {/* Shared Review Reason Modal */}
      <ReviewModal 
        open={reviewModal.open}
        onOpenChange={(open) => setReviewModal({ ...reviewModal, open })}
        mode={reviewModal.mode}
        loading={loading}
        onConfirm={handleReviewConfirm}
      />
    </main>
  );
}
