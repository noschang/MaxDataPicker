package br.lusoft.max.data.picker.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "description", "value", "type" })
public final class Property
{
	@XmlAttribute(name = "description")
	private String description;

	@XmlAttribute(name = "type")
	private PropertyType type;

	@XmlAttribute(name = "value")
	private double value;

	public Property()
	{

	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public PropertyType getType()
	{
		return type;
	}

	public void setType(final PropertyType type)
	{
		this.type = type;
	}

	public double getValue()
	{
		return value;
	}

	public void setValue(final double value)
	{
		this.value = value;
	}
}