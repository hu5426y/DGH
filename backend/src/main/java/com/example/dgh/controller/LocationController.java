package com.example.dgh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dgh.dto.LocationOptionResponse;
import com.example.dgh.entity.Location;
import com.example.dgh.mapper.LocationMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationMapper locationMapper;

    public LocationController(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    @GetMapping
    public List<LocationOptionResponse> list() {
        return locationMapper.selectList(new LambdaQueryWrapper<Location>().eq(Location::getIsActive, 1))
            .stream()
            .map(location -> new LocationOptionResponse(location.getId(), location.getName()))
            .collect(Collectors.toList());
    }
}
