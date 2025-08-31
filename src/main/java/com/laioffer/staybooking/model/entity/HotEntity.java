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
    private Long districtCode;
    private Point location;

    public HotEntity() {
    }

    public HotEntity(Long id, String name, Long districtCode, Point location) {
        this.id = id;
        this.name = name;
        this.districtCode = districtCode;
        this.location = location;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Long getDistrictCode() {
        return districtCode;
    }
    public Point getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotEntity that = (HotEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(districtCode, that.districtCode) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, districtCode, location);
    }

    @Override
    public String toString() {
        return "HotEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", districtCode='" + districtCode + '\'' +
                ", location=" + location +
                '}';
    }
}
