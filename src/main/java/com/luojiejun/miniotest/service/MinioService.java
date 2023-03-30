package com.luojiejun.miniotest.service;

import com.luojiejun.miniotest.entity.MinioResponseDTO;
import io.minio.ObjectStat;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface MinioService {

    /**
     * 查询桶名称
     * @return
     */
    List listBucketName();

    /**
     * 上传文件
     * @param files
     * @return
     */
    List<MinioResponseDTO> uploadFile(List<MultipartFile> files);

    /**
     * 创建数据桶
     * @param bucketName
     * @return
     */

    boolean makeBucket(String bucketName);

    boolean downloadObject(String objectName, String fileName);

    ObjectStat getObject(String objectName);

    InputStream getObjectStream(String objectNam);
}
