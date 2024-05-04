package us.lagc.controller;

import java.lang.invoke.MethodHandles;
import java.util.List;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.ModelAndView;

import us.lagc.model.ApplicationProperty;
import us.lagc.model.Member;
import us.lagc.model.PaymentFor;
import us.lagc.model.Receipt;
import us.lagc.service.CommonService;
import us.lagc.service.MemberService;

@Controller
@RequestMapping("/member")
@RequestScope
public class MemberController {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	@Autowired
	MemberService memberService;
	
	@GetMapping("")
	public ModelAndView getForm() {
		ModelAndView mv = new ModelAndView();
		memberService.setDefaultModelViewForMember(mv, memberService.getEmptyMember());
		return mv;
	}

	@PostMapping("add")
	public ModelAndView add(@Valid Member member, Errors errors) {								// Errors should be after @Valid
		logger.info("member=" + member);
		Receipt receipt = member.getReceipts().get(0);											// the first receipt of membership
		logger.info(receipt);
		ModelAndView mv = new ModelAndView();
		if(!errors.hasErrors()) {
			if(receipt.getAmount().doubleValue() <= 0 && !receipt.getPaymentFor().equals(PaymentFor.AnnualMembership)) {
				mv.addObject("error", "Amount must be positive.");
				memberService.setDefaultModelViewForMember(mv, member);
				return mv;
			}

			if(member.getIsLifeMember().equals("Yes")) {
				member.getReceipts().get(0).setPaymentFor(PaymentFor.LifeTimeMembership);
				member.setMembershipExpiryDate(ApplicationProperty.lifetimeMembershipExpiryDate);
			} else {
				member.getReceipts().get(0).setPaymentFor(PaymentFor.AnnualMembership);
			}
			
			// add if member does not already exist
			Integer existingMemberId = memberService.findMemberIdOfExistingMember(member);
			if(existingMemberId == null) {
				receipt.setMember(member);
				try {
					mv = memberService.addMemberFirstTimeWithReceipt(mv, member, receipt);
					memberService.setDefaultModelViewForMember(mv, memberService.getEmptyMember());
				} catch (Exception e) {
					mv.addObject("error", "Something went wrong while adding member and receipt.");
					logger.error("Something went wrong while adding member and receipt");
					logger.error("Exception Occurred=" + e.getMessage(), e);
					member.setId(null);					// so that UI does not assume it exist in DB
					receipt.setReceiptNumber(null);
					memberService.setDefaultModelViewForMember(mv, member);
				}
			} else {
				mv.addObject("error", "Form resubmitted or member already exist. Search member id=" + existingMemberId + ".");
				memberService.setDefaultModelViewForMember(mv, member);
			}
		} else {
			for (ObjectError e : errors.getAllErrors()) {
				logger.error("ERR: " + e.getDefaultMessage());
			}
			memberService.setDefaultModelViewForMember(mv, member);
		}
		return mv;
	}
	
	@GetMapping("{id}")													// IMPORTANT - because of @GetMapping("{id}")	server will give 400 instead of 404 error for http://localhost:8080/member/lookup (which is actually PostMapping) in url and then pressed enter on browser
	public ModelAndView getMemberById(@PathVariable Integer id) {
		logger.info("id=" + id);
		ModelAndView mv = new ModelAndView();
		Member member = memberService.findMemberByMemberId(id);
		if(member == null) {
			mv.addObject("error", "Member do not exist with id=" + id + ".");
			member = memberService.getEmptyMember();
		}
		memberService.setDefaultModelViewForMember(mv, member);
		return mv;
	}

