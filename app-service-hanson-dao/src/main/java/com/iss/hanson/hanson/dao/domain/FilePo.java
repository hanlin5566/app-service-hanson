package com.iss.hanson.hanson.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 *  * sys_file 云文件表
 * @author huhanlin 2022-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_file")
public class FilePo implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    @TableField("original_file_name")
    private String originalFileName;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 路径
     */
    @TableField("path")
    private String path;

    /**
     * URL
     */
    @TableField("url")
    private String url;

    /**
     * 文件大小
     */
    @TableField("size")
    private Long size;

    /**
     * 备注
     */
    @TableField("comment")
    private String comment;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
}