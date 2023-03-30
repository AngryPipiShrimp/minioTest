package com.luojiejun.miniotest.entity;

import lombok.Data;

@Data
public class MinioFile {
    private String originalFileName;
    private String fileExtName;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private Integer isDelete;
    private String fileUrl;
}
