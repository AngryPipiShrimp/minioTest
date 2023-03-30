package com.luojiejun.miniotest.entity;

import lombok.Data;

@Data
public class MinioResponseDTO {
    private Long fileId;
    private String fileUrl;
    private String originalFileName;
    private MinioFile minioFile;
}
