package us.lagc.service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import us.lagc.data.MemberRepository;
import us.lagc.model.ApplicationProperty;
import us.lagc.model.Member;
import us.lagc.model.PaymentFor;
import us.lagc.model.PaymentType;
import us.lagc.model.Receipt;
import us.lagc.model.State;

@Service
public class MemberService {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	@Autowired
	private MemberRepository memberRepo;
	@Autowired
	ReceiptService receiptService;
	
	public Member getEmptyMember() {
		Member member = new Member();
		List<Receipt> receipts = new ArrayList<Receipt>();
		receipts.add(new Receipt(PaymentFor.AnnualMembership, ApplicationProperty.annualMembershipAmount));
		member.setReceipts(receipts);
		return member;
	}
	
	public boolean addOrUpdateMember(Member member) {
		try {
			memberRepo.save(member);						// add if id null otherwise update
		} catch (Exception e) {
			logger.error("Exception Occurred=" + e.getMessage(), e);
			return false;
		}
		return true;
	}

//	---@Transactional comment START---
//	When you call this method, Spring will begin a transaction.
//	The method performs two operations: adding or updating a member AND adding a receipt.
//	If an exception occurs during the addition of the receipt, Spring will automatically roll back the transaction, reverting both the member and receipt operations.
//	If everything completes successfully, the transaction is committed.
//	---@Transactional comment END---
	@Transactional(rollbackFor = Exception.class)
	public ModelAndView addMemberFirstTimeWithReceipt(ModelAndView mv, Member member, Receipt receipt) {
		if(addOrUpdateMember(member)) {
			if (receiptService.addReceipt(receipt)) {
				mv.addObject("success", "Member " + member.getFirstName() + " " + member.getLastName() + " added successfully. Member id=" + member.getId() + ".");
			} else {
				mv.addObject("error", "Something went wrong while adding receipt.");
				logger.error("Something went wrong while adding receipt");
				logger.info("@Transactional will rolling back member just inserted in DB");
			}
		} else {
			mv.addObject("error", "Something went wrong while adding member.");
			logger.error("Something went wrong while adding member");
		}
		return mv;
	}

	public List<Member> lookUpMember(Integer memberId, Long phone, String firstName, String lastName) {				// returns empty list if member not found
		List<Member> members = new ArrayList<Member>();
		if(!CommonService.allEmptyOrNull(memberId)) {
			members.add(findMemberByMemberId(memberId));
			logger.info(members);
		}
		if (!CommonService.allEmptyOrNull(phone)) {
			members.addAll(findMembersByPhone(phone));
			logger.info(members);
		}
		if(!CommonService.allEmptyOrNull(firstName) && !CommonService.allEmptyOrNull(lastName)) {
			members.addAll(findMembersContainingFirstNameAndContainingFirstNameLastName(firstName, lastName));
			logger.info(members);
		}
		if(!CommonService.allEmptyOrNull(firstName)) {
			members.addAll(findMembersContainingFirstName(firstName));
			logger.info(members);
		}
		if(!CommonService.allEmptyOrNull(lastName)) {
			members.addAll(findMembersContainingLastName(lastName));
			logger.info(members);
		}
		members = cleanList(members);																	// removes duplicate members and nulls
		logger.info("Clean list of members found="+members);
		return members;
	}
	
	public Member findMemberByMemberId(Integer memberId) {												// returns null if not found
		return memberRepo.findById(memberId).orElse(null);
	}
	
	public String validateInput(String memberIdString, String phoneString, String firstName, String lastName) {		// returns null if no error
		String errorMsg = null;
		Integer memberId = null;
		Long phone = null;
		if(CommonService.allEmptyOrNull(memberIdString, phoneString, firstName, lastName)) {
			errorMsg = "Use at least one member lookup box to find existing member.";
		} else {
			if(! CommonService.allEmptyOrNull(memberIdString)) {
				try {
					memberId = Integer.parseInt(memberIdString);
					if(memberId <= 0 || memberId > Integer.MAX_VALUE) {
						errorMsg = "Member id must have range 0 to " + Integer.MAX_VALUE;
					}
				} catch (NumberFormatException e) {
					errorMsg = "Member id must have range 0 to " + Integer.MAX_VALUE;
					logger.error(errorMsg);
					logger.error("Exception Occurred=" + e.getMessage(), e);
				}
			} else if(! CommonService.allEmptyOrNull(phoneString)) {
				try {
					phone = Long.parseLong(phoneString);
					if(phone < 1000000000L || phone > 9999999999L) {
						errorMsg = "Phone Number must be 10 digits only not starting with 0.";
					}				
				} catch (NumberFormatException e) {
					errorMsg = "Phone Number must be 10 digits only not starting with 0.";
					logger.error(errorMsg);
					logger.error("Exception Occurred=" + e.getMessage(), e);
				}
			}
		}
		return errorMsg;
	}
	
	public ModelAndView setDefaultModelViewForMember(ModelAndView mv, Member m) {
		mv.addObject("member", m);
		mv.addObject("annualMembershipAmount", ApplicationProperty.annualMembershipAmount);
		mv.addObject("lifetimeMembershipAmount", ApplicationProperty.lifetimeMembershipAmount);
		mv.addObject("states", State.values());
		mv.addObject("lifetimeMembershipExpiryDate", ApplicationProperty.lifetimeMembershipExpiryDate.toString());				// 2022-12-31 format
		mv.addObject("annualMembershipExpiryDate", m.getMembershipExpiryDate().toString());										// 2022-12-31 format
		mv.addObject("paymentTypeCategories", PaymentType.values());
//		if(m.getId() == null) {
//			mv.addObject("paymentTypeCategories", PaymentType.values());
//		}
		mv.setViewName("member");
		return mv;
	}

	// used before adding a member
	public Integer findMemberIdOfExistingMember(@Valid Member member) {
		Member dbMember = findAllMembers().stream().filter(x -> x.equals(member)).findFirst().orElse(null);
		if(dbMember != null)
			return dbMember.getId();
		else
			return null;
	}
	
	// used before updating a member
	public Integer findMemberIdOfExactExistingMember(@Valid Member member) {
		Member dbMember = findAllMembers().stream().filter(x -> x.exactEquals(member)).findFirst().orElse(null);
		if(dbMember != null) {
			return dbMember.getId();
		} else {
			return null;			
		}
	}
	
	private List<Member> findAllMembers() {																// returns empty list if no members found
		return memberRepo.findAll();
	}
	
	private List<Member> findMembersByPhone(Long phone) {												// returns null if not found
		return memberRepo.findByPhone(phone).orElse(null);
	}
	
	private List<Member> findMembersContainingFirstNameAndContainingFirstNameLastName(String firstName, String lastName) {	// returns null if not found
		return memberRepo.findByFirstNameContainingAndLastNameContaining(firstName, lastName).orElse(null);
	}

	private List<Member> findMembersContainingFirstName(String firstName) {								// returns null if not found
		return memberRepo.findByFirstNameContaining(firstName).orElse(null);
	}

	private List<Member> findMembersContainingLastName(String lastName) {								// returns null if not found
		return memberRepo.findByLastNameContaining(lastName).orElse(null);
	}
	
	private List<Member> cleanList(List<Member> members) {
		return members.stream().filter(x -> x != null).distinct().collect(Collectors.toList());			// Member class have equals method overridden
	}
		
}