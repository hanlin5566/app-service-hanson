package com.iss.hanson.hanson.serevice.internal;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hanson.mybatis.domain.PageQuery;
import com.hanson.mybatis.enums.DeletedEnum;
import com.hanson.mybatis.handler.EncryptColumn;
import com.hanson.mybatis.util.PageUtil;
import com.hanson.rest.BizException;
import com.hanson.rest.PageResult;
import com.iss.hanson.hanson.common.bo.UserInfoBo;
import com.iss.hanson.hanson.common.constant.CacheKeyConstant;
import com.iss.hanson.hanson.common.constant.CacheKeyConstant.MoudleNames;
import com.iss.hanson.hanson.common.enums.error.UserErrorCodeEnum;
import com.iss.hanson.hanson.common.util.JwtUtil;
import com.iss.hanson.hanson.dao.domain.UserInfoPo;
import com.iss.hanson.hanson.dao.mappers.UserInfoMapper;
import com.iss.hanson.hanson.serevice.UserInfoService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Hanson
 * @date 2021/11/22  22:12
 */
@Service
@Slf4j
@CacheConfig(cacheNames = MoudleNames.DEMO_USER_INFO)
public class UserInfoServiceImpl implements UserInfoService {
	@Autowired
	private UserInfoMapper userInfoMapper;
	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CacheManager cacheManager;

	@Override
	public String loginByUserCode(String userCode) {
		UserInfoBo userInfoBo = getByCode(userCode);
		String token = jwtUtil.createToken(userCode);
		cacheManager.getCache(CacheKeyConstant.Tokens.TOKEN_KEY).put(token,userInfoBo);
		return token;
	}

	@Override
	public UserInfoBo save(UserInfoBo userInfoBo) {
		/**
		 *  PO 新增更新策略 @TableField(value = "user_code",insertStrategy = FieldStrategy.NOT_NULL)
		 */
		UserInfoPo userInfoPo = BeanUtil.copyProperties(userInfoBo, UserInfoPo.class,"mobile");
		String mobile = userInfoBo.getMobile();
		if(StringUtils.isNotBlank(mobile)){
			userInfoPo.setMobileMd5(SecureUtil.md5(mobile));
			userInfoPo.setMobile(EncryptColumn.encrypt(mobile));
		}
		userInfoMapper.saveOrUpdate(userInfoPo);
		userInfoBo.setId(userInfoPo.getId());
		return userInfoBo;
	}

	@Override
	@CacheEvict(key = "#id")
	public Integer del(Long id) {
		LambdaQueryWrapper<UserInfoPo> condtion = Wrappers.<UserInfoPo>lambdaQuery()
			.eq(UserInfoPo::getId,id);
		UserInfoPo po = UserInfoPo.builder().build();
		po.setDeleted(DeletedEnum.DELETED);
		return userInfoMapper.update(po,condtion);
	}

	@Override
	public List<UserInfoBo> list(UserInfoBo userInfoBo) {
		String mobileMD5 = SecureUtil.md5(userInfoBo.getMobile());

		LambdaQueryWrapper<UserInfoPo> query = Wrappers.<UserInfoPo>lambdaQuery()
			.eq(UserInfoPo::getMobileMd5,mobileMD5)
			.eq(UserInfoPo::getDeleted, DeletedEnum.NORMAL)
			.orderByDesc(UserInfoPo::getUpdateTime);
		List<UserInfoPo> userInfoPos = userInfoMapper.selectList(query);

		List<UserInfoBo> list = userInfoPos.stream().map(itm -> {
			UserInfoBo bo = BeanUtil.copyProperties(itm, UserInfoBo.class);
			if(Objects.nonNull(itm)){
				bo.setMobile(itm.getMobile().getValue());
			}
			return bo;
		}).collect(Collectors.toList());

		return list;
	}

