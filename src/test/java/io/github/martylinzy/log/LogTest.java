package io.github.martylinzy.log;

import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {
    Logger log = LoggerFactory.getLogger(LogTest.class);
    @Test
    public void testOne() {
        // 记录debug级别的信息
        log.debug(">>This is debug message");
        // 记录info级别的信息
        log.info(">>This is info message");
        // 记录error级别的信息
        log.error(">>This is error message");

    }
}