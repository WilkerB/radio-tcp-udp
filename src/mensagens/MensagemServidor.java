package mensagens;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MensagemServidor {

	/* A MENSAGEM WELCOME DEVE POSSUIR A SEGUINTE CONFIGURAÇÃO:
	 * REPLYTYPLE = 0
	 * ARRAY DE BYTES B DE TAMANHO 3
	 * B[1] = REPLYTYPE 
	 * RESTANTE DO ARRAY CONTEM A QUANTIDADE DE ESTACOES*/
	public void welcome(DataOutputStream saida, int replyType, int qntdEstacoes) throws IOException{
		byte[] msg = new byte[2];
		msg[0] = (byte) replyType;
		msg[1] = (byte) qntdEstacoes;
		saida.write(msg);
	}

	/* INFORMAR A MUSICA QUE ESTA SENDO TOCADA 
	 * NA ESTACAO QUE O CLIENTE ESCOLHEU */
	public void announce(DataOutputStream saida, int replyType, int songNameSize, String nomeDaMusica) throws IOException {
		/* PRIMEIRO BYTE PARA REPLYTYPE
		 * SEGUNDO PARA SONGNAMESIZE
		 * RESTANTE UM PARA CADA CARACTERE DO NOME */
		byte[] msg = new byte[songNameSize + 2];
		msg[0] = (byte) replyType;
		msg[1] = (byte) songNameSize;

		for(int i = 0; i < songNameSize; i++) {
			//System.out.println("Letra: " + nomeDaMusica.charAt(i));
			msg[i+2] = (byte) nomeDaMusica.charAt(i);
		}

		saida.write(msg);
	}

	/* ENVIA UMA MENSAGEM INFORMANDO QUE
	 * O COMANDO NAO É VÁLIDO E ENCERRA A CONEXAO
	 * COM ESTE CLIENTE */
	public void invalidCommand(DataOutputStream saida, int replyType, int sizeErro, String erro) throws IOException {
		byte[] msg = new byte[sizeErro+2];
		msg[0] = (byte) replyType;
		msg[1] = (byte) sizeErro;

		for(int i = 0; i < sizeErro; i++) {
			msg[i+2] = (byte) erro.charAt(i);
		}

		saida.write(msg);
	}

	/* VAI RECEBER A MENSAGEM E RETORNAR A PORTAUDP */
	public int helloRecebido(byte[] msg) throws UnsupportedEncodingException {
		String porta = "";
		for(int i = 0; i < msg.length; i++) {
			if(msg[i] != 0) {
				porta += (char) msg[i];
				//System.out.println((char) msg[i]);
			}
		}
		return Integer.parseInt(porta);
	}

	/* VAI RECEBER A MENSAGEM E RETORNAR A ESTACAO ESCOLHIDA */
	public int setStationRecebido(byte[] msg) {
		return msg[1];
	}
}