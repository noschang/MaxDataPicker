package br.lusoft.max.data.picker.processing;

public final class ServerLoginException extends Exception
{
	public ServerLoginException()
	{
		super("N�o foi poss�vel logar no site. O usu�rio ou a senha est�o incorretos");
	}
}
