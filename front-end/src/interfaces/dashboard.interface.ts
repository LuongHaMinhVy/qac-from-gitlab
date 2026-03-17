export interface DashboardSummaryResponse {
  totalUsers: number;
  newUsersToday: number;

  totalArticles: number;
  publishedArticles: number;
  pendingArticles: number;

  totalViews: number;
  totalComments: number;

  articlesGrowth: number;
  usersGrowth: number;
}

export interface DashboardChartResponse {
  userGrowth: ChartDataPoint[];
  articleGrowth: ChartDataPoint[];
}

export interface ChartDataPoint {
  date: string;
  count: number;
}