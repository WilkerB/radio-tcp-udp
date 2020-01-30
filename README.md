# radio-tcp-udp

Troca de mensagens via TCP e envio de arquivos .mp3 via UDP. Estação e Cliente possuem uma porta para cada protocolo. Cada Estação particiona sua música e envia parte a parte para os Clientes conectados, que por sua vez reproduzem conforme vão recebendo, para a reprodução foi usada a biblioteca JLayer.
