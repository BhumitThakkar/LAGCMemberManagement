package us.lagc.controller;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.ModelAndView;

import us.lagc.model.ApplicationProperty;
import us.lagc.model.Member;
import us.lagc.model.PaymentFor;
import us.lagc.model.Receipt;
import us.lagc.service.MemberService;
import us.lagc.service.ReceiptService;

@Controller
@RequestMapping("/receipt")
@RequestScope
public class ReceiptController {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	MemberService memberService;

	@Autowired
	ReceiptService receiptService;

	@GetMapping("")
	public ModelAndView getForm(@RequestParam Integer memberId) {
		logger.info("memberId=" + memberId);
		ModelAndView mv = new ModelAndView();
		Member member = memberService.findMemberByMemberId(memberId);
		if(member == null) {														// which is never possible because after different url testing and logs, memberId is required and can't be empty(null)
			mv.addObject("error", "Member do not exist with id=" + memberId + ".");
			member = memberService.getEmptyMember();
			memberService.setDefaultModelViewForMember(mv, member);
		} else {
			receiptService.setDefaultModelViewForReceipt(mv, receiptService.getEmptyReceipt(member));
		}
		return mv;
	}

	@PostMapping("add")
	public ModelAndView add(Integer memberId, @RequestParam("membershipExpiryDate") String membershipExpiryDateString,  @Valid Receipt receipt, Errors errors) {					// Errors should be after @Valid
		logger.info("memberId=" + memberId);
		logger.info("membershipExpiryDateString=" + membershipExpiryDateString);
		logger.info("receipt=" + receipt);
		
		LocalDate membershipExpiryDate = LocalDate.parse(membershipExpiryDateString);
		ModelAndView mv = new ModelAndView();
		Member member = memberService.findMemberByMemberId(memberId);
		logger.info(member);
		if(member == null) {
			mv.addObject("error", "Member do not exist with id=" + memberId + ".");
			member = memberService.getEmptyMember();
			memberService.setDefaultModelViewForMember(mv, member);
		} else {
			if(!errors.hasErrors()) {
				// Stop if Amount is zero for non-AnnualMembershipFee payments
				// order matters to check this
				if(receipt.getAmount().doubleValue() <= 0 && !receipt.getPaymentFor().equals(PaymentFor.AnnualMembership)) {
					mv.addObject("error", "Amount must be positive. Fill out the form from the top.");
					receiptService.setDefaultModelViewForReceipt(mv, receiptService.getEmptyReceipt(member));			// on errors
					return mv;
				}

				if(receipt.getPaymentFor().equals(PaymentFor.LifeTimeMembership)) {
					member.setIsLifeMember("Yes");
					member.setMembershipExpiryDate(ApplicationProperty.lifetimeMembershipExpiryDate);
				} else if (receipt.getPaymentFor().equals(PaymentFor.AnnualMembership)) {
					if(membershipExpiryDate != null) {
						// go green only if membership expiry date is later than existing date
						if(membershipExpiryDate.isBefore(member.getMembershipExpiryDate()) || membershipExpiryDate.isEqual(member.getMembershipExpiryDate())) {
							mv.addObject("error", "New membership expiry date must be later than existing membership expiry date "+ member.getMembershipExpiryDate() + " [yyyy-mm-dd]. Fill out the form from the top.");
							receiptService.setDefaultModelViewForReceipt(mv, receiptService.getEmptyReceipt(member));
							return mv;
						} else {
							member.setMembershipExpiryDate(membershipExpiryDate);
						}
					} else {
						mv.addObject("error", "Member Expiry Date cannot be null. Fill out the form from the top.");
						receiptService.setDefaultModelViewForReceipt(mv, receiptService.getEmptyReceipt(member));		// on errors
						return mv;
					}
				}

				// add if receipt does not already exist
				receipt.setMember(member);											// set it here otherwise it will not get member while comparing receipts in next line: findReceiptNumberOfExistingReceipt
				Integer existingReceiptNumber = receiptService.findReceiptNumberOfExistingReceipt(receipt, member.getReceipts());
				if(existingReceiptNumber == null) {
					if(receiptService.addReceipt(receipt) && memberService.addOrUpdateMember(member)) {
						mv.addObject("success", "Receipt added successfully. Receipt number=" + receipt.getReceiptNumber() + ".");
						member.getReceipts().add(receipt);
						memberService.setDefaultModelViewForMember(mv, member);
					} else {
						mv.addObject("error", "Something went wrong. Fill out the form from the top.");
						receiptService.setDefaultModelViewForReceipt(mv, receiptService.getEmptyReceipt(member));			// on errors
					}
				} else {
					mv.addObject("error", "Form resubmitted or receipt already exist. Search member with member id="+ member.getId() +" and click view member and look for receipt number=" + existingReceiptNumber + " or fill out the form from the top.");
					receiptService.setDefaultModelViewForReceipt(mv, receiptService.getEmptyReceipt(member));
					return mv;
				}
			} else {
				for (ObjectError e : errors.getAllErrors()) {
					logger.error("ERR: " + e.getDefaultMessage());
				}
				// ONLY IN THIS CASE: adding the most possible error message manually because if we sent error receipt back to the form, we might not get expiry date field if amount was empty for lifetimeMembership. So sending empty receipt so they can start from the top.
				mv.addObject("error", "Reminder: Amount cannot be empty. Fill out the form from the top.");
				receiptService.setDefaultModelViewForReceipt(mv, receiptService.getEmptyReceipt(member));				// on errors
			}
		}
		return mv;
	}
	
}
