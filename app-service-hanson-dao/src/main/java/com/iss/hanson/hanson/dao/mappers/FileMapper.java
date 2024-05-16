package com.iss.hanson.hanson.dao.mappers;

import com.hanson.mybatis.MyBaseMapper;
import com.iss.hanson.hanson.dao.domain.FilePo;
import org.apache.ibatis.annotations.Mapper;

/**
* sys_file  云文件表
* @author: huhanlin 2022-11-08 15:24:35
*/
@Mapper
public interface FileMapper extends MyBaseMapper<FilePo> {
}