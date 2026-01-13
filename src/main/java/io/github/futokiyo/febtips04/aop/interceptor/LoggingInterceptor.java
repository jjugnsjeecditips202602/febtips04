package io.github.futokiyo.febtips04.aop.interceptor;

import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.futokiyo.febtips04.aop.Logging;


@Dependent
@Interceptor
@Logging
public class LoggingInterceptor {

	private static Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

	
	@AroundInvoke
	public Object manageUserTransaction(final InvocationContext invocationContext) throws Exception {
		String clazzName = invocationContext.getTarget().getClass().getCanonicalName();
		String methodName = invocationContext.getMethod().getName();
		logger.info("{}#{} start.", clazzName, methodName);
		try {
			return invocationContext.proceed();
		} finally {
			logger.info("{}#{} end.", clazzName, methodName);
		}
	}
}
