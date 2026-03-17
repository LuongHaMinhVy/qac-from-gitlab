import { useEffect } from "react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Badge } from "../components/ui/badge";
import { MoreHorizontal, Eye, Edit, Trash2, Loader2, CheckCircle, XCircle, Filter, Send, Globe } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from "../components/ui/dropdown-menu";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../components/ui/select";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import { getAllArticlesThunk } from "@/store/thunks/article.thunk";
import type { ArticleStatus } from "@/interfaces/article.interface";
import { format } from "date-fns";
import { vi } from "date-fns/locale";
import { useState } from "react";

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

interface ArticlesTableProps {
  onEdit?: (article: any) => void;
  onView?: (article: any) => void;
  onDelete?: (id: number) => void;
  onApprove?: (id: number) => void;
  onReject?: (id: number) => void;
  onRevision?: (id: number) => void;
  onSubmitForReview?: (id: number) => void;
  onPublish?: (id: number) => void;
  initialStatus?: ArticleStatus;
}

export function ArticlesTable({ 
  onEdit, 
  onView, 
  onDelete, 
  onApprove, 
  onReject, 
  onRevision, 
  onSubmitForReview,
  onPublish,
  initialStatus 
}: ArticlesTableProps) {
  const dispatch = useAppDispatch();
  const { articles, loading, error, pagination } = useAppSelector((state) => state.articles);
  const [statusFilter, setStatusFilter] = useState<ArticleStatus | "all">(initialStatus || "all");

  useEffect(() => {
    dispatch(
      getAllArticlesThunk({
        page: 0,
        size: 10,
        sort: "createdAt",
        direction: "desc",
        status: statusFilter === "all" ? undefined : statusFilter
      })
    );
  }, [dispatch, statusFilter, initialStatus]);



  if (loading && articles.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (error) {
    return (
      <Card className="p-6 text-center text-destructive">
        <p>Lỗi: {error}</p>
        <Button 
          variant="outline" 
          className="mt-4"
          onClick={() => dispatch(getAllArticlesThunk({ page: 0, size: 10, sort: "createdAt", direction: "desc" }))}
        >
          Thử lại
        </Button>
      </Card>
    );
  }

  return (
    <Card className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="font-semibold text-lg">Bài viết gần đây</h3>
          <p className="text-sm text-muted-foreground">
            Quản lý và theo dõi các bài viết trong hệ thống
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Select 
            value={statusFilter} 
            onValueChange={(val) => setStatusFilter(val as any)}
          >
            <SelectTrigger className="w-[180px]">
              <Filter className="h-4 w-4 mr-2" />
              <SelectValue placeholder="Trạng thái" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Tất cả bài viết</SelectItem>
              <SelectItem value="pending_review">Chờ duyệt</SelectItem>
              <SelectItem value="approved">Đã duyệt</SelectItem>
              <SelectItem value="published">Đã xuất bản</SelectItem>
              <SelectItem value="rejected">Từ chối</SelectItem>
              <SelectItem value="needs_revision">Cần sửa lại</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border text-xs uppercase tracking-wider">
              <th className="text-left py-3 px-4 font-medium text-muted-foreground">
                Tiêu đề
              </th>
              <th className="text-left py-3 px-4 font-medium text-muted-foreground">
                Tác giả
              </th>
              <th className="text-left py-3 px-4 font-medium text-muted-foreground">
                Danh mục
              </th>
              <th className="text-left py-3 px-4 font-medium text-muted-foreground">
                Trạng thái
              </th>
              <th className="text-left py-3 px-4 font-medium text-muted-foreground">
                Thống kê
              </th>
              <th className="text-left py-3 px-4 font-medium text-muted-foreground">
                Ngày tạo
              </th>
              <th className="text-right py-3 px-4 font-medium text-muted-foreground">
                Hành động
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {articles.length === 0 ? (
              <tr>
                <td colSpan={7} className="py-8 text-center text-muted-foreground">
                  Không tìm thấy bài viết nào
                </td>
              </tr>
            ) : (
              articles.map((article) => {
                const canReview = article.status === 'pending_review';
                const canSubmit = article.status === 'draft' || article.status === 'needs_revision';
                const canPublish = article.status === 'approved';
                
                return (
                <tr
                  key={article.id}
                  className="hover:bg-muted/50 transition-colors group"
                >
                  <td className="py-4 px-4 max-w-xs lg:max-w-md">
                    <p className="font-medium text-sm line-clamp-2" title={article.title}>
                      {article.title}
                    </p>
                    <p className="text-xs text-muted-foreground mt-1 line-clamp-1">
                      {article.slug}
                    </p>
                  </td>
                  <td className="py-4 px-4 text-sm text-muted-foreground">
                    {article.authorName}
                  </td>
                  <td className="py-4 px-4 text-sm text-muted-foreground whitespace-nowrap">
                    {article.categoryName}
                  </td>
                  <td className="py-4 px-4 whitespace-nowrap">
                    <Badge
                      className="capitalize"
                      variant={statusConfig[article.status]?.variant || "outline"}
                    >
                      {statusConfig[article.status]?.label || article.status}
                    </Badge>
                  </td>
                  <td className="py-4 px-4">
                    <div className="flex flex-col gap-1 text-xs text-muted-foreground whitespace-nowrap">
                      <div className="flex items-center gap-1.5">
                        <Eye className="h-3 w-3" />
                        {article.viewCount.toLocaleString()} xem
                      </div>
                      {article.publishedAt && (
                        <div className="text-[10px] text-green-600 font-medium">
                          Đã XB: {format(new Date(article.publishedAt), "dd/MM/yyyy", { locale: vi })}
                        </div>
                      )}
                    </div>
                  </td>
                  <td className="py-4 px-4 text-sm text-muted-foreground whitespace-nowrap">
                    {format(new Date(article.createdAt), "dd/MM/yyyy HH:mm", { locale: vi })}
                  </td>
                  <td className="py-4 px-4">
                    <div className="flex justify-end gap-2">
                      {canReview && (
                        <>
                          <Button 
                            variant="default" 
                            size="sm" 
                            className="bg-emerald-600 hover:bg-emerald-700 text-white h-8 shadow-sm transition-all"
                            onClick={() => onApprove?.(article.id)}
                            title="Phê duyệt bài"
                          >
                            <CheckCircle className="h-4 w-4 mr-1" />
                            Duyệt
                          </Button>
                          <Button 
                            variant="default" 
                            size="sm" 
                            className="bg-rose-600 hover:bg-rose-700 text-white h-8 shadow-sm transition-all"
                            onClick={() => onReject?.(article.id)}
                            title="Từ chối bài"
                          >
                            <XCircle className="h-4 w-4 mr-1" />
                            Từ chối
                          </Button>
                        </>
                      )}
                      
                      {canSubmit && (
                        <Button 
                          variant="outline" 
                          size="sm" 
                          className="text-blue-600 border-blue-200 hover:bg-blue-50 h-8 shadow-sm transition-all"
                          onClick={() => onSubmitForReview?.(article.id)}
                          title="Gửi duyệt bài viết"
                        >
                          <Send className="h-4 w-4 mr-1" />
                          Gửi duyệt
                        </Button>
                      )}

                      {canPublish && (
                        <Button 
                          variant="default" 
                          size="sm" 
                          className="bg-blue-600 hover:bg-blue-700 text-white h-8 shadow-sm transition-all"
                          onClick={() => onPublish?.(article.id)}
                          title="Công khai bài viết"
                        >
                          <Globe className="h-4 w-4 mr-1" />
                          Công khai
                        </Button>
                      )}
                      
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-40">
                          <DropdownMenuItem onClick={() => onView?.(article)}>
                            <Eye className="h-4 w-4 mr-2" />
                            Xem chi tiết
                          </DropdownMenuItem>
                          
                          {canReview && (
                            <>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem onClick={() => onApprove?.(article.id)}>
                                <CheckCircle className="h-4 w-4 mr-2 text-green-600" />
                                Phê duyệt ngay
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => onReject?.(article.id)}>
                                <XCircle className="h-4 w-4 mr-2 text-destructive" />
                                Từ chối bài
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => onRevision?.(article.id)}>
                                <Edit className="h-4 w-4 mr-2 text-orange-500" />
                                Yêu cầu sửa
                              </DropdownMenuItem>
                              <DropdownMenuSeparator />
                            </>
                          )}

                          {canSubmit && (
                            <>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={() => onSubmitForReview?.(article.id)}>
                                  <Send className="h-4 w-4 mr-2 text-blue-600" />
                                  Gửi duyệt
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                            </>
                          )}

                          {canPublish && (
                            <>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={() => onPublish?.(article.id)}>
                                  <Globe className="h-4 w-4 mr-2 text-blue-600" />
                                  Công khai
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                            </>
                          )}

                          <DropdownMenuItem onClick={() => onEdit?.(article)}>
                            <Edit className="h-4 w-4 mr-2" />
                            Chỉnh sửa
                          </DropdownMenuItem>
                          <DropdownMenuItem 
                            className="text-destructive focus:text-destructive"
                            onClick={() => onDelete?.(article.id)}
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            Xóa bài viết
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </td>
                </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
      
      {pagination && pagination.totalPages > 1 && (
        <div className="flex items-center justify-between mt-6 pt-6 border-t border-border">
          <p className="text-sm text-muted-foreground">
            Hiển thị bài viết {pagination.currentPage * pagination.pageSize + 1} - {Math.min((pagination.currentPage + 1) * pagination.pageSize, pagination.totalElements)} trong số {pagination.totalElements}
          </p>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={pagination.currentPage === 0}
              onClick={() => dispatch(getAllArticlesThunk({ ...pagination, page: pagination.currentPage - 1, size: pagination.pageSize, sort: "createdAt", direction: "desc" }))}
            >
              Trước
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={pagination.currentPage >= pagination.totalPages - 1}
              onClick={() => dispatch(getAllArticlesThunk({ ...pagination, page: pagination.currentPage + 1, size: pagination.pageSize, sort: "createdAt", direction: "desc" }))}
            >
              Sau
            </Button>
          </div>
        </div>
      )}

    </Card>
  );
}
