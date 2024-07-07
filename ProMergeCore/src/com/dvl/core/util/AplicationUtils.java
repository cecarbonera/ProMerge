package com.dvl.core.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.apache.log4j.Logger;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class AplicationUtils {

	private static Logger log = Logger.getLogger(AplicationUtils.class);

	private static XStream xStream = new XStream(new DomDriver("UTF-8")) {
		protected MapperWrapper wrapMapper(MapperWrapper next) {
			return new MapperWrapper(next) {
				public boolean shouldSerializeMember(Class definedIn, String fieldName) {

					if (definedIn == Object.class) {
						return false;
					}

					try {
						return super.shouldSerializeMember(definedIn, fieldName);

					} catch (Exception e) {
						e.printStackTrace();
					}

					return false;
				}
			};
		}
	};

	public static XStream getXStream() {
		return xStream;
	}

	public static void setXStream(XStream xStream) {
		AplicationUtils.xStream = xStream;
	}

	/**
	 * Método responsável por enviar mensagem ao servidor
	 * 
	 * @param obj
	 */
	public static String enviarMensagemServidor(String serverAdress, int porta, Object objeto) {

		Socket socket = null;
		InputStream input = null;
		OutputStream output = null;

		try {

			while (true) {

				try {
					// Conecta com o servidor que foi criado la no plugin
					socket = new Socket(serverAdress, porta);
					break;
				} catch (Exception e) {
					log.error("Erro ao conectar no socket do servidor [" + serverAdress + ":" + porta + "] + " + e.getMessage().toString());
					Thread.sleep(2000);
				}
			}
			// Original
			output = socket.getOutputStream();
			input = socket.getInputStream();

			String xml = AplicationUtils.getXStream().toXML(objeto);
			log.info("XML ENVIADO VIA HOOK: " + xml);

			// Aqui que ta matando o SocketServer(9192)
			byte[] arrBytes = xml.getBytes(StandardCharsets.UTF_8);
			output.write(arrBytes);
			output.flush();

			while (true) {

				int available = input.available();
				if (available == 0) {
					Thread.sleep(500);
					continue;
				}

				byte[] readBuffer = new byte[available];

				input.read(readBuffer);
				String answer = new String(readBuffer);

				log.info("Resposta: " + answer);

				return answer;
			} 

		} catch (Exception e) {
			log.error("Erro ao enviar mensagem ao servidor.", e);
		} finally {

			if (output != null) {
				try {
					output.close();
				} catch (Exception e) {
					log.error("Erro ao fechar outputstream.", e);
				}
			}

			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					log.error("Erro ao fechar socket.", e);
				}
			}

			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					log.error("Erro ao fechar socket.", e);
				}
			}
		}

		return null;
	}

	public static String getDescricaoSeveridade(Integer grau) {

		String retorno = null;

		switch (grau) {
		case 1:
			retorno = "Lower";
			break;
		case 2:
			retorno = "Medium";
			break;
		case 3:
			retorno = "High";
			break;
		default:
			retorno = "Serious";
			break;
		}

		return retorno;
	}
}