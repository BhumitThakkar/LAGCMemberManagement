package us.lagc.LAGCMemberManagement;

//import javax.servlet.annotation.WebListener;
//import javax.servlet.http.HttpSessionEvent;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@WebListener
public class SessionManagement extends HttpSessionEventPublisher {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	@Override
	public void sessionCreated(HttpSessionEvent event) {
		super.sessionCreated(event);
		logger.info("Session Created");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		super.sessionDestroyed(event);
		logger.info("Session Destroyed");
	}

}

// this class is just listener of session created and destroyed.
// Actually session in managed by LagcMemberManagementApplication.java and applications.properties