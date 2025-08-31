package com.laioffer.staybooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.storage.Storage;

@Service
public class ImageStorageService {

    private final String bucketName;
    private final Storage storage;

    // 这个 @Value 注解用于从 Spring 配置文件（application.yml）中获取 GCS 的存储桶（Bucket）名称。
    public ImageStorageService(@Value("${staybooking.gcs.bucket}") String bucketName, Storage storage) {
        this.bucketName = bucketName;
        this.storage = storage;
    }

    public String upload(MultipartFile file) {
        return "A fake URL of images";

        // 取消了google服务，无法使用其存储图片
//        String filename = UUID.randomUUID().toString(); // 生成一个唯一的文件名，防止文件名冲突，会返回一个随机字符串
//        BlobInfo blobInfo;
//        try {
//            blobInfo = storage.createFrom( // storage.createFrom() 方法将文件上传到 Google Cloud Storage
//                    BlobInfo
//                            .newBuilder(bucketName, filename) // 创建 GCS 文件的元信息，指定存储桶和文件名
//                            .setContentType("image/jpeg") // 指定文件类型为 JPEG 图片
//                            .setAcl(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))) // 让 所有人都能访问（公开访问），只允许读取文件（不能修改或删除）
//                            .build(), // 构建 BlobInfo 对象
//                    file.getInputStream()); // file.getInputStream() 获取文件的二进制流，然后上传到 GCS
//        } catch (IOException exception) {
//            throw new IllegalStateException("Failed to upload file to GCS");
//        }
//        return blobInfo.getMediaLink(); // 返回 文件的公开 URL，可以在浏览器中访问
    }
}

// 获取 GCS 存储桶名称（staybooking.gcs.bucket）。
// 接收前端上传的图片文件（MultipartFile file）。
// 生成一个随机文件名，防止文件名冲突。
// 构建 BlobInfo（文件信息），包括：
// JPEG 文件格式
// 设置文件为公开可读
// 将文件流上传到 Google Cloud Storage。
// 返回图片的 URL，供前端展示