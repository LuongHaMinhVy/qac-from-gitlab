package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.SystemSetting;
import com.ra.base_spring_boot.model.constants.SettingCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

        Optional<SystemSetting> findBySettingKey(String settingKey);

        boolean existsBySettingKey(String settingKey);

        List<SystemSetting> findByCategory(SettingCategory category);

        List<SystemSetting> findByIsPublicTrue();

        List<SystemSetting> findByCategoryAndIsPublicTrue(SettingCategory category);

        List<SystemSetting> findBySettingKeyIn(List<String> keys);

        @Query("SELECT s FROM SystemSetting s WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR " +
                        "LOWER(s.settingKey) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.value) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:category IS NULL OR s.category = :category)")
        Page<SystemSetting> searchSettings(
                        @Param("keyword") String keyword,
                        @Param("category") SettingCategory category,
                        Pageable pageable);
}
