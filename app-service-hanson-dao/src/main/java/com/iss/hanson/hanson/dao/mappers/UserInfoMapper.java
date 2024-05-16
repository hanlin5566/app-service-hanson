package com.iss.hanson.hanson.dao.mappers;

import com.hanson.mybatis.MyBaseMapper;
import com.iss.hanson.hanson.dao.domain.UserInfoPo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectKey;

/**
tmp_user_info  用户示例表
* @author: huhanlin 2021-12-03 16:31:42
*/
@Mapper
public interface UserInfoMapper extends MyBaseMapper<UserInfoPo> {
}