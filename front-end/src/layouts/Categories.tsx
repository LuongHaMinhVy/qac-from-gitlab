import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

import { CategoryList } from "../components/CategoryList";
import { CategoryForm } from "../components/CategoryForm";
import type { CategoryResponse } from "@/interfaces/category.interface";

export default function CategoriesPage() {
  const [selectedCategory, setSelectedCategory] =
    useState<CategoryResponse | null>(null);
  const [open, setOpen] = useState(false);

  const handleCreate = () => {
    setSelectedCategory(null);
    setOpen(true);
  };

  const handleEdit = (category: CategoryResponse) => {
    setSelectedCategory(category);
    setOpen(true);
  };

  const handleSuccess = () => {
    setOpen(false);
    setSelectedCategory(null);
  };

  const handleCancel = () => {
    setOpen(false);
    setSelectedCategory(null);
  };

  return (
    <main className="min-h-screen bg-background p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Quản lý danh mục</h1>
            <p className="text-muted-foreground">
              Quản lý danh mục sản phẩm
            </p>
          </div>

          <Button size="lg" onClick={handleCreate}>
            Create Category
          </Button>
        </div>

        <CategoryList onEdit={handleEdit} />


        <Dialog open={open} onOpenChange={setOpen}>
          <DialogContent className="max-w-lg">
            <DialogHeader>
              <DialogTitle>
                {selectedCategory ? "Update Category" : "Create Category"}
              </DialogTitle>
            </DialogHeader>

            <CategoryForm
              category={selectedCategory ?? undefined}
              onSuccess={handleSuccess}
              onCancel={handleCancel}
            />
          </DialogContent>
        </Dialog>
      </div>
    </main>
  );
}
