package com.laioffer.staybooking.repository;

import com.laioffer.staybooking.model.entity.HotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotRepository extends JpaRepository<HotEntity, Long> {
    HotEntity findById(long id);

    List<HotEntity> findAllByDistrictCode(Long districtCode);
}
