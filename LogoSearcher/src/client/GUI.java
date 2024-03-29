package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI{

	private JFrame frame;
	private Client client;
	private BufferedImage logo;
	private File jDir[];
	private DefaultListModel<String> model = new DefaultListModel<>();
	private DefaultListModel<String> typesOfWorkers = new DefaultListModel<>();
	private List<String> types = new ArrayList<String>();
	private HashMap<String, ImageIcon> images;


	public GUI(Client c) {
		frame = new JFrame("Interface");
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addcontent();
		client = c;
	}

	private void addcontent() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel panelImage = new JPanel();
		panelImage.setLayout(new BorderLayout());

		JPanel panelIcon = new JPanel();
		JScrollPane paneIcon = new JScrollPane(panelIcon);
		frame.add(paneIcon, BorderLayout.CENTER);

		JTextField pasta = new JTextField("");
		pasta.setPreferredSize(new Dimension(400, 24));
		panelImage.add(pasta, BorderLayout.CENTER);

		JTextField imagem = new JTextField("");
		imagem.setPreferredSize(new Dimension(400, 24));
		panelImage.add(imagem, BorderLayout.SOUTH);

		model.addElement("Imagens encontradas");

		JList<String> list = new JList<>(model);
		list.setFixedCellWidth(125);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					JLabel imageLabel = new JLabel(images.get(list.getSelectedValue()));
					panelIcon.removeAll();
					panelIcon.add(imageLabel, BorderLayout.CENTER);
					panelIcon.revalidate();
					panelIcon.repaint();
				}
			}
		});
		JScrollPane listScroll = new JScrollPane(list);
		frame.add(listScroll, BorderLayout.EAST);

		JList<String> listOfTypes = new JList<>(typesOfWorkers);
		listOfTypes.setFixedCellWidth(100);
		listOfTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listOfTypes.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					
					types = listOfTypes.getSelectedValuesList();
					
				}
			}
		});
		JScrollPane listOfTypesScroll = new JScrollPane(listOfTypes);
		frame.add(listOfTypesScroll, BorderLayout.WEST);
		
		JPanel panelButton = new JPanel();
		panelButton.setLayout(new BorderLayout());

		JButton BPasta = new JButton("pasta");
		BPasta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser jfc = new JFileChooser(".");
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setMultiSelectionEnabled(true);
				int returnValue = jfc.showOpenDialog(null);
				File selectedFile = null;
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					selectedFile = jfc.getSelectedFile();
				}

				File dir = new File(jfc.getSelectedFile().getAbsolutePath());

				jDir = dir.listFiles();

				pasta.setText(selectedFile.getName());
			}
		});
		panelButton.add(BPasta, BorderLayout.NORTH);

		JButton BLogo = new JButton("Logo");
		BLogo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser imageChooser = new JFileChooser(".");
				imageChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnValue2 = imageChooser.showOpenDialog(null);
				File selectedImage = null;
				if (returnValue2 == JFileChooser.APPROVE_OPTION) {
					selectedImage = imageChooser.getSelectedFile();
				}
				imagem.setText(selectedImage.getName());

				try {
					logo = ImageIO.read(new File(selectedImage.getAbsolutePath()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		panelButton.add(BLogo, BorderLayout.SOUTH);

		// Vai buscar a Imagem de BImagem e do JList
		JButton procura = new JButton("procura");
		procura.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.sendImages(jDir, logo, types);
			}
		});
		panel.add(procura, BorderLayout.SOUTH);

		frame.add(panel, BorderLayout.SOUTH);
		panel.add(panelButton, BorderLayout.EAST);
		panel.add(panelImage, BorderLayout.CENTER);
		frame.setLocation(250, 50);
	}

	public void open() {
		frame.setSize(1500, 1000);
		frame.setVisible(true);
	}

	// Quando o cliente recebe um resultado de pesquisa vai ativar este procedimento
	public void update(HashMap<String,ImageIcon> images) {
		this.images = images;
		model.clear();
		for (String s: images.keySet()) {
			model.addElement(s);
		}
	}

	public void writeSearchTypes(ArrayList<String> types) {
		typesOfWorkers.clear();
		for(String s: types) {
			typesOfWorkers.addElement(s);
		}
		
	}

}