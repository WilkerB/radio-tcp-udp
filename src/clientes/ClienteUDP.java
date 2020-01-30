package clientes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class ClienteUDP extends Thread {
	private Thread thread;
	private DatagramSocket socketUDP;
	private DatagramPacket pacoteRecebido;
	private byte[] dadosRecebidos;
	private Player player;


	public ClienteUDP() throws SocketException {
		this.socketUDP = new DatagramSocket();
		this.thread = new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("PortaUDP (" + getSocketUDP().getLocalPort() + ")");
					reproduzir();
				} catch (IOException e) {
					System.err.println("Erro ClienteUDP "+getSocketUDP().getLocalPort()+": " + e.getMessage());
					System.out.println("Descricao: Pacote recebido da estacao");
				} catch (JavaLayerException e) {
					System.err.println("Erro ClienteUDP "+getSocketUDP().getLocalPort()+":" + e.getMessage());
					System.out.println("Descricao: Reproducao da parte da musica");
				}
			}
		};

		this.iniciarThread();
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public DatagramSocket getSocketUDP() {
		return socketUDP;
	}

	public void setSocketUDP(DatagramSocket socketUDP) {
		this.socketUDP = socketUDP;
	}

	public DatagramPacket getPacoteRecebido() {
		return pacoteRecebido;
	}

	public void setPacoteRecebido(DatagramPacket pacoteRecebido) {
		this.pacoteRecebido = pacoteRecebido;
	}

	public byte[] getDadosRecebidos() {
		return dadosRecebidos;
	}

	public void setDadosRecebidos(byte[] dadosRecebidos) {
		this.dadosRecebidos = dadosRecebidos;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void pararThread() {
		this.getThread().interrupt();
	}

	public void iniciarThread() {
		this.getThread().start();
	}

	private void reproduzir() throws JavaLayerException, IOException{
		while(true) { 
			setDadosRecebidos(new byte[50000]); 
			setPacoteRecebido(new DatagramPacket(dadosRecebidos, dadosRecebidos.length));
			getSocketUDP().receive(getPacoteRecebido()); /* PARA RECEBER O PACOTE CONTENDO A MUSICA */

			setPlayer(new Player(new ByteArrayInputStream(getPacoteRecebido().getData())));
			getPlayer().play(); 
		} 
	}
}