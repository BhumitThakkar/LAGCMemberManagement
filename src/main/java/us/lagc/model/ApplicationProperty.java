package us.lagc.model;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApplicationProperty {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	// This class will be loaded once till server is up
	// but We need updated annualMembershipExpiryDate every time a member is created/used
	// So we cannot create static variable here. Hence, it is default implemented in Member.java for membershipExpiryDate
	
	// Load custom.properties
	private static Properties props = new Properties();
	static {
		try {
			String customPropertiesFileName = "custom.properties";
			props.load(MethodHandles.lookup().lookupClass().getClassLoader().getResourceAsStream(customPropertiesFileName));
			logger.info("============="+customPropertiesFileName+" | Start=============");
			logger.info("other.properties size="+props.size());
			logger.info("==================================================");
			List<String> keys = props.keySet().stream().map(x->x.toString()).collect(Collectors.toList());
			Collections.sort(keys);
			for (String key : keys) {
				logger.info(key + "=" + props.getProperty(key));
				if(props.getProperty(key) == null || props.getProperty(key).isBlank()) {
					throw new RuntimeException(key + " cannot be null or blank");
				}
			}
			logger.info("============="+customPropertiesFileName+" | END=============");
		} catch (IOException e) {
			logger.error("Exception Occurred=" + e.getMessage(), e);
		}
	}
	// Load environment properties
	private static final Map<String, String> env = System.getenv();					// I also saw somewhere: System.getProperties()
	static {
//			the below block will print all env key value including system environment and custom user pass from environment		
		List<String> envKey = new ArrayList<String>(env.keySet());
		Collections.sort(envKey);
		logger.info("=============env properties | Start=============");
		logger.info("env properties size="+envKey.size());
		logger.info("================================================");
		for (String envName : envKey) {
			logger.info(envName + "=" + env.get(envName));
		}
		logger.info("=============env properties | End===============");
	}
	
	private static final String lifetimeMembershipExpiryDateYear = props.getProperty("lifetimeMembershipExpiryDateYear").strip();
	public static final BigDecimal annualMembershipAmount = BigDecimal.valueOf(Double.parseDouble(props.getProperty("annualMembershipAmount").strip()));
	public static final BigDecimal lifetimeMembershipAmount = BigDecimal.valueOf(Double.parseDouble(props.getProperty("lifetimeMembershipAmount").strip()));
	public static final String[] userNames = props.getProperty("commaSeperatedUserNames").strip().split("(,\\s)|(,)");
	public static final String[] userPasswords = props.getProperty("commaSeperatedPasswords").strip().split("(,\\s)|(,)");
	public static final String[] userEmails = props.getProperty("commaSeperatedEmails").strip().split("(,\\s)|(,)");
	public static final String[] userFirstNames = props.getProperty("commaSeperatedFirstNames").strip().split("(,\\s)|(,)");
	public static final String[] userLastNames = props.getProperty("commaSeperatedLastNames").strip().split("(,\\s)|(,)");
	public static final LocalDate lifetimeMembershipExpiryDate = LocalDate.of(Integer.parseInt(lifetimeMembershipExpiryDateYear), 12, 31);

}
