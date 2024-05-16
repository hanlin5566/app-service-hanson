package com.iss.hanson.hanson.common.bo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 云文件表
 * </p>
 *
 * @author Hanson
 * @since 2022-11-08
 */
@Data
@Builder
@ApiModel(value="云文件表对象", description="云文件表")
public class FileBo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "原始文件名")
    private String originalFileName;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "模块名称")
    private String moduleName;

    @ApiModelProperty(value = "路径")
    private String path;

    @ApiModelProperty(value = "URL")
    private String url;

    @ApiModelProperty(value = "文件大小")
    private Long size;

    @ApiModelProperty(value = "备注")
    private String comment;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
