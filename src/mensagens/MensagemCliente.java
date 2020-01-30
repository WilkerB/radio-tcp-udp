package mensagens;

import java.io.DataOutputStream;
import java.io.IOException;

public class MensagemCliente {
	
	/* ENVIA MENSAGEM HELLO SEGUIDO DA PORTA UDP 
	 * PARA RECEBER A MUSICA POSTERIORMENTE */
	public void hello(DataOutputStream saida, int commandType, int portaUDP) throws IOException {
		String porta = Integer.toString(portaUDP);
		byte[] msg = new byte[porta.length()+1];
		msg[0] = (byte) commandType;
		for(int i = 0; i < porta.length(); i++) {
			msg[i+1] = (byte) porta.charAt(i);
		}
		saida.write(msg);
	}
	
	/* SETAR ESTACAO APOS A LISTA 
	 * COM AS DISPONIVEIS SEREM ENVIADAS */
	public void setStation(DataOutputStream saida, int commandType, int numStation) throws IOException {
		byte[] msg = new byte[2];
		msg[0] = (byte) commandType;
		msg[1] = (byte) numStation;
		saida.write(msg);
	}
	
	/* CONVERTE A MENSAGEM E RETORNA A QUANTIDADE DE ESTACOES DISPONIVEIS */
	public int welcomeRecebido(byte[] msg) {
		return msg[1];
	}
	
	/* CONVERTE A MENSAGEM ANNOUCE E RETORNA O NOME DA MUSICA QUE ESTA SENDO TOCADA */
	public String announceRecebido(byte[] msg) {
		String songName = "";
		int songNameSize = (char) msg[1];
		for(int i = 2; i < songNameSize+2; i++) {
			songName += (char) msg[i];
		}
		return songName;
	}
	
	public String invalidCommandRecebido(byte[] msg) {
		String erro = "";
		int sizeErro = (char) msg[1];
		for(int i = 2; i < sizeErro+2; i++) {
			erro += (char) msg[i];
		}
		return erro;
	}
}
