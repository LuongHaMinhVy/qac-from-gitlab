import { useEffect, useState } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import { getPendingAuthorRequestsThunk, approveMemberThunk, rejectMemberThunk } from "@/store/thunks/member.thunk";
import { format } from "date-fns";
import { vi } from "date-fns/locale";
import { Loader2, CheckCircle, XCircle, RefreshCcw } from "lucide-react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";

export default function RoleRequests() {
  const dispatch = useAppDispatch();
  const { roleRequests, loading, actionLoading } = useAppSelector((state) => state.members);
  
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [selectedRequestId, setSelectedRequestId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState("");

  const fetchData = () => {
    dispatch(getPendingAuthorRequestsThunk());
  };

  useEffect(() => {
    fetchData();
  }, [dispatch]);

  const handleApprove = async (id: number) => {
    if (window.confirm("Bạn có chắc chắn muốn duyệt yêu cầu này?")) {
      const result = await dispatch(approveMemberThunk({ roleRequestId: id, reviewComments: "Approved by Admin" }));
      if (approveMemberThunk.fulfilled.match(result)) {
        fetchData();
      }
    }
  };

  const openRejectModal = (id: number) => {
    setSelectedRequestId(id);
    setRejectReason("");
    setRejectModalOpen(true);
  };

  const handleReject = async () => {
    if (!selectedRequestId) return;
    if (!rejectReason.trim()) {
      alert("Vui lòng nhập lý do từ chối");
      return;
    }

    const result = await dispatch(rejectMemberThunk({ 
      roleRequestId: selectedRequestId, 
      reviewComments: rejectReason 
    }));
    
    if (rejectMemberThunk.fulfilled.match(result)) {
      setRejectModalOpen(false);
      fetchData();
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Duyệt nâng cấp quyền</h2>
          <p className="text-muted-foreground">
            Quản lý các yêu cầu trở thành Tác giả (Author) từ người dùng.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchData} disabled={loading}>
          <RefreshCcw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
          Làm mới
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Danh sách yêu cầu đang chờ</CardTitle>
          <CardDescription>
            Các yêu cầu được sắp xếp theo thời gian gửi gần nhất.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading && roleRequests.length === 0 ? (
            <div className="flex h-40 items-center justify-center">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Người dùng</TableHead>
                  <TableHead>Quyền yêu cầu</TableHead>
                  <TableHead>Lý do</TableHead>
                  <TableHead>Ngày gửi</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="text-right">Hành động</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {roleRequests.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-24 text-center text-muted-foreground">
                      Không có yêu cầu nào đang chờ xử lý.
                    </TableCell>
                  </TableRow>
                ) : (
                  roleRequests.map((req) => (
                    <TableRow key={req.id}>
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-medium">{req.accountUsername}</span>
                          <span className="text-xs text-muted-foreground">{req.accountEmail}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline">{req.requestedRoleName}</Badge>
                      </TableCell>
                      <TableCell className="max-w-xs truncate" title={req.reason}>
                        {req.reason}
                      </TableCell>
                      <TableCell>
                        {format(new Date(req.createdAt), "dd/MM/yyyy HH:mm", { locale: vi })}
                      </TableCell>
                      <TableCell>
                        <Badge variant="secondary" className="bg-yellow-100 text-yellow-800 border-yellow-200">
                          Đang chờ
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button 
                            variant="default" 
                            size="sm" 
                            className="bg-green-600 hover:bg-green-700"
                            onClick={() => handleApprove(req.id)}
                            disabled={actionLoading}
                          >
                            <CheckCircle className="h-4 w-4 mr-1" />
                            Duyệt
                          </Button>
                          <Button 
                            variant="destructive" 
                            size="sm"
                            onClick={() => openRejectModal(req.id)}
                            disabled={actionLoading}
                          >
                            <XCircle className="h-4 w-4 mr-1" />
                            Từ chối
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog open={rejectModalOpen} onOpenChange={setRejectModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Từ chối yêu cầu nâng cấp quyền</DialogTitle>
            <DialogDescription>
              Vui lòng cung cấp lý do từ chối để người dùng nắm rõ thông tin.
            </DialogDescription>
          </DialogHeader>
          <div className="py-4 space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Lý do từ chối</label>
              <Textarea 
                placeholder="Nhập lý do chi tiết..." 
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                className="min-h-[100px]"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectModalOpen(false)}>Hủy</Button>
            <Button variant="destructive" onClick={handleReject} disabled={actionLoading}>
              {actionLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Xác nhận từ chối
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
