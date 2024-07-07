package com.dvl.promerge.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import com.dvl.core.entitys.ProMergeResumosConflitos;
import com.dvl.core.util.AplicationUtils;
import com.dvl.core.vos.ArquivoVO;
import com.dvl.core.vos.IntegracaoVO;
import com.dvl.core.vos.MetodosVO;
import com.dvl.core.vos.SobrescritaVisualizacaoVO;
import com.dvl.promerge.vo.CallHierarchyGenerator;

/**
 * This sample class demonstrates how to use the TableViewerExample inside a workbench view. The view is essentially a wrapper for the TableViewerExample. It handles the Selection
 * event for the close button.
 */

public class MainView extends ViewPart {

	private static Logger log = Logger.getLogger(MainView.class);

	private TableViewerProMerge viewer;

	private Action action1; // Ação Implementada
	private Action action2; // Ação Implementada
	private Action action3; // Consultar os Commits Realizados
	private Action action4; // Complexity of methods and classes

	public static String usuario; // Atualizado do arquivo de configuração
	public static String ipServidor; // atualizado do arquivo de configuração
	public static int porta = 9090;
	public static List<ArquivoVO> listaArquivosModificados;

	/**
	 * The constructor.
	 */
	public MainView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewerProMerge(parent);

		// Define as ações dos itens do menu popup (Clique do botão direito)
		makeActions();

		// Define o menu popup - Clique botão direito
		hookContextMenu();

		// Cria os botes no barra (A direita)
		contributeToActionBars();

		// Carrega as informações do Arquivo C:\Java\eclipse-jee-juno-win32-x86_64\eclipse\configuracoes.properties - Se apagar - recria o
		// arquivo
		carregarConfiguracoes();

		// Habilita Socket do cliente - Abre a port 9191
		dispararSocketClient();

