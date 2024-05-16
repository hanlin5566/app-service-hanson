package com.iss.hanson.hanson.common.util;
import cn.hutool.extra.spring.SpringUtil;
import com.iss.hanson.hanson.common.configuration.MyLocaleResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * @author: HansonHu
 * @date: 2023-05-23 18:14
 * 获取i18n资源文件
 **/
@Slf4j
public class I18nMessageUtils {

    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String code, Object... args) {
        MessageSource messageSource = SpringUtil.getBean(MessageSource.class);
        MyLocaleResolver myLocaleResolver = SpringUtil.getBean(MyLocaleResolver.class);
        String message = code;
        try {
            message = messageSource.getMessage(code, args, myLocaleResolver.getLocal());
        } catch (NoSuchMessageException e) {
            log.warn("i18n not found:{}",code);
        }
        return message;
    }

    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String code, Locale locale, Object... args) {
        MessageSource messageSource = SpringUtil.getBean(MessageSource.class);
        String message = code;
        try {
            message = messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            log.warn("i18n not found:{}",code);
        }
        return message;
    }

}

