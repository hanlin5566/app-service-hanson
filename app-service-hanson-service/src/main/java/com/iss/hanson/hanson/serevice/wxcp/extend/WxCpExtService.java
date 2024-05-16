package com.iss.hanson.hanson.serevice.wxcp.extend;


import me.chanjar.weixin.common.error.WxErrorException;

/**
 * 企业微信 API 扩展服务接口
 * API 扩展是指 WXCP 工具库中暂未提供的 API 调用方法，由此接口内的方法代替
 *
 * @author iSoftStone-Robert
 * @date 2023/2/9
 */
public interface WxCpExtService {
    /**
     * <pre>
     *
     * 通过邮箱获取其所对应的userid。
     *
     * 请求方式：POST（HTTPS）
     * 请求地址：https://qyapi.weixin.qq.com/cgi-bin/user/get_userid_by_email?access_token=ACCESS_TOKEN
     *
     * 文档地址：https://developer.work.weixin.qq.com/document/path/95892
     * </pre>
     *
     * @param email 邮箱
     * @param emailType 邮箱类型：1-企业邮箱（默认）；2-个人邮箱
     * @return userid email对应的成员userid
     * @throws WxErrorException .
     */
    String getUseridByEmail(String email, Integer emailType) throws WxErrorException;
}
