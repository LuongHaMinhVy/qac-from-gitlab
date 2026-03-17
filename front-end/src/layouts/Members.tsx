"use client";

import { useState, useRef, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Upload, Download, Lock, Unlock, Search } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { toast } from "sonner";
import type { SearchMemberRequest } from "@/interfaces/member.interface";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import {
  getMembersThunk,
  updateMemberStatusThunk,
  importMembersThunk,
} from "@/store/thunks/member.thunk";
import { memberActions } from "@/store/slices/member.slice";
import * as XLSX from "xlsx";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Pagination } from "antd";
import type { PaginationProps } from "antd";

export default function Members() {
  const dispatch = useAppDispatch();
  const { members, loading, error, actionLoading, actionMessage, pagination } =
    useAppSelector((state) => state.members);

  const [searchParams, setSearchParams] = useState<SearchMemberRequest>({
    page: 0,
    size: 10,
    email: "",
    status: "",
    role: "",
    sort: "createdAt",
    direction: "desc",
  });

  const [selectedMember, setSelectedMember] = useState<{
    userId: number;
    fullName: string;
    isActive: boolean;
  } | null>(null);
  const [showLockDialog, setShowLockDialog] = useState(false);
  const [showImportDialog, setShowImportDialog] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const searchTimeout = useRef<number | null>(null);

  useEffect(() => {
    dispatch(getMembersThunk(searchParams));
  }, [searchParams, dispatch]);

  useEffect(() => {
    if (error) {
      toast.error(error);
      dispatch(memberActions.clearMemberError());
    }
  }, [error, dispatch]);

  useEffect(() => {
    if (actionMessage) {
      if (actionMessage !== "success") {
        toast.success(actionMessage);
        dispatch(getMembersThunk(searchParams));
      } else {
        toast.error(actionMessage);
      }
      dispatch(memberActions.clearActionMessage());
    }
  }, [actionMessage, dispatch, searchParams]);

  const handleSearch = (value: string) => {
    const trimmed = value.trim();

    if (searchTimeout.current) {
      window.clearTimeout(searchTimeout.current);
    }

    if (trimmed === "") {
      setSearchParams((prev) => ({
        ...prev,
        email: "",
        page: 0,
      }));
      return;
    }

    searchTimeout.current = window.setTimeout(() => {
      setSearchParams((prev) => ({
        ...prev,
        email: trimmed,
        page: 0,
      }));
    }, 400);
  };

  const handleFilterChange = (key: string, value: string) => {
    let newValue: unknown = value;
    if (key === "isActive") {
      if (value === "all") newValue = undefined;
      else newValue = value === "true";
    } else {
      if (value === "all") newValue = "";
    }
    setSearchParams((prev) => ({ ...prev, [key]: newValue, page: 0 }));
  };

  const handlePaginationChange: PaginationProps["onChange"] = (
    current,
    pageSize
  ) => {
    setSearchParams((prev) => ({
      ...prev,
      page: current - 1,
      size: pageSize,
    }));
  };

  const handleToggleLock = async () => {
    if (!selectedMember) return;
    await dispatch(
      updateMemberStatusThunk({
        userId: selectedMember.userId,
        isActive: !selectedMember.isActive,
      })
    );
    setShowLockDialog(false);
    setSelectedMember(null);
  };

  const handleImport = async () => {
    if (!selectedFile) {
      toast.error("Vui lòng chọn file để import");
      return;
    }
    await dispatch(importMembersThunk(selectedFile));
    setShowImportDialog(false);
    setSelectedFile(null);
  };

  const handleExportTemplate = () => {
    const template = [
      {
        username: "john_doe",
        email: "john@example.com",
        fullName: "John Doe",
        phone: "0123456789",
        avatar: "https://example.com/avatar.jpg",
        bio: "Software developer",
        dateOfBirth: "1990-01-15",
        gender: "MALE",
        address: "123 Main St, City",
        isActive: true,
        emailVerified: true,
      },
    ];

    const ws = XLSX.utils.json_to_sheet(template);

    Object.keys(ws).forEach((cell) => {
      if (cell.startsWith("D") && cell !== "D1") {
        ws[cell].z = "@";
        ws[cell].t = "s";
      }
    });

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Members");
    XLSX.writeFile(wb, "members_template.xlsx");
  };

  return (
    <div className="space-y-6 p-6">
      <div>
        <h1 className="text-3xl font-bold text-balance">Quản lý Thành viên</h1>
        <p className="text-muted-foreground mt-2">
          Quản lý và theo dõi tất cả thành viên trong hệ thống
        </p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Danh sách Thành viên</CardTitle>
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={() => setShowImportDialog(true)}
                disabled={actionLoading}
              >
                <Upload className="h-4 w-4 mr-2" />
                Import
              </Button>
              <Button
                variant="outline"
                onClick={handleExportTemplate}
                disabled={actionLoading}
              >
                <Download className="h-4 w-4 mr-2" />
                Export
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-col gap-4 md:flex-row md:items-center">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Tìm kiếm theo email..."
                onChange={(e) => handleSearch(e.target.value)}
                className="pl-9"
              />
            </div>
            <div className="flex gap-2">
              <Select
                value={
                  searchParams.isActive === undefined
                    ? "all"
                    : String(searchParams.isActive)
                }
                onValueChange={(value) => handleFilterChange("isActive", value)}
              >
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Trạng thái" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Tất cả trạng thái</SelectItem>
                  <SelectItem value="true">Đang hoạt động</SelectItem>
                  <SelectItem value="false">Bị khóa</SelectItem>
                </SelectContent>
              </Select>
              <Select
                value={searchParams.role || "all"}
                onValueChange={(value) => handleFilterChange("role", value)}
              >
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Vai trò" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Tất cả vai trò</SelectItem>
                  <SelectItem value="ROLE_AUTHOR">Author</SelectItem>
                  <SelectItem value="ROLE_EDITOR">Editor</SelectItem>
                  <SelectItem value="ROLE_USER">User</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {loading ? (
            <div className="text-center py-8">Đang tải...</div>
          ) : members.length === 0 ? (
            <div className="text-center py-8">Không có thành viên nào</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Thành viên</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Vai trò</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="text-right">Hành động</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {members.map((member) => (
                  <TableRow key={member.userId}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Avatar>
                          <AvatarImage
                            src={member.avatar || ""}
                            alt={member.fullName}
                          />
                          <AvatarFallback>
                            {member.fullName.charAt(0)}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <div className="font-medium">{member.fullName}</div>
                          <div className="text-sm text-muted-foreground">
                            @{member.username}
                          </div>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>{member.email}</TableCell>
                    <TableCell>
                      <Badge variant="secondary">
                        {member.roles?.join(", ") || "User"}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-col gap-1">
                        <Badge
                          variant={member.isActive ? "default" : "secondary"}
                        >
                          {member.isActive ? "Đang hoạt động" : "Bị khóa"}
                        </Badge>
                        {member.emailVerified && (
                          <Badge variant="outline" className="text-xs">
                            Email đã xác thực
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant={member.isActive ? "destructive" : "default"}
                          onClick={() => {
                            setSelectedMember({
                              userId: member.userId,
                              fullName: member.fullName,
                              isActive: member.isActive,
                            });
                            setShowLockDialog(true);
                          }}
                          disabled={actionLoading}
                          aria-label={
                            member.isActive
                              ? `Khóa ${member.fullName}`
                              : `Mở khóa ${member.fullName}`
                          }
                        >
                          {member.isActive ? (
                            <>
                              <Lock className="h-4 w-4 mr-1" />
                              Khóa
                            </>
                          ) : (
                            <>
                              <Unlock className="h-4 w-4 mr-1" />
                              Mở khóa
                            </>
                          )}
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}

          {pagination && pagination.totalElements > 0 && (
            <div className="flex items-center justify-between mt-4">
              <div className="text-sm text-muted-foreground">
                Hiển thị {pagination.currentPage * pagination.pageSize + 1} -{" "}
                {Math.min(
                  (pagination.currentPage + 1) * pagination.pageSize,
                  pagination.totalElements
                )}{" "}
                trong tổng số {pagination.totalElements} thành viên
              </div>

              <Pagination
                current={pagination.currentPage + 1}
                pageSize={pagination.pageSize}
                total={pagination.totalElements}
                showSizeChanger
                onChange={handlePaginationChange}
                disabled={loading}
                showTotal={(total, range) =>
                  `${range[0]}-${range[1]} / ${total}`
                }
              />
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={showImportDialog} onOpenChange={setShowImportDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Import Thành viên</DialogTitle>
            <DialogDescription>
              Chọn file Excel để import danh sách thành viên
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <input
                ref={fileInputRef}
                type="file"
                accept=".xlsx,.xls"
                onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                className="block w-full text-sm text-muted-foreground file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-primary file:text-primary-foreground hover:file:bg-primary/90"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setShowImportDialog(false);
                setSelectedFile(null);
              }}
              disabled={actionLoading}
            >
              Hủy
            </Button>
            <Button
              onClick={handleImport}
              disabled={actionLoading || !selectedFile}
            >
              {actionLoading ? "Đang import..." : "Import"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showLockDialog} onOpenChange={setShowLockDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {selectedMember?.isActive
                ? "Khóa tài khoản"
                : "Mở khóa tài khoản"}
            </DialogTitle>
            <DialogDescription>
              Bạn có chắc chắn muốn{" "}
              {selectedMember?.isActive ? "khóa" : "mở khóa"} tài khoản của{" "}
              {selectedMember?.fullName}?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setShowLockDialog(false);
                setSelectedMember(null);
              }}
              disabled={actionLoading}
            >
              Hủy
            </Button>
            <Button
              variant={selectedMember?.isActive ? "destructive" : "default"}
              onClick={handleToggleLock}
              disabled={actionLoading}
            >
              {actionLoading
                ? "Đang xử lý..."
                : selectedMember?.isActive
                ? "Xác nhận khóa"
                : "Xác nhận mở khóa"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
