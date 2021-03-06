package com.mask.apigateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.mask.apigateway.constant.RedisConstant;
import com.mask.apigateway.utils.CookieUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.micrometer.core.instrument.util.StringUtils;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;

/**
 * 	权限拦截（区分买家和卖家）
 * 
 * @author Mask
 *
 */
@Component
public class AuthBuyerFilter extends ZuulFilter {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public boolean shouldFilter() {
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletRequest request = requestContext.getRequest();
		if ("/order/order/create".equals(request.getRequestURI())) {
			return true;
		}
		return false;
	}

	@Override
	public Object run() {
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletRequest request = requestContext.getRequest();
		/**
		 * /order/create 只能买家访问（cookie里有openid） 
		 */
		Cookie cookie = CookieUtil.get(request, "openid");
		if (cookie == null || StringUtils.isEmpty(cookie.getValue())) {
			requestContext.setSendZuulResponse(false);
			requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
		}
		
		return null;
	}

	@Override
	public String filterType() {
		return PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return PRE_DECORATION_FILTER_ORDER - 1;
	}

}
