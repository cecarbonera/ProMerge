package com.dvl.promerge.hook;

import java.util.List;
import org.apache.log4j.Logger;
import com.dvl.core.util.AplicationUtils;
import com.dvl.core.vos.ArquivoComitadoVO;
import com.dvl.promerge.hook.util.HistoryUtil;

/**
 * Classe responsável por enviar arquivos comitados para servidor ProMerge, executada na operação pos-commit do SVN
 * 
 * @author daniel.armino
 */
public class ProMergeHook {

	private static Logger log = Logger.getLogger(ProMergeHook.class);

	public static void main(String[] args) throws Exception {
	
		// Caso não receba nenhum parâmetro não executa
		if (args == null || args.length == 0) {
			log.info("Nenhum Parâmetro.");
		}

		// String repositorio = args[0];
		final String revisao = args[0];

		log.info("Parametro 1." + revisao);

		// 1) Buscar a lista de arquivos referente a revisão (primeiro parametro)
		final List<ArquivoComitadoVO> arquivosDaRevisao = HistoryUtil.getFilesFromRevision(Long.parseLong(revisao), Long.parseLong(revisao));

		// Caso não exista arquivos tipo texto no commit efetuado
		if (arquivosDaRevisao == null || arquivosDaRevisao.isEmpty()) {
			return;
		}

		for (ArquivoComitadoVO arquivoComitadoVO : arquivosDaRevisao) {

			String nomeCompletoArquivo = arquivoComitadoVO.getNomeCompletoArquivo();

			nomeCompletoArquivo = nomeCompletoArquivo.replaceAll("/trunk/", "");

			arquivoComitadoVO.setNomeCompletoArquivo(nomeCompletoArquivo);

			log.info("Arquivo: " + nomeCompletoArquivo);
			log.info("Usuário: " + arquivoComitadoVO.getNomeUsuario());
			log.info("Conteúdo: " + arquivoComitadoVO.getConteudoArquivo());
			log.info("Revisão: " + arquivoComitadoVO.getRevisao());
		}

		Thread a = new Thread() {

			public void run() {

				// Envia mensagem para o servidor, no caso o servidor ProMerge deve estar na mesma máquina do repositório SVN
				//String retorno = AplicationUtils.enviarMensagemServidor("localhost", 9090, arquivosDaRevisao);
				String retorno = AplicationUtils.enviarMensagemServidor("192.168.0.156", 9090, arquivosDaRevisao);
				log.info("Retorno commit: " + retorno);

			}
		};
		a.start();
	}
}
