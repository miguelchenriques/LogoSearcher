package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import streamedobjects.Job;

public class Client {
	private GUI window;

	private final int PORTO;
	private final String endereco;

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Socket socket;

	private static final int CLIENT = 1;

	private void runClient() {
		try {
			doConections();
			out.writeInt(CLIENT);
			out.flush();
			window = new GUI(this);
			window.open();
			@SuppressWarnings("unchecked")
			ArrayList<String> types = (ArrayList<String>) in.readObject();
			window.writeSearchTypes(types); // Vai escrever os tipos de pesquisa disponoveis
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void doConections() throws IOException {
		InetAddress server = InetAddress.getByName(endereco);
		socket = new Socket(server, PORTO);
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
	}

	public void sendImages(File[] imagensDir, BufferedImage subimagem, List<String> types) {
		byte[] subimg = convertToByteArray(subimagem);
		ArrayList<byte[]> imgs = new ArrayList<>();
		ArrayList<BufferedImage> buffImgs = new ArrayList<>();

		for (File f : imagensDir) {
			try {
				BufferedImage imagem = ImageIO.read(f);
				buffImgs.add(imagem);
				byte[] img = convertToByteArray(imagem);
				imgs.add(img);
			} catch (Exception e) {
			}
		}
		try {
			out.writeObject(new Job(imgs, subimg, types));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new ReceiveResults(buffImgs,imagensDir).start();
	}

	private byte[] convertToByteArray(BufferedImage img) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos);
			baos.flush();
			byte[] bImg = baos.toByteArray();
			baos.close();
			return bImg;
		} catch (IOException e) {
		}
		return null;
	}

	private class ReceiveResults extends Thread {
		private ArrayList<BufferedImage> buffImgs;
		private File[] imagensDir;

		private ReceiveResults(ArrayList<BufferedImage> buffImgs, File[] imagensDir) {
			this.buffImgs = buffImgs;
			this.imagensDir = imagensDir;
		}

		public void run() {
			try {
				@SuppressWarnings("unchecked")
				HashMap<Integer, ArrayList<Point[]>> results = (HashMap<Integer, ArrayList<Point[]>>) in.readObject();
				LinkedHashMap<String, ImageIcon> images = new LinkedHashMap<>();
				for (Integer i : results.keySet()) {
					drawImage(results.get(i), buffImgs.get(i));
					String imageName = imagensDir[i].getName();
					images.put(imageName, new ImageIcon(buffImgs.get(i)));
				}
				window.update(images);
			} catch (ClassNotFoundException e) {
			} catch (IOException e) {
			}
		}

		private void drawImage(ArrayList<Point[]> results, BufferedImage imagem) {
			for (Point[] p : results) {
				Graphics2D g2d = imagem.createGraphics();
				g2d.setColor(Color.RED);
				g2d.drawRect((int) p[0].getX(), (int) p[0].getY(), (int) (p[1].getX() - p[0].getX()),
						(int) (p[1].getY() - p[0].getY()));
				g2d.dispose();
			}
		}
	}

	public Client(String endereco, int PORTO) {
		this.endereco = endereco;
		this.PORTO = PORTO;
	}

	public static void main(String[] args) {
		Client c = new Client(args[0], Integer.parseInt(args[1]));
		c.runClient();
	}
}