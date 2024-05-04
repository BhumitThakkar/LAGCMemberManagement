package us.lagc.LAGCMemberManagement;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Testing {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	public static void main(String[] args) {
		logger.info(ZoneId.systemDefault());
	}

}
