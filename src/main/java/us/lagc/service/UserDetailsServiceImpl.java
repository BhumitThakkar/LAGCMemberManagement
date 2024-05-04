package us.lagc.service;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import us.lagc.data.UserRepository;
import us.lagc.model.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	private UserRepository userRepo;
	
	@Autowired
	public UserDetailsServiceImpl(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepo.findByUsername(username);
		if(user == null) {
			logger.error("Username " + username + " not found in databse");
			throw new UsernameNotFoundException("Username " + username + " not found");
		} else {
			logger.info("Username " + username + " found in databse");
		}
		return user;
	}

}
