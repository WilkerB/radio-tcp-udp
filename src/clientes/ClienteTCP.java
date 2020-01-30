package clientes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import mensagens.MensagemCliente;

public class ClienteTCP extends Thread {
	private Thread threadMenu;
	private Thread threadPrincipal;
	private Socket conexao;
	private ClienteUDP clienteUDP;
	private int portaUDP;
	private int estacaoConectada;
	private int estacoesDisponiveis;
	private boolean desativa = false;
	private MensagemCliente msgCliente;
	private Scanner lerTeclado;

	public ClienteTCP(int portaServidor, ClienteUDP clienteUDP) throws UnknownHostException, IOException {
		this.clienteUDP = clienteUDP;
		this.conexao = new Socket("localhost", portaServidor);
		this.portaUDP = clienteUDP.getSocketUDP().getLocalPort();
		this.msgCliente = new MensagemCliente();
		this.lerTeclado = new Scanner(System.in);
		this.threadMenu = new Thread() {
			@Override
			public void run() {
				menu();
			}
		};
	}

	public Thread getThreadMenu() {
		return threadMenu;
	}

	public void setThreadMenu(Thread thread) {
		this.threadMenu = thread;
	}

	public Thread getThreadPrincipal() {
		return threadPrincipal;
	}

	public void setThreadPrincipal(Thread threadPrincipal) {
		this.threadPrincipal = threadPrincipal;
	}

	public Socket getConexao() {
		return conexao;
	}

	public void setConexao(Socket conexao) {
		this.conexao = conexao;
	}

	public ClienteUDP getClienteUDP() {
		return clienteUDP;
	}

	public void setClienteUDP(ClienteUDP clienteUDP) {
		this.clienteUDP = clienteUDP;
	}

	public int getPortaUDP() {
		return portaUDP;
	}

	public void setPortaUDP(int portaUDP) {
		this.portaUDP = portaUDP;
	}

	public int getEstacaoConectada() {
		return estacaoConectada;
	}

	public void setEstacaoConectada(int estacaoConectada) {
		this.estacaoConectada = estacaoConectada;
	}

	public int getEstacoesDisponiveis() {
		return estacoesDisponiveis;
	}

	public void setEstacoesDisponiveis(int estacoesDisponiveis) {
		this.estacoesDisponiveis = estacoesDisponiveis;
	}

	public boolean getDesativa() {
		return desativa;
	}

	public void setDesativa(boolean desativa) {
		this.desativa = desativa;
	}

	public MensagemCliente getMsgCliente() {
		return msgCliente;
	}

	public void setMsgCliente(MensagemCliente msgCliente) {
		this.msgCliente = msgCliente;
	}

	public void iniciarThread() {
		getThreadMenu().start();
	}

	public void pararThread() {
		getThreadMenu().interrupt();
	}

	public synchronized Scanner getLerTeclado() {
		return lerTeclado;
	}

	public synchronized void setLerTeclado(Scanner lerTeclado) {
		this.lerTeclado = lerTeclado;
	}

	public void encerrarConexao() throws IOException {
		System.exit(0);
	}

	private int escolherEstacao() {
		System.out.println("Escolher Estacao");
		System.out.println("Disponivel: 0 a " + (getEstacoesDisponiveis() - 1));
		System.out.println("Qual estacao deseja Ouvir? ");
		getLerTeclado().reset();
		int estEscolhida = getLerTeclado().nextInt();
		System.out.println("EscolherEstacao: " + estEscolhida);
		return estEscolhida;
	}

