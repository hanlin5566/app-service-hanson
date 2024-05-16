package com.iss.hanson.hanson.serevice.internal;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iss.hanson.hanson.common.bo.FileBo;
import com.iss.hanson.hanson.dao.domain.FilePo;
import com.iss.hanson.hanson.dao.mappers.FileMapper;
import com.iss.hanson.hanson.serevice.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class OssFileServiceImpl implements FileService {
    @Value("${aliyun.oss.bucketName}")
    private  String bucketName;
    @Value("${aliyun.oss.defaultPath}")
    private  String defaultPath;
    @Value("${aliyun.oss.expiration-ms:31536000000}")
    private Long expirationConfig;
    @Value("${spring.profiles.active}")
    private String env;


    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private OSS aliyunOssClient;

    @Override
    public List<FileBo> upload(MultipartFile[] files, FileBo fileBo) {
        List<FileBo> list = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            FileBo uploadBo = BeanUtil.copyProperties(fileBo, FileBo.class);
            FilePo upload = upload(files[i], uploadBo);
            uploadBo.setId(upload.getId());
            list.add(uploadBo);
        }
        return list;
    }

    @Override
    public FilePo upload(MultipartFile file, FileBo fileBo) {
        try {
            String originalFileName = file.getOriginalFilename();
            String newFileName = getNewFileName(originalFileName);
            String newFilePath = getNewFilePath(fileBo.getModuleName());
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            fileBo.setOriginalFileName(file.getOriginalFilename());
            fileBo.setPath(newFilePath);
            fileBo.setSize(file.getSize());
            fileBo.setFileName(newFileName+"."+extension);
            //上传
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getInputStream().available());
            metadata.setCacheControl("no-cache");
            metadata.setHeader("Pragma", "no-cache");
            metadata.setContentType(getContentType(extension));
            aliyunOssClient.putObject(bucketName, fileBo.getPath()+"/"+fileBo.getFileName(), file.getInputStream(),metadata);
            // 指定签名URL过期时间一年。
            Date expiration = new Date(System.currentTimeMillis() +  expirationConfig);
            URL url = aliyunOssClient.generatePresignedUrl(bucketName, fileBo.getFileName(), expiration);
            fileBo.setUrl("https://"+url.getHost()+"/"+fileBo.getPath()+"/"+fileBo.getFileName());
            FilePo filePo = BeanUtil.copyProperties(fileBo, FilePo.class);
            fileMapper.insert(filePo);
        } catch (IOException e) {
            log.error("file upload failed",e);
        }
        QueryWrapper<FilePo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FilePo::getFileName,fileBo.getFileName());
        return fileMapper.selectOne(queryWrapper);
    }

    @Override
    public InputStream download(String fileId) {
        FilePo filePo = getFilePo(fileId);
        InputStream content = download(filePo);
        return content;
    }

    @Override
    public InputStream download(FilePo filePo) {
        // 调用ossClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
        OSSObject ossObject = aliyunOssClient.getObject(bucketName, filePo.getPath()+"/"+filePo.getFileName());
        // 调用ossObject.getObjectContent获取文件输入流，可读取此输入流获取其内容。
        InputStream content = ossObject.getObjectContent();
        return content;
    }

    @Override
    public InputStream downloads(List<FilePo> FilePo) {
        return null;
    }

    @Override
    public FilePo getFilePo(String fileId) {
        return fileMapper.selectById(fileId);
    }

//     public String getDownloadableUrl(String fileName) {
// //        Date expiration = new Date(new Date().getTime() + EXPIRATION_TIME);
//         Date expiration = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
//         URL url = aliyunOssClient.generatePresignedUrl(aliyunConstants.getAliyun_oss_bucket_name(), fileName, expiration);
//         String urlString = url.toString().replace("http:", "https:");
//         String fileUrl = urlString.substring(0, urlString.indexOf("?"));
//         return fileUrl;
//     }

    // 获取文件夹名称
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String getNewFileName(String originalFileName) {
        return IdUtil.fastSimpleUUID();
    }

    public String getNewFilePath(String moudle) {
        LocalDate now = LocalDate.now();
        String yyyyMMdd = now.format(dateFormat);
        if(StringUtils.isNotBlank(defaultPath)){
            return defaultPath + "/" + env + "/" + moudle + "/" + yyyyMMdd;
        }else{
            return env + "/" + moudle + "/" + yyyyMMdd;
        }

    }

    public static String getContentType(String filenameExtension) {
        if (filenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (filenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (filenameExtension.equalsIgnoreCase(".jpeg") ||
                filenameExtension.equalsIgnoreCase(".jpg") ||
                filenameExtension.equalsIgnoreCase(".png")) {
            return "image/jpg";
        }
        if (filenameExtension.equalsIgnoreCase(".html")) {
            return "text/html";
        }
        if (filenameExtension.equalsIgnoreCase(".txt")) {
            return "text/plain";
        }
        if (filenameExtension.equalsIgnoreCase(".vsd")) {
            return "application/vnd.visio";
        }
        if (filenameExtension.equalsIgnoreCase(".pptx") ||
                filenameExtension.equalsIgnoreCase(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (filenameExtension.equalsIgnoreCase(".docx") ||
                filenameExtension.equalsIgnoreCase(".doc")) {
            return "application/msword";
        }
        if (filenameExtension.equalsIgnoreCase(".xml")) {
            return "text/xml";
        }
        if (filenameExtension.equalsIgnoreCase(".mp4")) {
            return "video/mp4";
        }
        if (filenameExtension.equalsIgnoreCase(".pdf")) {
            return "application/pdf";
        }
        if (filenameExtension.equalsIgnoreCase(".xls")) {
            return "application/vnd.ms-excel";
        }
        if (filenameExtension.equalsIgnoreCase(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        return "image/jpg";
    }
}
