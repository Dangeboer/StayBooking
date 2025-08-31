package com.laioffer.staybooking.model.entity;

import jakarta.persistence.*;

import org.locationtech.jts.geom.Point;
import java.util.Objects;

@Entity
@Table(name = "hots")
public class HotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    @Column(name = "district_code")
    private Long districtCode;
    private Point location;

    public HotEntity() {
    }

    public HotEntity(Long id, String name, String address, Long districtCode, Point location) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.districtCode = districtCode;
        this.location = location;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public Long getDistrictCode() {
        return districtCode;
    }
    public Point getLocation() {
        return location;
    }
}
