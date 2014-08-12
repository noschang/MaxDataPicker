package br.lusoft.max.data.picker.view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import br.lusoft.max.data.picker.model.Client;
import br.lusoft.max.data.picker.model.PhoneNumber;

final class DataModel extends AbstractTableModel
{
	private final List<Client> data;

	public DataModel(final List<Client> data)
	{
		this.data = data;
	}

	public DataModel()
	{
		this.data = new ArrayList<>();
	}

	@Override
	public int getColumnCount()
	{
		return 10;
	}

	@Override
	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0:
				return "Nome";
			case 1:
				return "Cônjuge";
			case 2:
				return "Empresa";
			case 3:
				return "Cargo";
			case 4:
				return "Email";
			case 5:
				return "Imóvel";
			case 6:
				return "Valor";
			case 7:
				return "Tipo";
			case 8:
				return "Observação";
			case 9:
				return "Telefones";
		}

		return "?";
	}

	@Override
	public int getRowCount()
	{
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		final Client client = data.get(rowIndex);

		switch (columnIndex)
		{
			case 0:
				return client.getName();
			case 1:
				return client.getPartnerName();
			case 2:
				return client.getEnterprise();
			case 3:
				return client.getJobFunction();
			case 4:
				return client.getEmail();
			case 5:
				return client.getInterestProperty().getDescription();
			case 6:
				return client.getInterestProperty().getValue();
			case 7:
				return client.getInterestProperty().getType().getName();
			case 8:
				return client.getRemark();
			case 9:
				return extractPhones(client);
		}

		return null;
	}

	private String extractPhones(final Client client)
	{
		final StringBuilder stringBuilder = new StringBuilder();

		if (client.getPhoneNumbers() != null)
		{
			for (int index = 0; index < client.getPhoneNumbers().size(); index++)
			{
				PhoneNumber phoneNumber = client.getPhoneNumbers().get(index);
	
				stringBuilder.append(phoneNumber.getNumber());
				stringBuilder.append(" (");
				stringBuilder.append(phoneNumber.getType().getName());
				stringBuilder.append(")");
	
				if (index < client.getPhoneNumbers().size() - 1)
				{
					stringBuilder.append(", ");
				}
			}
		}

		return stringBuilder.toString();
	}

	public List<Client> getData()
	{
		return data;
	}
}
