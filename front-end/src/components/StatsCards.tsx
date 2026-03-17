import { Card } from "@/components/ui/card";
import { FileText, Users } from "lucide-react";
import { useAppSelector } from "@/hooks/redux";

export function StatsCards() {
  const { summary, loading } = useAppSelector((state) => state.dashboard);

  if (loading && !summary) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="p-6 animate-pulse h-[120px]" />
        <Card className="p-6 animate-pulse h-[120px]" />
      </div>
    );
  }

  if (!summary) return null;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <Card className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-muted-foreground">
              Tổng bài viết
            </p>
            <p className="text-3xl font-bold mt-2">
              {summary.totalArticles}
            </p>
            <p className="text-sm text-muted-foreground mt-1">
              {summary.articlesGrowth >= 0 ? "+" : ""}
              {summary.articlesGrowth}% so với tháng trước
            </p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center">
            <FileText className="h-6 w-6 text-primary" />
          </div>
        </div>
      </Card>

      <Card className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-muted-foreground">
              Tổng thành viên
            </p>
            <p className="text-3xl font-bold mt-2">
              {summary.totalUsers}
            </p>
            <p className="text-sm text-muted-foreground mt-1">
              {summary.usersGrowth >= 0 ? "+" : ""}
              {summary.usersGrowth}% so với tháng trước
            </p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center">
            <Users className="h-6 w-6 text-primary" />
          </div>
        </div>
      </Card>
    </div>
  );
}
