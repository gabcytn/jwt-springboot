package com.gabcytyn.jwtdemo.DTO;

public class RegisterUserDto
{
	// TODO: add validation

	private String email;
	private String fullName;
	private String password;

	public RegisterUserDto(String email, String fullName, String password)
	{
		this.email = email;
		this.fullName = fullName;
		this.password = password;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getFullName()
	{
		return fullName;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public String toString()
	{
		return "RegisterUserDto{" +
						"email='" + email + '\'' +
						", fullName='" + fullName + '\'' +
						", password='" + password + '\'' +
						'}';
	}
}
