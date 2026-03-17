package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.req.VideoRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.dto.resp.VideoResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Category;
import com.ra.base_spring_boot.model.Media;
import com.ra.base_spring_boot.model.Video;
import com.ra.base_spring_boot.model.constants.VideoSource;
import com.ra.base_spring_boot.model.constants.VideoStatus;
import com.ra.base_spring_boot.repository.CategoryRepository;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.VideoRepository;
import com.ra.base_spring_boot.service.MediaService;
import com.ra.base_spring_boot.service.VideoService;
import com.ra.base_spring_boot.service.ContentModerationService;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final CategoryRepository categoryRepository;
    private final IAccountRepository accountRepository;
    private final MediaService mediaService;
    private final ContentModerationService contentModerationService;

    @Override
    @Transactional
    public ApiResponse<VideoResponse> createVideo(VideoRequest request,
            MultipartFile thumbnailFile, MultipartFile videoFile) {
        VideoStatus initialStatus = request.getStatus() != null ? request.getStatus() : VideoStatus.draft;

        if (initialStatus != VideoStatus.draft) {
            boolean isEditorOrAdmin = hasRole("ROLE_EDITOR") || hasRole("ROLE_ADMIN");
            if (!isEditorOrAdmin) {
                throw new HttpForbiden("Tác giả chỉ có thể tạo video ở trạng thái Nháp");
            }
        }

        Account author = getCurrentAccount();
        contentModerationService.validateContent(request.getTitle());
        contentModerationService.validateContent(request.getDescription());

        String embedCode = "";
        String videoUrl = request.getVideoUrl();
        VideoSource source = request.getSource() != null ? request.getSource() : VideoSource.youtube;

        if (source == VideoSource.youtube) {
            String videoId = extractYoutubeId(videoUrl);
            if (videoId == null) {
                throw new HttpBadRequest("URL Video YouTube không hợp lệ.");
            }
            embedCode = "https://www.youtube.com/embed/" + videoId;
        } else if (source == VideoSource.vimeo) {
            String videoId = extractVimeoId(videoUrl);
            if (videoId == null) {
                throw new HttpBadRequest("URL Video Vimeo không hợp lệ.");
            }
            embedCode = "https://player.vimeo.com/video/" + videoId;
        } else if (source == VideoSource.upload) {
            if (videoFile == null || videoFile.isEmpty()) {
                throw new HttpBadRequest("Vui lòng tải lên file video.");
            }
            Media videoMedia = mediaService.uploadFile(videoFile);
            videoUrl = videoMedia.getFileUrl();
            embedCode = videoUrl;
        }

        Media thumbnail = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnail = mediaService.uploadFile(thumbnailFile);
        } else if (request.getMediaId() != null) {
            thumbnail = mediaService.findById(request.getMediaId());
        } else if (source == VideoSource.youtube) {
            String videoId = extractYoutubeId(videoUrl);
            String youtubeThumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
            thumbnail = mediaService.createExternal(youtubeThumbnailUrl, "youtube_thumbnail_" + videoId, author);
        }

        Video video = Video.builder()
                .title(request.getTitle())
                .slug(toSlug(request.getTitle()))
                .description(request.getDescription())
                .videoUrl(videoUrl)
                .embedCode(embedCode)
                .thumbnail(thumbnail)
                .source(source)
                .status(initialStatus)
                .category(request.getCategoryId() != null
                        ? categoryRepository.findById(request.getCategoryId()).orElse(null)
                        : null)
                .author(author)
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .publishedAt(initialStatus == VideoStatus.published ? LocalDateTime.now() : null)
                .build();

        Video savedVideo = videoRepository.save(video);
        return ApiResponse.success(mapToResponse(savedVideo), "Tạo video thành công");
    }

    @Override
    @Transactional
    public ApiResponse<VideoResponse> updateVideo(Long id, VideoRequest request,
            MultipartFile thumbnailFile, MultipartFile videoFile) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Video không tồn tại"));

        checkPrivilegeAndOwnership(video);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            contentModerationService.validateContent(request.getTitle());
            video.setTitle(request.getTitle());
            video.setSlug(toSlug(request.getTitle()));
        }
        if (request.getDescription() != null) {
            contentModerationService.validateContent(request.getDescription());
            video.setDescription(request.getDescription());
        }

        VideoSource source = request.getSource() != null ? request.getSource() : video.getSource();
        video.setSource(source);

        if (source == VideoSource.upload) {
            if (videoFile != null && !videoFile.isEmpty()) {
                Media videoMedia = mediaService.uploadFile(videoFile);
                video.setVideoUrl(videoMedia.getFileUrl());
                video.setEmbedCode(videoMedia.getFileUrl());
            }
        } else {
            if (request.getVideoUrl() != null && !request.getVideoUrl().isBlank()) {
                video.setVideoUrl(request.getVideoUrl());
                if (source == VideoSource.youtube) {
                    String videoId = extractYoutubeId(request.getVideoUrl());
                    if (videoId != null) {
                        video.setEmbedCode("https://www.youtube.com/embed/" + videoId);
                    }
                } else if (source == VideoSource.vimeo) {
                    String videoId = extractVimeoId(request.getVideoUrl());
                    if (videoId != null) {
                        video.setEmbedCode("https://player.vimeo.com/video/" + videoId);
                    }
                }
            }
        }

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            video.setThumbnail(mediaService.uploadFile(thumbnailFile));
        } else if (request.getMediaId() != null) {
            video.setThumbnail(mediaService.findById(request.getMediaId()));
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new HttpNotFound("Danh mục không tồn tại"));
            video.setCategory(category);
        }

        if (request.getStatus() != null && request.getStatus() != video.getStatus()) {
            boolean isEditorOrAdmin = hasRole("ROLE_EDITOR") || hasRole("ROLE_ADMIN");
            if (!isEditorOrAdmin) {
                throw new HttpForbiden("Bạn không có quyền thay đổi trạng thái video");
            }
            video.setStatus(request.getStatus());
            if (request.getStatus() == VideoStatus.published && video.getPublishedAt() == null) {
                video.setPublishedAt(LocalDateTime.now());
            }
        }

        video.setUpdatedAt(LocalDateTime.now());
        Video savedVideo = videoRepository.save(video);
        return ApiResponse.success(mapToResponse(savedVideo), "Cập nhật video thành công");
    }

    @Override
    public ApiResponse<String> deleteVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Video không tồn tại"));

        checkPrivilegeAndOwnership(video);

        videoRepository.deleteById(id);
        return ApiResponse.success(null, "Xóa video thành công");
    }

    @Override
    @Transactional
    public ApiResponse<VideoResponse> getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Video không tồn tại"));

        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);

        return ApiResponse.success(mapToResponse(video), "Lấy thông tin video thành công");
    }

    @Override
    @Transactional
    public ApiResponse<VideoResponse> getVideoBySlug(String slug) {
        Video video = videoRepository.findBySlug(slug)
                .orElseThrow(() -> new HttpNotFound("Video không tồn tại"));

        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);

        return ApiResponse.success(mapToResponse(video), "Lấy thông tin video thành công");
    }

    @Override
    public ApiResponse<List<VideoResponse>> getAllVideos(String search, VideoStatus status, Long categoryId,
            int page,
            int size, String sort, String direction) {
        Sort sorting = Sort.by(direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Video> videos = videoRepository.findAllWithFilters(status, categoryId, search, pageable);
        List<VideoResponse> responseList = videos.map(this::mapToResponse).getContent();

        Pagination pagination = Pagination.builder()
                .currentPage(videos.getNumber())
                .pageSize(videos.getSize())
                .totalElements(videos.getTotalElements())
                .totalPages(videos.getTotalPages())
                .build();

        return ApiResponse.success(responseList, "Lấy danh sách video thành công", pagination);
    }

    private void checkPrivilegeAndOwnership(Video video) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasUpdateAll = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionCode.ARTICLE_UPDATE_ALL));

        if (hasUpdateAll) {
            return;
        }

        Account currentAccount = getCurrentAccount();
        if (!video.getAuthor().getAccountId().equals(currentAccount.getAccountId())) {
            throw new HttpForbiden("Bạn không có quyền thực hiện thao tác này");
        }
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    private Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new HttpForbiden("Bạn cần đăng nhập để thực hiện thao tác này");
        }
        String username = authentication.getName();
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("Tài khoản không tồn tại"));
    }

    private VideoResponse mapToResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .slug(video.getSlug())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnail(video.getThumbnail() != null ? mediaService.convertToDTO(video.getThumbnail()) : null)
                .embedCode(video.getEmbedCode())
                .duration(video.getDuration())
                .categoryName(video.getCategory() != null ? video.getCategory().getName() : null)
                .categoryId(video.getCategory() != null ? video.getCategory().getId() : null)
                .authorName(video.getAuthor().getUsername())
                .authorId(Long.valueOf(video.getAuthor().getAccountId()))
                .status(video.getStatus())
                .source(video.getSource())
                .viewCount(video.getViewCount())
                .likeCount(video.getLikeCount())
                .commentCount(video.getCommentCount())
                .publishedAt(video.getPublishedAt())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }

    private String toSlug(String input) {
        String nowhitespace = Pattern.compile("[\\s]").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    private String extractYoutubeId(String url) {
        if (url == null)
            return null;
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractVimeoId(String url) {
        if (url == null)
            return null;
        String pattern = "(?:vimeo\\.com\\/)([0-9]+)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
