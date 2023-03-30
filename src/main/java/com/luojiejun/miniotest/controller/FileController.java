package com.luojiejun.miniotest.controller;

import com.luojiejun.miniotest.entity.MinioResponseDTO;
import com.luojiejun.miniotest.service.MinioService;
import com.luojiejun.miniotest.utils.AjaxResult;
import com.luojiejun.miniotest.utils.StreamUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/minio")
@Slf4j
public class FileController {

    @Resource
    private MinioService minioService;

    @Resource
    private MinioClient minioClient;


    @GetMapping("/getBucketNames")
    public AjaxResult listBucketNames(){
        List list = minioService.listBucketName();
        return AjaxResult.success(list);
    }

    /**
     * 上传文件
     * @param files
     * @return
     */
    @PostMapping("/uploadFiles")
    public AjaxResult uploadFiles(@RequestParam("files") List<MultipartFile> files){
        log.info(files.toString());
        if (CollectionUtils.isEmpty(files)){
            return AjaxResult.error("未选择文件");
        }
        List<MinioResponseDTO> minioResponseDTOS = minioService.uploadFile(files);
        if (minioResponseDTOS == null){
            return AjaxResult.error("服务器异常，文件上传错误");
        }
        return AjaxResult.success(minioResponseDTOS);
    }

    /**
     * 创建存储桶
     * @param bucketName
     * @return
     */
    @PostMapping("/makeBucket")
    public AjaxResult makeBucket(String bucketName){
        if (minioService.makeBucket(bucketName)){
            return AjaxResult.success("success");
        }
        return AjaxResult.error("error");
    }

    /**
     * 下载文件到本地
     * @param objectName
     * @param fileName
     * @return
     */
    @GetMapping("/downloadObject")
    public AjaxResult downloadObject(String objectName,String fileName){
        boolean b = minioService.downloadObject(objectName, fileName);
        return AjaxResult.success(b);
    }

    /**
     * 获取对象的元数据
     * @param objectName
     * @return
     */
    @GetMapping("/getObject")
    public AjaxResult getObject(String objectName){
        ObjectStat object = minioService.getObject(objectName);
        log.info(object.toString());
        if (object != null){
            return AjaxResult.success(object);
        }
        return AjaxResult.error("获取文件失败");
    }

    @GetMapping("/download")
    public void downloadFile(String objectName,HttpServletResponse response,String fileSimpleName)  {
        BufferedOutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        // 读取文件内容
        File file = new File("D:\\huntercatCode\\minioTest\\123.txt");
        try{
            fileInputStream = new FileInputStream(file);
            inputStream = StreamUtils.getInputStream(fileInputStream);
//            inputStream = minioService.getObjectStream(objectName);
//            ObjectStat object = minioService.getObject(objectName);

            outputStream = new BufferedOutputStream(response.getOutputStream());
            response.reset();
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileSimpleName, "UTF-8"));
            response.addHeader("Content-Length", String.valueOf(file.length()));
            // 指明response的返回对象是文件流
            response.setContentType("application/octet-stream");
            log.debug("开始读取文件流");

            //返回从该输入流中可以读取（或跳过）的字节数的估计值，而不会被下一次调用此输入流的方法阻塞
            byte[] bytes = new byte[inputStream.available()];
            //将pdf的内容写入bytes中
            inputStream.read(bytes);
            //将bytes中的内容写入
            outputStream.write(bytes);
            //刷新输出流，否则不会写出数据
            outputStream.flush();

            log.info("======文件下载处理完成==========");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    @GetMapping("/getFile")
    public void downloadFile2(String fileName, String originalName, HttpServletResponse response){
        String bucketName = "minio-dve";
        try {

            InputStream file = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            String filename = new String(fileName.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
            if (StringUtils.isNotEmpty(originalName)) {
                fileName = originalName;
            }
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            ServletOutputStream servletOutputStream = response.getOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = file.read(buffer)) > 0) {
                servletOutputStream.write(buffer, 0, len);
            }
            servletOutputStream.flush();
            file.close();
            servletOutputStream.close();
        } catch (ErrorResponseException e) {
            log.error("ErrorResponseException",e);
        } catch (Exception e) {
            log.error("Exception",e);
        }
    }

    @GetMapping("/downloadFile")
    public void downloadFile(HttpServletResponse response,String objectName,String FileName) throws IOException {
        String bucketName = "minio-dve";
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {

            // 获取文件对象
            inputStream = minioClient.getObject(bucketName, objectName);
            byte buf[] = new byte[1024];
            int length = 0;
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(FileName, "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            outputStream= response.getOutputStream();
            // 输出文件
            while ((length = inputStream.read(buf)) > 0) {

                outputStream.write(buf, 0, length);
            }
            // 关闭输出流
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {

            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败";
            OutputStream ps = response.getOutputStream();
            ps.write(data.getBytes("UTF-8"));
        }finally {
            try{
                if (inputStream != null){
                    inputStream.close();
                }
                if (outputStream != null){
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
