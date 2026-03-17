import { createSlice } from "@reduxjs/toolkit";
import type { MediaResponse } from "@/interfaces/media.interface";
import {
  getAllMediaThunk,
  uploadMediaThunk,
  deleteMediaThunk,
  restoreMediaThunk
} from "../thunks/media.thunk";
import type { BaseResponse } from "@/utils/response-data";

interface MediaState {
  library: MediaResponse[];
  loading: boolean;
  uploading: boolean;
  error: string | null;
  pagination: BaseResponse<MediaResponse>["pagination"] | null;
  lastUploadedMedia: MediaResponse | null;
}

const initialState: MediaState = {
  library: [],
  loading: false,
  uploading: false,
  error: null,
  pagination: null,
  lastUploadedMedia: null,
};

const mediaSlice = createSlice({
  name: "media",
  initialState,
  reducers: {
    clearLastUploadedMedia: (state) => {
      state.lastUploadedMedia = null;
    },
    clearMediaError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Get Library
      .addCase(getAllMediaThunk.pending, (state) => {
        state.loading = true;
      })
      .addCase(getAllMediaThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.library = action.payload.data;
        state.pagination = action.payload.pagination;
      })
      .addCase(getAllMediaThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload?.message || "Failed to fetch media library";
      })
      // Upload
      .addCase(uploadMediaThunk.pending, (state) => {
        state.uploading = true;
      })
      .addCase(uploadMediaThunk.fulfilled, (state, action) => {
        state.uploading = false;
        state.lastUploadedMedia = action.payload.data;
        // Add to library if not already there (though backend handles duplication, FE should refresh or prepend)
        const exists = state.library.some(m => m.id === action.payload.data.id);
        if (!exists) {
            state.library.unshift(action.payload.data);
        }
      })
      .addCase(uploadMediaThunk.rejected, (state, action) => {
        state.uploading = false;
        state.error = action.payload?.message || "Upload failed";
      })
      // Soft Delete
      .addCase(deleteMediaThunk.fulfilled, (state, action) => {
        const id = action.meta.arg;
        state.library = state.library.filter(m => m.id !== id);
      })
      // Restore
      .addCase(restoreMediaThunk.fulfilled, (state, action) => {
        state.library.unshift(action.payload.data);
      });
  },
});

export const { clearLastUploadedMedia, clearMediaError } = mediaSlice.actions;
export default mediaSlice.reducer;
