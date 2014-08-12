package br.lusoft.max.data.picker.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumnModel;
import br.lusoft.max.data.picker.model.Client;
import br.lusoft.max.data.picker.util.ImageLoader;

final class DataView extends JDialog implements DataPresenter
{
	private static final Image resultIcon = ImageLoader.load("result.png");
	private static final Image okIcon = ImageLoader.load("ok.png");

	private final JFileChooser fileChooser = new ConfirmFileChooser();
	private final XmlFileFIlter xmlFileFIlter = new XmlFileFIlter();

	private final JTable dataTable = new JTable(new DataModel());
	private DataModel dataModel;

	private final JButton closeButton = new JButton("Salvar");
	private final JLabel updatesLabel = new JLabel("Teste");

	private Action closeAction;

	public DataView(final JFrame parent)
	{
		super(parent);

		initFrame(parent);
		initComponents();
		configureActions();
	}

	private void initFrame(final JFrame parent)
	{
		setIconImage(parent.getIconImage());
		setSize(new Dimension(640, 480));
		setLocationRelativeTo(null);
		setResizable(false);
		setModal(true);
		setTitle("Coletor de Dados");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void configureActions()
	{
		configureCloseAction();
	}

	private void configureCloseAction()
	{
		closeAction = new AbstractAction("OK", new ImageIcon(okIcon))
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				switch (getDefaultCloseOperation())
				{
					case JDialog.DISPOSE_ON_CLOSE:
						dispose();
						break;
					case JDialog.EXIT_ON_CLOSE:
						System.exit(0);
						break;
					case JDialog.HIDE_ON_CLOSE:
						setVisible(false);
						break;
				}
			}
		};

		closeButton.setAction(closeAction);

		getRootPane().getActionMap().put("closeAction", closeAction);
		getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeAction");
	}

	private void initComponents()
	{
		initTopPanel();
		initCenterPanel();
		initBottomPanel();
		initFileChooser();
	}

	private void initTopPanel()
	{
		final JPanel topPanel = new JPanel();
		final JLabel titleLabel = new JLabel();
		final JSeparator topSeparator = new JSeparator();

		titleLabel.setText("Dados coletados");
		titleLabel.setIconTextGap(10);
		titleLabel.setIcon(new ImageIcon(resultIcon));
		titleLabel.setFont(titleLabel.getFont().deriveFont(18.0f));
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
		final JScrollPane scrollPane = new JScrollPane();
		final TableColumnModel columnModel = dataTable.getColumnModel();

		dataTable.setFocusable(false);
		dataTable.setColumnSelectionAllowed(false);
		dataTable.setRowSelectionAllowed(true);
		dataTable.setShowVerticalLines(true);
		dataTable.setShowHorizontalLines(true);
		dataTable.setRowHeight(24);
		dataTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.getTableHeader().setReorderingAllowed(false);
		dataTable.getTableHeader().setResizingAllowed(false);
		dataTable.setAutoCreateColumnsFromModel(false);
		dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		columnModel.getColumn(0).setPreferredWidth(200);
		columnModel.getColumn(1).setPreferredWidth(200);
		columnModel.getColumn(2).setPreferredWidth(150);
		columnModel.getColumn(3).setPreferredWidth(150);
		columnModel.getColumn(4).setPreferredWidth(150);
		columnModel.getColumn(5).setPreferredWidth(250);
		columnModel.getColumn(6).setPreferredWidth(100);
		columnModel.getColumn(7).setPreferredWidth(100);
		columnModel.getColumn(8).setPreferredWidth(350);
		columnModel.getColumn(9).setPreferredWidth(350);

		scrollPane.setViewportView(dataTable);

		centerPanel.setLayout(new GridLayout(1, 1));
		centerPanel.setBorder(new EmptyBorder(6, 8, 6, 8));
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		getContentPane().add(centerPanel, BorderLayout.CENTER);
	}

	private void initBottomPanel()
	{
		final JPanel bottomPanel = new JPanel();
		final JPanel buttonAlignmentPanel = new JPanel();
		final JPanel labelAlignmentPanel = new JPanel();
		final JPanel separatorAlignmentPanel = new JPanel();
		final JSeparator bottomSeparator = new JSeparator();

		separatorAlignmentPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
		separatorAlignmentPanel.setLayout(new BorderLayout());
		separatorAlignmentPanel.add(bottomSeparator, BorderLayout.CENTER);

		updatesLabel.setPreferredSize(new Dimension(450, 30));
		updatesLabel.setVerticalAlignment(JLabel.TOP);
		updatesLabel.setBorder(new EmptyBorder(14, 0, 0, 6));

		closeButton.setText("Fechar");
		closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeButton.setPreferredSize(new Dimension(110, 32));

		buttonAlignmentPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonAlignmentPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
		buttonAlignmentPanel.add(closeButton);

		labelAlignmentPanel.setLayout(new BorderLayout());
		labelAlignmentPanel.setBorder(new EmptyBorder(0, 8, 0, 0));
		labelAlignmentPanel.add(updatesLabel, BorderLayout.CENTER);
		labelAlignmentPanel.add(buttonAlignmentPanel, BorderLayout.EAST);

		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setPreferredSize(new Dimension(10, 50));
		bottomPanel.add(separatorAlignmentPanel, BorderLayout.NORTH);
		bottomPanel.add(labelAlignmentPanel, BorderLayout.CENTER);

		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(closeButton);
	}

	private void initFileChooser()
	{
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(xmlFileFIlter);
	}

	@Override
	public void present(List<Client> data, final int updatesCount)
	{
		if (data != null)
		{
			dataModel = new DataModel(data);
			dataTable.setModel(dataModel);
		}

		if (updatesCount > 0)
		{
			updatesLabel.setText(String.format("Foram inseridos %d novo(s) registro(s)", updatesCount));
		}
		else
		{
			updatesLabel.setText("Não foram encontrados novos registros");
		}

		setVisible(true);
	}
}
