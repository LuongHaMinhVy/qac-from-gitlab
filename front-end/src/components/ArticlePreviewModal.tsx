import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "./ui/dialog";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { 
  Loader2, 
  CheckCircle, 
  XCircle, 
  Edit3, 
  Calendar, 
  User, 
  Tag,
  History,
  MessageSquare,
  ArrowRight,
  Send,
  Globe
} from "lucide-react";
import { getArticleByIdOrSlug, getReviewLogs } from "@/apis/article.apis";
import type { ArticleDetail } from "@/interfaces/article.interface";
import type { ApprovalResponse } from "@/interfaces/approval.interface";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { format } from "date-fns";
import { vi } from "date-fns/locale";
import type { ArticleStatus } from "@/interfaces/article.interface";

const statusConfig: Record<
  ArticleStatus,
  { label: string; variant: "default" | "secondary" | "outline" | "destructive" }
> = {
  published: { label: "Đã xuất bản", variant: "default" },
  draft: { label: "Nháp", variant: "secondary" },
  pending_review: { label: "Chờ duyệt", variant: "outline" },
  approved: { label: "Đã duyệt", variant: "outline" },
  rejected: { label: "Từ chối", variant: "destructive" },
  needs_revision: { label: "Cần sửa lại", variant: "secondary" },
};

interface ArticlePreviewModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  articleId: number | null;
  onApprove: (id: number) => void;
  onReject: (id: number) => void;
  onRevision: (id: number) => void;
  onSubmitForReview?: (id: number) => void;
  onPublish?: (id: number) => void;
}