	private void menu() {
		try {
			String opcao = "";
			do {
				System.out.println();
				System.out.println("|--PRESSIONE A QUALQUER MOMENTO--|");
				System.out.println("|   T + Enter: Trocar Estacao    |");
				System.out.println("|   Q + Enter: Encerrar Conexao  |");
				System.out.println("|--------------------------------|");
				getLerTeclado().reset();
				opcao = getLerTeclado().next();
				if (opcao.charAt(0) == 'T' || opcao.charAt(0) == 't') {
					int i = escolherEstacao();
					getMsgCliente().setStation(new DataOutputStream(getConexao().getOutputStream()), 1, i);
				} else if (opcao.charAt(0) == 'Q' || opcao.charAt(0) == 'q') {
					encerrarConexao();
				}
			} while (opcao.charAt(0) != 'Q' || opcao.charAt(0) != 'q');
		} catch (IOException e) {
			System.err.println("Erro ClienteTCP: " + e.getMessage());
			System.out.println("Descricao: Erro ao trocar estacao ou\n" + "Erro ao encerrar conexao");
		}
	}

	@Override
	public void run() {
		try {
			// System.out.println("PortaTCP (" + getConexao().getPort() + ")");
			System.out.println("PortaTCP (" + getConexao().getLocalPort() + ")");
			getMsgCliente().hello(new DataOutputStream(getConexao().getOutputStream()), 0, getPortaUDP());

			while (true) {
				byte[] dadosRecebidos = new byte[128];
				new DataInputStream(getConexao().getInputStream()).read(dadosRecebidos);

				if (dadosRecebidos[0] == 0) {
					setEstacoesDisponiveis(getMsgCliente().welcomeRecebido(dadosRecebidos));
					System.out.println("\nServidor --> Welcome (" + getEstacoesDisponiveis() + ")");
					setEstacaoConectada(escolherEstacao());
					getMsgCliente().setStation(new DataOutputStream(getConexao().getOutputStream()), 1, getEstacaoConectada());
					threadMenu.start();
				} else if (dadosRecebidos[0] == 1) {
					String nomeDaMusica = getMsgCliente().announceRecebido(dadosRecebidos);
					System.out.println("\nServidor --> Announce (" + nomeDaMusica + ")");
				} else if (dadosRecebidos[0] == 2) {
					String msgErro = getMsgCliente().invalidCommandRecebido(dadosRecebidos);
					System.out.println("\nServidor --> InvalidCommand (" + msgErro + ")");
					throw new Exception();
				}
			}
		} catch (SocketException e) {
			System.err.println("\nErro ClienteTCP: " + e.getMessage());
			System.out.println("Descricao: Conexao com servidor perdida, encerrando cliente!");
		} catch (IOException e) {
			System.err.println("\nErro ClienteTCP: " + e.getMessage());
			System.out.println("Descricao: Erro ao enviar hello ou \n" + "Erro ao receber mensagem do servidor ou \n"
					+ "Erro ao chamar metodo SetStation() ou \n" + "Erro ao encerrar conexao");
		} catch (Exception e) {
			System.err.println("\nErro ClienteTCP: " + e.getMessage());
			System.out.println("Descricao: Comando invalido ou\n" + "Erro desconhecido");
		} finally {
			try {
				encerrarConexao();
			} catch (IOException e) {
				System.err.println("\nErro ClienteTCP: " + e.getMessage());
				System.out.println("Descricao: Erro ao encerrar conexao com servidor");
			}
		}
	}

	public static void main(String[] args) {
		try {
			ClienteUDP clienteUDP = new ClienteUDP();

			ClienteTCP clienteTCP = new ClienteTCP(12500, clienteUDP);
			Thread t = clienteTCP;
			clienteTCP.setThreadPrincipal(t);
			t.start();

		} catch (UnknownHostException e) {
			System.err.println("Erro ClienteTCP: " + e.getMessage());
			System.out.println("Descricao: Erro ao buscar enderecoIP");
		} catch (SocketException e) {
			System.err.println("Erro ClienteTCP: " + e.getMessage());
			System.out.println("Descricao: Erro ao criar ClienteUDP");
		} catch (IOException e) {
			System.err.println("Erro ClienteTCP: " + e.getMessage());
			System.out.println("Descricao: Erro ao criar ClienteTCP");
		}
	}
}