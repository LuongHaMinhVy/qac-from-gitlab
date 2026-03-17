import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import z from "zod";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { useAppDispatch, useAppSelector } from "@/hooks/redux";
import { getAllCategoriesThunk } from "@/store/thunks/category.thunk";
import { createArticleThunk, updateArticleThunk } from "@/store/thunks/article.thunk";
import { uploadMediaThunk } from "@/store/thunks/media.thunk";
import type { ArticleListItem, ArticleDetail, ArticleRequest } from "@/interfaces/article.interface";
import { Loader2, Image as ImageIcon, X, Check } from "lucide-react";
import { toast } from "sonner";

const articleSchema = z.object({
  title: z.string().min(10, "Tiêu đề phải ít nhất 10 ký tự"),
  excerpt: z.string().min(20, "Trích dẫn phải ít nhất 20 ký tự"),
  content: z.string().min(50, "Nội dung phải ít nhất 50 ký tự"),
  categoryId: z.string().min(1, "Vui lòng chọn danh mục"),
  hashtag: z.string().nullable().optional(),
  isHighlight: z.boolean().default(false),
  isFeatured: z.boolean().default(false),
  allowComments: z.boolean().default(true),
});

type ArticleFormValues = z.infer<typeof articleSchema>;

interface ArticleFormProps {
  article?: ArticleListItem | ArticleDetail;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function ArticleForm({ article, onSuccess, onCancel }: ArticleFormProps) {
  const dispatch = useAppDispatch();
  const { categories, loading: categoryLoading } = useAppSelector((state) => state.categories);
  const [selectedImage, setSelectedImage] = useState<{ id: number; url: string } | null>(
    article?.featuredImage ? { id: article.featuredImage.id, url: article.featuredImage.fileUrl } : null
  );
  const [isUploading, setIsUploading] = useState(false);

  const form = useForm<ArticleFormValues>({
    resolver: zodResolver(articleSchema),
    defaultValues: {
      title: article?.title || "",
      excerpt: article?.excerpt || "",
      content: (article as ArticleDetail)?.content || "",
      categoryId: article?.categoryId?.toString() || "",
      hashtag: article?.hashtag || "",
      isHighlight: (article as ArticleDetail)?.isHighlight || false,
      isFeatured: (article as ArticleDetail)?.isFeatured || false,
      allowComments: (article as ArticleDetail)?.allowComments ?? true,
    },
  });

  const hashtagValue = form.watch("hashtag");
  const [normalizedHashtag, setNormalizedHashtag] = useState("");

  useEffect(() => {
    dispatch(getAllCategoriesThunk({ page: 0, size: 100, sort: "name", direction: "asc" }));
  }, [dispatch]);

  useEffect(() => {
    if (hashtagValue) {
      const normalized = hashtagValue
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/\s+/g, "-")
        .replace(/[^\w-]/g, "")
        .toLowerCase();
      setNormalizedHashtag(normalized);
    } else {
      setNormalizedHashtag("");
    }
  }, [hashtagValue]);

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    try {
      const resultAction = await dispatch(uploadMediaThunk(file));
      if (uploadMediaThunk.fulfilled.match(resultAction)) {
        const media = resultAction.payload.data;
        setSelectedImage({ id: media.id, url: media.fileUrl });
        toast.success(media.deduplicated ? "Đã dùng lại ảnh từ thư viện" : "Tải ảnh lên thành công");
      } else {
        toast.error("Không thể tải ảnh lên");
      }
    } finally {
      setIsUploading(false);
    }
  };

  const onSubmit = async (values: ArticleFormValues) => {
    if (!selectedImage) {
      toast.error("Vui lòng chọn ảnh minh họa");
      return;
    }

    const requestData: ArticleRequest = {
      ...values,
      categoryId: parseInt(values.categoryId),
      mediaId: selectedImage.id,
      hashtag: normalizedHashtag || null,
    };

    let resultAction;
    if (article) {
      resultAction = await dispatch(updateArticleThunk({ id: article.id, request: requestData }));
    } else {
      resultAction = await dispatch(createArticleThunk(requestData));
    }

    if (createArticleThunk.fulfilled.match(resultAction) || updateArticleThunk.fulfilled.match(resultAction)) {
      toast.success(article ? "Cập nhật bài viết thành công" : "Tạo bài viết thành công");
      onSuccess?.();
    } else {
      const errorPayload = resultAction.payload as any;
      const errorMessage = errorPayload?.message || "Có lỗi xảy ra, vui lòng thử lại";
      toast.error(errorMessage);
      console.error("Article submission failed:", errorPayload);
    }
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <FormField
              control={form.control}
              name="title"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Tiêu đề bài viết</FormLabel>
                  <FormControl>
                    <Input placeholder="Nhập tiêu đề hấp dẫn..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="excerpt"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Trích dẫn ngắn</FormLabel>
                  <FormControl>
                    <Textarea 
                      placeholder="Tóm tắt ngắn gọn nội dung bài viết..." 
                      className="h-24 resize-none"
                      {...field} 
                    />
                  </FormControl>
                  <FormDescription>
                    Hiển thị ở danh sách bài viết và kết quả tìm kiếm.
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="content"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Nội dung chi tiết</FormLabel>
                  <FormControl>
                    <Textarea 
                      placeholder="Nội dung bài viết..." 
                      className="min-h-[400px]"
                      {...field} 
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>

          <div className="space-y-6">
            <Card className="p-4 space-y-4">
              <h4 className="font-medium text-sm border-b pb-2">Hình đại diện</h4>
              <div className="relative aspect-video rounded-lg border-2 border-dashed border-muted-foreground/25 overflow-hidden group">
                {selectedImage ? (
                  <>
                    <img src={selectedImage.url} alt="Preview" className="w-full h-full object-cover" />
                    <Button
                      type="button"
                      variant="destructive"
                      size="icon"
                      className="absolute top-2 right-2 h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity"
                      onClick={() => setSelectedImage(null)}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </>
                ) : (
                  <label className="flex flex-col items-center justify-center w-full h-full cursor-pointer hover:bg-muted/50 transition-colors">
                    <ImageIcon className="h-10 w-10 text-muted-foreground mb-2" />
                    <span className="text-xs text-muted-foreground font-medium">Click để tải ảnh lên</span>
                    <input type="file" className="hidden" accept="image/*" onChange={handleImageUpload} disabled={isUploading} />
                  </label>
                )}
                {isUploading && (
                  <div className="absolute inset-0 bg-background/50 flex items-center justify-center">
                    <Loader2 className="h-6 w-6 animate-spin" />
                  </div>
                )}
              </div>
            </Card>

            <Card className="p-4 space-y-4">
              <h4 className="font-medium text-sm border-b pb-2">Phân loại</h4>
              <FormField
                control={form.control}
                name="categoryId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Danh mục</FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger className={!categoryLoading && categories.length === 0 ? "border-destructive/50" : ""}>
                          <SelectValue placeholder={categoryLoading ? "Đang tải danh mục..." : "Chọn danh mục"} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {categories.length === 0 && !categoryLoading && (
                          <div className="p-4 text-sm text-center space-y-2">
                            <p className="text-destructive font-medium">Không tìm thấy danh mục</p>
                            <Button 
                              type="button"
                              variant="outline" 
                              size="sm" 
                              className="h-7 text-xs"
                              onClick={() => dispatch(getAllCategoriesThunk({ page: 0, size: 100, sort: "name", direction: "asc" }))}
                            >
                              Thử lại
                            </Button>
                          </div>
                        )}
                        {categories.filter(c => c.status !== false).map((category) => (
                          <SelectItem key={category.id} value={category.id.toString()}>
                            {category.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {categories.length === 0 && !categoryLoading && (
                      <p className="text-[10px] text-destructive mt-1 font-medium">
                        * Bạn cần có ít nhất một danh mục để đăng bài.
                      </p>
                    )}
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="hashtag"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Hashtag liên quan</FormLabel>
                    <FormControl>
                      <Input placeholder="Ví dụ: Công nghệ AI" {...field} value={field.value || ""} />
                    </FormControl>
                    {normalizedHashtag && (
                      <div className="mt-2 flex items-center gap-2 text-xs font-mono bg-muted p-2 rounded border border-dashed">
                        <span className="text-muted-foreground">Preview:</span>
                        <span className="text-primary font-bold">#{normalizedHashtag}</span>
                        <Check className="h-3 w-3 text-green-600" />
                      </div>
                    )}
                    <FormDescription>
                      Dùng để gợi ý các bài viết liên quan.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </Card>

            <Card className="p-4 space-y-4">
              <h4 className="font-medium text-sm border-b pb-2">Tùy chọn hiển thị</h4>
              <FormField
                control={form.control}
                name="isHighlight"
                render={({ field }) => (
                  <FormItem className="flex items-center justify-between rounded-lg border p-3">
                    <div className="space-y-0.5">
                      <FormLabel className="text-sm">Nổi bật</FormLabel>
                    </div>
                    <FormControl>
                      <Switch checked={field.value} onCheckedChange={field.onChange} />
                    </FormControl>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="isFeatured"
                render={({ field }) => (
                  <FormItem className="flex items-center justify-between rounded-lg border p-3">
                    <div className="space-y-0.5">
                      <FormLabel className="text-sm">Tiêu điểm</FormLabel>
                    </div>
                    <FormControl>
                      <Switch checked={field.value} onCheckedChange={field.onChange} />
                    </FormControl>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="allowComments"
                render={({ field }) => (
                  <FormItem className="flex items-center justify-between rounded-lg border p-3">
                    <div className="space-y-0.5">
                      <FormLabel className="text-sm">Cho phép bình luận</FormLabel>
                    </div>
                    <FormControl>
                      <Switch checked={field.value} onCheckedChange={field.onChange} />
                    </FormControl>
                  </FormItem>
                )}
              />
            </Card>
          </div>
        </div>

        <div className="flex justify-end gap-4 border-t pt-6">
          <Button type="button" variant="outline" onClick={onCancel}>
            Hủy bỏ
          </Button>
          <Button type="submit" disabled={isUploading}>
            {article ? "Lưu thay đổi" : "Lưu bản nháp"}
          </Button>
        </div>
      </form>
    </Form>
  );
}
