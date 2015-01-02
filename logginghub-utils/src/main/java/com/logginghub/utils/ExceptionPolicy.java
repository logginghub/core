package com.logginghub.utils;

import com.logginghub.utils.logging.Logger;

public class ExceptionPolicy {
    private Policy policy;
    private ExceptionHandler handler;
    private Logger logger;

    public ExceptionPolicy(Policy log) {
        this.policy = log;
    }

    public ExceptionPolicy(Policy policy, ExceptionHandler handler) {
        this.handler = handler;
        this.policy = policy;
    }

    public ExceptionPolicy(Policy policy, Logger logger) {
        this.logger = logger;
        this.policy = policy;
    }

    public enum Policy {
        RethrowOnAny,
        SendToHandler,
        Ignore,
        Log,
        SystemOut,
        SystemErr
    }

    public void setHandler(ExceptionHandler handler) {
        this.handler = handler;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Policy getPolicy() {
        return policy;
    }

    public ExceptionHandler getHandler() {
        return handler;
    }

    public void handle(String format, Object... params) {

        String message = StringUtils.format(format, params);

        switch (this.policy) {
            case Ignore: {
                break;
            }
            case Log: {
                FormattedRuntimeException e = new FormattedRuntimeException(message, params);
                e.fillInStackTrace();
                logger.warn(e, message);
                break;
            }
            case RethrowOnAny: {
                throw new FormattedRuntimeException(message, params);
            }
            case SendToHandler: {
                FormattedRuntimeException e = new FormattedRuntimeException(message, params);
                e.fillInStackTrace();
                handler.handleException(message, e);
                break;
            }
            case SystemOut: {
                System.out.println(message);
                break;
            }
            case SystemErr: {
                System.err.println(message);
                break;
            }

        }

    }

    public void handle(Exception e) {

        switch (this.policy) {
            case Ignore: {
                break;
            }
            case Log: {
                logger.warn(e, e.getMessage());
                break;
            }
            case RethrowOnAny: {
                throw new RuntimeException(e);
            }
            case SendToHandler: {
                handler.handleException(e.getMessage(), e);
                break;
            }
            case SystemOut: {
                e.printStackTrace(System.out);
                break;
            }
            case SystemErr: {
                e.printStackTrace(System.err);
                break;
            }

        }
        
    }

    public void handle(Throwable e, String format, Object... params){

        String message = StringUtils.format(format, params);

        switch (this.policy) {
            case Ignore: {
                break;
            }
            case Log: {
                logger.warn(e, message);
                break;
            }
            case RethrowOnAny: {
                throw new FormattedRuntimeException(e, message);
            }
            case SendToHandler: {
                handler.handleException(message, e);
                break;
            }
            case SystemOut: {
                System.out.println(message);
                e.printStackTrace(System.out);
                break;
            }
            case SystemErr: {
                System.err.println(message);
                e.printStackTrace(System.err);
                break;
            }

        }

        
    }
}
