package com.iss.hanson.hanson.configuration;


import cn.hutool.core.exceptions.ExceptionUtil;
import com.hanson.rest.BizException;
import com.hanson.rest.SimpleResult;
import com.iss.hanson.hanson.common.util.I18nMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

import static com.hanson.rest.enmus.ErrorCodeEnum.*;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

/**
 * @author Hanson
 * @date 2021/7/2  13:53
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {
	/**
	 * 400 - Bad Request
	 */
	@ResponseStatus(SERVICE_UNAVAILABLE)
	@ExceptionHandler(HttpServerErrorException.ServiceUnavailable.class)
	public SimpleResult<Void> handlerServiceUnavailableException(HttpMessageNotReadableException e) {
		log.error(e.getMessage(), e);
		return SimpleResult.fail(SERVICE_UNAVAILABLE.getReasonPhrase());
	}

	/**
	 * 400 - Bad Request
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public SimpleResult<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		log.error(e.getMessage(), e);
		return new SimpleResult<>(BAD_REQUEST);
	}

	/**
	 * 404 - Resource Not Found
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public SimpleResult<Void> handleHttpMessageNotFoundException(NoHandlerFoundException e) {
		log.error(e.getMessage(), e);
		return new SimpleResult<>(NOT_FOUND);
	}

	/**
	 * 405 - Method Not Allowed
	 */
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public SimpleResult<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		return new SimpleResult<>(METHOD_NOT_ALLOWED);
	}

	/**
	 * 500 - Internal Server Error
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(Exception.class)
	public SimpleResult<Void> handleException(Exception e) {
		log.error(e.getMessage(), e);
		return new SimpleResult<>(FAIL.getCode(),e.getMessage());
	}

	/**
	 * 参数异常
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(ValidationException.class)
	public SimpleResult<Void> handleException(ValidationException e) {
		return new SimpleResult<>(INVALID_PARAMETER);
	}


	/**
	 * 参数异常
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public SimpleResult<Void> handleException(MethodArgumentNotValidException e) {
		StringBuilder sb = new StringBuilder();
		List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
		String message = allErrors.stream().map(s -> s.getDefaultMessage()).collect(Collectors.joining(";"));
		return SimpleResult.fail(INVALID_PARAMETER.getCode(),message);
	}


	/**
	 * 服务异常
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(BizException.class)
	public SimpleResult<Void> ServiceException(BizException e) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		String requestURI = request.getRequestURI();
		log.error("call {} failed , cause :{}",requestURI, ExceptionUtil.getMessage(e),e);
		return SimpleResult.fail(e.getErrorCode(),I18nMessageUtils.message(e.getErrorMessage()));
	}
}
