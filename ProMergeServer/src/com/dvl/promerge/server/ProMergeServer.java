package com.dvl.promerge.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import com.dvl.core.dao.DaoGenerico;
import com.dvl.core.entitys.ProMergeAvaliacaoComplexidade;
import com.dvl.core.entitys.ProMergeHistoricoAlteracoes;
import com.dvl.core.entitys.ProMergeHistoricoCommits;
import com.dvl.core.entitys.ProMergeHistoricoMetodos;
import com.dvl.core.util.AntExecutor;
import com.dvl.core.util.AplicationUtils;
import com.dvl.core.util.CheckoutUpdateUtils;
import com.dvl.core.util.ConflictedMergeUtil;
import com.dvl.core.util.DiffParser;
import com.dvl.core.util.DiffWCToRepository;
import com.dvl.core.util.GetFileSVN;
import com.dvl.core.util.SVNUtility;
import com.dvl.core.vos.ArquivoComitadoVO;
import com.dvl.core.vos.ArquivoVO;
import com.dvl.core.vos.IntegracaoVO;
import com.dvl.core.vos.MetodosVO;
import com.dvl.core.vos.SobrescritaVisualizacaoVO;

public class ProMergeServer {

	private static Logger log = Logger.getLogger(ProMergeServer.class);
	private static String DIR_WORKSPACE_SERVIDOR = "C:\\workspaces\\wrk-experimento-mestrado";
	private static String DIR_TEMP_INTEGRACOES = "C:\\workspaces\\wrk-experimento-mestrado-auxiliar";
	private static String urlSVN = "http://localhost:8443/svn/mestrado";
	private static File dirProjeto = new File(DIR_WORKSPACE_SERVIDOR);

	// Configuração do Ambiente (Aqui mudar para o caminho do Experimento
	private static String DIR_FONTES = "\\trunk\\prjExperimento01\\src\\PrimeiroExperimento";

	// Variáveis de processamento
	private static String _buildStarted = "Build Started...";
	private static String _buildFinished = "Build Finished...";
	private static String _buildSucessful = "BUILD SUCCESSFUL";
	private static int _qtdPontosDefault = 50;

	// Sem o construtor
	private static String _regex = "((public|private|protected|static|final|native|synchronized|abstract|threadsafe|transient)+\\s)+[\\$_\\w\\<\\>\\[\\]]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?";
	private static String _regexConstructor = "((public|private|protected|static|final|native|synchronized|abstract|threadsafe|transient)+\\s)+[a-zA-Z0-9_]+\\(";

