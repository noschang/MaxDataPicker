package br.lusoft.max.data.picker.view;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

final class ConfirmFileChooser extends JFileChooser
{
	@Override
	public void approveSelection()
	{
		final File file = getSelectedFile();

		if (file.exists())
		{
			int option = JOptionPane.showConfirmDialog(this, "O arquivo já existe, deseja sobreescrevê-lo?", getDialogTitle(), JOptionPane.YES_NO_CANCEL_OPTION);

			if (option == JOptionPane.YES_OPTION)
			{
				super.approveSelection();
			}
			else if (option == JOptionPane.CANCEL_OPTION)
			{
				cancelSelection();
			}
		}
		else
		{
			super.approveSelection();
		}
	}
}
