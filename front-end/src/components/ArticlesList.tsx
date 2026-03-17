import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Trash2 } from "lucide-react";
import { format } from "date-fns";
import { vi } from "date-fns/locale";
import type { ArticleResponse } from "@/interfaces/article.interface";
import { Skeleton } from "@/components/ui/skeleton";

interface ArticlesListProps {
  articles: ArticleResponse[];
  loading: boolean;
  onDelete: (id: number) => void;
}

export default function ArticlesList({
  articles,
  loading,
  onDelete,
}: ArticlesListProps) {
  const getStatusColor = (status: string) => {
    switch (status) {
      case "PUBLISHED":
        return "bg-emerald-500/10 text-emerald-700 dark:text-emerald-400";
      case "DRAFT":
        return "bg-amber-500/10 text-amber-700 dark:text-amber-400";
      case "ARCHIVED":
        return "bg-slate-500/10 text-slate-700 dark:text-slate-400";
      default:
        return "bg-slate-500/10 text-slate-700 dark:text-slate-400";
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case "PUBLISHED":
        return "Đã xuất bản";
      case "DRAFT":
        return "Nháp";
      case "ARCHIVED":
        return "Lưu trữ";
      default:
        return status;
    }
  };

  if (loading && articles.length === 0) {
    return (
      <div className="border border-border rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50 hover:bg-muted/50">
              <TableHead>Tiêu đề</TableHead>
              <TableHead>Danh mục</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {[...Array(5)].map((_, i) => (
              <TableRow key={i}>
                <TableCell colSpan={5}>
                  <Skeleton className="h-4 w-full" />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    );
  }

  if (articles.length === 0) {
    return (
      <div className="border border-border rounded-lg overflow-hidden shadow-sm">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50 hover:bg-muted/50">
              <TableHead>Tiêu đề</TableHead>
              <TableHead>Danh mục</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Lượt xem</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow>
              <TableCell
                colSpan={6}
                className="text-center text-muted-foreground py-12"
              >
                Chưa có bài viết nào
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    );
  }

  return (
    <div className="border border-border rounded-lg overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow className="bg-muted/50 hover:bg-muted/50">
            <TableHead>Tiêu đề</TableHead>
            <TableHead>Danh mục</TableHead>
            <TableHead>Trạng thái</TableHead>
            <TableHead>Lượt xem</TableHead>
            <TableHead>Ngày tạo</TableHead>
            <TableHead className="text-right">Thao tác</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {articles.map((article) => (
            <TableRow key={article.id} className="hover:bg-muted/50">
              <TableCell className="font-medium">
                <div className="flex flex-col">
                  <span className="line-clamp-1">{article.title}</span>
                  <span className="text-xs text-muted-foreground">
                    Tác giả: {article.authorName}
                  </span>
                </div>
              </TableCell>
              <TableCell>{article.categoryName}</TableCell>
              <TableCell>
                <Badge
                  variant="outline"
                  className={getStatusColor(article.status)}
                >
                  {getStatusLabel(article.status)}
                </Badge>
              </TableCell>
              <TableCell>{article.viewCount.toLocaleString()}</TableCell>
              <TableCell className="text-sm text-muted-foreground">
                {format(new Date(article.createdAt), "dd MMM yyyy", {
                  locale: vi,
                })}
              </TableCell>
              <TableCell className="text-right">
                <div className="flex items-center justify-end gap-2">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => onDelete(article.id)}
                    className="h-8 w-8 text-destructive hover:text-destructive"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      
    </div>
  );
}