	@PostMapping("update")
	public ModelAndView update(@Valid Member member, Errors errors) {									// Errors should be after @Valid
		logger.info("member=" + member);
		ModelAndView mv = new ModelAndView();
		if(!errors.hasErrors()) {
			Integer existingMemberId = memberService.findMemberIdOfExactExistingMember(member);			// checks if member is exactly equal to any member in database
			if(existingMemberId != null) {
				Member dbMember = memberService.findMemberByMemberId(member.getId());					// gets member with same id from db
				if(dbMember != null) {
					member.setReceipts(dbMember.getReceipts());
					mv.addObject("error", "Form resubmitted or no changes detected. Search member id=" + existingMemberId + ".");
					memberService.setDefaultModelViewForMember(mv, member);
				} else {																				// someone edited id from front end
					mv.addObject("error", "Something went wrong.");
					memberService.setDefaultModelViewForMember(mv, memberService.getEmptyMember());
				}
			} else {
				Member dbMember = memberService.findMemberByMemberId(member.getId());					// gets member with same id from db
				if(dbMember == null) {																	// someone edited id from front end
					mv.addObject("error", "Member do not exist with id=" + member.getId() + ".");
					memberService.setDefaultModelViewForMember(mv, member);
				} else {
					member.setCreated(dbMember.getCreated());											// required because "created" can not be null
					member.setCreatedBy(dbMember.getCreatedBy());										// required because "created_by" can not be null
					member.setReceipts(dbMember.getReceipts());											// required if next UI page uses receipts and this object is sent
					if(memberService.addOrUpdateMember(member)) {
						mv.addObject("success", "Member " + member.getFirstName() + " " + member.getLastName() + " updated successfully. Member id=" + member.getId() + ".");
						memberService.setDefaultModelViewForMember(mv, member);
					} else {
						mv.addObject("error", "Something went wrong.");
						memberService.setDefaultModelViewForMember(mv, memberService.getEmptyMember());
					}
				}
			}
			
		} else {
			for (ObjectError e : errors.getAllErrors()) {
				logger.error("ERR: " + e.getDefaultMessage());
			}
			Member dbMember = memberService.findMemberByMemberId(member.getId());
			if(dbMember == null) {																		// someone edited id from from front end
				mv.addObject("error", "Member do not exist with id=" + member.getId() + ".");
				member = memberService.getEmptyMember();
			} else {
				member.setReceipts( dbMember.getReceipts() );
			}
			memberService.setDefaultModelViewForMember(mv, member);
		}
		return mv;
	}
	
	@PostMapping("lookup")
	public ModelAndView lookup(@RequestParam(name = "memberId") String memberIdString, @RequestParam(name = "phone") String phoneString, String firstName, String lastName) {
		logger.info("memberId:" + memberIdString);
		logger.info("phone:" + phoneString);
		logger.info("firstName:" + firstName);
		logger.info("lastName:" + lastName);
		
		ModelAndView mv = new ModelAndView();
		
		if(memberIdString != null) {
			memberIdString = memberIdString.strip();
		}
		if(phoneString != null) {
			phoneString = phoneString.strip();
		}
		if(firstName != null) {
			firstName = firstName.strip();
		}
		if(lastName != null) {
			lastName = lastName.strip();
		}
		
		String errorMsg = memberService.validateInput(memberIdString, phoneString, firstName, lastName);		// returns null if no error
		if(! CommonService.allEmptyOrNull(errorMsg)) {
			mv.addObject("error", errorMsg);
			mv.setViewName("member");
			memberService.setDefaultModelViewForMember(mv, memberService.getEmptyMember());			
		} else {
			Integer memberId = null;
			Long phone = null;
			List<Member> members = null;
			if(CommonService.allEmptyOrNull(memberIdString)) {
				memberId = null;
			} else {
				memberId = Integer.parseInt(memberIdString);									// don't worry about exception, it is already checked in validateInput() method above
			}
			if(CommonService.allEmptyOrNull(phoneString)) {
				phone  = null;
			} else {
				phone = Long.parseLong(phoneString);											// don't worry about exception, it is already checked in validateInput() method above
			}
			members = memberService.lookUpMember(memberId, phone, firstName, lastName);			// returns empty list if member not found
			if(members.isEmpty()) {
				mv.addObject("error", "Member not found.");
				mv.setViewName("member");
				memberService.setDefaultModelViewForMember(mv, memberService.getEmptyMember());			
			} else {
				mv.addObject("members", members);
				mv.setViewName("members");
			}
		}
		return mv;
	}
	
}