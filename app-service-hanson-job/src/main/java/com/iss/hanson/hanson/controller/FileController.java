package com.iss.hanson.hanson.controller;

import cn.hutool.core.io.IoUtil;
import com.hanson.rest.SimpleResult;
import com.iss.hanson.hanson.common.bo.FileBo;
import com.iss.hanson.hanson.dao.domain.FilePo;
import com.iss.hanson.hanson.serevice.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@Api(value = "文件上传相关接口")
public class FileController {
    @Autowired
    private FileService fileService;

    @Value("${aliyun.oss.defaultModuleName:sys_file}")
    private String moduleName;

    @PostMapping("/file/upload")
    @ApiOperation(value = "接口名称:上传文件", notes = "接口描述:上传文件")
    public SimpleResult<List<FileBo>> upload(@RequestParam("file") MultipartFile[] file, @RequestHeader Map<String, String> headsMap){
        FileBo build = FileBo.builder()
                             .moduleName(moduleName)
                             .build();
        List<FileBo> upload = fileService.upload(file, build);
        return SimpleResult.success(upload);
    }

    @GetMapping("/file/download/{fileId}")
    @ApiOperation("单独文件下载")
    public void download(HttpServletResponse response, @PathVariable String fileId) throws Exception {
        FilePo filePo = fileService.getFilePo(fileId);
        InputStream content = fileService.download(filePo);
        if(Objects.isNull(content)){
            return;
        }
        // response.setHeader("content-Type", "application/vnd.ms-excel");
        String fileName = filePo.getOriginalFileName();
        fileName = new String(fileName.getBytes(), "ISO-8859-1");
        // 下载文件的默认名称
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        //编码
        response.setCharacterEncoding("UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IoUtil.copy(content,baos);
        response.setHeader("Content-Length", String.valueOf(baos.size()));
        response.getOutputStream().write( baos.toByteArray() );
        content.close();
        baos.close();
    }
}
