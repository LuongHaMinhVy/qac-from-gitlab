
export type VideoStatus = "DRAFT" | "PUBLISHED" | "PRIVATE";

export interface GetAllVideosParams {
  search?: string;
  status?: VideoStatus;
  categoryId?: number;
  page?: number;    
  size?: number;      
  sort?: string;     
  direction?: "asc" | "desc";
}

export interface VideoRequest {
  title: string;
  description?: string;
  sourceType: "YOUTUBE" | "VIDEO" | "UPLOAD";
  videoUrl?: string;  
  categoryId: number;
  status?: VideoStatus;
}