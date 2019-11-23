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
import java.util.List;
import java.util.Observable;
import java.util.Set;

import javax.imageio.ImageIO;

import streamedobjects.Job;

public class Client extends Observable {
	private static String OUTPUT_NAME = "OUT";
	private File file;
	
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
			GUI window = new GUI(this);
			this.addObserver(window);
			window.open();
			@SuppressWarnings("unchecked")
			Set<String> types = (Set<String>) in.readObject();
			window.writeSearchTypes(types); // Vai escrever os tipos de pesquisa disponoveis
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void doConections() throws IOException {
		InetAddress endereco = InetAddress.getByName(null);
		socket = new Socket(endereco, PORTO);
		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());
	}
	
	public void sendImages(File[] imagensDir, BufferedImage subimagem, List<String> types) {
		byte[] subimg = convertToByteArray(subimagem);
		int n = 1;
		ArrayList<byte[]> imgs = new ArrayList<>();
		
		for (File f:imagensDir) {
			try {
				BufferedImage imagem = ImageIO.read(f);
				byte[] img = convertToByteArray(imagem);
				imgs.add(img);
			} catch (Exception e) {}
		}
		try {
			out.writeObject(new Job(imgs, subimg, types));
			out.flush();
		} catch (IOException e) {}
			try {
				@SuppressWarnings("unchecked")
				HashMap<byte[], ArrayList<Point[]>> results = (HashMap<byte[], ArrayList<Point[]>>) in.readObject();
			} catch (ClassNotFoundException e) {
			} catch (IOException e) {}
			
			/*if (results.size()!=0) {
				drawImage(results, imagem);
				n++;
			}*/
			
			/*String fileName = OUTPUT_NAME+"\\out" + n + ".png";
			try {
				ImageIO.write(imagem, "png", new File(fileName));
			} catch (IOException e) {
			}*/
			
		file = new File(OUTPUT_NAME);
		setChanged();
		notifyObservers(file);
	}

	private void drawImage(ArrayList<Point[]> results, BufferedImage imagem) {
		for (Point[] p: results) {
			Graphics2D g2d = imagem.createGraphics();
			g2d.setColor(Color.RED);
			g2d.drawRect((int)p[0].getX(), (int)p[0].getY(), (int)(p[0].getX()-p[1].getX()), (int)(p[0].getY()-p[1].getY()));
			g2d.dispose();
		}
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
	
	public Client(String endereco, int PORTO) {
		this.endereco = endereco;
		this.PORTO = PORTO;
		runClient();
	}

	public static void main(String[] args) {
		Client c = new Client(args[0], Integer.parseInt(args[1]));
		c.runClient();
	}
}