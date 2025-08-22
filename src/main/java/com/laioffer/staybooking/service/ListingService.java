package com.laioffer.staybooking.service;

import com.laioffer.staybooking.exception.DeleteListingNotAllowedException;
import com.laioffer.staybooking.exception.InvalidListingSearchException;
import com.laioffer.staybooking.model.GeoPoint;
import com.laioffer.staybooking.model.dto.ListingDto;
import com.laioffer.staybooking.model.entity.ListingEntity;
import com.laioffer.staybooking.repository.ListingRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListingService {
    private final BookingService bookingService;
    private final GeocodingService geocodingService;
    private final ImageStorageService imageStorageService;
    private final ListingRepository listingRepository;

    public ListingService(
            BookingService bookingService,
            GeocodingService geocodingService,
            ImageStorageService imageStorageService,
            ListingRepository listingRepository
    ) {
        this.bookingService = bookingService;
        this.geocodingService = geocodingService;
        this.imageStorageService = imageStorageService;
        this.listingRepository = listingRepository;
    }

    // 通过房东id找到房源信息，找到 entity 之后先转换成 DTO 再返回
    public List<ListingDto> getListings(Long hostId) {
        return listingRepository.findAllByHostId(hostId)
                .stream()
                .map(ListingDto::new)
                .toList();
    }

    // 创建新的房源信息
    public void createListing(
            long hostId,
            String name,
            String address,
            String description,
            int guestNumber,
            List<MultipartFile> images) {
        // 得到图片的URL
        List<String> uploadedUrls = images.parallelStream()
                .filter(image -> !image.isEmpty())
                .map(imageStorageService::upload)
                .toList();

        // 把String的地址转换成经纬度
        GeoPoint geoPoint = geocodingService.getGeoPoint(address);

        GeometryFactory geometryFactory = new GeometryFactory();

        listingRepository.save(new ListingEntity(
                null,
                hostId,
                name,
                address,
                description,
                guestNumber,
                uploadedUrls,
                geometryFactory.createPoint(new Coordinate(geoPoint.lon(), geoPoint.lat())) // 转成一个point
        ));
    }

    // 通过房东id删除房源
    public void deleteListing(long hostId, long listingId) {
        ListingEntity listing = listingRepository.getReferenceById(listingId);
        // 不是自己的房源不能删
        if (listing.getHostId() != hostId) {
            throw new DeleteListingNotAllowedException("Host " + hostId + " not allowed to delete listing " + listingId);
        }
        // 如果正活跃，也就是已经被预定，不能删除
        if (bookingService.existsActiveBookings(listingId)) {
            throw new DeleteListingNotAllowedException("Active bookings exist, not allowed to delete listing " + listingId);
        }
        listingRepository.deleteById(listingId); // 关于这里为什么listingRepository里不用写这个方法，而BookingRepository里需要，个人理解是因为这里是主键，就可以用默认的方法
    }

    // 搜索可用房源
    public List<ListingDto> search(
            double lat,
            double lon,
            int distance,
            LocalDate checkIn,
            LocalDate checkOut,
            int guestNum
    ) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new InvalidListingSearchException("Invalid latitude or longitude.");
        }
        if (distance <= 0) {
            throw new InvalidListingSearchException("Distance must be positive.");
        }
        if (checkIn.isAfter(checkOut)) {
            throw new InvalidListingSearchException("Check-in date must be before check-out date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidListingSearchException("Check-in date must be in the future.");
        }
        return listingRepository.searchListings(
                        lat,
                        lon,
                        distance,
                        checkIn,
                        checkOut,
                        guestNum
                )
                .stream()
                .map(ListingDto::new)
                .toList();
    }
}
