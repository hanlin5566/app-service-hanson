package com.iss.hanson.hanson.serevice;

import com.hanson.mybatis.domain.PageQuery;
import com.hanson.rest.PageResult;
import com.iss.hanson.hanson.common.bo.UserInfoBo;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * @author Hanson
 * @date 2021/11/22  22:12
 */
public interface UserInfoService {
	String loginByUserCode(String userCode);
	UserInfoBo save(UserInfoBo userInfoBo);
	Integer del(Long id);
	UserInfoBo get(Long id);
	UserInfoBo getByCode(String userCode);
	List<UserInfoBo> list(UserInfoBo userInfoBo);
	PageResult<UserInfoBo> page(PageQuery<UserInfoBo> pageQuery);
}
