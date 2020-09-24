package com.o1teck.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.o1teck.validation.PasswordMatch;

@Entity
@Table(name="Users", uniqueConstraints={@UniqueConstraint(columnNames={"email"})})
@PasswordMatch(message="{register.password.repeatPassword.mismatch}")
public class SiteUser {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private Long id;
	
	@Column(name="email")
	@Email(message="{register.email.invalid}")
	//@NotBlank(message="{register.email.invalid}")
	private String email;
	
	//@NotNull
	@Column(name="firstname", length=20)
	@Size(min=2, max=20, message="{register.firstname.size}")
	private String firstname;
	
	//@NotNull
	@Column(name="surname", length=25)
	@Size(min=2, max=25, message="{register.surname.size}")
	private String surname;
	
	@Column(name="role")
	private String role;
	
	@Column(name="enabled")
	private boolean enabled = false;
	
	@Transient
	@Size(min=4, max=15, message="{register.password.size}")
	private String plainPassword;
	
	@Transient
	private String repeatPassword;
	
	
	public SiteUser(){
		
	}
	
	public SiteUser(String firstname, String surname, String email, String password){
		this.firstname = firstname;
		this.surname = surname;
		this.email = email;
		this.setPlainPassword(password);
		this.repeatPassword = password;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPlainPassword() {
		return plainPassword;
	}

	public void setPlainPassword(String plainPassword) {
		this.password = new BCryptPasswordEncoder().encode(plainPassword);
		this.plainPassword = plainPassword;
	}

	public Long getId() {
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

	@Column(name="password")
	private String password;


	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "SiteUser [id = " + id 
				+ ", "+ "email = " + email 
				+ ", firstname = " + firstname 
				+ ", surname = " + surname
				+ ", role = " + role 
				+ ", enabled = " + enabled 
				+ ", plainPassword = " + plainPassword 
				+ ", repeatPassword = "+ repeatPassword 
				+ ", password = " + password + "]";
	}
}
