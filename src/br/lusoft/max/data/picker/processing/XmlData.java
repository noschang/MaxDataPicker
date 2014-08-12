package br.lusoft.max.data.picker.processing;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import br.lusoft.max.data.picker.model.Client;

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
final class XmlData
{
	@XmlElement(name = "client")
	private List<Client> data;

	public XmlData()
	{

	}

	public List<Client> getData()
	{
		return data;
	}

	public void setData(final List<Client> data)
	{
		this.data = data;
	}
}
