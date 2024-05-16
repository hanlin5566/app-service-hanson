package com.iss.hanson.hanson.controller;

/**
 * @author Hanson
 * @date 2021/11/22  22:09
 */

import com.hanson.mybatis.domain.PageQuery;
import com.hanson.rest.PageResult;
import com.hanson.rest.SimpleResult;
import com.iss.hanson.hanson.common.bo.UserInfoBo;
import com.iss.hanson.hanson.common.constant.Constant;
import com.iss.hanson.hanson.serevice.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 示例控制器
 * @author hanson
 */
@Api(value = "用户相关接口")
@RestController
public class UserInfoController {
	@Autowired
	private UserInfoService userInfoService;

	@PostMapping("/user-info/login")
	@ApiOperation(value = "接口名称:用户登录", notes = "接口描述:用户登录")
	public SimpleResult<String> login(@PathVariable("userCode") String userCode) {
		//TODO:模拟用户登录，按实际业务编写。
		return SimpleResult.success(userInfoService.loginByUserCode(userCode));
	}

	@GetMapping("/user-info/detail/{id}")
	@ApiOperation(value = "接口名称:用户详情", notes = "接口描述:用户详情")
	@ApiImplicitParams({@ApiImplicitParam(name = Constant.TOKEN, paramType = "header" ,required = false)})
	public SimpleResult<UserInfoBo> get(@PathVariable("id") Long id) {
		UserInfoBo userInfoBo = userInfoService.get(id);
		return SimpleResult.success(userInfoBo);
	}

	@PostMapping("/user-info/list")
	public SimpleResult<List<UserInfoBo>> list(@RequestBody UserInfoBo userInfoBo) {
		List<UserInfoBo> ret = userInfoService.list(userInfoBo);
		return SimpleResult.success(ret);
	}

	@PostMapping("/user-info/page")
	public SimpleResult<PageResult<UserInfoBo>> page(@RequestBody PageQuery<UserInfoBo> pageQuery) {
		PageResult<UserInfoBo> page = userInfoService.page(pageQuery);
		return SimpleResult.success(page);
	}


	/**
	 * 枚举传入 name
	 * 如果传入主键则是更新，否则是新增。
	 */
	@PostMapping("/user-info/save")
	public SimpleResult<UserInfoBo> save(@Valid @RequestBody UserInfoBo userInfoBo) {
		UserInfoBo result = userInfoService.save(userInfoBo);
		return SimpleResult.success(result);
	}

	@DeleteMapping("/user-info/del/{id}")
	public SimpleResult<Integer> del(@PathVariable("id") Long id) {
		return SimpleResult.success(userInfoService.del(id));
	}
}
