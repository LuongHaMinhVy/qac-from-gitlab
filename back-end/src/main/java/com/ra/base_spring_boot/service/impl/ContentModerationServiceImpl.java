package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.BadWord;
import com.ra.base_spring_boot.repository.BadWordRepository;
import com.ra.base_spring_boot.service.ContentModerationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentModerationServiceImpl implements ContentModerationService {

    private final BadWordRepository badWordRepository;
    private final com.ra.base_spring_boot.repository.account.IAccountRepository accountRepository;

    private java.util.Map<String, BadWord> badWordsCache;
    private Pattern badWordsPattern;

    @PostConstruct
    public void init() {
        refreshBadWordsCache();
    }

    @Override
    public void validateContent(String content) {
        if (content == null || content.isEmpty() || badWordsPattern == null) {
            return;
        }

        Matcher matcher = badWordsPattern.matcher(content);
        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            BadWord badWord = badWordsCache.get(word);

            if (badWord != null
                    && badWord.getSeverity() == com.ra.base_spring_boot.model.constants.SeverityLevel.high) {
                throw new HttpBadRequest("Nội dung chứa từ khóa cấm (Mức độ nghiêm trọng): " + word);
            }
        }
    }

    @Override
    public String sanitizeContent(String content) {
        if (content == null || content.isEmpty() || badWordsPattern == null) {
            return content;
        }

        Matcher matcher = badWordsPattern.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String wordKey = matcher.group().toLowerCase();
            BadWord badWord = badWordsCache.get(wordKey);

            String replacement = "***";
            if (badWord != null && badWord.getReplacement() != null && !badWord.getReplacement().isEmpty()) {
                replacement = badWord.getReplacement();
            } else {
                replacement = "*".repeat(matcher.group().length());
            }

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @Override
    public boolean containsToxicContent(String content) {
        if (content == null || content.isEmpty() || badWordsPattern == null) {
            return false;
        }
        Matcher matcher = badWordsPattern.matcher(content);
        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            BadWord badWord = badWordsCache.get(word);
            if (badWord != null
                    && badWord.getSeverity() == com.ra.base_spring_boot.model.constants.SeverityLevel.high) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void refreshBadWordsCache() {
        List<BadWord> activeBadWords = badWordRepository.findAllByIsActiveTrue();

        badWordsCache = new java.util.HashMap<>();
        for (BadWord bw : activeBadWords) {
            String key = bw.getWord().trim().replaceAll("\\s+", " ").toLowerCase();

            if (badWordsCache.containsKey(key)) {
                BadWord existing = badWordsCache.get(key);

                if (bw.getSeverity().compareTo(existing.getSeverity()) > 0) {
                    badWordsCache.put(key, bw);
                    continue;
                }

                if (bw.getSeverity() == existing.getSeverity()) {
                    boolean existingHasReplacement = existing.getReplacement() != null
                            && !existing.getReplacement().isEmpty();
                    boolean newHasReplacement = bw.getReplacement() != null && !bw.getReplacement().isEmpty();

                    if (!existingHasReplacement && newHasReplacement) {
                        badWordsCache.put(key, bw);
                    }
                }
            } else {
                badWordsCache.put(key, bw);
            }
        }

        if (!badWordsCache.isEmpty()) {
            List<String> sortedWords = badWordsCache.keySet().stream()
                    .sorted((s1, s2) -> s2.length() - s1.length())
                    .map(Pattern::quote)
                    .toList();

            String patternString = "\\b(" + String.join("|", sortedWords) + ")\\b";
            badWordsPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } else {
            badWordsPattern = null;
        }

        log.info("Refreshed bad words cache with {} words", badWordsCache.size());
    }

    @Override
    public com.ra.base_spring_boot.dto.resp.ApiResponse<BadWord> addBadWord(
            com.ra.base_spring_boot.dto.req.BadWordRequest request) {
        String normalizedWord = request.getWord().trim().replaceAll("\\s+", " ");

        if (badWordRepository.existsByWordIgnoreCase(normalizedWord)) {
            throw new HttpBadRequest("Từ khóa đã tồn tại");
        }

        BadWord badWord = BadWord.builder()
                .word(normalizedWord)
                .replacement(request.getReplacement() != null ? request.getReplacement() : "***")
                .severity(request.getSeverity() != null ? request.getSeverity()
                        : com.ra.base_spring_boot.model.constants.SeverityLevel.medium)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(getCurrentAccount())
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        BadWord saved = badWordRepository.save(badWord);
        refreshBadWordsCache();
        return com.ra.base_spring_boot.dto.resp.ApiResponse.success(saved, "Thêm từ khóa thành công");
    }

    @Override
    public com.ra.base_spring_boot.dto.resp.ApiResponse<BadWord> updateBadWord(Long id,
            com.ra.base_spring_boot.dto.req.BadWordRequest request) {
        BadWord badWord = badWordRepository.findById(id)
                .orElseThrow(() -> new com.ra.base_spring_boot.exception.HttpNotFound("Từ khóa không tồn tại"));

        if (request.getWord() != null) {
            String normalizedWord = request.getWord().trim().replaceAll("\\s+", " ");
            if (!normalizedWord.equals(badWord.getWord())) {
                if (badWordRepository.existsByWordIgnoreCase(normalizedWord)) {
                    throw new HttpBadRequest("Từ khóa đã tồn tại");
                }
                badWord.setWord(normalizedWord);
            }
        }

        if (request.getReplacement() != null)
            badWord.setReplacement(request.getReplacement());
        if (request.getSeverity() != null)
            badWord.setSeverity(request.getSeverity());
        if (request.getIsActive() != null)
            badWord.setIsActive(request.getIsActive());

        badWord.setUpdatedAt(java.time.LocalDateTime.now());

        BadWord saved = badWordRepository.save(badWord);
        refreshBadWordsCache();
        return com.ra.base_spring_boot.dto.resp.ApiResponse.success(saved, "Cập nhật từ khóa thành công");
    }

    @Override
    public com.ra.base_spring_boot.dto.resp.ApiResponse<String> deleteBadWord(Long id) {
        if (!badWordRepository.existsById(id)) {
            throw new com.ra.base_spring_boot.exception.HttpNotFound("Từ khóa không tồn tại");
        }
        badWordRepository.deleteById(id);
        refreshBadWordsCache();
        return com.ra.base_spring_boot.dto.resp.ApiResponse.success(null, "Xóa từ khóa thành công");
    }

    @Override
    public com.ra.base_spring_boot.dto.resp.ApiResponse<List<BadWord>> getAllBadWords() {
        return com.ra.base_spring_boot.dto.resp.ApiResponse.success(badWordRepository.findAll(),
                "Lấy danh sách từ khóa thành công");
    }

    private com.ra.base_spring_boot.model.Account getCurrentAccount() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String username = auth.getName();
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new com.ra.base_spring_boot.exception.HttpNotFound("Account not found"));
    }
}
