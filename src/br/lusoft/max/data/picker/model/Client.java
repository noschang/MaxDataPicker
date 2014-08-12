package br.lusoft.max.data.picker.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "dealId", "name", "partnerName", "enterprise", "jobFunction", "email", "interestProperty", "remark", "phoneNumbers" })
public final class Client
{
	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "email")
	private String email;

	@XmlAttribute(name = "enterprise")
	private String enterprise;

	@XmlAttribute(name = "job-function")
	private String jobFunction;

	@XmlElement(name = "interest-property")
	private Property interestProperty;

	@XmlAttribute(name = "partner-name")
	private String partnerName;

	@XmlElement(name = "remark")
	private String remark;

	@XmlElementWrapper(name = "phone-numbers")
	@XmlElement(name = "phone")
	private List<PhoneNumber> phoneNumbers;

	@XmlAttribute(name = "deal-id")
	private int dealId;

	public Client()
	{

	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public List<PhoneNumber> getPhoneNumbers()
	{
		return phoneNumbers;
	}

	public void setPhoneNumbers(final List<PhoneNumber> phoneNumbers)
	{
		if (!phoneNumbers.isEmpty())
		{
			this.phoneNumbers = phoneNumbers;
		}
		else
		{
			this.phoneNumbers = null;
		}
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public String getEnterprise()
	{
		return enterprise;
	}

	public void setEnterprise(final String enterprise)
	{
		this.enterprise = enterprise;
	}

	public String getJobFunction()
	{
		return jobFunction;
	}

	public void setJobFunction(final String jobFunction)
	{
		this.jobFunction = jobFunction;
	}

	public Property getInterestProperty()
	{
		return interestProperty;
	}

	public void setInterestProperty(final Property interestProperty)
	{
		this.interestProperty = interestProperty;
	}

	public String getPartnerName()
	{
		return partnerName;
	}

	public void setPartnerName(final String partnerName)
	{
		this.partnerName = partnerName;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(final String remark)
	{
		if (remark.length() > 0)
		{
			this.remark = remark;
		}
		else
		{
			this.remark = null;
		}
	}

	public int getDealId()
	{
		return dealId;
	}

	public void setDealId(final int dealId)
	{
		this.dealId = dealId;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof Client)
		{
			return ((Client) obj).dealId == this.dealId;
		}

		return false;
	}
}
