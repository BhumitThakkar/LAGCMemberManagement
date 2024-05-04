package us.lagc.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
@RequestScope
public class GenericErrorController implements ErrorController {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	@GetMapping("error")
	public ModelAndView handleError(HttpServletRequest request, @RequestParam() Map<String,String> requestParams) {
		logger.info("requestURI="+request.getRequestURL().toString());
		logger.info("requestParams="+requestParams);
		ModelAndView mv = new ModelAndView();

		// get error status
        Object status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            logger.error("Request Error Code:" + statusCode);
            
            // display specific error page
            mv.addObject("error", "Error Code "+ statusCode +". Do not modify url or go back using browser's back button. Take a screenshot of the full screen and contact Administrator if required.");
        } else {
        	mv.addObject("error", "Something went wrong. Do not modify url or go back using browser's back button. Take a screenshot of the full screen and contact Administrator if required.");
        }

		logger.error("Request Path:" + request.getRequestURL().toString());
		logger.error("Request Method:" + request.getMethod());
		
		mv.setViewName("error");
		return mv;
	}
	
	@GetMapping("sessionExpired")
	public ModelAndView handleSessionTimeout(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
        mv.addObject("msg", "Session timed out.");
		mv.setViewName("login");
		return mv;
	}
	
	@GetMapping("*")
	public String redirectToHomePage(HttpServletRequest request, @RequestParam() Map<String,String> requestParams) {
		logger.info("requestURI="+request.getRequestURL().toString());
		logger.info("requestParams="+requestParams);
		logger.info("Redirecting to login page...");
		return "redirect:/login";
	}


}

//request.getRequestURL().toString()  --------> might always show http even though it was https. To avoid this problem use below:-
//request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +  request.getRequestURI() + "?" + request.getQueryString();
