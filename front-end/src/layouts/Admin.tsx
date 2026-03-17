import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import { Outlet } from "react-router";

export default function Admin() {
  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Header />
        <main className="flex-1 p-6 lg:p-8 space-y-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
