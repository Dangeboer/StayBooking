package com.laioffer.staybooking.service;

import com.laioffer.staybooking.model.dto.GeoPoint;
import com.laioffer.staybooking.model.entity.HotEntity;
import com.laioffer.staybooking.repository.HotRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotService {
    private final HotRepository hotRepository;
    private final GeocodingService geocodingService;

    public HotService(
            HotRepository hotRepository,
            GeocodingService geocodingService
    ) {
        this.hotRepository = hotRepository;
        this.geocodingService = geocodingService;
    }


    public List<HotEntity> getHotsByDistrict(Long districtId) {
        return hotRepository.findAllByDistrictCode(districtId);
    }

    public void createHot(String name, Long districtCode, String address) {
        GeoPoint geoPoint = geocodingService.getGeoPoint(address);
        GeometryFactory geometryFactory = new GeometryFactory();

        hotRepository.save(new HotEntity(
                null,
                name,
                districtCode,
                geometryFactory.createPoint(new Coordinate(geoPoint.lon(), geoPoint.lat()))
        ));
    }
}
