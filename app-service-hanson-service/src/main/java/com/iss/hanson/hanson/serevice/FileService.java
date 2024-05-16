package com.iss.hanson.hanson.serevice;

import com.iss.hanson.hanson.common.bo.FileBo;
import com.iss.hanson.hanson.dao.domain.FilePo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileService {
    List<FileBo> upload(MultipartFile[] files, FileBo fileBo);
    FilePo upload(MultipartFile file, FileBo fileBo) throws IOException;
    InputStream download(String fileId);
    InputStream download(FilePo FilePo);
    InputStream downloads(List<FilePo> FilePo);
    FilePo getFilePo(String fileId);
}
