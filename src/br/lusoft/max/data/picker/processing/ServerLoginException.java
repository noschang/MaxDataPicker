package br.lusoft.max.data.picker.processing;

public final class ServerLoginException extends Exception
{
	public ServerLoginException()
	{
		super("Não foi possível logar no site. O usuário ou a senha estão incorretos");
	}
}
