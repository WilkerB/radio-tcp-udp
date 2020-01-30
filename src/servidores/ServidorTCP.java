package servidores;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.Vector;

import mensagens.MensagemServidor;

public class ServidorTCP extends Thread {

    private static Thread threadMenu;
    private Thread threadPrincipal;
    private Socket conexao;
    private Integer portaUDP;
    private static int qntdEstacoes;
    private static Vector<Estacao> estacoes;
    private static Vector<ServidorTCP> clientes;
    private static String opc = "";
    private static ServerSocket server;
    private MensagemServidor msgServidor;
    private static final int PORTA_CONEXAO = 12500;

    public ServidorTCP(Socket conexao) throws SocketException, FileNotFoundException, IOException {
        this.conexao = conexao;
        this.msgServidor = new MensagemServidor();
    }

    public static ServerSocket getServer() {
        return server;
    }

    public static void setServer(ServerSocket server) {
        ServidorTCP.server = server;
    }

    public static Thread getThreadMenu() {
        return threadMenu;
    }

    public static void setThreadMenu(Thread thread) {
        ServidorTCP.threadMenu = thread;
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

    public Integer getPortaUDP() {
        return portaUDP;
    }

    public void setPortaUDP(Integer portaUDP) {
        this.portaUDP = portaUDP;
    }

    public static int getQntdEstacoes() {
        return qntdEstacoes;
    }

    public static void setQntdEstacoes(int qntdEstacoes) {
        ServidorTCP.qntdEstacoes = qntdEstacoes;
    }

    public static Vector<Estacao> getEstacoes() {
        return estacoes;
    }

    public static void setEstacoes(Vector<Estacao> estacoes) {
        ServidorTCP.estacoes = estacoes;
    }

    public static Vector<ServidorTCP> getClientes() {
        return clientes;
    }

    public static void setClientes(Vector<ServidorTCP> clientes) {
        ServidorTCP.clientes = clientes;
    }

    public static String getOpc() {
        return opc;
    }

    public static void setOpc(String opc) {
        ServidorTCP.opc = opc;
    }

    public MensagemServidor getMsgServidor() {
        return msgServidor;
    }

    public void setMsgServidor(MensagemServidor msgServidor) {
        this.msgServidor = msgServidor;
    }

    public static int getPortaConexao() {
        return ServidorTCP.PORTA_CONEXAO;
    }

    public static void desativarClientes() throws IOException {
        for (ServidorTCP c : ServidorTCP.getClientes()) {
            c.getConexao().close();
            c.getThreadPrincipal().interrupt();
        }
    }

    public static void encerrarServidorTCP() throws IOException {
        getServer().close();
    }

    public static void listarEstacoes() {
        if (ServidorTCP.getEstacoes().size() > 0) {
            System.out.println("--> LISTANDO ESTACOES E CLIENTES...");
            System.out.println("--> Total de " + getEstacoes().size() + " Estacoe(s)!");
            for (int i = 0; i < getEstacoes().size(); i++) {
                System.out.println("    (" + i + ") " + getEstacoes().get(i).getNomeDaMusica());
                ServidorTCP.getEstacoes().get(i).listarOuvintes();
            }
        } else {
            System.out.println("Total de 0 Estacoe(s)");
        }
    }

    public int estaConectado(int portaUDP) {
        for (int i = 0; i < getEstacoes().size(); i++) {
            if (ServidorTCP.getEstacoes().get(i).getOuvintesPortaUDP().contains(portaUDP)) {
                return i;
            }
        }
        return -1;
    }

    public static void desativarEstacoes() {
        System.out.println("--> DESATIVANDO TODAS AS ESTACOES");
        for (Estacao estacao : getEstacoes()) {
            System.out.println(estacao.getNomeDaMusica() + ", desativada!");
        }
        getEstacoes().clear();
    }

    public void removerCliente() {
        try {
            int estacaoAtual = estaConectado(getPortaUDP());
            if (estacaoAtual != -1) {
                ServidorTCP.getEstacoes().get(estacaoAtual).remOuvinte(getPortaUDP());
            }
            getConexao().close();
            getThreadPrincipal().interrupt();
        } catch (IOException e) {
            System.out.println("Erro ServidorTCP: " + e.getMessage());
            System.out.println("Descricao: Erro ao remover cliente (" + getPortaUDP() + ")");
        }
    }

    public static void menu() {
        new Thread() {
            @Override
            public void run() {
                System.out.println();
                System.out.println("|------PRESSIONE A QUALQUER MOMENTO-----|");
                System.out.println("| P + Enter: Listar Estacoes e Clientes |");
                System.out.println("| Q + Enter: Encerrar Conexoes e Fechar |");
                System.out.println("|---------------------------------------|");
                do {
                    Scanner scanner = new Scanner(System.in);
                    opc = scanner.next();
                    //					scanner.close();
                    if (opc.charAt(0) == 'P' || opc.charAt(0) == 'p') {
                        ServidorTCP.listarEstacoes();
                    } else if (opc.charAt(0) == 'Q' || opc.charAt(0) == 'q') {
                        ServidorTCP.desativarEstacoes();
                        //ServidorTCP.desativarClientes();
                        //ServidorTCP.encerrarServidorTCP();
                        System.exit(0);
                    }
                    System.out.println();
                } while (opc.charAt(0) != 'q' || opc.charAt(0) != 'Q');
            }
        }.start();
    }

    @Override
    public void run() {
        try {
            System.out.println("\n---> NOVA CONEXAO ESTABELECIDA <---");
            while (true) {

                byte[] dadosRecebidos = new byte[128];
                new DataInputStream(getConexao().getInputStream()).read(dadosRecebidos);

                if (dadosRecebidos[0] == 0) {

                    setPortaUDP(getMsgServidor().helloRecebido(dadosRecebidos));
                    System.out.println("\nCliente --> Hello (" + getPortaUDP() + ") !");
                    getMsgServidor().welcome(new DataOutputStream(getConexao().getOutputStream()), 0, getQntdEstacoes());

                } else if (dadosRecebidos[0] == 1) {

                    int estEscolhida = getMsgServidor().setStationRecebido(dadosRecebidos);
                    System.out.println("\nCliente " + getPortaUDP() + " --> SetStation " + estEscolhida);

                    if (estEscolhida >= 0 && estEscolhida < getQntdEstacoes()) {
                        int estacaoAtual = this.estaConectado(getPortaUDP());

                        if (estacaoAtual != -1) {

                            ServidorTCP.getEstacoes().get(estacaoAtual).remOuvinte(getPortaUDP());
                            ServidorTCP.getEstacoes().get(estEscolhida).addOuvinte(getPortaUDP());

                            String songName = getEstacoes().get(estEscolhida).getNomeDaMusica();
                            int songNameSize = songName.length();
                            getMsgServidor().announce(new DataOutputStream(getConexao().getOutputStream()), 1, songNameSize, songName);

                        } else {

                            ServidorTCP.getEstacoes().get(estEscolhida).addOuvinte(getPortaUDP());

                            String songName = getEstacoes().get(estEscolhida).getNomeDaMusica();
                            int songNameSize = songName.length();
                            getMsgServidor().announce(new DataOutputStream(getConexao().getOutputStream()), 1, songNameSize, songName);
                        }
                    } else {

                        System.out.println("\nCliente " + getPortaUDP() + " --> Tentativa de comando invalido! CLIENTE DESCONECTADO!");
                        String msgErro = "Estacao " + estEscolhida + " nao existe!";
                        getMsgServidor().invalidCommand(new DataOutputStream(getConexao().getOutputStream()), 2, msgErro.length(), msgErro);
                        throw new ArrayIndexOutOfBoundsException();
                    }
                } else {

                    System.out.println("\nCliente " + getPortaUDP() + " --> Tentativa de comando invalido! CLIENTE DESCONECTADO!");
                    String msgErro = "Comando invalido";
                    getMsgServidor().invalidCommand(new DataOutputStream(getConexao().getOutputStream()), 2, msgErro.length(), msgErro);
                    throw new Exception();
                }
            }
        } catch (SocketException e) {
            System.err.println("\nErro ServidorTCP: " + e.getMessage());
            System.out.println("Descricao: Conexao com cliente perdida");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("\nErro ServidorTCP: " + e.getMessage());
            System.out.println("Descricao: Tentativa de acesso a estacao nao existente");
        } catch (IOException e) {
            System.err.println("\nErro ServidorTCP: " + e.getMessage());
            System.out.println("Descricao: Erro ao receber em pacote ou\n"
                    + "Erro ao enviar mensagem ou em converter mensagem");
        } catch (Exception e) {
            System.err.println("\nErro ServidorTCP: " + e.getMessage());
            System.out.println("Descricao: Comando invalido ou\n"
                    + "Erro desconhecido");
        } finally {
            System.out.println("CLIENTE " + getPortaUDP() + " REMOVIDO!");
            removerCliente();
        }
    }

    public static void main(String[] args) throws SocketException, FileNotFoundException, IOException {

        try {
            ServidorTCP.setClientes(new Vector<ServidorTCP>());
            ServidorTCP.setEstacoes(new Vector<Estacao>());
            
            /* CAMINHO PARA O ARQUIVO.MP3 */
            Estacao e1 = new Estacao("/home/wilker/Música/Haitam/Haitam-Guajira" + ".mp3");
            Estacao e2 = new Estacao("/home/wilker/Música/Hungria/Hungria-PrimeiroMilhao" + ".mp3");
          
            /* ADICIONAR ESTACAO A LISTA DO SERVIDOR TCP */
            ServidorTCP.getEstacoes().add(e1);
            ServidorTCP.getEstacoes().add(e2);

            /* ATUALIZA QUANTIDADE DE ESTAÇÕES */
            ServidorTCP.setQntdEstacoes(getEstacoes().size());

            /* SOCKET PARA ESCUTAR CONEXOES */
            ServerSocket server = new ServerSocket(PORTA_CONEXAO);
            System.out.println("ServidorTCP: " + PORTA_CONEXAO);

            ServidorTCP.menu();
            ServidorTCP.setServer(server);

            while (true) {
                Socket conexao = server.accept(); // ACEITA AS CONEXOES
                ServidorTCP servidor = new ServidorTCP(conexao);
                ServidorTCP.getClientes().add(servidor);
                Thread thread = servidor;
                servidor.setThreadPrincipal(thread);
                thread.start();
            }

        } catch (NullPointerException e) {
            System.err.println("Erro ServidorTCP: " + e.getMessage());
            System.out.println("Descricao: ");
        } catch (SocketException e) {
            System.err.println("Erro ServidorTCP: " + e.getMessage());
            System.out.println("Descricao: Erro ao criar Socket(UDP/TCP)");
        } catch (FileNotFoundException e) {
            System.err.println("Erro ClienteTCP: " + e.getMessage());
            System.out.println("Descricao: Caminho da musica invalido");
        } catch (IOException e) {
            System.err.println("Erro ClienteTCP: " + e.getMessage());
            System.out.println("Descricao: Erro ao criar ServidorTCP");
        } finally {
            server.close();
        }
    }
}
