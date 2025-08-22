package com.laioffer.staybooking.location;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.laioffer.staybooking.model.GeoPoint;
import org.springframework.stereotype.Service;

import java.io.IOException;

// 就是将String类型的地址 转换成 GeoPoint（经纬度）
@Service
public class GeocodingService {

    private final GeoApiContext context;

    public GeocodingService(GeoApiContext context) {
        this.context = context;
    }

    public GeoPoint getGeoPoint(String address) {
        try {
            // 发送地理编码请求：调用 GeocodingApi.geocode() 方法，传入 address（地址）
            // await() 方法是 同步等待 API 响应，返回 GeocodingResult 数组，这里取第一个结果 [0]
            GeocodingResult result = GeocodingApi.geocode(context, address).await()[0];
            if (result.partialMatch) { // partialMatch 表示 Google 仅找到部分匹配的地址，例如缺少具体门牌号
                throw new InvalidAddressException();
            }
            // 返回封装好的纬度和经度
            return new GeoPoint(result.geometry.location.lat, result.geometry.location.lng);
        } catch (IOException | ApiException | InterruptedException e) {
            throw new GeocodingException();
        }
    }
}