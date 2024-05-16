package com.iss.hanson.hanson.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.hanson.rest.SimpleResult;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author: HansonHu
 * @date: 2023-09-05 13:55
 **/
@Component
@Slf4j
public class MyJob {
    @XxlJob("simpleJob")
    public void simpleJob() {
      log.info("simple job:{}", DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
    }
}
