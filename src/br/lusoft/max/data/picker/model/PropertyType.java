package br.lusoft.max.data.picker.model;

public enum PropertyType
{
	APARTMENT("Apartamento"), HOUSE("Casa"), BUSINESS("Comercial"), HANGAR("Galp�o"),

	STORE("Loja"), ROOM("Sala"), SITE("S�tio"), LAND("Terreno"), UNTOLD("-");

	private String name;

	private PropertyType(final String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public static PropertyType byName(String name)
	{
		name = normalizeName(name);

		for (PropertyType type : PropertyType.values())
		{
			if (name.equals(normalizeName(type.name)))
			{
				return type;
			}
		}

		return PropertyType.UNTOLD;
	}

	private static String normalizeName(String name)
	{
		name = name.toUpperCase();
		name = name.replace("�", "A");
		name = name.replace("�", "I");

		return name;
	}
}
