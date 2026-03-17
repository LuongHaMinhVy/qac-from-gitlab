import { useEffect, useState } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import { getAllAdminsThunk, deleteAdminThunk, assignRolesThunk, createAdminThunk } from "@/store/thunks/admin.thunk";
import { Loader2, Plus, Trash2, Shield, RefreshCcw } from "lucide-react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { format } from "date-fns";
import { vi } from "date-fns/locale";

const AVAILABLE_ROLES = [
  { code: 'ROLE_EDITOR', name: 'Biên tập viên (Editor)' },
];

export default function AdminManagement() {
  const dispatch = useAppDispatch();
  const { admins, loading, actionLoading } = useAppSelector((state) => state.adminManagement);
  const currentAccount = JSON.parse(localStorage.getItem("account") || "{}");

  const [dialogState, setDialogState] = useState<{ 
    open: boolean; 
    mode: 'CREATE' | 'EDIT' | 'ROLES'; 
    adminId: number | null;
    formData: any;
  }>({
    open: false,
    mode: 'CREATE',
    adminId: null,
    formData: { username: "", email: "", fullName: "", password: "", isActive: true, roles: [] }
  });

  const fetchData = () => {
    dispatch(getAllAdminsThunk({ page: 0, size: 20 }));
  };

  useEffect(() => {
    fetchData();
  }, [dispatch]);

  const handleOpenCreate = () => {
    setDialogState({
      open: true,
      mode: 'CREATE',
      adminId: null,
      formData: { username: "", email: "", fullName: "", password: "", isActive: true, roles: [] }
    });
  };

  const handleOpenRoles = (admin: any) => {
    setDialogState({
      open: true,
      mode: 'ROLES',
      adminId: admin.accountId,
      formData: { ...admin }
    });
  };

  const handleDelete = async (id: number) => {
    if (id === currentAccount?.accountId) {
      alert("Bạn không thể xóa chính mình!");
      return;
    }
    if (window.confirm("Bạn có chắc chắn muốn gỡ bỏ quyền của biên tập viên này?")) {
      const result = await dispatch(deleteAdminThunk(id));
      if (deleteAdminThunk.fulfilled.match(result)) {
        fetchData();
      }
    }
  };

  const handleSave = async () => {
    const { mode, adminId, formData } = dialogState;
    
    if (mode === 'CREATE') {
      const payload = {
        ...formData,
        roleCodes: ['ROLE_EDITOR']
      };
      const result = await dispatch(createAdminThunk(payload));
      if (createAdminThunk.fulfilled.match(result)) {
        setDialogState({ ...dialogState, open: false });
        fetchData();
      }
    } else if (mode === 'ROLES') {
      if (adminId === currentAccount?.accountId) {
        alert("Bạn không thể tự thay đổi quyền của chính mình!");
        return;
      }
      const result = await dispatch(assignRolesThunk({ 
        id: adminId!, 
        request: { roleCodes: formData.roles } 
      }));
      if (assignRolesThunk.fulfilled.match(result)) {
        setDialogState({ ...dialogState, open: false });
        fetchData();
      }
    }
  };

  const toggleRole = (roleCode: string) => {
    const currentRoles = dialogState.formData.roles || [];
    const newRoles = currentRoles.includes(roleCode)
      ? currentRoles.filter((r: string) => r !== roleCode)
      : [...currentRoles, roleCode];
    
    setDialogState({
      ...dialogState,
      formData: { ...dialogState.formData, roles: newRoles }
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Quản lý Biên tập viên</h2>
          <p className="text-muted-foreground">
            Quản lý đội ngũ biên tập viên vận hành nội dung hệ thống.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={fetchData} disabled={loading}>
            <RefreshCcw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
            Làm mới
          </Button>
          <Button onClick={handleOpenCreate}>
            <Plus className="h-4 w-4 mr-2" />
            Thêm tài khoản mới
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Danh sách Biên tập viên</CardTitle>
          <CardDescription>
            Hệ thống hiển thị tất cả các tài khoản có vai trò Biên tập viên (Editor).
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading && admins.length === 0 ? (
            <div className="flex h-40 items-center justify-center">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Thông tin</TableHead>
                  <TableHead>Vai trò</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead>Ngày tham gia</TableHead>
                  <TableHead className="text-right">Hành động</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {admins.map((admin) => (
                  <TableRow key={admin.accountId}>
                    <TableCell>
                      <div className="flex flex-col">
                        <span className="font-medium">{admin.fullName}</span>
                        <span className="text-xs text-muted-foreground">@{admin.username} • {admin.email}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {admin.roles.map(role => (
                          <Badge key={role} variant="outline" className="bg-blue-50">
                            {role.replace('ROLE_', '')}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant={admin.isActive ? "default" : "secondary"} className={admin.isActive ? "bg-green-100 text-green-800" : ""}>
                        {admin.isActive ? "Hoạt động" : "Bị khóa"}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {format(new Date(admin.createdAt), "dd/MM/yyyy", { locale: vi })}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => handleOpenRoles(admin)}
                          disabled={admin.accountId === currentAccount?.accountId}
                          title="Phân quyền"
                        >
                          <Shield className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="sm" 
                          className="text-destructive hover:text-destructive hover:bg-destructive/10"
                          onClick={() => handleDelete(admin.accountId)}
                          disabled={admin.accountId === currentAccount?.accountId}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog open={dialogState.open} onOpenChange={(open) => setDialogState({ ...dialogState, open })}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>
              {dialogState.mode === 'CREATE' ? "Thêm Biên tập viên mới" : "Quản lý vai trò"}
            </DialogTitle>
            <DialogDescription>
              {dialogState.mode === 'CREATE' 
                ? "Thành viên sẽ được cấp các thông tin đăng nhập mặc định." 
                : `Thiết lập quyền hạn cho ${dialogState.formData.fullName}`}
            </DialogDescription>
          </DialogHeader>

          <div className="py-4 space-y-4">
            {dialogState.mode === 'CREATE' ? (
              <>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Họ và tên</label>
                  <Input 
                    value={dialogState.formData.fullName}
                    onChange={(e) => setDialogState({ ...dialogState, formData: { ...dialogState.formData, fullName: e.target.value } })}
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Username</label>
                  <Input 
                    value={dialogState.formData.username}
                    onChange={(e) => setDialogState({ ...dialogState, formData: { ...dialogState.formData, username: e.target.value } })}
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Email</label>
                  <Input 
                    type="email"
                    value={dialogState.formData.email}
                    onChange={(e) => setDialogState({ ...dialogState, formData: { ...dialogState.formData, email: e.target.value } })}
                  />
                </div>
              </>
            ) : (
              <div className="space-y-3">
                <label className="text-sm font-medium">Danh sách vai trò</label>
                <div className="grid grid-cols-1 gap-2">
                  {AVAILABLE_ROLES.map(role => (
                    <div 
                      key={role.code}
                      className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-colors ${
                        dialogState.formData.roles?.includes(role.code) 
                          ? 'bg-primary/5 border-primary' 
                          : 'hover:bg-muted'
                      }`}
                      onClick={() => toggleRole(role.code)}
                    >
                      <div className="flex flex-col">
                        <span className="text-sm font-medium">{role.name}</span>
                        <span className="text-xs text-muted-foreground">{role.code}</span>
                      </div>
                      <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${
                        dialogState.formData.roles?.includes(role.code) ? 'bg-primary border-primary' : 'bg-white'
                      }`}>
                        {dialogState.formData.roles?.includes(role.code) && (
                          <div className="w-1.5 h-1.5 rounded-full bg-white" />
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogState({ ...dialogState, open: false })}>Hủy</Button>
            <Button onClick={handleSave} disabled={actionLoading}>
              {actionLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Lưu thay đổi
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
