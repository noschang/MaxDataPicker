package br.lusoft.max.data.picker.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.JAXBException;
import br.lusoft.max.data.picker.model.Realtor;
import br.lusoft.max.data.picker.processing.DataPicker;
import br.lusoft.max.data.picker.processing.DataPickerListener;
import br.lusoft.max.data.picker.processing.ServerLoginException;
import br.lusoft.max.data.picker.util.ImageLoader;

public final class MainView extends JFrame
{
	private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());
	private static final File workDirectory = new File(System.getProperty("user.home"), ".max_data_picker");

	private static final Image smallIcon = ImageLoader.load("small_icon.png");
	private static final Image largeIcon = ImageLoader.load("large_icon.png");
	private static final Image cancelIcon = ImageLoader.load("cancel.png");
	private static final Image startIcon = ImageLoader.load("start.png");

	private final JButton startButton = new JButton("Iniciar");
	private final JTextField loginTextField = new JTextField();
	private final JPasswordField passwordTextField = new JPasswordField();
	private final JProgressBar progressBar = new JProgressBar();
	private final JLabel statusLabel = new JLabel("Estado atual: Parado");
	private final DataPresenter dataPresenter = new DataView(this);

	private final ExecutorService service = Executors.newSingleThreadExecutor();
	private DataPickingTask dataPickingTask;

	private Action startPickingAction;
	private Action cancelPickingAction;
	private Action closeAction;

	public MainView()
	{
		initFrame();
		initComponents();
		configureActions();
	}

	private void initFrame()
	{
		setSize(520, 320);
		setResizable(false);
		setTitle("Coletor de Dados");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		setIconImage(smallIcon);
	}

	private void configureActions()
	{
		configureStartPickingAction();
		configureCancelPickingAction();
		configureCloseAction();
	}

	private void configureStartPickingAction()
	{
		startPickingAction = new AbstractAction("Iniciar", new ImageIcon(startIcon))
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{

				if (validateFields() && !taskIsRunning())
				{
					dataPickingTask = new DataPickingTask();
					service.execute(dataPickingTask);
				}
			}
		};

		startButton.setAction(startPickingAction);
	}

	private void configureCancelPickingAction()
	{
		cancelPickingAction = new AbstractAction("Cancelar", new ImageIcon(cancelIcon))
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (taskIsRunning())
				{
					dataPickingTask.cancel();
				}
			}
		};
	}

	private void configureCloseAction()
	{
		closeAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (taskIsRunning())
				{
					JOptionPane.showMessageDialog(MainView.this, "A coleta dos dados ainda está em execução.\nVocê deve cancelar a tarefa ou aguardar até que ela termine antes de poder sair.", "Coletor de Dados", JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					System.exit(0);
				}
			}
		};

		getRootPane().getActionMap().put("closeAction", closeAction);
		getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeAction");
	}

	private boolean taskIsRunning()
	{
		return (dataPickingTask != null) && (dataPickingTask.isRunning());
	}

	private boolean validateFields()
	{
		final String login = loginTextField.getText();
		final String password = new String(passwordTextField.getPassword());

		if (login.length() == 0)
		{
			JOptionPane.showMessageDialog(MainView.this, "Você deve informar seu nome de usuário da Intranet", "Coletor de Dados", JOptionPane.INFORMATION_MESSAGE);
			loginTextField.requestFocus();

			return false;
		}
		else if (password.length() == 0)
		{
			JOptionPane.showMessageDialog(MainView.this, "Você deve informar sua senha da Intranet", "Coletor de Dados", JOptionPane.INFORMATION_MESSAGE);
			passwordTextField.requestFocus();

			return false;
		}

		return true;
	}

	private final class DataPickingTask implements Runnable, DataPickerListener
	{
		private DataPicker dataPicker;
		private boolean running = false;

		@Override
		public void run()
		{
			if (!isRunning())
			{
				try
				{
					setRunning(true);

					final String login = loginTextField.getText();
					final String password = new String(passwordTextField.getPassword());

					final Realtor realtor = new Realtor(login, password);

					dataPicker = new DataPicker(workDirectory, realtor);
					dataPicker.addListener(this);
					dataPicker.pick();

					setRunning(false);
				}
				catch (final ServerLoginException exception)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							dataPickingTask = null;
							progressBar.setValue(0);
							progressBar.setIndeterminate(false);
							progressBar.setStringPainted(false);
							statusLabel.setText("Estado atual: ".concat(exception.getMessage()));

							LOGGER.log(Level.INFO, exception.getMessage(), exception);
							JOptionPane.showMessageDialog(MainView.this, exception.getMessage(), "Coletor de Dados", JOptionPane.ERROR_MESSAGE);

							startButton.setAction(startPickingAction);
							loginTextField.setEnabled(true);
							passwordTextField.setEnabled(true);
							passwordTextField.requestFocus();
						}
					});
				}
				catch (final IOException | JAXBException | URISyntaxException exception)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
							JOptionPane.showMessageDialog(MainView.this, exception.getMessage(), "Coletor de Dados", JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
					});
				}
			}
		}

		public void cancel()
		{
			while (isRunning())
			{
				if (!dataPicker.isCanceled())
				{
					dataPicker.cancel();
				}
			}
		}

		private void setRunning(boolean running)
		{
			this.running = running;
		}

		public synchronized boolean isRunning()
		{
			return running;
		}

		@Override
		public void downloadStarted()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					statusLabel.setText("Estado atual: Preparando para baixar os arquivos");
				}
			});
		}

		@Override
		public void fileDownloadStarted(final int current, final int total)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					if (current == 1)
					{
						progressBar.setIndeterminate(false);
						progressBar.setStringPainted(true);
						progressBar.setMaximum(total);
					}

					progressBar.setValue(current);
					statusLabel.setText(String.format("Estado atual: Baixando arquivo %d de %d", current, total));
				}
			});
		}

		@Override
		public void downloadFinished()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					statusLabel.setText("Estado atual: Arquivos baixados com sucesso");
				}
			});
		}

		@Override
		public void miningStarted()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					progressBar.setIndeterminate(false);
					progressBar.setStringPainted(true);
					progressBar.setValue(0);
					statusLabel.setText("Estado atual: Iniciando a mineração dos dados");
				}
			});
		}

		@Override
		public void fileMiningStarted(final int current, final int total)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (current == 1)
					{
						progressBar.setMaximum(total);
					}

					progressBar.setValue(current);
					statusLabel.setText(String.format("Estado atual: Minerando arquivo %d de %d", current, total));
				}
			});
		}

		@Override
		public void miningFinished()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					statusLabel.setText("Estado atual: A mineração dos dados foi concluída");
				}
			});
		}

		@Override
		public void pickingStarted()
		{
			this.running = true;

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					startButton.setAction(cancelPickingAction);
					progressBar.setIndeterminate(true);
					progressBar.setStringPainted(false);
					progressBar.setValue(0);
					statusLabel.setText("Estado atual: Iniciando a coleta de dados");
					loginTextField.setEnabled(false);
					passwordTextField.setEnabled(false);
				}
			});
		}

		@Override
		public void pickingFinished()
		{
			this.running = false;

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					startButton.setAction(startPickingAction);
					progressBar.setIndeterminate(false);
					progressBar.setStringPainted(false);
					progressBar.setValue(0);
					statusLabel.setText("Estado atual: A coleta dos dados foi concluída");
					loginTextField.setEnabled(true);
					passwordTextField.setEnabled(true);

					dataPresenter.present(dataPicker.getData(), dataPicker.getUpdatesCount());
				}
			});
		}

		@Override
		public void pickingCanceled()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					startButton.setAction(startPickingAction);
					progressBar.setIndeterminate(false);
					progressBar.setValue(0);
					statusLabel.setText("Estado atual: A coleta dos dados foi cancelada");
					loginTextField.setEnabled(true);
					passwordTextField.setEnabled(true);
				}
			});
		}

		@Override
		public void fileMiningFinished(final int current, final int total)
		{

		}

		@Override
		public void fileDownloadFinished(final int current, final int total)
		{

		}

		@Override
		public void savingDataStarted()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					progressBar.setIndeterminate(true);
					progressBar.setStringPainted(false);
					progressBar.setValue(0);
					statusLabel.setText("Estado atual: Salvando dados");
				}
			});
		}

		@Override
		public void savingDataFinished()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					progressBar.setIndeterminate(true);
					progressBar.setStringPainted(false);
					progressBar.setValue(0);
					statusLabel.setText("Estado atual: Dados salvos com sucesso");
				}
			});
		}
	}

	private void initComponents()
	{
		initComponentListeners();
		initTopPanel();
		initCenterPanel();
		initBottomPanel();
	}

	private void initComponentListeners()
	{
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				closeAction.actionPerformed(null);
			}
		});
	}

	private void initTopPanel()
	{
		final JPanel topPanel = new JPanel();
		final JLabel titleLabel = new JLabel();
		final JSeparator topSeparator = new JSeparator();

		titleLabel.setIcon(new ImageIcon(largeIcon));
		titleLabel.setIconTextGap(10);
		titleLabel.setText("Coletor de dados da Max");
		titleLabel.setFont(titleLabel.getFont().deriveFont(21.0f));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		titleLabel.setVerticalAlignment(JLabel.CENTER);

		topSeparator.setOrientation(JSeparator.HORIZONTAL);

		topPanel.setPreferredSize(new Dimension(10, 70));
		topPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
		topPanel.setLayout(new BorderLayout());
		topPanel.add(titleLabel, BorderLayout.CENTER);
		topPanel.add(topSeparator, BorderLayout.SOUTH);

		getContentPane().add(topPanel, BorderLayout.NORTH);
	}

	private void initCenterPanel()
	{
		final JPanel centerPanel = new JPanel();
		final JPanel loginPanel = createLoginPanel();
		final JPanel progressPanel = createProgressPanel();

		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(loginPanel, BorderLayout.NORTH);
		centerPanel.add(progressPanel, BorderLayout.CENTER);

		getContentPane().add(centerPanel, BorderLayout.CENTER);
	}

	private JPanel createLoginPanel()
	{
		final JPanel loginPanel = new JPanel();
		final JPanel labelsAlginmentPanel = new JPanel();
		final JPanel fieldsAlignmentPanel = new JPanel();
		final JLabel loginLabel = new JLabel();
		final JLabel passwordLabel = new JLabel();

		loginLabel.setText("Usuário:");
		loginLabel.setVerticalAlignment(JLabel.TOP);
		loginLabel.setLabelFor(loginTextField);

		passwordLabel.setText("Senha:");
		passwordLabel.setVerticalAlignment(JLabel.TOP);
		passwordLabel.setLabelFor(passwordTextField);

		labelsAlginmentPanel.setPreferredSize(new Dimension(10, 28));
		labelsAlginmentPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
		labelsAlginmentPanel.setLayout(new GridLayout(1, 1, 8, 0));
		labelsAlginmentPanel.add(loginLabel);
		labelsAlginmentPanel.add(passwordLabel);

		fieldsAlignmentPanel.setLayout(new GridLayout(1, 1, 8, 0));
		fieldsAlignmentPanel.add(loginTextField);
		fieldsAlignmentPanel.add(passwordTextField);

		loginPanel.setLayout(new BorderLayout());
		loginPanel.setBorder(new EmptyBorder(0, 8, 15, 8));
		loginPanel.setPreferredSize(new Dimension(10, 75));
		loginPanel.add(labelsAlginmentPanel, BorderLayout.NORTH);
		loginPanel.add(fieldsAlignmentPanel, BorderLayout.CENTER);

		return loginPanel;
	}

	private JPanel createProgressPanel()
	{
		final JPanel progressPanel = new JPanel();
		final JPanel progressBarAlignmentPanel = new JPanel();
		final JLabel progressLabel = new JLabel("Progresso:");

		progressLabel.setPreferredSize(new Dimension(10, 20));
		progressLabel.setVerticalAlignment(JLabel.TOP);
		progressLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

		progressBar.setPreferredSize(new Dimension(10, 30));

		statusLabel.setVerticalAlignment(JLabel.TOP);
		statusLabel.setBorder(new EmptyBorder(4, 2, 0, 0));

		progressBarAlignmentPanel.setLayout(new BorderLayout());
		progressBarAlignmentPanel.add(progressBar, BorderLayout.NORTH);
		progressBarAlignmentPanel.add(statusLabel, BorderLayout.CENTER);

		progressPanel.setLayout(new BorderLayout());
		progressPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
		progressPanel.add(progressLabel, BorderLayout.NORTH);
		progressPanel.add(progressBarAlignmentPanel, BorderLayout.CENTER);

		return progressPanel;
	}

	private void initBottomPanel()
	{
		final JPanel bottomPanel = new JPanel();
		final JPanel buttonAlignmentPanel = new JPanel();
		final JPanel separatorAlignmentPanel = new JPanel();
		final JSeparator bottomSeparator = new JSeparator();

		separatorAlignmentPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
		separatorAlignmentPanel.setLayout(new BorderLayout());
		separatorAlignmentPanel.add(bottomSeparator, BorderLayout.CENTER);

		startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		startButton.setPreferredSize(new Dimension(110, 32));

		buttonAlignmentPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonAlignmentPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
		buttonAlignmentPanel.add(startButton);

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setPreferredSize(new Dimension(10, 50));
		bottomPanel.add(separatorAlignmentPanel, BorderLayout.NORTH);
		bottomPanel.add(buttonAlignmentPanel, BorderLayout.CENTER);

		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(startButton);
	}
}
