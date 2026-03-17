import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { useToast } from "@/components/ui/use-toast";
import {
  getAllBadWords,
  createBadWord,
  updateBadWord,
  deleteBadWord,
} from "@/apis/badwords.apis";
import type { BadWordResponse, BadWordRequest } from "@/interfaces/badword.interface";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Trash2, Edit } from "lucide-react";

export default function BadWordManagement() {
  const [data, setData] = useState<BadWordResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const { toast } = useToast();

  const [formData, setFormData] = useState<BadWordRequest>({
    word: "",
    replacement: "",
    severity: 1,
    isActive: true,
  });

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await getAllBadWords({ page, size: 10 });
      if (res.data) {
        setData(res.data);
        if (res.pagination) {
          setTotalPages(res.pagination.totalPages);
        }
      }
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to fetch data",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [page]);

  const handleOpenCreate = () => {
    setEditingId(null);
    setFormData({ word: "", replacement: "", severity: 1, isActive: true });
    setOpen(true);
  };

  const handleOpenEdit = (item: BadWordResponse) => {
    setEditingId(item.id);
    setFormData({
      word: item.word,
      replacement: item.replacement,
      severity: item.severity,
      isActive: item.isActive,
    });
    setOpen(true);
  };

  const handleSubmit = async () => {
    try {
      if (editingId) {
        await updateBadWord(editingId, formData);
        toast({ title: "Success", description: "Updated successfully" });
      } else {
        await createBadWord(formData);
        toast({ title: "Success", description: "Created successfully" });
      }
      setOpen(false);
      fetchData();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to save",
        variant: "destructive",
      });
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Are you sure you want to delete this word?")) return;
    try {
      await deleteBadWord(id);
      toast({ title: "Success", description: "Deleted successfully" });
      fetchData();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to delete",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Quản lý từ khóa vi phạm</h1>
        <Button onClick={handleOpenCreate}>Thêm từ khóa</Button>
      </div>

      <div className="border rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Word</TableHead>
              <TableHead>Replacement</TableHead>
              <TableHead>Severity</TableHead>
              <TableHead>Active</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center">
                  Loading...
                </TableCell>
              </TableRow>
            ) : data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center">
                  No data found
                </TableCell>
              </TableRow>
            ) : (
              data.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.word}</TableCell>
                  <TableCell>{item.replacement}</TableCell>
                  <TableCell>
                    <span
                      className={`px-2 py-1 rounded text-xs ${
                        item.severity === 1
                          ? "bg-yellow-100 text-yellow-800"
                          : "bg-red-100 text-red-800"
                      }`}
                    >
                      Level {item.severity}
                    </span>
                  </TableCell>
                  <TableCell>
                    {item.isActive ? (
                      <span className="text-green-600">Active</span>
                    ) : (
                      <span className="text-gray-400">Inactive</span>
                    )}
                  </TableCell>
                  <TableCell className="text-right space-x-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleOpenEdit(item)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="text-red-500 hover:text-red-700"
                      onClick={() => handleDelete(item.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
      
      {totalPages > 1 && (
         <div className="flex justify-end">
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious 
                    href="#" 
                    onClick={(e) => { e.preventDefault(); if (page > 0) setPage(page - 1); }}
                    className={page <= 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                  />
                </PaginationItem>
                {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
                    // Simple logic to show near current page
                    let p = i;
                    if (totalPages > 5 && page > 2) {
                        p = page - 2 + i;
                        if (p >= totalPages) p = totalPages - (5 - i);
                    }
                    return (
                        <PaginationItem key={p}>
                        <PaginationLink 
                            href="#" 
                            isActive={page === p}
                            onClick={(e) => { e.preventDefault(); setPage(p); }}
                        >
                            {p + 1}
                        </PaginationLink>
                        </PaginationItem>
                    );
                })}
                <PaginationItem>
                  <PaginationNext 
                    href="#" 
                    onClick={(e) => { e.preventDefault(); if (page < totalPages - 1) setPage(page + 1); }}
                    className={page >= totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
         </div>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingId ? "Sửa từ khóa" : "Thêm mới"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Word</Label>
              <Input
                value={formData.word}
                onChange={(e) =>
                  setFormData({ ...formData, word: e.target.value })
                }
              />
            </div>
            <div className="space-y-2">
              <Label>Replacement</Label>
              <Input
                value={formData.replacement}
                onChange={(e) =>
                  setFormData({ ...formData, replacement: e.target.value })
                }
                placeholder="***"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Severity</Label>
                <Select
                  value={String(formData.severity)}
                  onValueChange={(val) =>
                    setFormData({ ...formData, severity: Number(val) })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="1">Level 1 (Warning)</SelectItem>
                    <SelectItem value="2">Level 2 (Block)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-center space-x-2 pt-8">
                <Checkbox
                  id="isActive"
                  checked={formData.isActive}
                  onCheckedChange={(checked) =>
                    setFormData({ ...formData, isActive: checked as boolean })
                  }
                />
                <Label htmlFor="isActive">Active</Label>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit}>Save</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