	public static void main(String[] args) {

		ServerSocket listener = null;
		long latestRevision = 0;

		try {
			// verificar se pasta do workspace já está criada
			File dirProjetoTemp = new File(DIR_WORKSPACE_SERVIDOR + "\\trunk");
			if (!dirProjetoTemp.exists()) {
				// Caso negativo cria as pastas
				dirProjeto.mkdirs();

				// Efetua checkout (Pega tudo do servidor)
				latestRevision = CheckoutUpdateUtils.doCheckoutUpdate(urlSVN, dirProjeto, true, "Carlos.Carbonera");

			}

			/*
			 * **************** CUIDADO AQUI *******************************
			 * **************** CUIDADO AQUI *******************************
			 * **************** CUIDADO AQUI *******************************
			 * **************** CUIDADO AQUI *******************************
			 * 
			 * SEMPRE QUE FOR CRIADO UM NOVO PROJETO NO SERVIDOR SVN APAGAR A
			 * PASTA ".SVN" E A PASTA ".METADATA" DO DIRETÓRIO AONDE ESTÁ O
			 * WORKSPACE NA MAQUINA E FORCAR A FAZER UM CHECKOUT SENAO NAO
			 * ATUALIZA
			 * 
			 * **************** CUIDADO AQUI *******************************
			 * **************** CUIDADO AQUI *******************************
			 * **************** CUIDADO AQUI *******************************
			 * **************** CUIDADO AQUI *******************************
			 */
			// Efetua update dos arquivos no diretório do workspace do usuário
			// (Busca apenas os arquivos alterados)
			latestRevision = CheckoutUpdateUtils.doCheckoutUpdate(urlSVN, dirProjeto, false, "Carlos.Carbonera");

			// Calcular a complexidade Funcionalidade
			log.info("Atualização da complexidade dos métodos...");
			calcularComplexidade();

			// Carregar os métodos das classes (Carga inicial)
			log.info("Atualização dos históricos dos métodos...");
			cargaInicialMetodosClasses(false);

			log.info("Atualizações concluídas.");

			// Conexão com a porta 9090
			listener = new ServerSocket(9090);

			while (true) {

				Socket socket = listener.accept();

				InputStream input = null;
				OutputStream output = null;

				try {
					output = socket.getOutputStream();
					input = socket.getInputStream();

					while (true) {

						int available = input.available();

						if (available == 0) {
							Thread.sleep(500);
							continue;
						}

						String _historicoCompilacao = _buildSucessful, _result = "", _usuario = "", _trunk = File.separator + "trunk"
								+ File.separator;
						int _revisao = 0;
						boolean _commitado = false;

						byte[] readBuffer = new byte[available];
						input.read(readBuffer);

						// Lista os arquivos existentes na Revisão (Que foi
						// cmiitado no servido do SVN)
						String texto = new String(readBuffer);
						Object objeto = AplicationUtils.getXStream().fromXML(texto);

						// IntegracaoVO - Quando um arquivo é enviado do
						// Workspace ao Servidor SVN
						if (objeto instanceof IntegracaoVO) {

							// 1) IntegracaoVO
							IntegracaoVO vo = (IntegracaoVO) objeto;

							// Setar o nome do usuário do conflito
							_usuario = vo.getConflito().getDesUsuario();

							// 1.1) Busca o ip do cliente
							String ipCliente = DaoGenerico.getInstance().buscarIpCliente(vo.getConflito().getDesUsuario());

							// 1.2) Servidor irá solicitar arquivo ao client
							String arquivoOutroWorkspace = AplicationUtils.enviarMensagemServidor(ipCliente, 9191, vo.getNomeCompletoArquivo());

							// 1.3) As duas versões deverão ser integradas em
							// uma pasta temporária
							Map<String, String> arquivoIntegrado = ConflictedMergeUtil
									.integrarArquivos(new File(DIR_TEMP_INTEGRACOES), vo.getNomeUsuarioRequisicao(), vo.getConteudoArquivo(),
											vo.getNomeCompletoArquivo(), arquivoOutroWorkspace, false);

							// 1.4) Envia conteúdo para o arquivo
							output.write(arquivoIntegrado.get("arquivo").getBytes(StandardCharsets.UTF_8));

							break;

						}
						// Arquivo é sobrescrito no workspace com a versão
						// existente no Servidor SVN
						else if (objeto instanceof SobrescritaVisualizacaoVO) {

							// 2) SobrescritaVisualizacaoVO
							SobrescritaVisualizacaoVO vo = (SobrescritaVisualizacaoVO) objeto;

							// Setar o nome do usuário do conflito
							_usuario = vo.getConflito().getDesUsuario();

							// 2.1) Busca ip do cliente
							String ipCliente = DaoGenerico.getInstance().buscarIpCliente(vo.getConflito().getDesUsuario());

							// 2.2) Servidor irá solicitar arquivo ao client
							String arquivoOutroWorkspace = AplicationUtils.enviarMensagemServidor(ipCliente, 9191, vo.getNomeCompletoArquivo());

							// 2.3) Envia conteúdo para o arquivo
							output.write(arquivoOutroWorkspace.getBytes(StandardCharsets.UTF_8));

							break;

						}
						// Demais Casos (Commit em arquivos alterados no
						// workspace, ...)
						else {
							Object aux = ((List) objeto).get(0);

							// Quando um arquivo é adicionado
							if (aux instanceof ArquivoVO) {

								// Lista de Arquivos alterados
								List<ArquivoVO> lista = (List<ArquivoVO>) objeto;

								// Percorrer a lista de arquivos alterados
								for (ArquivoVO arquivo : lista) {
									// 1) Salva o registro na tabela
									ProMergeHistoricoAlteracoes vo = new ProMergeHistoricoAlteracoes();

									// Setar as propriedades
									vo.setDesArquivo(_trunk
											+ arquivo.getNomeCompletoArquivo().replaceAll("/", Matcher.quoteReplacement(File.separator)));
									vo.setDesUsuario(arquivo.getNomeUsuario());
									vo.setTxtArquivo(arquivo.getConteudoArquivo());

									// Setar as propriedades
									_usuario = arquivo.getNomeUsuario();
									_revisao = (int) latestRevision;

									// Setar o retorno do método
									_result = efetuaProcessoPrincipal(vo, _revisao, _commitado, _usuario);

								}

								// Se deu erro de compilação
								if (_result != "")
									_historicoCompilacao = _result;

							}
							// Quando um arquivo for alterado e está sendo
							// comitado a partir do workspace do usuário
							else if (aux instanceof ArquivoComitadoVO) {

								// Lista de Arquivos alterados
								List<ArquivoComitadoVO> lista = (List<ArquivoComitadoVO>) objeto;

								// Commitou o Arquuivo - Grava histórico da
								// compilação
								_commitado = true;

								// Percorrer a lista de arquivos alterados (que
								// foram comitados
								for (ArquivoComitadoVO arquivo : lista) {
									// 1) Salva o registro na tabela
									ProMergeHistoricoAlteracoes vo = new ProMergeHistoricoAlteracoes();

									// Setar as propriedades
									vo.setDesArquivo(_trunk
											+ arquivo.getNomeCompletoArquivo().replaceAll("/", Matcher.quoteReplacement(File.separator)));
									vo.setDesUsuario(arquivo.getNomeUsuario());
									vo.setTxtArquivo(arquivo.getConteudoArquivo());

									// Setar o nome do usuário do conflito
									_usuario = arquivo.getNomeUsuario();
									_revisao = Integer.valueOf(arquivo.getRevisao());

									// Efetua o processamento principal
									_result = efetuaProcessoPrincipal(vo, _revisao, _commitado, _usuario);

								}

								// Se deu erro de compilação
								if (_result.trim() != "")
									_historicoCompilacao = _result;

							}

						}

						// Se commitou o arquivo - Grava histórico do
						// processamento
						if (_commitado) {
							// Gravar o Log do histórico de compilação (Geral)
							ProMergeHistoricoCommits objVo = new ProMergeHistoricoCommits();

							objVo.setRevisao(_revisao);
							objVo.setUsuario(_usuario);
							objVo.setSeq(DaoGenerico.getInstance().buscarSequenciaCommitsPorPK(_revisao, _usuario));
							objVo.setStatus(_historicoCompilacao == _buildSucessful ? 0 : 1);
							objVo.setMensagem(_historicoCompilacao);

							DaoGenerico.getInstance().inserirHistoricoCommit(objVo);

						}

						log.info(new String(readBuffer));
						output.write("OK".getBytes(StandardCharsets.UTF_8));

						break;

					}

				} catch (Exception e) {
					log.error("Erro na Hierarquia - ", e);

				} finally {
					if (output != null) {
						try {
							output.close();
							log.info("Processamento concluído...");

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

			}

		} catch (Exception e) {
			log.error("Erro no servidor, é necessário reestartá-lo.", e);

		} finally {
			if (listener != null) {
				try {
					listener.close();

				} catch (Exception e) {
					log.error("Erro ao fechar socket.", e);

				}

			}

		}

	}

	/**
	 * 
	 * @param _cargaInicialMetodos
	 *            - True - Deverá realizar o Diff | False - Não deverá processar
	 *            o diff entre os arquivos
	 */
	private static void cargaInicialMetodosClasses(boolean _cargaInicialMetodos) {
		try {
			String _pathArquivos = DIR_WORKSPACE_SERVIDOR + DIR_FONTES;
			File f = new File(_pathArquivos);

			// Filtrar os arquivos com
			FilenameFilter filtro = new FilenameFilter() {
				@Override
				public boolean accept(File f, String name) {
					return name.endsWith(".java");

				}

			};

			File[] files = f.listFiles(filtro);
			ProMergeHistoricoAlteracoes objetoVO;

			for (int i = 0; i < files.length; i++) {
				// Inicializar o Objeyo
				objetoVO = new ProMergeHistoricoAlteracoes();

				// Ler o conteúdo do arquivo e inicializar a variável
				String conteudoArquivo = FileUtils.readFileToString(files[i]);

				// Atribuir as propriedades
				objetoVO.setDesArquivo(DIR_FONTES + File.separator + files[i].getName());
				objetoVO.setDesUsuario("Sistemas");
				objetoVO.setTxtArquivo(conteudoArquivo);

				// Gravar registros (Versão que está no SERVIDOR) históricos
				// alterações dos métodos no Banco de dados
				objetoVO = DaoGenerico.getInstance().saveOrUpdateHistorico(objetoVO);

				// Gravar métodos também existentes nas classes (Carga Inicial)
				salvarMetodosAlterados(objetoVO, _cargaInicialMetodos, "Carlos.Carbonera");

			}

		} catch (Exception e) {
			log.error("Erro ao executar metodo cargaInicialMetodosClasses.", e);

		}

	}

	private static void excluirResultadoCompilacao() {
		// Excluir o arquivo como resultado da compilação
		File file = new File("Compile.ERR");

		if (file.exists()) {
			file.delete();

		}

	}

	private static void calcularComplexidade() throws IOException {
		try {
			String _pathArquivos = DIR_WORKSPACE_SERVIDOR + DIR_FONTES;
			File f = new File(_pathArquivos);

			// Filtrar os arquivos com
			FilenameFilter filtro = new FilenameFilter() {
				@Override
				public boolean accept(File f, String name) {
					return name.endsWith(".java");
				}
			};

			// Filtrar os arquivos com extensão ".java"
			File[] files = f.listFiles(filtro);
			List<ProMergeAvaliacaoComplexidade> listComplexidade = new ArrayList<ProMergeAvaliacaoComplexidade>();

			// 1° PASSO - Percorrer todos os arquivos do projeto e EXTRAIR todos
			// os métodos das classes (Exceto os métodos construtores)
			for (int i = 0; i < files.length; i++) {
				// Definir o caminho do arquivo
				String _nomeArquivo = _pathArquivos + File.separator + files[i].getName();

				// Ler o conteúdo do arquivo e inicializar a variável
				String conteudoArquivo = FileUtils.readFileToString(new File(_nomeArquivo));
				ProMergeHistoricoAlteracoes vo = null;
				ProMergeAvaliacaoComplexidade voComplexidade = null;

				// Se não for vazio
				if (!conteudoArquivo.isEmpty()) {
					// Inicializar o objeto
					vo = new ProMergeHistoricoAlteracoes();

					// Setar as propriedades
					vo.setDesArquivo(DIR_FONTES + File.separator + files[i].getName());

					// Encontrar os métodos existentes nas classes (Exceto o
					// método construtor da classe)
					List<MetodosVO> lstMetodosArquivo = buscarMetodosJavaArquivo(vo, conteudoArquivo);

					// Se encontrou os métodos
					if (lstMetodosArquivo != null) {
						for (MetodosVO metodosVO : lstMetodosArquivo) {
							// Inicialização do objeto
							voComplexidade = new ProMergeAvaliacaoComplexidade();

							// Setar as propriedades no objeto
							voComplexidade.setClasse(metodosVO.getNomeClasse());
							voComplexidade.setMetodo(metodosVO.getNomeMetodo());
							voComplexidade.setSeveridade(0);
							voComplexidade.setQtdOcorrencias(0);
							voComplexidade.setQtdPontos(0);

							// Adicionar a lista
							listComplexidade.add(voComplexidade);

						}

					}

				}

			}

			// 2° PASSO - Avaliar a quantidade de chamadas dos métodos
			// (encontradas nas classes)
			//
			// *** IMPORTANTE:
			// O método CONSTRUTOR da Classe SERÁ CONSIDERADO NA QUANTIDADE DE
			// Métodos existentes na classe
			//
			for (int i = 0; i < files.length; i++) {
				// Ler o conteúdo do arquivo e inicializar a variável
				String conteudoArquivo = FileUtils.readFileToString(new File(_pathArquivos + File.separator + files[i].getName()));

				// Se não for vazio o arquivo
				if (!conteudoArquivo.isEmpty()) {
					// Se a lista de métodos não for vazia (Contituida acima)
					if (listComplexidade != null) {
						int _qtdOcorrencias = 0, _posInicial = 0;

						// Percorrer TODOS os métodos encontrados
						// (listComplexidade) p/ calcular a complexidade
						for (ProMergeAvaliacaoComplexidade _complexidade : listComplexidade) {
							// Inicializar as variáveis
							_qtdOcorrencias = 0;
							_posInicial = 0;

							// Pesquisa se realizou chamada(s) do método
							// (incluiído o contrutor)
							Pattern pattern = Pattern.compile("[\\s\\.]" + _complexidade.getMetodo() + "+\\(", Pattern.CASE_INSENSITIVE);
							Matcher match = pattern.matcher(conteudoArquivo);

							// Se encontrou o nome da classe
							Pattern patternClasse = Pattern.compile(buscarNomeClasse(_complexidade.getClasse()) + "+\\(", Pattern.CASE_INSENSITIVE);
							Matcher matchClasse = patternClasse.matcher(conteudoArquivo);

							boolean bfindclassName = matchClasse.find();

							// Verifica a quantidade de ocorrências (Pelo nome
							// do método)e que deve pertencer a MESMA CLASSE)
							//
							// Observação: Duas classes diferentes PODEM conter
							// métodos com o mesmo NOME
							//
							while (match.find(_posInicial)) {
								// Encontrou referência a classe
								if (bfindclassName) {
									// Atualiza a quantidade
									_qtdOcorrencias++;

								}
								// Atualizar a posição
								_posInicial = match.start() + 1;

							}

							// Se encontrou ocorrências dos métodos nas classes
							// (mais vezes que a própria declaração)
							if (_qtdOcorrencias > 0) {
								// Adicionar a quantidade de ocorrências
								// encontradas em outros arquivos
								_qtdOcorrencias += _complexidade.getQtdOcorrencias();

								// Quantidade de pontos (Calulcado)
								double _qtdPontos = _qtdPontosDefault * _qtdOcorrencias;

								// Definido como complexidade baixa
								_complexidade.setQtdOcorrencias(_qtdOcorrencias);
								_complexidade.setQtdPontos(_qtdPontos);

							}

						}

					}

				}

			}

			// 3° PASSO - Atualizar a tabela de complexidade do método
			DaoGenerico.getInstance().saveOrUpdateMetodosComplexidade(listComplexidade);

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	/**
	 * Efetua o processo principal - Integração dos fontes no Servidor SVN
	 * 
	 * @param objetoVO
	 * @param revisao
	 */
	private static String efetuaProcessoPrincipal(ProMergeHistoricoAlteracoes objetoVO, int revisao, boolean _commitado, String usuario) {
		// Resultado do processamento
		String _resultadoCompilacao = "";

		try {
			// 2) Gravar registros (Versão que está no SERVIDOR) históricos
			// alterações dos métodos no Banco de dados
			objetoVO = DaoGenerico.getInstance().saveOrUpdateHistorico(objetoVO);

			// 3) Gravar métodos alterados
			salvarMetodosAlterados(objetoVO, true, usuario);

			// Calcular a complexidade Funcionalidade
			calcularComplexidade();

			// 4) Verificar se existem OUTROS WORKSPACES que estão alterando os
			// mesmos arquivos
			List<ProMergeHistoricoAlteracoes> lstOutrosArquivos = DaoGenerico.getInstance().buscarArquivoWorkspaces(objetoVO.getDesUsuario(),
					objetoVO.getDesArquivo(), false);

			// Se encontrou arquivos em outros Workspaces
			if (lstOutrosArquivos != null && !lstOutrosArquivos.isEmpty()) {

				// 5) Realiza a integração dos arquivos
				integrarArquivos(objetoVO, lstOutrosArquivos);

				// Itera sobre os arquivos para verificar se teve conflito
				for (ProMergeHistoricoAlteracoes promergeHistoricoAlteracoes : lstOutrosArquivos) {
					// 6) Caso conflitou
					if (promergeHistoricoAlteracoes.isConflitou()) {
						// 1o - Buscar os métodos do arquivo conflitante
						// List<ProMergeHistoricoMetodos> lstMetodosPorHistorico
						// =
						// DaoGenerico.getInstance().buscarHistoricoMetodosPorCodigo(promergeHistoricoAlteracoes.getCodHistorico());

						// 2o - Identificar os métodos Java do arquivo
						// ProMergeHistoricoAlteracoes voArquivo = new
						// ProMergeHistoricoAlteracoes();
						// voArquivo.setDesArquivo(DIR_FONTES + File.separator +
						// voArquivo.getDesArquivo());

						// Atribuição inicial do grau de severidade
						int iGrauSeveridade = retornarGrauSeveridade(objetoVO);

						// Setar a severidade como 1 - Low (pq ainda não foi
						// commitado (está na workspaces)
						promergeHistoricoAlteracoes.setSeveridade(iGrauSeveridade);

						// Salva status 1 - Conflito direto | 2 - Conflito
						// Indireto | 3 - Erro de Compilação
						DaoGenerico.getInstance().saveOrUpdateResumoConflitos(promergeHistoricoAlteracoes, 1, false);

						// Se conflitou (NÃO Compila) - Vai para o próximo
						// registro
						continue;

					}

					// Se não commitou os fontes
					if (!_commitado)
						// Processsar as ocorrência de conflitos indiretos
						processarOcorrenciasConflitosIndiretos(objetoVO);

					else {
						// 7) Caso não conflitou, atualizar workspace do
						// servidor com as alterações e compilar os projetos
						String _result = atualizarECompilarArquivo(promergeHistoricoAlteracoes, false);

						// 8) Caso erro de compilação (Grava o registro do
						// conflito)
						if (_result != _buildSucessful) {
							// Nome da class
							String _nomeClasse = buscarNomeClasse(objetoVO.getDesArquivo()).toLowerCase();

							// Se na mensagem de erro resultante CONTER o nome
							// da classe EFETUA O PROCESSAMENTO DO ERRO
							if (_result.toLowerCase().indexOf(_nomeClasse) > 0) {
								// Atribuir o histórico do erro
								_resultadoCompilacao = _result;

								// Processar a Gravação de conflitos
								processarOcorrenciasConflitos(promergeHistoricoAlteracoes, _resultadoCompilacao, false);

							}

						}

					}

				}

			}
			// Não encontrou arquivos em outros Workspaces (Outras Máquinas) com
			// os mesmos nomes de fontes alterados
			else {
				// Consultar se os métodos afetados já estão cadastrados na
				// tabela de históricos
				List<ProMergeHistoricoAlteracoes> lstArquivos = DaoGenerico.getInstance().buscarArquivoWorkspaces(objetoVO.getDesUsuario(),
						objetoVO.getDesArquivo(), true);

				// Se encontrou arquivos em outros Workspaces
				if (lstArquivos != null && !lstArquivos.isEmpty()) {
					// Itera sobre os arquivos para verificar se existem
					// conflitos
					for (ProMergeHistoricoAlteracoes promergeHistoricoAlteracoes : lstArquivos) {
						// Atualizar workspace do servidor com as alterações e
						// compilar os projetos
						String _result = atualizarECompilarArquivo(promergeHistoricoAlteracoes, true);

						// 8) Caso erro de compilação
						if (_result != _buildSucessful) {
							// Nome da Classe
							String _nomeClasse = buscarNomeClasse(objetoVO.getDesArquivo()).toLowerCase();

							// Se na mensagem de erro resultante CONTER o nome
							// da classe EFETUA O PROCESSAMENTO DO ERRO
							if (_result.toLowerCase().indexOf(_nomeClasse) > 0) {
								// Concatenar os histórios de erros das
								// compilações
								_resultadoCompilacao = _result;

								// Processar a Gravação de conflitos
								processarOcorrenciasConflitos(promergeHistoricoAlteracoes, _resultadoCompilacao, true);

							}

						}

					}

					// Se não commitou os fontes
					if (!_commitado)
						// Processsar as ocorrência de conflitos indiretos
						processarOcorrenciasConflitosIndiretos(objetoVO);

				}

			}

		} catch (Exception e) {
			log.error("Erro no processo principal.", e);

		}

		// Retorno da Função
		return _resultadoCompilacao;
	}

	/**
	 * 
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	private static void processarOcorrenciasConflitosIndiretos(ProMergeHistoricoAlteracoes vo) throws Exception {

		// 9) Caso não tiver erro de compilacao, verificar conflitos indiretos
		Map<Integer, ProMergeHistoricoAlteracoes> lstConflitosIndiretos = buscarConflitosIndiretos(vo);

		// 10) Caso não houver conflitos diretos retornar processo
		if (lstConflitosIndiretos == null || lstConflitosIndiretos.isEmpty()) {
			return;
		}

		// Atribuição inicial do grau de severidade
		int iGrauSeveridade = retornarGrauSeveridade(vo);

		// Itera sobre os conflitos indiretos
		for (Entry<Integer, ProMergeHistoricoAlteracoes> paccsHistoricoAlteracoes : lstConflitosIndiretos.entrySet()) {

			ProMergeHistoricoAlteracoes reg = DaoGenerico.getInstance().buscarHistoricoPorPK(paccsHistoricoAlteracoes.getKey());

			// Setar o grau de severidade do método alterado
			reg.setSeveridade(iGrauSeveridade);

			// Gravar/Atualizar o conflito:
			// 1 - Conflito Direto | 2 - Conflito Indireto | 3 - Erro
			DaoGenerico.getInstance().saveOrUpdateResumoConflitos(reg, 2, false);

		}

	}

	private static int retornarGrauSeveridade(ProMergeHistoricoAlteracoes vo) {
		int iGrauSeveridade = 1;

		try {
			// Identifcar os métodos e ranges do Arquivo em processamento
			List<MetodosVO> lstMetodosArquivo = buscarMetodosJavaArquivo(vo, vo.getTxtArquivo());

			// Buscar os históricos originais
			List<ProMergeHistoricoMetodos> lstMetodosPorHistorico = new ArrayList<ProMergeHistoricoMetodos>(0);
			int codhistoricoOrig = DaoGenerico.getInstance().buscarHistoricoOriginalClasse(buscarNomeClasse(vo.getDesArquivo()));

			if (codhistoricoOrig > 0)
				lstMetodosPorHistorico = DaoGenerico.getInstance().buscarHistoricoMetodosPorCodigo(codhistoricoOrig);

			// Percorrer os métodos encontrados (Originais)
			for (ProMergeHistoricoMetodos metodo : lstMetodosPorHistorico) {
				// Pegar o nome do método
				String _nomeMetodo = metodo.getDesMetodo();

				for (MetodosVO metodosVO : lstMetodosArquivo) {
					String nomeMetodo = _nomeMetodo.toLowerCase().trim();
					String nomeMetodo2 = metodosVO.getNomeMetodo().toLowerCase().trim();

					// Se for o mesmo método
					if (nomeMetodo.equalsIgnoreCase(nomeMetodo2)) {
						// A diferença será pelo número de linhas
						int nroLinhas1 = metodo.getNumLinhaFinal() - metodo.getNumLinhaInicial();
						int nroLinhas2 = metodosVO.getRangeFinal() - metodosVO.getRangeInicial();

						if (nroLinhas1 != nroLinhas2) {
							// Consultar o Grau de Severidade
							ProMergeAvaliacaoComplexidade _severidade = DaoGenerico.getInstance().buscarComplexidadePorPK(metodo.getNomeClasse(),
									metodo.getDesMetodo());

							if (_severidade != null)
								iGrauSeveridade = _severidade.getSeveridade();
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Erro ao executar buscarConflitosIndiretos.", e);

		}

		return iGrauSeveridade;

	}

	/**
	 * 
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * 
	 * 1o) A função recebe como parâmetro um objeto vo que é do tipo
	 * ProMergeHistoricoAlteracoes - que é uma classe que tem entre a
	 * propriedade List<MetodosVO> listaMetodos;. * 2o) A CLASSE "MetodosVO",
	 * ESTÁ DESCRITA ABAIXO:
	 * 
	 * private String nomeProjeto; private String nomeClasse; private String
	 * nomeMetodo; private Integer rangeInicial; private Integer rangeFinal;
	 * private List<MetodosVO> metodosInfluenciados;
	 * 
	 * NA PROPRIEDADE "nomeClasse" DEVERÁ RECEBER A ESTRUTURA DO PACOTE E A
	 * CLASSE, POIS EM PACOTES DIFERENTES EU POSSO TER A MESMA CLASSE: CONFORME
	 * DETALHADO ABAIXO:
	 * 
	 * NOME DO PACOTE + "." + NOME DA CLASSE SEM O JAVA (E.......
	 * CONFORMEDIGITADO - É CASE SENSITIVE, CUIDADO - É CASE SENSITIVE
	 * ..............*
	 * 
	 * POR EXEMPLO: PrimeiroExperimento.Funcionario PrimeiroExperimento.Gerennte
	 * ... SegundoExperimento.Paises SegundoExperimento.Cidades
	 * 
	 * 
	 * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ***** IMPORTANTE. DENTRO DE UM PACOTE
	 * PODERÁ EXISTIR OUTROS PACOTES E ESSA ORGANIZAÇÃO DEVERÁ SER RESPEITADA
	 * ********************* >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *****
	 * IMPORTANTE. DENTRO DE UM PACOTE PODERÁ EXISTIR OUTROS PACOTES E ESSA
	 * ORGANIZAÇÃO DEVERÁ SER RESPEITADA *********************
	 * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ***** IMPORTANTE. DENTRO DE UM PACOTE
	 * PODERÁ EXISTIR OUTROS PACOTES E ESSA ORGANIZAÇÃO DEVERÁ SER RESPEITADA
	 * *********************
	 * 
	 * 
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * IMPORTANTE *************** NAO APAGAR *************** IMPORTANTE
	 * ************** *********** IMPORTANTE *************** NAO APAGAR
	 * ****************** IMPORTANTE *************** NAO APAGAR ***************
	 * IMPORTANTE ************** *********** IMPORTANTE *************** NAO
	 * APAGAR ****************** IMPORTANTE *************** NAO APAGAR
	 * *************** IMPORTANTE ************** *********** IMPORTANTE
	 * *************** NAO APAGAR ****************** IMPORTANTE ***************
	 * NAO APAGAR *************** IMPORTANTE ************** ***********
	 * IMPORTANTE *************** NAO APAGAR ****************** IMPORTANTE
	 * *************** NAO APAGAR *************** IMPORTANTE **************
	 * *********** IMPORTANTE *************** NAO APAGAR ******************
	 * IMPORTANTE *************** NAO APAGAR *************** IMPORTANTE
	 * ************** *********** IMPORTANTE *************** NAO APAGAR
	 * ****************** IMPORTANTE *************** NAO APAGAR ***************
	 * IMPORTANTE **************
	 * 
	 */
	private static Map<Integer, ProMergeHistoricoAlteracoes> buscarConflitosIndiretos(ProMergeHistoricoAlteracoes vo) {
		try {
			// Pegar a lista atual
			List<MetodosVO> _listaTemp = vo.getListaMetodos();

			// Percorrer a lista
			for (MetodosVO _metodoVOTemp : _listaTemp) {
				// Pegar o nome da classe
				String[] _dadosProjeto = _metodoVOTemp.getNomeClasse().split("src");
				String _nomeProjeto = _dadosProjeto[0].replace("trunk", "").replace("src", "").replace(File.separator, "");
				String _nomeClasse = _dadosProjeto[1].replace(".java", "").replace(File.separator, ".").substring(1);

				// Setar o nome do projeto e o nome da classe SEGUINDO o modelo
				// (PACOTE(s) + "." + Nome da classe SEM O JAVA)
				_metodoVOTemp.setNomeProjeto(_nomeProjeto);
				_metodoVOTemp.setNomeClasse(_nomeClasse);

			}

			// Setar a lista ajustada
			vo.setListaMetodos(_listaTemp);

			// 2.6.1) Chamar socket do workspace do servidor
			String metodos = AplicationUtils.enviarMensagemServidor("192.168.0.156", 9192, vo.getListaMetodos()); //colocar o IP da rede
			//String metodos = AplicationUtils.enviarMensagemServidor("localhost", 9192, vo.getListaMetodos()); //colocar o IP da rede
			

			// 2.6.2) Este socket irá retornar os métodos que sofreão influência
			List<MetodosVO> listaMetodos = (List<MetodosVO>) AplicationUtils.getXStream().fromXML(metodos);
			if (listaMetodos == null || listaMetodos.isEmpty()) {
				return null;
			}

			Map<Integer, ProMergeHistoricoAlteracoes> listaRetorno = new HashMap<Integer, ProMergeHistoricoAlteracoes>(0);

			// Itera nos métodos e busca arquivos com métodos alterados
			for (MetodosVO metodosVO : listaMetodos) {
				// Se estiver vazio
				if (metodosVO.getMetodosInfluenciados() == null || metodosVO.getMetodosInfluenciados().isEmpty()) {
					continue;
				}

				// Busca os conflitos indiretos (Banco de Dados)
				List<ProMergeHistoricoAlteracoes> historico = DaoGenerico.getInstance().buscarConflitosIndiretos(vo, metodosVO);
				if (historico == null) {
					continue;
				}

				for (ProMergeHistoricoAlteracoes promergeHistoricoAlteracoes : historico) {
					listaRetorno.put(promergeHistoricoAlteracoes.getCodHistorico(), promergeHistoricoAlteracoes);

				}

			}

			// 2.6.3) Com os métodos retornados do socket, efetuar busca na
			// tabela para identificar usuários que estão alterando estes
			// métodos
			return listaRetorno;

		} catch (Exception e) {
			log.error("Erro ao executar buscarConflitosIndiretos.", e);

		}

		return null;

	}

	/**
	 * Identifica e salva os métodos alterados entre a versão do servidor HEAD e
	 * a do arquivo recebido por parâmetro
	 * 
	 * @param vo
	 */
	private static void salvarMetodosAlterados(ProMergeHistoricoAlteracoes vo, boolean _cargaInicialMetodos, String usuario) {

		try {
			// Se deve atualizar os fontes locais
			if (_cargaInicialMetodos)
				// 2.1.1) Sincronizar workspace local
				CheckoutUpdateUtils.doCheckoutUpdate(urlSVN, dirProjeto, false, usuario);

			// 2.1.2) Ler arquivo em questão
			File arquivo = new File(DIR_WORKSPACE_SERVIDOR + File.separator, vo.getDesArquivo());

			// 2.2) Definir range de linhas dos métodos neste arquivo
			String arquivoIntegrado = FileUtils.readFileToString(arquivo);

			// 2.3) Com isso é possível saber os métodos e seus ranges
			// List<MetodosVO> lstMetodosArquivo = buscarMetodosJavaArquivo(vo,
			// arquivoIntegrado);
			List<MetodosVO> lstMetodosArquivo = buscarMetodosJavaArquivo(vo, vo.getTxtArquivo());

			// 2.5.1) Identificar quais métodos foram alterados
			List<MetodosVO> lstMetodos = identificarMetodosAlterados(vo, lstMetodosArquivo, _cargaInicialMetodos);

			// Setar as propriedades
			vo.setListaMetodos(lstMetodos);

			// Cria a lista
			List<ProMergeHistoricoMetodos> listaRetorno = new ArrayList<ProMergeHistoricoMetodos>(0);
			Integer sequencial = 1;

			for (MetodosVO metodosVO : lstMetodosArquivo) {
				// Criara a propriedade
				ProMergeHistoricoMetodos metodo = new ProMergeHistoricoMetodos();

				// Setar as propriedades do objeto
				metodo.setCodHistorico(vo.getCodHistorico());
				metodo.setDesMetodo(metodosVO.getNomeMetodo());
				metodo.setNumLinhaInicial(metodosVO.getRangeInicial());
				metodo.setNumLinhaFinal(metodosVO.getRangeFinal());
				metodo.setSequencial(sequencial);
				metodo.setNomeClasse(buscarNomeClasse(metodosVO.getNomeClasse()));

				// Adicionar o objeto a lista
				listaRetorno.add(metodo);

				// Próximo Sequencial dos Métodos
				sequencial += 1;

			}

			// 2.6) Os métodos e classes identificados devem ser salvos na
			// tabela de Históricos dos métodos
			DaoGenerico.getInstance().saveOrUpdateMetodosConflitos(vo, listaRetorno);

		} catch (Exception e) {
			log.error("Erro ao executar salvarMetodosAlterados.", e);

		}

	}

	/**
	 * 2.4) Efetuar diff entre esta versão e a versão recebida
	 * 
	 * 2.4.1) Sobrepor arquivo local com a versão recebida
	 * 
	 * 2.4.2) Efetuar diff entre a versão atual (não comitada) e a que está no
	 * servidor
	 * 
	 * 2.5) Linhas adicionadas ou removidas vão determinar quais métodos foram
	 * alterados
	 * 
	 * @param vo
	 * @param lstMetodosArquivo
	 * @param _cargaInicialMetodos
	 * 
	 * @return Lista de métodos com os ranges das linhas
	 */
	private static List<MetodosVO> identificarMetodosAlterados(ProMergeHistoricoAlteracoes vo, List<MetodosVO> lstMetodosArquivo,
			boolean _cargaInicialMetodos) {

		try {

			// Arquivo que recebeu do workspace
			String conteudoArquivoLocal = vo.getTxtArquivo();

			// TRUE - Deve realizar o Diff entre os arquivo (Válido somente para
			// COMMIT de fontes alterados)
			if (_cargaInicialMetodos) {
				// buscar arquivo do repositório
				String conteudoArquivoRemoto = GetFileSVN.getFile(vo.getDesArquivo());
				// Se estiver nulo
				if (conteudoArquivoRemoto == null)
					return null;

				// Efetua diff dos arquivos e gera arquivo result.txt
				DiffWCToRepository.doDiff(conteudoArquivoLocal, conteudoArquivoRemoto, vo.getDesUsuario());

			}

			// Efetua parse do arquivo result.txt e remove métodos da lista que
			// não foram alterados
			lstMetodosArquivo = DiffParser.identificarMetodosAlterados(lstMetodosArquivo);
			if (lstMetodosArquivo == null || lstMetodosArquivo.isEmpty()) {
				return null;
			}

			return lstMetodosArquivo;

		} catch (Exception e) {
			log.error("Erro ao executar identificarMetodosAlterados", e);

		}

		return null;

	}

	/**
	 * 
	 * @param vo
	 * @param arquivoIntegrado
	 * @return
	 */
	private static List<MetodosVO> buscarMetodosJavaArquivo(ProMergeHistoricoAlteracoes vo, String arquivoIntegrado) {

		List<MetodosVO> lstRetorno = new ArrayList<MetodosVO>(0);

		try {
			String[] split = arquivoIntegrado.split("\n");
			MetodosVO metodo = new MetodosVO();
			int linha = 1, count = 0;
			boolean foundedConstructor = false;

			// Método Construtor
			for (String string : split) {
				// regex diferente para o método construtor
				Pattern r = Pattern.compile(foundedConstructor ? _regex : _regexConstructor);
				Matcher m = r.matcher(string);

				if (m.find()) {
					// Se encontrou
					if (count++ > 0) {
						// Adicionar o método na lista
						metodo.setRangeFinal(linha - 1);
						lstRetorno.add(metodo);

						metodo = new MetodosVO();
						break;

					}

					// metodo.setNomeClasse(buscarNomeClasse(vo.getDesArquivo()));
					metodo.setNomeClasse(vo.getDesArquivo());
					metodo.setRangeInicial(linha);
					metodo.setNomeMetodo(buscarNomeMetodo(m.group(0)));
					metodo.setNomeProjeto(buscaNomeProjeto(vo.getDesArquivo()));

					// Achou o método construtor
					foundedConstructor = true;

				}

				linha++;

			}

			// Demais métodos da classe
			linha = 1;
			count = 0;

			for (String string : split) {
				// Now create matcher object.
				Pattern r = Pattern.compile(_regex);
				Matcher m = r.matcher(string);

				if (m.find()) {
					// Se encontrou
					if (count++ > 0) {

						metodo.setRangeFinal(linha - 1);
						lstRetorno.add(metodo);

						metodo = new MetodosVO();
					}

					// metodo.setNomeClasse(buscarNomeClasse(vo.getDesArquivo()));
					metodo.setNomeClasse(vo.getDesArquivo());
					metodo.setNomeClasse(vo.getDesArquivo());
					metodo.setRangeInicial(linha);
					metodo.setNomeMetodo(buscarNomeMetodo(m.group(0)));
					metodo.setNomeProjeto(buscaNomeProjeto(vo.getDesArquivo()));
				}

				linha++;
			}

			metodo.setRangeFinal(linha - 2);

			// Adicionar o registro
			lstRetorno.add(metodo);

		} catch (Exception e) {
			log.error("Erro ao executar buscarMetodosJavaArquivo.", e);

		}

		return lstRetorno;

	}

	public static String buscarNomeMetodo(String metodo) {

		int indexOf = metodo.indexOf("(");
		String aux = (metodo.substring(0, indexOf)).trim();

		return aux.substring(aux.lastIndexOf(" "), aux.length()).trim();

	}

	private static String buscarNomeClasse(String desArquivo) {
		// Serve para a processamento em Rede
		int indexOf = desArquivo.lastIndexOf(File.separator) + 1;

		// Se não achou pelo padrão do sistema (busca pelo separador padrão
		if (indexOf <= 0)
			indexOf = desArquivo.lastIndexOf("/") + 1;

		return desArquivo.substring(indexOf, desArquivo.length()).replace(".java", "");

	}

	/**
	 * Atualiza o arquivo resultante da integração no workspace local, efetua o
	 * compile do projeto, caso de erro retorna a mensagem de erro
	 * 
	 * @param vo
	 * @param arquivosAlterados
	 * @return
	 */
	private static String atualizarECompilarArquivo(ProMergeHistoricoAlteracoes arquivosAlterados, boolean apenasCompilar) {

		try {
			// 1) Grava arquivo resultado da integração
			File original = new File(DIR_WORKSPACE_SERVIDOR + File.separator, arquivosAlterados.getDesArquivo());

			// Se não apenas compilação
			if (apenasCompilar == false) {
				// Escreve o arquivo Contendo o resultado da integração (Até
				// aqui não realizou nenhuma integração com o SVN AINDA)
				SVNUtility.writeToFile(original, arquivosAlterados.getArquivoResultanteIntegracao(), false);

			}

			/*
			 * Atualizar a versão dos fontes p/ o evento do pos-commit
			 */
			long latestRevision = CheckoutUpdateUtils.doCheckoutUpdate(urlSVN, dirProjeto, true, "Carlos.Carbonera");

			/*
			 * Atualizar a versão dos fontes p/ o evento do pos-commit
			 */

			// Obtém diretório do projeto
			String[] camProjeto = DIR_FONTES.split("src");
			File fileBuildXml = new File(dirProjeto + File.separator + camProjeto[0], "build.xml");

			// Excluir o arquivo com o resultado da compilação (A cada
			// compilação cria um novo arquivo
			excluirResultadoCompilacao();

			// ******** Esta linha não opderá ser apagada
			// ********************************************
			// ******** Esta linha não opderá ser apagada
			// ********************************************
			System.out.println(_buildStarted);
			// ******** Esta linha não opderá ser apagada
			// ********************************************
			// ******** Esta linha não opderá ser apagada
			// ********************************************

			// 2) Chama compilação do projeto
			boolean CompilacaoOK = AntExecutor.executeAntTask(fileBuildXml.getAbsolutePath());

			// ******** Esta linha não opderá ser apagada
			// ********************************************
			// ******** Esta linha não opderá ser apagada
			// ********************************************
			System.out.println(_buildFinished);
			// ******** Esta linha não opderá ser apagada
			// ********************************************
			// ******** Esta linha não opderá ser apagada
			// ********************************************

			// Se não compilou OK
			if (!CompilacaoOK) {
				// Abre o arquivodo Erro da compilacao
				File file = new File("Compile.ERR");

				// Se o arquivo com o resultado da compilação existe
				if (file.exists()) {
					// Le o conteúdo do Arquivo
					String conteudo = FileUtils.readFileToString(file);

					// Pega o conteúdo do erro da compilação (Erro ou Sucesso)
					int iPosI = conteudo.toLowerCase().lastIndexOf(_buildStarted.toLowerCase());
					int iPosF = conteudo.toLowerCase().lastIndexOf(_buildFinished.toLowerCase());

					// Retorna o resultado da compilação (Mensagem de erro)
					return conteudo.substring(iPosI, iPosF).trim();

				}

			}

		} catch (Exception e) {
			log.error("Erro ao executar atualizarECompilarArquivo.", e);

		}

		return _buildSucessful;

	}

	/*
	 * private static String buscaCaminhoProjeto(File original, String projeto)
	 * {
	 * 
	 * String absolutePath = original.getAbsolutePath();
	 * 
	 * // Obtém o índice da primeira barra int indiceNomeProjeto =
	 * absolutePath.indexOf(projeto);
	 * 
	 * // A primeira palavra até a barra é o nome do projeto return
	 * absolutePath.substring(0, indiceNomeProjeto);
	 * 
	 * }
	 */

	/**
	 * Obtém o nome do projeto do workspace
	 * 
	 * @param desArquivo
	 * @return
	 */
	private static String buscaNomeProjeto(String desArquivo) {

		// Original (Caminho de rede Obtém o índice da primeira barra
		int indicePrimeiraBarra = desArquivo.indexOf(File.separator);

		// **** Avaliar melhor esta condição ************************* Carbonera
		if (indicePrimeiraBarra == -1)
			indicePrimeiraBarra = desArquivo.indexOf("/");

		// A primeira palavra até a barra é o nome do projeto
		return desArquivo.substring(0, indicePrimeiraBarra);

	}

	/**
	 * Integra o primeiro arquivo com os demais, setando o indicador conflito
	 * dos objetos da lista de outros arquivos, assim como o arquivo resultante
	 * da integração
	 * 
	 * ARQUIVO NÃO ESTÃO INTEGRADOS COM O SVN AINDA
	 * 
	 * @param vo
	 * @param lstOutrosArquivos
	 */
	private static void integrarArquivos(ProMergeHistoricoAlteracoes vo, List<ProMergeHistoricoAlteracoes> lstOutrosArquivos) {

		if (lstOutrosArquivos == null || lstOutrosArquivos.isEmpty())
			return;

		// Itera sobre os arquivos com conflito direto
		for (ProMergeHistoricoAlteracoes alteracao : lstOutrosArquivos) {

			log.info(">>> Arquivo original");
			log.info(vo.getTxtArquivo());

			log.info(">>> Arquivo outro usuário");
			log.info(alteracao.getTxtArquivo());

			// Utiliza método já existente para integrar arquivos e verificar se
			// houve conflito
			Map<String, String> arquivoIntegrado = ConflictedMergeUtil.integrarArquivos(new File(DIR_TEMP_INTEGRACOES), vo.getDesUsuario(),
					vo.getTxtArquivo(), vo.getDesArquivo(), alteracao.getTxtArquivo(), true);

			log.info(">>> Arquivo Integrado");
			alteracao.setArquivoResultanteIntegracao(arquivoIntegrado.get("arquivo"));

			log.info(">>> Gerou conflito?");
			alteracao.setConflitou(arquivoIntegrado.get("conflito").equals("1"));

		}

	}

	private static void processarOcorrenciasConflitos(ProMergeHistoricoAlteracoes _historico, String _resultadoCompilacao, boolean _comitado) {
		try {
			// Filtar o retorno da compilação até a ocorrência do erro
			String[] _split = _resultadoCompilacao.split("\n");
			List<String> _erros = new ArrayList<String>(0);
			String _splitCondicao = ": error: ";

			// Percorrer as mensagens de erros (Filtrar apenas as linhas com os
			// erros)
			for (String linhaErro : _split) {
				if (linhaErro.indexOf(_splitCondicao) > 0) {
					// Pegar somente o nome da classe
					String _mensagemErro = linhaErro.split(_splitCondicao)[0].replace(DIR_WORKSPACE_SERVIDOR, "");

					// if (!_erros.contains(_mensagemErro.split(":")[0]))
					if (!_erros.contains(_mensagemErro))
						// Adicionar a 1a mensagem de erro
						_erros.add(_mensagemErro);

				}

			}

			// Percorrer todas as linhas de erros
			for (String string : _erros) {
				// Buscar o nome da classe e do Erro
				String[] _informacoesErro = string.split(":");
				String _nomeClasse = buscarNomeClasse(_informacoesErro[0].replace(":", "")).toLowerCase();
				int linhaErro = Integer.valueOf(_informacoesErro[1]);

				// Consultar a complexidade
				ProMergeHistoricoMetodos _complexidade = DaoGenerico.getInstance().buscarHistoricoMetodo(linhaErro, _nomeClasse);

				if (_complexidade != null) {
					// Consultar a Severidade do método
					ProMergeAvaliacaoComplexidade _severidade = DaoGenerico.getInstance().buscarComplexidadePorPK(_complexidade.getNomeClasse(),
							_complexidade.getDesMetodo());

					// É Nulo ? Severidade Baixa Senão Seta o Grau de Severidade
					// encontrado
					_historico.setSeveridade(_severidade == null ? 1 : _severidade.getSeveridade());

					// 1 - Conflito Direto | 2 - Conflito Indireto
					String _nomeClasseHistoricoOrig = _historico.getDesArquivo();
					// String _nomeClasseHistorico =
					// buscarNomeClasse(_historico.getDesArquivo()).toLowerCase().trim();
					// String _nomeClasseConflito =
					// buscarNomeClasse(_severidade.getClasse()).toLowerCase().trim();

					// 1 - Conflito Direto e 2 - Conflito Indireto
					// int _tipoConflito =
					// _nomeClasseHistorico.equals(_nomeClasseConflito) ? 1 : 2;

					// Isto é devido a poder ocorrer um conflito indireto
					_historico.setDesArquivo(_severidade.getClasse());

					// Gravar/Atualizar o conflito: 1 - Conflito Direto | 2 -
					// Conflito Indireto | 3 - Erro de Compição
					DaoGenerico.getInstance().saveOrUpdateResumoConflitos(_historico, 3, _comitado);

					// -----------------------------------------------------------------/
					// ******* CUIDADO AQUI
					// ******************************************/
					// -----------------------------------------------------------------/
					// Voltar o nome original da classe que está sendo
					// processado
					_historico.setDesArquivo(_nomeClasseHistoricoOrig);

				}

			}

		} catch (Exception e) {
			log.error("Erro ao executar processarOcorrenciasConflitos.", e);

		}

	}

}