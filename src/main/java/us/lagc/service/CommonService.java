package us.lagc.service;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonService {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	public static boolean allEmptyOrNull(Object... objects) {
		for(Object o : objects) {
			if(o != null) {
				if(o instanceof String) {
					if(!( ((String)o).strip() ).equals("")) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
	public static String capitalizeOnlyAfterSpace(String input) {
		logger.info(">>> capitalizeOnlyAfterSpace input="+input);
		input = input.toLowerCase();
		
		// Split the input string by space
        String[] words = input.split(" ");
        
        // Use a StringBuilder to build the result
        StringBuilder result = new StringBuilder();

        // Iterate through each word
        for (int i = 0; i < words.length; i++) {
            // If the word is not empty
            if (!words[i].isEmpty()) {
                // Capitalize the first letter of the word and append the rest of the word
                result.append(Character.toUpperCase(words[i].charAt(0)))
                      .append(words[i].substring(1));

                // If this is not the last word, add a space
                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
        }
        logger.info("<<< capitalizeOnlyAfterSpace output="+result.toString());
        return result.toString();
	}

	public static String cleanStreetName(String street) {
		logger.info(">>> Street from form or db="+street);
		street = street.toLowerCase();
		
		// "(xyz)$" = xyz pattern comes in end
		street = street.replaceAll("( drive)$", " dr");
		street = street.replaceAll("( court)$", " ct");
		street = street.replaceAll("( apartment)$", " apt");
		street = street.replaceAll("( avenue)$", " ave");
		street = street.replaceAll("( highway)$", " hwy");
		street = street.replaceAll("( lane)$", " ln");
		street = street.replaceAll("( road)$", " rd");
		street = street.replaceAll("( street)$", " st");
		
		// " xyz " = xyz pattern comes in between
		street = street.replaceAll(" drive ", " dr ");
		street = street.replaceAll(" court ", " ct ");
		street = street.replaceAll(" apartment ", " apt ");
		street = street.replaceAll(" avenue ", " ave ");
		street = street.replaceAll(" highway ", " hwy ");
		street = street.replaceAll(" lane ", " ln ");
		street = street.replaceAll(" road ", " rd ");
		street = street.replaceAll(" street ", " st ");
		
		street = street.replaceAll(" e ", " east ");
		street = street.replaceAll(" w ", " west ");
		street = street.replaceAll(" n ", " north ");
		street = street.replaceAll(" s ", " south ");
		// First 8 from https://www.allacronyms.com/address/abbreviations/1 | Sort by Rating
		
		street = capitalizeOnlyAfterSpace(street);
		logger.info("<<< Street after modification="+street);
		return street;
	}

}
