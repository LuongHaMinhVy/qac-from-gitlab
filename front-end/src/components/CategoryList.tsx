import { useEffect, useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { CategoryResponse } from "@/interfaces/category.interface";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import {
  getAllCategoriesThunk,
  deleteCategoryThunk,
} from "@/store/thunks/category.thunk";
import { toast } from "sonner";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { Pagination } from "antd";
import type { PaginationProps } from "antd";

interface CategoryListProps {
  onEdit: (category: CategoryResponse) => void;
}
export function CategoryList({ onEdit }: CategoryListProps) {
  const dispatch = useAppDispatch();

  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteArticles, setDeleteArticles] = useState(false);

  const { categories, loading, error, pagination } = useAppSelector(
    (state) => state.categories
  );

  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState(searchTerm);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  useEffect(() => {
    dispatch(
      getAllCategoriesThunk({
        search: debouncedSearch,
        page,
        size: pageSize,
        sort: "displayOrder",
        direction: "ASC",
      })
    );
  }, [dispatch, page, pageSize, debouncedSearch]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
  };

  const handlePageChange: PaginationProps["onChange"] = (
    current,
    newPageSize
  ) => {
    setPage(current - 1);

    if (newPageSize !== pageSize) {
      setPageSize(newPageSize);
      setPage(0);
    }
  };

  useEffect(() => {
    if (error?.message) {
      toast.error(error.message);
    }
  }, [error]);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearch(searchTerm);
      setPage(0);
    }, 500);

    return () => clearTimeout(handler);
  }, [searchTerm]);

  return (
    <>
      <AlertDialog
        open={deleteId !== null}
        onOpenChange={() => {
          setDeleteId(null);
          setDeleteArticles(false);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Bạn chắc chắn?</AlertDialogTitle>
            <AlertDialogDescription>
              Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="flex items-center space-x-2 mt-4">
            <Checkbox
              id="deleteArticles"
              checked={deleteArticles}
              onCheckedChange={(checked) => setDeleteArticles(checked === true)}
            />
            <Label htmlFor="deleteArticles" className="text-sm">
              Xoá tất cả bài viết thuộc danh mục này
            </Label>
          </div>

          <AlertDialogFooter>
            <AlertDialogCancel>Huỷ</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground text-white hover:bg-destructive/90"
              onClick={() => {
                if (deleteId !== null) {
                  dispatch(
                    deleteCategoryThunk({ id: deleteId, deleteArticles: false })
                  );
                  setDeleteId(null);
                }
              }}
            >
              Xoá
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <Card className="w-full">
        <CardHeader className="space-y-4">
          <CardTitle>Danh mục</CardTitle>

          <form onSubmit={handleSearch} className="flex gap-2">
            <div className="relative w-full">
              <Input
                placeholder="Tìm kiếm danh mục..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pr-10"
              />

              {searchTerm && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchTerm("");
                    setDebouncedSearch("");
                    setPage(0);
                  }}
                  className="absolute font-bold cursor-pointer right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  ✕
                </button>
              )}
            </div>
          </form>
        </CardHeader>

        <CardContent>
          {loading ? (
            <div className="text-center py-8 text-muted-foreground">
              Loading...
            </div>
          ) : categories.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No categories found
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã</TableHead>
                    <TableHead>Tên</TableHead>
                    <TableHead>Mô tả</TableHead>
                    <TableHead className="text-center">Thứ tự</TableHead>
                    <TableHead className="text-right">Hành động</TableHead>
                  </TableRow>
                </TableHeader>

                <TableBody>
                  {categories.map((c: CategoryResponse) => (
                    <TableRow key={c.id}>
                      <TableCell>{c.id}</TableCell>
                      <TableCell>{c.name}</TableCell>
                      <TableCell className="truncate max-w-xs">
                        {c.description}
                      </TableCell>
                      <TableCell className="text-center">
                        {c.displayOrder}
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => onEdit(c)}
                          className="mr-4"
                        >
                          Edit
                        </Button>
                        
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => setDeleteId(c.id)}
                        >
                          Delete
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              <div className="mt-6 flex items-center justify-between">
                <Pagination
                  className="mt-4"
                  current={(pagination?.currentPage ?? 0) + 1}
                  pageSize={pagination?.pageSize ?? pageSize}
                  total={pagination?.totalElements ?? 0}
                  showSizeChanger
                  onChange={handlePageChange}
                  showTotal={(total, range) =>
                    `${range[0]}-${range[1]} / ${total} items`
                  }
                />
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </>
  );
}
