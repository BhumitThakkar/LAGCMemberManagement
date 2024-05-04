package us.lagc.model;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import us.lagc.service.CommonService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
public class Member {
	
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="memeber_seq")
	@SequenceGenerator(name = "memeber_seq", sequenceName = "memeber_seq" ,allocationSize = 1)
	@Column(updatable = false, columnDefinition="INT NOT NULL")
	private Integer id;
	
	@NotNull(message = "First Name cannot be null.")
	@NotBlank(message = "First Name cannot be empty.")
	@Size(min = 2, message = "First Name must have at least 2 characters.")
	@Pattern(regexp = "^[A-Za-z]{2,}$", message = "First Name must only be alphabetic. No space allowed.")	// allowed pattern
	private String firstName;
	
	@NotNull(message = "Last Name cannot be null.")
	@NotBlank(message = "Last Name cannot be empty.")
	@Size(min = 2, message = "Last Name must have at least 2 characters.")
	@Pattern(regexp = "^[A-Za-z]{2,}$", message = "Last Name must only be alphabetic. No space allowed.")	// allowed pattern
	private String lastName;
	
	@NotNull(message = "Phone Number cannot be empty.")
	@Positive(message = "Phone Number cannot be negative or zero.")
	@Min(value = 1000000000L, message = "Phone Number must be 10 digits only not starting with 0.")
//	@Max(value = 9999999999L, message = "Phone Number cannot be more than 10 digits.")
	@Digits(fraction = 0, integer = 10, message = "Phone Number must be 10 digits only not starting with 0.")		// max integer, max fraction
	private Long phone = null;					// 10 digits only
	
//	@NotNull(message = "Email cannot be null.")
	@NotBlank(message = "Email cannot be empty.")
	@Email(message = "Email format is incorrect.")
	private String email;
	
	@NotNull(message = "Street cannot be null.")
	@NotBlank(message = "Street cannot be empty.")
	@Pattern(regexp = "^([1-9][0-9]*[A-Za-z]{0,3})( [A-Za-z0-9]+)+$", message = "Street must be min 2 word alphanumeric. Starting first character must be a number. Address may end in unit number if any. No extra space or special characters allowed.")	// allowed pattern
	private String street;

	@NotNull(message = "City cannot be null.")
	@NotBlank(message = "City cannot be empty.")
	@Size(min = 2, message = "City must have at least 2 characters.")
	@Pattern(regexp = "^[A-Za-z]{2,}( [A-Za-z]{2,}){0,1}$", message = "Each word must have at least 2 characters. City must be max 2 word alphabetic. No extra space or special characters allowed.")	// allowed pattern
	private String city;
	
	@NotNull(message = "State cannot be null.")
	@Enumerated(EnumType.STRING)
	private State state;
	
	@NotNull(message = "Zip Code cannot be empty.")
	@Positive(message = "Zip Code cannot be negative or zero.")
	@Min(value = 10000, message = "Zip Code must be 5 digits only not starting with 0.")
