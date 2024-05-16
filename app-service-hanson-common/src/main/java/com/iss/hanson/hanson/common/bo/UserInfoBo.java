package com.iss.hanson.hanson.common.bo;

import com.iss.hanson.hanson.common.enums.GenderEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Hanson
 * @date 2021/11/22  22:21
 */
@Data
@Builder
public class UserInfoBo {
	/**
	 *
	 */
	@ApiModelProperty
	private Long id;

	/**
	 * 用户编号
	 */
	@ApiModelProperty(value="用户编号",name="userCode")
	@NotBlank(message="{com.hanson.user.name.not.blank}")
	@Size(message = "{com.hanson.user.name.len.less}",max = 10)
	@Range(message = "{com.hanson.user.name.len.range}", min = 1, max = 1000000)
	private String userCode;

	/**
	 * 用户姓名
	 */
	private String name;

	/**
	 * 性别 1:男 2:女
	 */
	private GenderEnum gender;

	/**
	 * 手机号
	 */
	private String mobile;
}
