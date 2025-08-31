package com.laioffer.staybooking.controller;

import com.laioffer.staybooking.model.HotRequest;
import com.laioffer.staybooking.model.entity.HotEntity;
import com.laioffer.staybooking.model.entity.UserEntity;
import com.laioffer.staybooking.service.HotService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hots")
public class HotController {
    private final HotService hotService;
    public HotController(HotService hotService) {
        this.hotService = hotService;
    }

    @GetMapping
    public List<HotEntity> getHotsByDistrict(Long districtId) {
        return hotService.getHotsByDistrict(districtId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createHot(@AuthenticationPrincipal UserEntity user, @RequestBody HotRequest hotRequest) {
        hotService.createHot(hotRequest.name(), hotRequest.districtCode(), hotRequest.address());
    }
}
