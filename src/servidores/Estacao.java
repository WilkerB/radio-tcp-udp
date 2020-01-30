package servidores;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;


public class Estacao extends Thread {
	private Thread thread;
	private DatagramSocket estacaoUDP;
	private DatagramPacket pacoteEnviado;
	private Vector<Integer> ouvintesPortaUDP;
	private InetAddress enderecoIP;
	private Vector<byte[]> partesDaMusica;
	private String nomeDaMusica;
	private String caminhoDaMusica;

	public Estacao(String caminhoDaMusica) throws SocketException, FileNotFoundException, IOException{
		this.estacaoUDP = new DatagramSocket(); // SocketException
		this.ouvintesPortaUDP = new Vector<Integer>();
		this.partesDaMusica = new Vector<byte[]>();
		this.caminhoDaMusica = caminhoDaMusica;
		this.nomeDaMusica = pegaNomeDaMusica(caminhoDaMusica);
		this.enderecoIP = InetAddress.getByName("localhost");
		this.particionarMusica(caminhoDaMusica); // FileNotFoundExecption
		this.thread = new Thread() {
			@Override
			public void run() {
				System.out.println("Estacao, reproduzindo: " + getNomeDaMusica() + ".");
				try {
					enviarMusica();
				} catch (IOException | InterruptedException e) {
					System.out.println("Erro ao EnviarMusica!");
					e.printStackTrace();
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
	
	public DatagramSocket getEstacaoUDP() {
		return estacaoUDP;
	}
	
	public void setEstacaoUDP(DatagramSocket estacaoUDP) {
		this.estacaoUDP = estacaoUDP;
	}
	
	public DatagramPacket getPacoteEnviado() {
		return pacoteEnviado;
	}
	
	public void setPacoteEnviado(DatagramPacket pacoteEnviado) {
		this.pacoteEnviado = pacoteEnviado;
	}
	
	public Vector<Integer> getOuvintesPortaUDP() {
		return ouvintesPortaUDP;
	}
	
	public void setOuvintesPortaUDP(Vector<Integer> portasUDP) {
		this.ouvintesPortaUDP = portasUDP;
	}
	
	public Vector<byte[]> getPartesDaMusica() {
		return partesDaMusica;
	}
	
	public void setPartesDaMusica(Vector<byte[]> partesDaMusica) {
		this.partesDaMusica = partesDaMusica;
	}
	
	public String getNomeDaMusica() {
		return nomeDaMusica;
	}
	
	public void setNomeDaMusica(String nomeDaMusica) {
		this.nomeDaMusica = nomeDaMusica;
	}
	
	public String getCaminhoDaMusica() {
		return caminhoDaMusica;
	}
	
	public void setCaminhoDaMusica(String caminhoDaMusica) {
		this.caminhoDaMusica = caminhoDaMusica;
	}
	
	public InetAddress getEnderecoIP() {
		return enderecoIP;
	}
	
	public void setEnderecoIP(InetAddress enderecoIP) {
		this.enderecoIP = enderecoIP;
	}
	
	public void pararThread() {
		getThread().interrupt();
	}

	public void iniciarThread() {
		getThread().start();
	}
	
	public void addOuvinte(int portaUDP) {
		getOuvintesPortaUDP().add(portaUDP);
	}
	
	public void remOuvinte(Integer portaUDP) {
		if (getOuvintesPortaUDP().contains(portaUDP)) {
			getOuvintesPortaUDP().remove(portaUDP);
		}
	}

	public void listarOuvintes() {
		if (getOuvintesPortaUDP().size() > 0) {
			System.out.println("     " + getOuvintesPortaUDP().size() + " Ouvinte(s) Conectado(s)!");
			for (int p : getOuvintesPortaUDP()) {
				System.out.println("     Cliente: " + p);
			}
		} else {
			System.out.println("     0 Ouvinte(s) Conectado(s)!");
		}
	}

	private String pegaNomeDaMusica(String caminhoDaMusica) {
		String[] partes = caminhoDaMusica.split("/");
		String nome = partes[(partes.length - 1)];
		return nome.substring(0, nome.length() - 4);
	}

	private void particionarMusica(String caminhoDaMusica) throws FileNotFoundException, IOException{
		BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(caminhoDaMusica));
		// FileNotFoundException
		int verifBuffer = 0; 
		while (verifBuffer != -1) { /* SE RETORNAR -1 � PORQUE MUSICA JA ESTA TODA PARTICIONADA */
			byte[] musica = new byte[50000]; /* BYTE PARA GUARDAR PEDA�O DA MUSICA */
			verifBuffer = buffer.read(musica); /* VERIFICAR SE AINDA HA MUSICA PARA PARTICIONAR */
			// IOException
			this.getPartesDaMusica().add(musica); /* ARRAY LIST DO TIPO BYTE QUE GUARDAR 
			 											TODOS OS PEDA�OS DA MUSICA */
		}
		buffer.close();
		// IOException
	}

	public void enviarMusica() throws IOException, InterruptedException{
		while (true) {
			for (int i = 0; i < getPartesDaMusica().size(); i++) {
				for (int j = 0; j < getOuvintesPortaUDP().size(); j++) {
					setPacoteEnviado(new DatagramPacket(getPartesDaMusica().get(i), getPartesDaMusica().get(i).length,
							enderecoIP, getOuvintesPortaUDP().get(j)));
					getEstacaoUDP().send(getPacoteEnviado()); 
					// IOException
				}
				Thread.sleep(2650); // InterruptedExcepction
			}
		}
	}
	
}