package com.iss.hanson.hanson.dao.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.hanson.mybatis.BasePo;
import com.hanson.mybatis.handler.EncryptColumn;
import com.hanson.mybatis.handler.EncryptHandler;
import com.iss.hanson.hanson.common.enums.GenderEnum;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
/**
 *  * tmp_user_info 用户示例表
 * @author huhanlin 2021-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("tmp_user_info")
public class UserInfoPo extends BasePo implements Serializable {
    /**
     * 
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号
     */
    @TableField(value = "user_code",insertStrategy = FieldStrategy.NOT_NULL)
    private String userCode;

    /**
     * 用户姓名
     */
    @TableField("name")
    private String name;

    /**
     * 性别 1:男 2:女
     */
    @TableField("gender")
    private GenderEnum gender;

    /**
     * 手机号
     */
    @TableField(value = "mobile" , typeHandler = EncryptHandler.class)
    private EncryptColumn mobile;

    /**
     * 手机号md5
     */
    @TableField("mobile_md5")
    private String mobileMd5;
}