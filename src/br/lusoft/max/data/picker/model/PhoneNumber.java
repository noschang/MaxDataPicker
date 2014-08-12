package br.lusoft.max.data.picker.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "number", "type" })
public final class PhoneNumber
{
	public static enum Type
	{
		HOME("Residencial"), BUSINESS("Comercial"), CELL("Celular"), OTHER("Alternativo");

		private String name;

		private Type(final String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	};

	@XmlAttribute(name = "type")
	private Type type;

	@XmlAttribute(name = "number")
	private String number;

	public PhoneNumber()
	{

	}

	public final Type getType()
	{
		return type;
	}

	public final void setType(final Type type)
	{
		this.type = type;
	}

	public final String getNumber()
	{
		return number;
	}

	public final void setNumber(final String number)
	{
		this.number = number;
	}
}