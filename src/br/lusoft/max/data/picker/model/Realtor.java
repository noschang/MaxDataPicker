package br.lusoft.max.data.picker.model;

public final class Realtor
{
	private final String login;
	private final String password;

	public Realtor(String login, String password)
	{
		this.login = login;
		this.password = password;
	}

	public String getLogin()
	{
		return login;
	}

	public String getPassword()
	{
		return password;
	}
}