//	@Max(value = 99999, message = "Zip Code cannot be more than 5 digits.")
	@Digits(fraction = 0, integer = 5, message = "Zip Code must be 5 digits only not starting with 0.")			// max integer, max fraction
	private Integer zip = null;					// 5 digits only
	
	@NotNull(message = "Member Since can't be null.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")		// yyyy-MM-dd somehow will get transfered into MM/dd/yyyy when it goes to html5 front end
	private LocalDate memberSince = LocalDate.now(); // yyyy-MM-dd backend | MM/dd/yyyy form front end
	
	@NotNull(message = "Membership Expiry Date can't be null.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")		// yyyy-MM-dd somehow will get transfered into MM/dd/yyyy when it goes to html5 front end
	private LocalDate membershipExpiryDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
	
	@NotNull(message = "Life Time Member? cannot be null.")
	@NotBlank(message = "Life Time Member? cannot be empty.")
	private String isLifeMember = "";			// Yes or No
	
	@NotNull(message = "Want Email? cannot be null.")
	@NotBlank(message = "Want Email? cannot be empty.")
	private String wantEmail = "";				// Yes or No
	
	@NotNull(message = "Want Mail? cannot be null.")
	@NotBlank(message = "Want Mail? cannot be empty.")
	private String wantMail = "";				// Yes or No
	
	@Column(columnDefinition="TEXT(65535)")		// instead of varchar(255) https://stackoverflow.com/a/13506920
	private String memberNotes;
	
	@Valid
	@OneToMany(mappedBy = "member", fetch=FetchType.EAGER)				// no cascade because on updating member we do not want receipt to be update as we are sure we will never update id of member
	private List<Receipt> receipts;
	
	@Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")					// this is not application level validation, this is database level validation
	private LocalDateTime created;
	
	@Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")				// this is not application level validation, this is database level validation
	private LocalDateTime modified;
	
	@Column(nullable = false)					// this is not application level validation, this is database level validation
	private String createdBy;
	
	@Column(nullable = false)					// this is not application level validation, this is database level validation
	private String modifiedBy;

	@Transient									// property will not participate in db as column but can be used for display only
	private Boolean membershipExpired;
	
	
	/*
	 * --------------------------------------
	 * Getter and Setters
	 * --------------------------------------
	 * 
	*/
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {				// do not uncomment or update functionality will not work
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Long getPhone() {
		return phone;
	}
	public void setPhone(Long phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = CommonService.cleanStreetName(street);
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = CommonService.capitalizeOnlyAfterSpace(city);
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public Integer getZip() {
		return zip;
	}
	public void setZip(Integer zip) {
		this.zip = zip;
	}
	public LocalDate getMemberSince() {
		return memberSince;
	}
	public void setMemberSince(LocalDate memberSince) {
		this.memberSince = memberSince;
	}
	public LocalDate getMembershipExpiryDate() {
		return membershipExpiryDate;
	}
	public void setMembershipExpiryDate(LocalDate membershipExpiryDate) {
		this.membershipExpiryDate = membershipExpiryDate;
	}
	public String getIsLifeMember() {
		return isLifeMember;
	}
	public void setIsLifeMember(String isLifeMember) {
		this.isLifeMember = isLifeMember;
	}
	public String getWantEmail() {
		return wantEmail;
	}
	public void setWantEmail(String wantEmail) {
		this.wantEmail = wantEmail;
	}
	public String getWantMail() {
		return wantMail;
	}
	public void setWantMail(String wantMail) {
		this.wantMail = wantMail;
	}
	public String getMemberNotes() {
		return memberNotes;
	}
	public void setMemberNotes(String memberNotes) {
		this.memberNotes = memberNotes;
	}
	public List<Receipt> getReceipts() {
		return receipts;
	}
	public void setReceipts(List<Receipt> receipts) {
		this.receipts = receipts;
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
	public Boolean getMembershipExpired() {
		return membershipExpired;
	}
	public void setMembershipExpired(Boolean membershipExpired) {
		this.membershipExpired = membershipExpired;
	}
	
	
	/*
	 * --------------------------------------
	 * Overriding and DB Methods
	 * --------------------------------------
	 * 
	*/
	
	
	@PrePersist
	public void onCreate() {
		id = null;
		email = email.toLowerCase();
		firstName = firstName.toLowerCase();
		firstName = firstName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(firstName.charAt(0))));
		lastName = lastName.toLowerCase();
		lastName = lastName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(lastName.charAt(0))));
		street = CommonService.cleanStreetName(street);
		city = CommonService.capitalizeOnlyAfterSpace(city);
		isLifeMember = isLifeMember.toLowerCase();
		isLifeMember = isLifeMember.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(isLifeMember.charAt(0))));
		wantEmail = wantEmail.toLowerCase();
		wantEmail = wantEmail.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(wantEmail.charAt(0))));
		wantMail = wantMail.toLowerCase();
		wantMail = wantMail.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(wantMail.charAt(0))));
		setCreated(LocalDateTime.now());
		setModified(LocalDateTime.now());
		setCreatedBy( ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername() );
		setModifiedBy( ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername() );
		if(getReceipts() == null || getReceipts().size() == 0) {
			logger.error("Can't create Member without receipt");
			throw new RuntimeException("Can't create Member without receipt");
		}
	}
	@PreUpdate
	public void onUpdate() {
		email = email.toLowerCase();
		firstName = firstName.toLowerCase();
		firstName = firstName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(firstName.charAt(0))));
		lastName = lastName.toLowerCase();
		lastName = lastName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(lastName.charAt(0))));
		street = CommonService.cleanStreetName(street);
		city = CommonService.capitalizeOnlyAfterSpace(city);
		isLifeMember = isLifeMember.toLowerCase();
		isLifeMember = isLifeMember.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(isLifeMember.charAt(0))));
		wantEmail = wantEmail.toLowerCase();
		wantEmail = wantEmail.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(wantEmail.charAt(0))));
		wantMail = wantMail.toLowerCase();
		wantMail = wantMail.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(wantMail.charAt(0))));
		setModified(LocalDateTime.now());
		setModifiedBy( ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername() );
	}
	@PostLoad
	public void onSelect() {
		membershipExpired = getMembershipExpiryDate().isBefore(LocalDate.now());
	}
	@Override
	public String toString() {
		return "Member [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", phone=" + phone
				+ ", email=" + email + ", street=" + street + ", city=" + city + ", state=" + state + ", zip=" + zip
				+ ", isLifeMember=" + isLifeMember + ", memberSince=" + memberSince + ", wantEmail=" + wantEmail
				+ ", wantMail=" + wantMail + ", memberNotes=" + memberNotes + ", created=" + created + ", modified=" + modified
				+ "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
		return result;
	}
	
	// For Adding Member
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equalsIgnoreCase(other.city))													// edited to equalsIgnoreCase
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equalsIgnoreCase(other.firstName))										// edited to equalsIgnoreCase
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equalsIgnoreCase(other.lastName))											// edited to equalsIgnoreCase
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (state != other.state)
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equalsIgnoreCase(other.street))												// edited to equalsIgnoreCase
			return false;
		if (zip == null) {
			if (other.zip != null)
				return false;
		} else if (!zip.equals(other.zip))
			return false;
		return true;
	}
	
	// For Update Member
	public boolean exactEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Member other = (Member) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equalsIgnoreCase(other.city))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equalsIgnoreCase(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equalsIgnoreCase(other.firstName))
			return false;
		if (isLifeMember == null) {
			if (other.isLifeMember != null)
				return false;
		} else if (!isLifeMember.equalsIgnoreCase(other.isLifeMember))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equalsIgnoreCase(other.lastName))
			return false;
		if (memberNotes == null) {
			if (other.memberNotes != null)
				return false;
		} else if (!memberNotes.equalsIgnoreCase(other.memberNotes))
			return false;
		if (memberSince == null) {
			if (other.memberSince != null)
				return false;
		} else if (!memberSince.equals(other.memberSince))
			return false;
		if (membershipExpired == null) {
			if (other.membershipExpired != null)
				return false;
		} else if (!membershipExpired.equals(other.membershipExpired))
			return false;
		if (membershipExpiryDate == null) {
			if (other.membershipExpiryDate != null)
				return false;
		} else if (!membershipExpiryDate.equals(other.membershipExpiryDate))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (state != other.state)
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equalsIgnoreCase(other.street))
			return false;
		if (wantEmail == null) {
			if (other.wantEmail != null)
				return false;
		} else if (!wantEmail.equalsIgnoreCase(other.wantEmail))
			return false;
		if (wantMail == null) {
			if (other.wantMail != null)
				return false;
		} else if (!wantMail.equalsIgnoreCase(other.wantMail))
			return false;
		if (zip == null) {
			if (other.zip != null)
				return false;
		} else if (!zip.equals(other.zip))
			return false;
		return true;
	}
	
	/*
	 * -------------------------
	 * Old Manual Equals Method
	 * -------------------------
	@Override
    public boolean equals(Object o) {
		// If the object is compared with itself then return true 
		if (o == this) {
			return true;
		}
		// Check if o is an instance of Member or not "null instanceof [type]" also returns false
		if (!(o instanceof Member)) {
        	return false;
        }
		// typecast o to Member so that we can compare data
		Member m = (Member) o;
		// Compare the data members and return accordingly
		return m.getId() == this.getId();
    }
	 */
	
}
