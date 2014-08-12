package br.lusoft.max.data.picker.view;

import java.util.List;
import br.lusoft.max.data.picker.model.Client;

interface DataPresenter
{
	public void present(final List<Client> data, final int updatesCount);
}
