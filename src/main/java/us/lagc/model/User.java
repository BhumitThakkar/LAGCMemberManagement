package us.lagc.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@SuppressWarnings("serial")
@Entity
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = "user_seq" ,allocationSize = 1)
	@Column(updatable = false, unique = true, columnDefinition="INT NOT NULL")
	private Long id;
	
	@Column(unique = true)
	@NotNull(message = "Email cannot be null.")
	@NotBlank(message = "Email cannot be empty.")
	@Email(message = "Email format is incorrect.")
	private String email;
	
	@Column(unique = true)
	@NotNull(message = "Username cannot be null.")
	@NotBlank(message = "Username cannot be empty.")
	@Size(min = 2, message = "Username must have at least 2 characters.")
	@Pattern(regexp = "^[A-Za-z]+$", message = "Username must only be alphabetic. No space allowed.")	// allowed pattern
	private String username;
	
	@NotNull(message = "Password cannot be null.")
	@NotBlank(message = "Password cannot be empty.")
	@Size(min = 2, message = "Password must have at least 2 characters.")
	@Pattern(regexp = "^[A-Za-z0-9_#@$./{}]+$", message = "Password can only contain _#@$./{} special chars, numbers and alphabets. No space allowed or other chars allowed.")	// $./ chars allowed because of bcrypt password, {} are allowed if using plain password to prepend: {noop}
	private String password;
	
	@NotNull(message = "First Name cannot be null.")
	@NotBlank(message = "First Name cannot be empty.")
	@Size(min = 2, message = "First Name must have at least 2 characters.")
	@Pattern(regexp = "^[A-Za-z]+$", message = "First Name must only be alphabetic. No space allowed.")	// allowed pattern
	private String firstName;
	
	@NotNull(message = "Last Name cannot be null.")
	@NotBlank(message = "Last Name cannot be empty.")
	@Size(min = 2, message = "Last Name must have at least 2 characters.")
	@Pattern(regexp = "^[A-Za-z]+$", message = "Last Name must only be alphabetic. No space allowed.")	// allowed pattern
	private String lastName;
	
	private boolean accountNonLocked;													// need exact same variable name
	private boolean enabled;															// need exact same variable name
	private boolean accountNonExpired;													// need exact same variable name
	private boolean credentialsNonExpired;												// need exact same variable name
	
	@ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Collection<Role> roles;
	
	public enum Role {																	// ROLE_ required in role names
		ROLE_USER,
		ROLE_ADMIN
	}

	public User() {
	}

	public User(String email, String username, String password, String firstName, String lastName, User.Role role) {
		this.email = email.toLowerCase();
		this.username = username.toLowerCase();
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.accountNonLocked = true;
		this.enabled = true;
		this.accountNonExpired = true;
		this.credentialsNonExpired = true;
		this.roles = Set.of(role);
	}
	
	
	public long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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

	public Collection<Role> getRoles() {
		return roles;
	}
	public void setRoles(Collection<Role> roles) {
		this.roles = roles;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<>();
		for(Role role: roles) {
			GrantedAuthority ga = new SimpleGrantedAuthority(role.toString());
			authorities.add(ga);
		}
		return authorities;
	}
	
	@Override
	public String getUsername() {
		return this.username;
	}
	public void setUsername(String username) {
		this.username = username.toLowerCase();
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	
	// FOR ALL ENTITY WE SHOULD HAVE hashCode() and equals()  
	@Override
	public int hashCode() {
		return Objects.hash(email, firstName, id, lastName, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(email, other.email) && Objects.equals(firstName, other.firstName) && id == other.id
				&& Objects.equals(lastName, other.lastName) && Objects.equals(password, other.password);
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
		username = username.toLowerCase();
		firstName = firstName.toLowerCase();
		firstName = firstName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(firstName.charAt(0))));
		lastName = lastName.toLowerCase();
		lastName = lastName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(lastName.charAt(0))));
	}
	@PreUpdate
	public void onUpdate() {
		email = email.toLowerCase();
		username = username.toLowerCase();
		firstName = firstName.toLowerCase();
		firstName = firstName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(firstName.charAt(0))));
		lastName = lastName.toLowerCase();
		lastName = lastName.replaceFirst("^[A-Za-z]", String.valueOf(Character.toUpperCase(lastName.charAt(0))));
	}

}

// Week 11 Lecture => DATA\Study\Masters - NEIU\Sem 5\CS 321 414\Week 11\Lecture