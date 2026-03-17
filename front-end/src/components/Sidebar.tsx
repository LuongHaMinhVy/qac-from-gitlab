"use client";

import { useState } from "react";
import { Link, useLocation } from "react-router";
import {
  FileText,
  Users,
  BarChart3,
  Settings,
  Menu,
  X,
  BookOpen,
  Folder,
  Image,
  UserCog,
} from "lucide-react";
import { Button } from "../components/ui/button";
import { cn } from "../lib/utils";
import { useSelector } from "react-redux";
import type { RootState } from "../store";
import { hasPermission } from "../utils/permissions";

const navigation = [
  {
    name: "Tổng quan",
    href: "/admin/dashboard",
    icon: BarChart3,
    permission: "ANALYTICS_VIEW",
  },
  { name: "Bài viết", href: "/admin/articles", icon: FileText, permission: "ARTICLE_CREATE" },
  { name: "Danh mục", href: "/admin/categories", icon: Folder, permission: "CATEGORY_MANAGE" },
  { name: "Thư viện", href: "/admin/media", icon: Image, permission: "MEDIA_MANAGE" },
  { name: "Thành viên", href: "/admin/members", icon: Users, permission: "USER_VIEW" },
  { name: "Ban quản trị", href: "/admin/admins", icon: UserCog, permission: "ROLE_ASSIGN" },
  { name: "Duyệt quyền", href: "/admin/role-requests", icon: BookOpen, permission: "ROLE_REQUEST_APPROVE" },
  { name: "Bad Words", href: "/admin/bad-words", icon: FileText, permission: "BADWORD_MANAGE" },
];

export default function Sidebar() {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();
  const { account } = useSelector((state: RootState) => state.auth);

  return (
    <>
      <Button
        variant="ghost"
        size="icon"
        className="fixed top-4 left-4 z-50 lg:hidden"
        onClick={() => setIsOpen(!isOpen)}
      >
        {isOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
      </Button>

      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={() => setIsOpen(false)}
        />
      )}

      <aside
        className={cn(
          "fixed lg:sticky top-0 left-0 z-40 h-screen w-64 bg-sidebar border-r border-sidebar-border transition-transform duration-200 lg:translate-x-0",
          isOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div className="flex flex-col h-full">
          <div className="h-16 flex items-center px-6 border-b border-sidebar-border">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-sidebar-primary flex items-center justify-center">
                <BookOpen className="h-5 w-5 text-sidebar-primary-foreground" />
              </div>
              <span className="font-bold text-lg text-sidebar-foreground">
                QAC Journal
              </span>
            </div>
          </div>

          <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
            {navigation.map((item) => {
              const isActive = item.href.includes('?') 
                ? (location.pathname + location.search) === item.href
                : location.pathname === item.href;
              if (item.permission && !hasPermission(account?.account || null, item.permission)) {
                 return null;
              }
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                    isActive
                      ? "bg-sidebar-accent text-sidebar-accent-foreground"
                      : "text-sidebar-foreground/60 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
                  )}
                  onClick={() => setIsOpen(false)}
                >
                  <item.icon className="h-5 w-5" />
                  {item.name}
                </Link>
              );
            })}
          </nav>

          <div className="p-3 border-t border-sidebar-border">
            {hasPermission(account?.account || null, "SYSTEM_SETTING") && (
              <a
                href="#"
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-sidebar-foreground/60 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground transition-colors"
              >
                <Settings className="h-5 w-5" />
                Cài đặt
              </a>
            )}
          </div>
        </div>
      </aside>
    </>
  );
}
