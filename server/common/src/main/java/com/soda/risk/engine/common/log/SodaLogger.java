package com.soda.risk.engine.common.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 统一日志工具
 */
@Slf4j
@Component
public class SodaLogger {

    private static final String LOG_FORMAT = "[{}] {}";

    public void info(String module, String message, Object... args) {
        log.info(LOG_FORMAT + " " + message, prepend(module, args));
    }

    public void warn(String module, String message, Object... args) {
        log.warn(LOG_FORMAT + " " + message, prepend(module, args));
    }

    public void error(String module, String message, Object... args) {
        log.error(LOG_FORMAT + " " + message, prepend(module, args));
    }

    public void error(String module, Throwable t, String message, Object... args) {
        log.error(LOG_FORMAT + " " + message, prependWithThrowable(module, t, args));
    }

    public void debug(String module, String message, Object... args) {
        log.debug(LOG_FORMAT + " " + message, prepend(module, args));
    }

    private Object[] prepend(String module, Object[] args) {
        Object[] result = new Object[args.length + 1];
        result[0] = module;
        System.arraycopy(args, 0, result, 1, args.length);
        return result;
    }

    private Object[] prependWithThrowable(String module, Throwable t, Object[] args) {
        Object[] result = new Object[args.length + 2];
        result[0] = module;
        System.arraycopy(args, 0, result, 1, args.length);
        result[args.length + 1] = t;
        return result;
    }
}
