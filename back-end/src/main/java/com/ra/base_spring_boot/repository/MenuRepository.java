package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Menu;
import com.ra.base_spring_boot.model.constants.MenuLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByLocationAndIsActiveTrue(MenuLocation location);

    List<Menu> findAllByIsActiveTrue();
}
