import { Card } from "@/components/ui/card";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  ResponsiveContainer,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from "recharts";
import { useAppSelector } from "@/hooks/redux";

export function AnalyticsCharts() {
  const { charts, loading } = useAppSelector((state) => state.dashboard);

  if (loading && !charts) {
    return <Card className="p-6 h-[320px] animate-pulse" />;
  }

  if (!charts) return null;

  const userGrowthData = charts.userGrowth.map((item) => ({
    name: item.date,
    value: item.count,
  }));

  const articleGrowthData = charts.articleGrowth.map((item) => ({
    name: item.date,
    value: item.count,
  }));

  return (
    <div className="grid gap-4 lg:grid-cols-7">
      <Card className="p-6 lg:col-span-4">
        <h3 className="font-semibold text-lg mb-4">
          Người dùng mới
        </h3>
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={userGrowthData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Area
              type="monotone"
              dataKey="value"
              stroke="hsl(var(--chart-1))"
              fill="hsl(var(--chart-1))"
              fillOpacity={0.2}
            />
          </AreaChart>
        </ResponsiveContainer>
      </Card>

      <Card className="p-6 lg:col-span-3">
        <h3 className="font-semibold text-lg mb-4">
          Bài viết mới
        </h3>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={articleGrowthData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Bar
              dataKey="value"
              fill="hsl(var(--chart-2))"
              radius={[6, 6, 0, 0]}
            />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </div>
  );
}