		// Dispara hierarquia dos objetos - Abre a porta 9192
		dispararSocketServerHierarquia();

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResourceChangeListener listener = new IResourceChangeListener() {
			public void resourceChanged(final IResourceChangeEvent event) {
				Thread a = new Thread() {
					public void run() {
						dispararProcessoProMerge(workspace, event);
					}
				};
				a.start();
			}
		};
		workspace.addResourceChangeListener(listener);
	}

	/**
	 * Dispara processo principal
	 * 
	 * @param workspace
	 * @param event
	 */
	private void dispararProcessoProMerge(final IWorkspace workspace, IResourceChangeEvent event) {

		// Alterações efetuadas -- No Hooks do SVN está como POSTCOMMIT
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;

		final IWorkspaceRoot root = workspace.getRoot();

		IProject[] projects = root.getProjects();

		if (projects == null || projects.length == 0) {
			return;
		}

		IResourceDelta rootDelta = event.getDelta();

		// Inicializa array para salvar arquivos
		final ArrayList<IResource> changed = new ArrayList<IResource>();
		final List<String> projetosAlterados = new ArrayList<String>();

		for (IProject iProject : projects) {

			// obtém o projeto
			final String projeto = iProject.getName();

			IResourceDelta docDelta = rootDelta.findMember(new Path(projeto + "/src"));

			if (docDelta == null)
				continue;

			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {

					// Apenas alterações de conteúdo
					if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
						return true;

					IResource resource = delta.getResource();

					// Alterações em arquivos que terminam com .java
					if (resource.getType() == IResource.FILE && "java".equalsIgnoreCase(resource.getFileExtension())) {
						changed.add(resource);
						projetosAlterados.add(projeto);

					}

					return true;
				}
			};

			try {
				docDelta.accept(visitor);
			} catch (CoreException e) {
				log.error("Erro método accept da classe IResourceDelta ", e);
			}

		}

		// Caso nenhum projeto tenha sido alterado
		if (projetosAlterados == null || projetosAlterados.isEmpty()) {
			return;
		}

		// Limpa a lista de arquivos modificados
		listaArquivosModificados = new ArrayList<ArquivoVO>(0);

		// Lista de arquivos para envio
		List<ArquivoVO> listaEnvioArquivos = new ArrayList<ArquivoVO>(0);

		// 1) Sempre que tiver algum arquivo alterado ou estiver iniciando a app, efetuar sincronize do workspace com o servidor para
		// identificar os arquivos que estão alterados
		for (String projeto : projetosAlterados) {

			// 2) Estes serão os arquivos enviados ao servidor e utilizados na consulta da aba ProMerge View
			List<File> listModifiedFiles = listModifiedFiles(projeto);

			if (listModifiedFiles == null || listModifiedFiles.isEmpty()) {
				continue;
			}

			// aqui irá chamar o servidor passando a lista de arquivos alterados/adicionados ou removidos (usar objeto ArquivoVO)
			for (File file : listModifiedFiles) {

				ArquivoVO arquivoVO = new ArquivoVO();
 
				try {
					arquivoVO.setConteudoArquivo(FileUtils.readFileToString(file));

				} catch (IOException e) {
					log.error("Erro ao ler arquivo.", e);
				}

				// seta caminho do arquivo apartir do projeto
				String caminhoRelativo = obterCaminhoRelativoArquivo(file.getAbsolutePath(), projeto);

				arquivoVO.setNomeCompletoArquivo(caminhoRelativo);
				arquivoVO.setNomeUsuario(usuario);
				arquivoVO.setNomeProjeto(projeto);

				listaEnvioArquivos.add(arquivoVO);

			}

			listaArquivosModificados.addAll(listaEnvioArquivos);
		}

		if (listaEnvioArquivos == null || listaEnvioArquivos.isEmpty()) {
			return;

		}

		AplicationUtils.enviarMensagemServidor(ipServidor, porta, listaEnvioArquivos);
	}

	/**
	 * 
	 * @param absolutePath
	 * @param projeto
	 * @return
	 */
	private String obterCaminhoRelativoArquivo(String absolutePath, String projeto) {

		int indexOf = absolutePath.indexOf(projeto);
		return absolutePath.substring(indexOf, absolutePath.length());
	}

	/**
	 * Habilita socket que irá receber as conexões do cliente
	 */
	private void dispararSocketClient() {

		Thread a = new Thread() {
			// Declaração do Socket
			ServerSocket listener;

			public void run() {

				try {
					listener = new ServerSocket(9191); // Uma porta para cada clientes 9191
					System.out.println("ABRIU 9191");

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

								byte[] readBuffer = new byte[available];

								input.read(readBuffer);

								String arquivo = (String) AplicationUtils.getXStream().fromXML(new String(readBuffer));
								log.info(arquivo);

								String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

								File fileLocation = new File(workspaceLocation + File.separator + arquivo);

								String arquivoString = FileUtils.readFileToString(fileLocation);

								output.write(arquivoString.getBytes(StandardCharsets.UTF_8));

								break;
							}
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
		}; 

		a.start();
	}

	/**
	 * Habilita socket que irá receber as conexões do cliente
	 */
	void dispararSocketServerHierarquia() {

		Thread a = new Thread() {
			// Declaração do Socket
			ServerSocket listener;

			public void run() {

				try {
					listener = new ServerSocket(9192); // 9192
					System.out.println("ABRIU 9192");

					while (true) {

						Socket socket = listener.accept();
						InputStream input = null;
						OutputStream output = null;

						try {
							output = socket.getOutputStream();
							input = socket.getInputStream();

							while (true) {
								// Como funciona isto ?????
								int available = input.available();
								if (available == 0) {
									Thread.sleep(500);
									continue;
								}

								byte[] readBuffer = new byte[available];
								input.read(readBuffer);
								String _conteudo = new String(readBuffer);

								List<MetodosVO> listaMetodos = (List<MetodosVO>) AplicationUtils.getXStream().fromXML(_conteudo);
								String _erro = "";

								try {
									if (listaMetodos != null) {
										for (MetodosVO metodosVO : listaMetodos) {
											List<MetodosVO> metodosInfluenciados = CallHierarchyGenerator.buscarHierarquiaChamada(
													metodosVO.getNomeProjeto(), metodosVO.getNomeMetodo(), metodosVO.getNomeClasse());

											metodosVO.setMetodosInfluenciados(metodosInfluenciados);
										}
									}
								} catch (Throwable e) {
									_erro = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();

								}

								String xml = AplicationUtils.getXStream().toXML(listaMetodos);
								output.write((_erro != "" ? _erro : xml).getBytes(StandardCharsets.UTF_8));
								break;
							}

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
		};

		a.start();
	}

	/**
	 * Carrega configurações do arquivo
	 */
	private void carregarConfiguracoes() {

		if (usuario != null) {
			return;
		}

		// Inicia variaveis stream
		FileInputStream fileInputStream = null;

		try {

			File file = new File("configuracoes.properties");
			log.info(file.getAbsolutePath());

			if (!file.exists()) {
				file.createNewFile();
				salvarUsuario();

			}

			// Obtém strem de entrada do arquivo
			fileInputStream = new FileInputStream(file);

			// Carrega a stream de entrada como properties
			Properties prop = new Properties();
			prop.load(fileInputStream);

			// Atualiza as propriedades
			usuario = prop.getProperty("usuario");
			ipServidor = prop.getProperty("ip");

		} catch (Exception e) {
			log.error("Erro ao obter atributos.", e);
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				log.error("Erro ao fechar arquivo de propriedades.", e);
			}
		}
	}

	private void salvarUsuario() {

		FileOutputStream fileOut = null;

		try {
			Properties properties = new Properties();
			properties.setProperty("usuario", (String) JOptionPane.showInputDialog("Informe o usuario:"));
			properties.setProperty("ip", (String) JOptionPane.showInputDialog("Informe o ip do servidor:"));

			properties.setProperty("driver", "org.postgresql.Driver");
			properties.setProperty("user", "postgres");
			properties.setProperty("pass", "postgres");

			File file = new File("configuracoes.properties");

			fileOut = new FileOutputStream(file);
			properties.store(fileOut, "Configurações-ProMerge");

		} catch (Exception e) {
			log.error("Erro ao salvar propriedades.", e);

		} finally {
			try {
				if (fileOut != null) {
					fileOut.close();
				}
			} catch (IOException e) {
				log.error("Erro ao fechar arquivo de propriedades.", e);
			}
		}

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenu(manager);
			}
		});

		TableViewer tableViewer = viewer.getTableViewer();
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Handle a 'close' event by disposing of the view
	 */

	public void handleDispose() {
		this.getSite().getPage().hideView(this);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {

				ISelection selection = viewer.getTableViewer().getSelection();

				ProMergeResumosConflitos obj = (ProMergeResumosConflitos) ((IStructuredSelection) selection).getFirstElement();

				if (obj == null) {
					return;
				}

				SobrescritaVisualizacaoVO vo = new SobrescritaVisualizacaoVO();
				vo.setConflito(obj);

				String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

				File fileLocation = new File(workspaceLocation + File.separator + obj.getDesArquivo());

				try {

					log.info(">>>:" + fileLocation.getAbsolutePath());

					vo.setConteudoArquivoUsuario(FileUtils.readFileToString(fileLocation));
				} catch (IOException e) {
					log.error("Não foi possível efetuar a leitura do arquivo [" + obj.getDesArquivo() + "].", e);
				}

				vo.setNomeCompletoArquivo(obj.getDesArquivo());
				vo.setNomeUsuarioRequisitante(usuario);

				// 1) Aqui deverá enviar uma mensagem ao servidor, solicitando
				// arquivo do usuário em questão
				// 2) Servidor irá solicitar arquivo ao client e enviar devolta
				String conteudoRetorno = AplicationUtils.enviarMensagemServidor(ipServidor, porta, vo);

				try {

					// 3) O conteúdo do arquivo deverá ser sobrescrito
					// localmente
					FileUtils.writeStringToFile(fileLocation, conteudoRetorno, false);
				} catch (IOException e) {
					log.error("Erro ao escrever arquivo [" + obj.getDesArquivo() + "]", e);
				}
			}
		};

		action1.setText("Overwrite local files");
		action1.setToolTipText("Overwrite local files");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEALL_EDIT));

		action2 = new Action() {
			public void run() {

				ISelection selection = viewer.getTableViewer().getSelection();

				ProMergeResumosConflitos obj = (ProMergeResumosConflitos) ((IStructuredSelection) selection).getFirstElement();

				// aqui irá disparar o integração dos fontes (usar objeto
				// IntegracaoVO)
				IntegracaoVO vo = new IntegracaoVO();
				vo.setConflito(obj);

				String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

				File fileLocation = new File(workspaceLocation + File.separator + obj.getDesArquivo());

				try {

					log.info(">>>:" + fileLocation.getAbsolutePath());

					vo.setConteudoArquivo(FileUtils.readFileToString(fileLocation));

					vo.setConteudoArquivo(FileUtils.readFileToString(fileLocation));
				} catch (IOException e) {
					log.error("Não foi possível efetuar a leitura do arquivo [" + obj.getDesArquivo() + "].", e);
				}

				vo.setNomeCompletoArquivo(obj.getDesArquivo());
				vo.setNomeUsuarioRequisicao(usuario);

				// 1) Aqui deverá enviar uma mensagem ao servidor, com o arquivo
				// local
				// 2) Servidor irá solicitar arquivo ao client
				// 3) No servidor, as duas versões deverão ser integradas
				// 4) O resultado será enviado ao workspace emitente

				String conteudoRetorno = AplicationUtils.enviarMensagemServidor(ipServidor, porta, vo);

				try {

					// 5) Neste workspace será sobrescrito o arquivo
					FileUtils.writeStringToFile(fileLocation, conteudoRetorno, false);
				} catch (IOException e) {
					log.error("Erro ao escrever arquivo [" + obj.getDesArquivo() + "]", e);
				}
			}
		};

		action2.setText("Integrate Files");
		action2.setToolTipText("Integrate Files");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action3 = new Action() {
			public void run() {

				// Chamar a tela de consulta dos commits
				ProMergeCommits _commits;
				try {
					_commits = new ProMergeCommits();
					_commits.setLocationRelativeTo(null);
					_commits.setVisible(true);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		action3.setText("Search commits details");
		action3.setToolTipText("Search commits details");
		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));

		action4 = new Action() {
			public void run() {

				// Chamar a tela de consulta dos commits
				ProMergeComplexity _complexity;
				try {
					_complexity = new ProMergeComplexity();
					_complexity.setLocationRelativeTo(null);
					_complexity.setVisible(true);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		action4.setText("Complexity classes and methods");
		action4.setToolTipText("Complexity classes and methods");
		action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action4);
		manager.add(new Separator());
		manager.add(action3);
		manager.add(new Separator());
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action4);
		manager.add(new Separator());
		manager.add(action3);
		manager.add(new Separator());
		manager.add(action2);
		manager.add(new Separator());
		manager.add(action1);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action4);
		manager.add(new Separator());
		manager.add(action3);
		manager.add(new Separator());
		manager.add(action2);
		manager.add(new Separator());
		manager.add(action1);
	}

	public static List<File> listModifiedFiles(String path) {

		// 1) path -> folder do workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		// get location of workspace (java.io.File)
		File projectDirectory = new File(workspace.getRoot().getLocation().toFile(), path);

		SVNClientManager svnClientManager = SVNClientManager.newInstance();

		final List<File> fileList = new ArrayList<File>();

		try {

			svnClientManager.getStatusClient().doStatus(projectDirectory, SVNRevision.HEAD, SVNDepth.INFINITY, false, false, false, false,
					new ISVNStatusHandler() {
						@Override
						public void handleStatus(SVNStatus status) throws SVNException {
							SVNStatusType statusType = status.getContentsStatus();
							if (statusType != SVNStatusType.STATUS_NONE && statusType != SVNStatusType.STATUS_NORMAL
									&& statusType != SVNStatusType.STATUS_IGNORED) {
								fileList.add(status.getFile());
							}
						}
					}, null);

		} catch (SVNException e) {
			log.error("Erro ao obter status dos arquivos.", e);
		}

		return fileList;
	}
}