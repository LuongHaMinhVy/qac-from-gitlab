import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { useAppDispatch } from "@/hooks/redux";
import {
  createCategoryThunk,
  updateCategoryThunk,
} from "@/store/thunks/category.thunk";
import type {
  CategoryRequest,
  CategoryResponse,
} from "@/interfaces/category.interface";

interface Props {
  category?: CategoryResponse;
  onSuccess: () => void;
  onCancel: () => void;
}
export function CategoryForm({ category, onSuccess, onCancel }: Props) {
  const dispatch = useAppDispatch();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [nameError, setNameError] = useState<string | null>(null);
  const [preview, setPreview] = useState<string | null>(
    category?.coverImage?.fileUrl ?? null
  );

  const [form, setForm] = useState({
    name: category?.name ?? "",
    description: category?.description ?? "",
    displayOrder: category?.displayOrder ?? null,
    status: category?.status ?? true,
  });

  const [file, setFile] = useState<File | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!form.name.trim()) {
      setNameError("Tên danh mục là bắt buộc");
      return;
    }

    setLoading(true);

    const req: CategoryRequest = {
      ...form,
    };

    try {
      if (category) {
        await dispatch(
          updateCategoryThunk({ id: category.id, request: req, file: file ?? undefined })
        ).unwrap();
      } else {
        await dispatch(createCategoryThunk({ request: req, file: file ?? undefined })).unwrap();
      }
      onSuccess();
    } catch (err: any) {
      setError(err?.message || "Save failed");
    } finally {
      setLoading(false);
    }
  };

  const validateName = (value: string) => {
    if (!value) return "Tên danh mục không được để trống";
    if (!value.trim()) return "Tên danh mục không hợp lệ";
    return null;
  };

  useEffect(() => {
    return () => {
      if (preview && preview.startsWith("blob:")) {
        URL.revokeObjectURL(preview);
      }
    };
  }, [preview]);

  return (
    <form onSubmit={submit} className="space-y-4">
      {error && (
        <div className="p-2 text-sm bg-destructive/10 text-destructive rounded">
          {error}
        </div>
      )}

      <div>
        <Input
          className={
            nameError ? "border-destructive focus-visible:ring-destructive" : ""
          }
          placeholder="Name"
          value={form.name}
          onChange={(e) => {
            const value = e.target.value;
            setForm((prev) => ({ ...prev, name: value }));
            setNameError(validateName(value));
          }}
        />

        {nameError && (
          <p className="text-sm text-destructive mt-1">{nameError}</p>
        )}
      </div>

      <Textarea
        placeholder="Description"
        value={form.description}
        onChange={(e) => setForm({ ...form, description: e.target.value })}
      />

      <div className="flex items-center space-x-2">
        <Switch
          id="category-status"
          checked={form.status}
          onCheckedChange={(checked) => setForm({ ...form, status: checked })}
        />
        <Label htmlFor="category-status">Hoạt động</Label>
      </div>

      <Input
        type="number"
        placeholder="Display Order"
        value={form.displayOrder ?? ""}
        onChange={(e) =>
          setForm({
            ...form,
            displayOrder: e.target.value ? Number(e.target.value) : null,
          })
        }
      />

      <Input
        type="file"
        accept="image/*"
        onChange={(e) => {
          const selectedFile = e.target.files?.[0] ?? null;
          setFile(selectedFile);

          if (selectedFile) {
            const objectUrl = URL.createObjectURL(selectedFile);
            setPreview(objectUrl);
          }
        }}
      />

      {preview && (
        <div className="mt-2">
          <img
            src={preview}
            alt="Preview"
            className="w-40 h-40 object-cover rounded border"
          />
        </div>
      )}

      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" disabled={loading}>
          {loading ? "Saving..." : "Save"}
        </Button>
      </div>
    </form>
  );
}
