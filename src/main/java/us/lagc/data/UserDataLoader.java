package us.lagc.data;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import us.lagc.model.ApplicationProperty;
import us.lagc.model.User;

@Component
public class UserDataLoader implements CommandLineRunner {

	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	private UserRepository userRepo;

//	Comment to use Plain Text Password
    private PasswordEncoder passwordEncoder;

//	Comment to use Plain Text Password
    @Autowired
    public UserDataLoader(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }
    
//	Uncomment to use Plain Text Password
//    @Autowired
//    public UserDataLoader(UserRepository userRepo) {
//        this.userRepo = userRepo;
//    }

    @Override
    public void run(String... args) throws Exception {
    	logger.info("Trying to load application user into db");
    	User user = null;
    	for (int i = 0; i < ApplicationProperty.userNames.length; i++) {
        	user = userRepo.findByUsername(ApplicationProperty.userNames[i]);
        	if (user == null) {
            	user = userRepo.findByEmail(ApplicationProperty.userEmails[i]);
        	}
        	if (user == null) {
                user = new User(ApplicationProperty.userEmails[i], ApplicationProperty.userNames[i], passwordEncoder.encode(ApplicationProperty.userPasswords[i]), ApplicationProperty.userFirstNames[i], ApplicationProperty.userLastNames[i], User.Role.ROLE_ADMIN);
//    			Uncomment to use Plain Text Password
//              user = new User(ApplicationProperty.userEmails[i], ApplicationProperty.userNames[i], "{noop}"+ApplicationProperty.userPasswords[i], ApplicationProperty.userFirstNames[i], ApplicationProperty.userLastNames[i], User.Role.ROLE_ADMIN);
                userRepo.save(user);
        		logger.info("Loaded application user with email:" + ApplicationProperty.userEmails[i] + " from UserDataLoader in DB");
        	} else {
        		logger.info("Not loading application user with email:" + ApplicationProperty.userEmails[i] + " from UserDataLoader because it already exists in DB");
        	}
		}
    }
}