export function ArticlePreviewModal({
  open,
  onOpenChange,
  articleId,
  onApprove,
  onReject,
  onRevision,
  onSubmitForReview,
  onPublish
}: ArticlePreviewModalProps) {
  const [article, setArticle] = useState<ArticleDetail | null>(null);
  const [logs, setLogs] = useState<ApprovalResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingLogs, setLoadingLogs] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open && articleId) {
      fetchArticle();
      fetchLogs();
    } else {
      setArticle(null);
      setLogs([]);
      setError(null);
    }
  }, [open, articleId]);

  const fetchLogs = async () => {
    if (!articleId) return;
    setLoadingLogs(true);
    try {
      const response = await getReviewLogs(articleId);
      setLogs(response.data);
    } catch (err) {
      console.error("Failed to fetch logs:", err);
    } finally {
      setLoadingLogs(false);
    }
  };

  const fetchArticle = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getArticleByIdOrSlug(articleId!);
      setArticle(response.data);
    } catch (err: any) {
      setError(err.message || "Không thể tải nội dung bài viết");
    } finally {
      setLoading(false);
    }
  };

  if (!open) return null;
  
  const canReview = article?.status === 'pending_review';
  const canSubmit = article?.status === 'draft' || article?.status === 'needs_revision';
  const canPublish = article?.status === 'approved';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] flex flex-col p-0 overflow-hidden">
        <DialogHeader className="p-6 border-b">
          <div className="flex items-center justify-between gap-4">
            <DialogTitle className="text-2xl font-bold line-clamp-2">
              {loading ? "Đang tải bài viết..." : article?.title || "Xem trước bài viết"}
            </DialogTitle>
            {!loading && article && (
              <Badge variant={statusConfig[article.status]?.variant || "outline"}>
                {statusConfig[article.status]?.label || article.status}
              </Badge>
            )}
          </div>
        </DialogHeader>

        <Tabs defaultValue="content" className="flex-1 flex flex-col overflow-hidden">
          <div className="px-6 border-b">
            <TabsList className="h-12 w-full justify-start bg-transparent gap-6">
              <TabsTrigger 
                value="content" 
                className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent px-2"
              >
                Nội dung bài viết
              </TabsTrigger>
              <TabsTrigger 
                value="history"
                className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent px-2"
              >
                Lịch sử duyệt {logs.length > 0 && `(${logs.length})`}
              </TabsTrigger>
            </TabsList>
          </div>

          <TabsContent value="content" className="flex-1 overflow-y-auto p-6 m-0">
            {loading ? (
              <div className="flex h-64 items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
              </div>
            ) : error ? (
              <div className="text-center py-12 text-destructive">
                <p>{error}</p>
                <Button variant="outline" className="mt-4" onClick={fetchArticle}>Thử lại</Button>
              </div>
            ) : article ? (
              <article className="prose prose-slate max-w-none dark:prose-invert">
                <div className="flex flex-wrap gap-4 mb-8 text-sm text-muted-foreground border-b pb-4">
                  <div className="flex items-center gap-1.5">
                    <User className="h-4 w-4" />
                    {article.authorName}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Calendar className="h-4 w-4" />
                    {format(new Date(article.createdAt), "dd/MM/yyyy HH:mm", { locale: vi })}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Tag className="h-4 w-4" />
                    {article.categoryName}
                  </div>
                </div>

                {article.featuredImage && (
                  <div className="mb-8 rounded-lg overflow-hidden border">
                    <img 
                      src={article.featuredImage.fileUrl} 
                      alt={article.title}
                      className="w-full aspect-video object-cover"
                    />
                  </div>
                )}

                <div className="font-semibold text-lg mb-6 text-slate-700 dark:text-slate-300 italic border-l-4 border-primary pl-4">
                  {article.excerpt}
                </div>

                <div 
                  className="article-content"
                  dangerouslySetInnerHTML={{ __html: article.content }} 
                />
                
                {article.hashtag && (
                  <div className="mt-8 pt-4 border-t flex gap-2">
                    {article.hashtag.split(',').map(tag => (
                      <Badge key={tag} variant="secondary" className="px-2 py-0">#{tag.trim()}</Badge>
                    ))}
                  </div>
                )}
              </article>
            ) : null}
          </TabsContent>

          <TabsContent value="history" className="flex-1 overflow-y-auto p-6 m-0 bg-muted/10">
            {loadingLogs ? (
              <div className="flex h-64 items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
              </div>
            ) : logs.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                <History className="h-12 w-12 mb-4 opacity-20" />
                <p>Chưa có lịch sử phê duyệt cho bài viết này</p>
              </div>
            ) : (
              <div className="relative space-y-6 before:absolute before:inset-0 before:ml-5 before:-translate-x-px before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-slate-300 before:to-transparent">
                {logs.map((log) => (
                  <div key={log.id} className="relative flex items-start gap-6 group">
                    <div className="absolute left-0 w-10 h-10 flex items-center justify-center rounded-full bg-white border-2 border-primary ring-4 ring-muted group-hover:scale-110 transition-transform z-10">
                      <History className="h-5 w-5 text-primary" />
                    </div>
                    <div className="flex-1 ml-12 bg-white rounded-xl p-4 shadow-sm border border-slate-100 hover:shadow-md transition-shadow">
                      <div className="flex items-center justify-between mb-2">
                        <span className="font-bold text-slate-800">{log.reviewerName}</span>
                        <time className="text-xs text-muted-foreground">
                          {format(new Date(log.createdAt), "dd/MM/yyyy HH:mm", { locale: vi })}
                        </time>
                      </div>
                      
                      <div className="flex items-center gap-2 mb-3">
                        <Badge variant={statusConfig[log.oldStatus]?.variant || "outline"} className="opacity-70 scale-90">
                          {statusConfig[log.oldStatus]?.label || log.oldStatus}
                        </Badge>
                        <ArrowRight className="h-3 w-3 text-muted-foreground" />
                        <Badge variant={statusConfig[log.newStatus]?.variant || "outline"}>
                          {statusConfig[log.newStatus]?.label || log.newStatus}
                        </Badge>
                      </div>

                      {log.reason && (
                        <div className="bg-slate-50 rounded-lg p-3 text-sm text-slate-600 flex gap-2 border border-slate-100">
                          <MessageSquare className="h-4 w-4 shrink-0 mt-0.5 text-primary/40" />
                          <p className="italic">"{log.reason}"</p>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </TabsContent>
        </Tabs>

        <DialogFooter className="p-4 border-t bg-muted/30 flex items-center justify-between sm:justify-between w-full">
          <Button variant="outline" onClick={() => onOpenChange(false)}>Đóng</Button>
          
          <div className="flex gap-2">
            {!loading && article && canSubmit && (
              <Button 
                variant="outline" 
                className="text-blue-600 border-blue-200 hover:bg-blue-50"
                onClick={() => onSubmitForReview?.(article.id)}
              >
                <Send className="h-4 w-4 mr-2" />
                Gửi duyệt
              </Button>
            )}

            {!loading && article && canPublish && (
              <Button 
                className="bg-blue-600 hover:bg-blue-700 text-white"
                onClick={() => onPublish?.(article.id)}
              >
                <Globe className="h-4 w-4 mr-2" />
                Công khai
              </Button>
            )}

            {!loading && article && canReview && (
              <>
                <Button 
                  variant="outline" 
                  className="text-orange-600 border-orange-200 hover:bg-orange-50"
                  onClick={() => onRevision(article.id)}
                >
                  <Edit3 className="h-4 w-4 mr-2" />
                  Yêu cầu sửa
                </Button>
                <Button 
                  variant="outline" 
                  className="text-destructive border-destructive/20 hover:bg-destructive/10"
                  onClick={() => onReject(article.id)}
                >
                  <XCircle className="h-4 w-4 mr-2" />
                  Từ chối
                </Button>
                <Button 
                  className="bg-green-600 hover:bg-green-700 text-white"
                  onClick={() => onApprove(article.id)}
                >
                  <CheckCircle className="h-4 w-4 mr-2" />
                  Duyệt bài
                </Button>
              </>
            )}
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
