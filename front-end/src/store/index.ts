import { configureStore } from "@reduxjs/toolkit";
import authSlice from "./slices/auth.slice";
import memberSlice from "./slices/member.slice";
import categorySlice from "./slices/category.slice";
import articleSlice from "./slices/article.slice";
import mediaSlice from "./slices/media.slice";
import adminSlice from "./slices/admin.slice";
import dashboardSlice from "./slices/dashboard.slice";

export const store = configureStore({
  reducer: {
    auth: authSlice,
    members: memberSlice,
    categories: categorySlice,
    articles: articleSlice,
    media: mediaSlice,
    adminManagement: adminSlice,
    dashboard: dashboardSlice,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
