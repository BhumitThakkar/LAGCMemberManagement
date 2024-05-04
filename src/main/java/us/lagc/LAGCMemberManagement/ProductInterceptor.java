package us.lagc.LAGCMemberManagement;

import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ProductInterceptor implements HandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String clientIP = request.getHeader("x-forwarded-for");									// x-forwarded-for is NOT case sensitive
		if(clientIP == null || "".equals(clientIP)) {
			clientIP = request.getRemoteAddr();
        }
		ThreadContext.put("threadContextUUID", UUID.randomUUID().toString());					// threadContextUUID is used then in log4j2.yml
		ThreadContext.put("clientIP", clientIP);		
		return true;
	}

}
