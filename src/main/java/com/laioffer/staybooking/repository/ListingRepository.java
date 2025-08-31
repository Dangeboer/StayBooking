package com.laioffer.staybooking.repository;

import com.laioffer.staybooking.model.entity.ListingEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<ListingEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM listings WHERE id = :listingId", nativeQuery = true)
    Optional<ListingEntity> findByIdForUpdate(long listingId);

    List<ListingEntity> findAllByHostId(Long hostId);

    // 查找符合条件的、在指定时间范围内可用的房源
    @Query(value = """
           SELECT l.*
           FROM listings l
           LEFT JOIN bookings b -- 连表查询，相当于两个表一块看；Left表示留下所有left表，即listing
               ON l.id = b.listing_id -- LEFT JOIN 会尝试将 listings 和 bookings 表进行匹配，下面两个是条件，是冲突条件，满足条件之后就b的listing_id就不会是null了
               AND b.check_in_date < :checkOut -- 预订的入住日期早于查询的退房日期
               AND b.check_out_date > :checkIn -- 预订的退房日期晚于查询的入住日期
           WHERE
               ST_DWithin(CAST(l.location AS geography), CAST(ST_MakePoint(:lon, :lat) AS geography), :distance)
               AND l.guest_number >= :guestNum
               AND b.listing_id IS NULL;
           """, nativeQuery = true)
    List<ListingEntity> searchListings(double lat, double lon, double distance, LocalDate checkIn, LocalDate checkOut, int guestNum);
}

// ST_DWithin( -- 过滤在 distance 范围内的房源
// CAST(l.location AS geography), -- 是 listings 表中的位置字段（存储房源的地理坐标）
// CAST(ST_MakePoint(:lon, :lat) AS geography), :distance)  -- 创建一个查询点（用户指定的搜索位置）-- 这里 CAST 的作用是将 location 和 ST_MakePoint(:lon, :lat) 转换为 geography 类型，使它们可以用于 ST_DWithin 进行地理范围查询
// AND l.guest_number >= :guestNum
// AND b.listing_id IS NULL; -- 如果某个房源在刚刚的left join里匹配到了（即有冲突），那么 b.listing_id 就不是 NULL，在这里就会被过滤掉（where 是只取符合条件的）