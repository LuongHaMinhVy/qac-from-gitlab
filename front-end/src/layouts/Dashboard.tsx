import { useEffect } from "react";
import { AnalyticsCharts } from "@/components/AnalyticsCharts";
import { ArticlesTable } from "@/components/ArticlesTable";
import { StatsCards } from "@/components/StatsCards";
import { useAppDispatch } from "@/hooks/redux";
import {
  getDashboardSummaryThunk,
  getDashboardChartsThunk,
} from "@/store/thunks/dashboard.thunk";

export default function Dashboard() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getDashboardSummaryThunk());
    dispatch(getDashboardChartsThunk());
  }, [dispatch]);

  return (
    <>
      <div>
        <h1 className="text-3xl font-bold text-balance">
          Quản trị Tạp chí QAC
        </h1>
        <p className="text-muted-foreground mt-2">
          Chào mừng trở lại! Đây là tổng quan hệ thống quản lý của bạn.
        </p>
      </div>

      <StatsCards />
      <AnalyticsCharts />
      <ArticlesTable />
    </>
  );
}
