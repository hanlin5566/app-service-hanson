package com.iss.hanson.hanson.serevice.oss;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunOssConfig {

    @Value("${aliyun.oss.endPoint}")
    private  String endPoint;
    @Value("${aliyun.oss.accessKeyId}")
    private  String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private  String accessKeySecret;

    @Bean
    public OSS initOssClient() {
        OSS ossClient = new OSSClientBuilder().build(
            endPoint,
            accessKeyId,
            accessKeySecret);
        return ossClient;
    }
}
