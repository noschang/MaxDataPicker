package br.lusoft.max.data.picker;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import br.lusoft.max.data.picker.view.CheckThreadViolationRepaintManager;
import br.lusoft.max.data.picker.view.MainView;

public final class Launcher
{
	private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

	public static void main(String[] args)
	{
		RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager(true));

		Launcher launcher = new Launcher();
		launcher.start();
	}

	private void start()
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						UIManager.setLookAndFeel(new NimbusLookAndFeel());
					}
					catch (UnsupportedLookAndFeelException exception)
					{
						LOGGER.log(Level.INFO, exception.getMessage(), exception);
					}

					MainView mainView = new MainView();
					mainView.setVisible(true);
				}
			});
		}
		catch (Exception exception)
		{
			JOptionPane.showMessageDialog(null, exception.getMessage(), "Coletor de Dados", JOptionPane.ERROR_MESSAGE);
		}
	}
}
