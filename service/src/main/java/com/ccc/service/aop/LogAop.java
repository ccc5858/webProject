package com.ccc.service.aop;

import com.ccc.service.annotation.logger;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class LogAop {

    @Pointcut("execution(* com.ccc.service.service.impl.*.*(..)) && @annotation(com.ccc.service.annotation.logger)")
    public void point(){}

    @Around("point()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signatureLog = (MethodSignature) joinPoint.getSignature();
        logger logger = signatureLog.getMethod().getAnnotation(logger.class);
        log.info("开始记录日志");
        FileOutputStream log = new FileOutputStream("F:\\develop\\webProject\\log.txt");
        FileOutputStream error = new FileOutputStream("F:\\develop\\webProject\\error.txt");

        if(logger != null) {
            int value = logger.value();
            switch (value) {
                case 1:
                    return around(log, error, joinPoint);
                case 2:
                    before(log, error, joinPoint);
                    break;
            }
        }

        log.close();
        error.close();

        return joinPoint.proceed();
    }

    public Object around(FileOutputStream log, FileOutputStream error, ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        Object proceed = joinPoint.proceed();
        try {
            log.write((Thread.currentThread()
                    + "开始执行方法："
                    + signature.getName()
                    + "(" + signature.getDeclaringTypeName()
                    + "参数："
                    + joinPoint.getArgs() + ")"
                    + LocalDateTime.now()).getBytes());

            log.write((Thread.currentThread()
                    + "结束执行方法："
                    + signature.getName()
                    + "(" + signature.getDeclaringTypeName()
                    + "结果:"
                    + proceed + ")"
                    + LocalDateTime.now()).getBytes());
        } catch (Exception e) {
            error.write((Thread.currentThread()
                    + "记录日志异常："
                    + e.getMessage()
                    + LocalDateTime.now()).getBytes());
        } finally {
            log.close();
            error.close();
        }

        return proceed;
    }

    public void before(FileOutputStream log, FileOutputStream error, ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        try {
            log.write((Thread.currentThread()
                    + "开始执行方法："
                    + signature.getName()
                    + "(" + signature.getDeclaringTypeName()
                    + "参数："
                    + joinPoint.getArgs() + ")"
                    + LocalDateTime.now()).getBytes());
        } catch (Exception e) {
            error.write((Thread.currentThread()
                    + "记录日志异常："
                    + e.getMessage()
                    + LocalDateTime.now()).getBytes());
        }
    }
}
