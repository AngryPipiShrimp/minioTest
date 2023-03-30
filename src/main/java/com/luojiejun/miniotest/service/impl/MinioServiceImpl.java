package com.luojiejun.miniotest.service.impl;

import cn.hutool.core.io.FileUtil;
import com.luojiejun.miniotest.config.MinioConfig;
import com.luojiejun.miniotest.entity.MinioFile;
import com.luojiejun.miniotest.entity.MinioResponseDTO;
import com.luojiejun.miniotest.service.MinioService;
import com.luojiejun.miniotest.utils.MinioClientUtils;
import io.minio.ObjectStat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {
    @Resource
    private MinioClientUtils minioClientUtils;

    @Resource
    private MinioConfig minioConfig;

    @Value("${minio.bucketName}")
    private String bucketName;
    @Override
    public List listBucketName() {
        List<String> strings = minioClientUtils.listBucketNames();
        return strings;
    }

    /**
     * 文件上传
     * @param files
     * @return
     */
    @Override
    public List<MinioResponseDTO> uploadFile(List<MultipartFile> files) {
        ArrayList<MinioResponseDTO> response = new ArrayList<>();
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            log.info("原文件名：{}",originalFilename);

            String extName = FileUtil.extName(originalFilename);
            log.info("扩展名:{}",extName);

            long milliSecond = Instant.now().toEpochMilli();
            String minioFileName = milliSecond+ RandomStringUtils.randomNumeric(12)+"."+extName;
            log.info("minio文件名：{}",minioFileName);

            String contentType = file.getContentType();
            log.info("文件上下文类型{}",contentType);

            long size = file.getSize();
            log.info("文件大小{}",size);

            try{
                String bucketName = minioConfig.getBucketName();
                minioClientUtils.putObject(bucketName,file,minioFileName);
                String fileUrl = minioClientUtils.getObjectUrl(bucketName, minioFileName);


                MinioFile minioFile = new MinioFile();
                minioFile.setOriginalFileName(originalFilename);
                minioFile.setFileExtName(extName);
                minioFile.setFileName(minioFileName);
                minioFile.setFileSize(size);
                minioFile.setContentType(contentType);
                minioFile.setIsDelete(NumberUtils.INTEGER_ZERO);
                minioFile.setFileUrl(fileUrl);

                MinioResponseDTO minioResponseDTO = new MinioResponseDTO();
                minioResponseDTO.setOriginalFileName(originalFilename);
                minioResponseDTO.setFileUrl(fileUrl);
                minioResponseDTO.setMinioFile(minioFile);
                response.add(minioResponseDTO);

            }catch (Exception e){
                log.error("文件上传异常");
                return null;
            }
        }
        return response;
    }

    /**
     * 创建存储桶
     * @param bucketName
     * @return
     */
    @Override
    public boolean makeBucket(String bucketName) {
        boolean b = minioClientUtils.makeBucket(bucketName);
        return b;
    }

    @Override
    public boolean downloadObject(String objectName, String fileName) {
        boolean b = minioClientUtils.downloadObject(this.bucketName, objectName, fileName);
        return b;
    }

    @Override
    public ObjectStat getObject(String objectName) {
        ObjectStat objectStat = minioClientUtils.statObject(this.bucketName, objectName);
        if (objectStat != null){
            return objectStat;
        }
        return null;
    }

    @Override
    public InputStream getObjectStream(String objectNam) {
        InputStream object = minioClientUtils.getObject(this.bucketName, objectNam);
        if (object == null){
            return null;
        }
        return object;
    }
}
