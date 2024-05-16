package com.iss.hanson.hanson.common.configuration;

/**
 * @author: HansonHu
 * @date: 2023-05-24 10:04
 **/

import com.iss.hanson.hanson.common.constant.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Objects;

import static com.iss.hanson.hanson.common.constant.Constant.LANG;

/**
 * @Description: 自定义LocaleResolver
 * @author Felix.Du
 * @Date: 2022/3/30 21:25
 */
@Configuration
public class MyLocaleResolver implements LocaleResolver {

    @Autowired
    private HttpServletRequest request;

    public Locale getLocal() {
        return resolveLocale(request);
    }

    /**
     * 从HttpServletRequest中获取Locale
     *
     * @param httpServletRequest    httpServletRequest
     * @return                      语言Local
     */
    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        //如果没有就使用默认的（根据主机的语言环境生成一个 Locale
        Locale locale = LocaleContextHolder.getLocale();
        //如果请求的链接中携带了 国际化的参数
        if(Objects.nonNull(httpServletRequest)){
            //获取请求中的语言参数
            String language = StringUtils.hasText(httpServletRequest.getHeader(LANG))? httpServletRequest.getHeader(LANG) : httpServletRequest.getParameter(LANG);
            if (StringUtils.hasText(language)){
                //zh_CN
                String[] s = language.split(Constant.LANG_CONNECTOR);
                //国家，地区
                locale = new Locale(s[0], s[1]);
            }
        }
        return locale;
    }

    /**
     * 用于实现Locale的切换。比如SessionLocaleResolver获取Locale的方式是从session中读取，但如果
     * 用户想要切换其展示的样式(由英文切换为中文)，那么这里的setLocale()方法就提供了这样一种可能
     *
     * @param request               HttpServletRequest
     * @param response   HttpServletResponse
     * @param locale                locale
     */
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

    }
}
