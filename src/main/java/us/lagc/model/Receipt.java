package us.lagc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
public class Receipt {

	public Receipt() {
	}
	
	// Auto Generated
	public Receipt(
			@NotNull(message = "Payment For cannot be null.") PaymentFor paymentFor,
			@NotNull(message = "Amount cannot be empty.") @PositiveOrZero(message = "Amount cannot be negative.") @Digits(fraction = 2, integer = 7, message = "Amount must be digits only. Max 2 decimal digits allowed.") BigDecimal amount) {
		super();
		this.paymentFor = paymentFor;
		this.amount = amount;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="receipt_seq")
	@SequenceGenerator(name = "receipt_seq", sequenceName = "receipt_seq" ,allocationSize = 1)
	@Column(updatable = false, columnDefinition="INT NOT NULL")
	private Integer receiptNumber;
	
	@NotNull(message = "Payment For cannot be null.")
	@Enumerated(EnumType.STRING)
	private PaymentFor paymentFor;								// Event or Donation or Advertisement or AnnualFee or Other
	
	@NotNull(message = "Amount cannot be empty.")
	@PositiveOrZero(message = "Amount must be positive.")		// hide knowledge that it could be zero for AnnualMembershipFee when we want to extend expiry date manually. This will allow us to know who did it.
	@Digits(fraction = 2, integer = 7, message = "Amount must be digits only. Max 2 decimal digits allowed.")																// max integer, max fraction
	@Column(updatable = false)
	private BigDecimal amount;
	
	@NotNull(message = "Payment Type cannot be null.")
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;								// Cash or Check or Credit Card or "Zelle or Other Online"
	
	@NotNull(message = "Payment Date cannot be null.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")							// yyyy-MM-dd somehow will get transfered into MM/dd/yyyy when it goes to html5 front end
	private LocalDate paymentDate = LocalDate.now();				// yyyy-MM-dd backend | MM/dd/yyyy form front end
	
	@Column(columnDefinition="TEXT(65535)")							// instead of varchar(255) https://stackoverflow.com/a/13506920
	private String receiptNotes;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)			// we will need Member details when we show receipt
	private Member member;
	
	@Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")			// this is not application level validation, this is database level validation
	private LocalDateTime created;
	
	@Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")		// this is not application level validation, this is database level validation
	private LocalDateTime modified;

	@Column(nullable = false)					// this is not application level validation, this is database level validation
	private String createdBy;
	
	@Column(nullable = false)					// this is not application level validation, this is database level validation
	private String modifiedBy;
	
	
	/*
	 * --------------------------------------
	 * Getter and Setters
	 * --------------------------------------
	 * 
	*/
	
	
	public Integer getReceiptNumber() {
		return receiptNumber;
	}
	public void setReceiptNumber(Integer receiptNumber) {
		this.receiptNumber = receiptNumber;
	}
	public PaymentFor getPaymentFor() {
		return paymentFor;
	}
	public void setPaymentFor(PaymentFor paymentFor) {
		this.paymentFor = paymentFor;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public PaymentType getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}
	public LocalDate getPaymentDate() {
		return paymentDate;
	}
	public void setPaymentDate(LocalDate paymentDate) {
		this.paymentDate = paymentDate;
	}
	public String getReceiptNotes() {
		return receiptNotes;
	}
	public void setReceiptNotes(String receiptNotes) {
		this.receiptNotes = receiptNotes;
	}
	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}
	public LocalDateTime getCreated() {
		return created;
	}
	public void setCreated(LocalDateTime created) {
		this.created = created;
	}
	public LocalDateTime getModified() {
		return modified;
	}
	public void setModified(LocalDateTime modified) {
		this.modified = modified;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	
	/*
	 * --------------------------------------
	 * Overriding and DB Methods
	 * --------------------------------------
	 * 
	*/
	
	
	@PrePersist
	public void onCreate() {
		receiptNumber = null;
		this.setCreated(LocalDateTime.now());
		this.setModified(LocalDateTime.now());
		setCreatedBy( ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername() );
		setModifiedBy( ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername() );
	}
	@Override
	public String toString() {
		return "Receipt [receiptNumber=" + receiptNumber + ", paymentType=" + paymentType + ", paymentFor=" + paymentFor
				+ ", paymentDate=" + paymentDate + ", amount=" + amount + ", receiptNotes=" + receiptNotes + ", member=" + member
				+ ", created=" + created + ", modified=" + modified + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((member == null) ? 0 : member.hashCode());
		result = prime * result + ((paymentDate == null) ? 0 : paymentDate.hashCode());
		result = prime * result + ((paymentFor == null) ? 0 : paymentFor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Receipt other = (Receipt) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.equals(other.member))
			return false;
		if (paymentDate == null) {
			if (other.paymentDate != null)
				return false;
		} else if (!paymentDate.equals(other.paymentDate))
			return false;
		if (paymentFor != other.paymentFor)
			return false;
		return true;
	}
	
}