	@Override
	public PageResult<UserInfoBo> page(PageQuery<UserInfoBo> pageQuery) {
		UserInfoBo userInfoBo = pageQuery.getParam();
		Wrapper<UserInfoPo> query = Wrappers.<UserInfoPo>lambdaQuery()
			//枚举类型作为查询条件要使用code
			.likeRight(UserInfoPo::getGender, userInfoBo.getGender().getCode())
			.eq(UserInfoPo::getDeleted, DeletedEnum.NORMAL)
			.orderByDesc(UserInfoPo::getUpdateTime);
		//转换成mybatisPage
		Page<UserInfoPo> page = PageUtil.toMyBatisPlusPage(pageQuery);
		//分页查询
		Page<UserInfoPo> result = userInfoMapper.selectPage(page, query);
		/**
		 * 结构转换
		 */
		//不需要特殊处理解密
		// PageResult<UserInfoBo> ret = PageUtil.toPageResult(result, UserInfoBo.class);
		//特殊处理则自定义
		PageResult<UserInfoBo> ret = PageUtil.toPageResult(result, itm -> {
			UserInfoBo bo = BeanUtil.copyProperties(itm, UserInfoBo.class);
			bo.setMobile(itm.getMobile().getValue());
			if(Objects.nonNull(itm)){
				bo.setMobile(itm.getMobile().getValue());
			}
			return bo;
		});

		return ret;
	}

	/**
	 *
	 * key="#id": 是指传入时的参数
	 * key="#p0": 表示第一个参数
	 * key="#user.id": 表示User中的id值
	 * key="#p0.id": 表示第一个参数里的id属性值
	 * key="#root.methodName": 当前方法名
	 * key="#root.method.name": 当前方法
	 * key="#root.target": 当前被调用的对象
	 * key="#root.targetClass": 当前被调用的对象的class
	 *
	 *
	 * key: 模块名+cacheKey值  demo_user_info::1
	 * condition： 缓存条件，支持EL表达式，只有为true 时才缓存。
	 * unless： 不缓存的条件，支持EL 表达式，为true 时 不缓存。例如 #user.id == 3 id 为3 的不缓存
	 */
	@Override
	@Cacheable(key = "#id")
	public UserInfoBo get(Long id) {

		LambdaQueryWrapper<UserInfoPo> query = Wrappers.<UserInfoPo>lambdaQuery()
			.eq(UserInfoPo::getId,id)
			.eq(UserInfoPo::getDeleted, DeletedEnum.NORMAL)
			.last(" LIMIT 1");

		UserInfoPo userInfoPo = userInfoMapper.selectOne(query);

		//模拟业务异常
		if(Objects.isNull(userInfoPo) || DeletedEnum.DELETED.equals(userInfoPo.getDeleted())){
			throw new BizException(UserErrorCodeEnum.FROZEN_USER);
		}

		UserInfoBo userInfoBo = BeanUtil.copyProperties(userInfoPo, UserInfoBo.class,"mobile");
		if(Objects.nonNull(userInfoPo)){
			userInfoBo.setMobile(userInfoPo.getMobile().getValue());
		}
		return userInfoBo;
	}

	@Override
	@Cacheable(key = "#userCode")
	public UserInfoBo getByCode(String userCode) {
		LambdaQueryWrapper<UserInfoPo> query = Wrappers.<UserInfoPo>lambdaQuery()
				.eq(UserInfoPo::getUserCode,userCode)
				.eq(UserInfoPo::getDeleted, DeletedEnum.NORMAL)
				.last(" LIMIT 1");

		UserInfoPo UserInfoPo = userInfoMapper.selectOne(query);

		//模拟业务异常
		if(Objects.isNull(UserInfoPo) || DeletedEnum.DELETED.equals(UserInfoPo.getDeleted())){
			throw new BizException(UserErrorCodeEnum.FROZEN_USER);
		}

		UserInfoBo UserInfoBo = BeanUtil.copyProperties(UserInfoPo, UserInfoBo.class,"mobile");
		if(Objects.nonNull(UserInfoPo)){
			UserInfoBo.setMobile(UserInfoPo.getMobile().getValue());
		}
		return UserInfoBo;
	}
}
