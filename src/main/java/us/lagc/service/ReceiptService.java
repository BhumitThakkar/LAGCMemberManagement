package us.lagc.service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import us.lagc.data.ReceiptRepository;
import us.lagc.model.ApplicationProperty;
import us.lagc.model.Member;
import us.lagc.model.PaymentFor;
import us.lagc.model.PaymentType;
import us.lagc.model.Receipt;

@Service
public class ReceiptService {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	@Autowired
	private ReceiptRepository receiptRepo;

	public Receipt getEmptyReceipt(Member m) {
		Receipt r = new Receipt();
		r.setMember(m);
		return r;
	}

	public boolean addReceipt(Receipt receipt) {
		try {
			receiptRepo.save(receipt);						// add is id null otherwise update
		} catch (Exception e) {
			logger.error("Exception Occurred=" + e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	public ModelAndView setDefaultModelViewForReceipt(ModelAndView mv, Receipt r) {					// receipt must always contain member
		mv.addObject("receipt", r);
		mv.addObject("paymentTypeCategories", PaymentType.values());
		mv.addObject("annualMembershipAmount", ApplicationProperty.annualMembershipAmount);
		mv.addObject("lifetimeMembershipAmount", ApplicationProperty.lifetimeMembershipAmount);
		PaymentFor[] paymentForCategories = PaymentFor.values();
		// required as per flow
		mv.addObject("lifetimeMembershipExpiryDate", ApplicationProperty.lifetimeMembershipExpiryDate.toString());				// 2022-12-31 format
		mv.addObject("annualMembershipExpiryDate", r.getMember().getMembershipExpiryDate().toString());							// 2022-12-31 format
		if(r.getMember().getIsLifeMember().equals("Yes")) {
			paymentForCategories = Arrays.stream(paymentForCategories).filter(i -> ! (i == PaymentFor.AnnualMembership || i == PaymentFor.LifeTimeMembership) ).toArray(PaymentFor[]::new);
		} else {																					// if not life time member
			long daysBetween = ChronoUnit.DAYS.between(r.getMember().getMembershipExpiryDate(), LocalDate.now());
			logger.info("Days Between " + r.getMember().getMembershipExpiryDate() + " and " + LocalDate.now() + "=" + daysBetween);
			int remindBeforeDays = -30;
			if(daysBetween < remindBeforeDays) {
				paymentForCategories = Arrays.stream(paymentForCategories).filter(i -> ! (i == PaymentFor.AnnualMembership) ).toArray(PaymentFor[]::new);
				LocalDate enableAnnualMemberShipEnrollmentFrom = LocalDate.now().minusDays(daysBetween - remindBeforeDays);
				String enableAnnualMemberShipEnrollmentFromString = enableAnnualMemberShipEnrollmentFrom.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
				mv.addObject("msg", "Annual membership available from " + enableAnnualMemberShipEnrollmentFromString + " for " + r.getMember().getFirstName() + " " + r.getMember().getLastName() + ".");
			} else {
				mv.addObject("msg", "Remind " + r.getMember().getFirstName() + " " + r.getMember().getLastName() + ": Annual Membership Available Now !");
				if(LocalDate.now().getMonth().getValue() < 12) {
					mv.addObject("annualMembershipExpiryDate", LocalDate.of(LocalDate.now().getYear(), 12, 31).toString());							// 2022-12-31 format
				} else {
					mv.addObject("annualMembershipExpiryDate", LocalDate.of(LocalDate.now().getYear() + 1, 12, 31).toString());							// 2022-12-31 format					
				}
			}
		}
		mv.addObject("paymentForCategories", paymentForCategories);
		mv.setViewName("receipt");
		return mv;
	}

	public Integer findReceiptNumberOfExistingReceipt(Receipt receipt, List<Receipt> receipts) {
		Receipt dbreceipt = receipts.stream().filter(x -> x.equals(receipt)).findFirst().orElse(null);
		if(dbreceipt != null)
			return dbreceipt.getReceiptNumber();
		else
			return null;
	}

}
