package com.dvl.core.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dvl.core.entitys.ProMergeAvaliacaoComplexidade;
import com.dvl.core.entitys.ProMergeHistoricoAlteracoes;
import com.dvl.core.entitys.ProMergeHistoricoMetodos;
import com.dvl.core.entitys.ProMergeResumosConflitos;
import com.dvl.core.entitys.ProMergeHistoricoCommits;

import com.dvl.core.vos.ArquivoVO;
import com.dvl.core.vos.MetodosVO;

public class DaoGenerico {
	private static String driver = "org.postgresql.Driver";
	private static String url = "jdbc:postgresql://localhost:5432/mestrado"; //Colocar o endereço do servidor (Localhost -> Servidor OU o IP do Servidor -> CLiente
	private static String user = "postgres";
	private static String pass = "postgres";

	private static Logger log = Logger.getLogger(DaoGenerico.class);

	private static DaoGenerico dao;
	private static Connection conn;

	public static Connection getConnection() {
		if (conn == null) {
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(url, user, pass);
			} catch (Exception e) {
				log.error("Erro ao obter conex�o com banco de dados.", e);
			}
		}
		return conn;
	}

	/**
	 * Retorna os conflitos identificados
	 * 
	 * @param vo
	 */
	public List<ProMergeResumosConflitos> buscarResumoConflitos(List<ArquivoVO> arquivosAlterados, String usuario) {

		/*
		 * if (arquivosAlterados == null || arquivosAlterados.isEmpty()) {
		 * return null; }
		 */

		List<ProMergeResumosConflitos> list = new ArrayList<ProMergeResumosConflitos>(0);
		PreparedStatement stmt = null;

		try {

			// String virgula = "";
			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_conflito  \n");
			sql.append("     , des_usuario   \n");
			sql.append("     , des_arquivo   \n");
			sql.append("     , tip_conflito  \n");
			sql.append("     , comitado  	 \n");
			sql.append("     , severidade    \n");
			sql.append("     , dta_alteracao \n");
			sql.append("  FROM promerge_resumos_conflitos \n");
			sql.append(" ORDER BY cod_conflito DESC");
			/*
			 * sql.append(" WHERE lower(des_arquivo) IN lower(");
			 * 
			 * for (ArquivoVO arquivo : arquivosAlterados) { // Fomatar a
			 * condição do Where sql.append(virgula + "'" +
			 * arquivo.getNomeCompletoArquivo() + "'"); virgula = ", "; }
			 * sql.append(") AND lower(des_usuario) != lower(?)");
			 */

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			// stmt.setString(1, usuario.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();
			ProMergeResumosConflitos dados = null;
			SimpleDateFormat _fmtDataHMS = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

			while (rs.next()) {
				// Inicializar o objeto
				dados = new ProMergeResumosConflitos();

				// Setar as propriedades
				dados.setCodConflito(rs.getInt(1));
				dados.setDesUsuario(rs.getString(2));
				dados.setDesArquivo(rs.getString(3));
				dados.setTipConflito(rs.getInt(4));
				dados.setComitado(rs.getInt(5));
				dados.setSeveridade(rs.getInt(6));
				dados.setDtaAlteracao(_fmtDataHMS.format(rs.getTimestamp(7)));

				// Adicionar o objeto
				list.add(dados);

			}

			return list;

		} catch (SQLException e) {
			log.error("Erro ao executar buscarResumoConflitos().", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	public List<ProMergeAvaliacaoComplexidade> buscarListaComplexidades() {
		PreparedStatement stmt = null;
		List<ProMergeAvaliacaoComplexidade> _lista = new ArrayList<ProMergeAvaliacaoComplexidade>(0);
		ProMergeAvaliacaoComplexidade dados = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT classe,         \n");
			sql.append("       metodo,         \n");
			sql.append("       complexidade,   \n");
			sql.append("       qtdreferencias, \n");
			sql.append("       qtdpontos       \n");
			sql.append("  FROM promerge_avaliacao_complexidade \n");
			sql.append(" ORDER BY classe desc, metodo");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			// Se encontrou registro (Retorna os dados)
			while (rs.next()) {
				// Variável de ambiente
				dados = new ProMergeAvaliacaoComplexidade();

				// Atribuir as propriedades
				dados.setClasse(rs.getString(1));
				dados.setMetodo(rs.getString(2));
				dados.setSeveridade(rs.getInt(3));
				dados.setQtdOcorrencias(rs.getInt(4));
				dados.setQtdPontos(rs.getInt(5));

				_lista.add(dados);

			}

		} catch (SQLException e) {
			log.error("Erro ao executar ProMergeAvaliacaoComplexidade().", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}

		return _lista;

	}

	/**
	 * Busca ip do usuário
	 * 
	 * @param vo
	 */
	public String buscarIpCliente(String usuario) {

		PreparedStatement stmt = null;

		try {

			// Preparar o comando
			stmt = getConnection().prepareStatement("SELECT ip_usuario FROM promerge_usuarios WHERE lower(des_usuario) = lower(?)");

			// Parâmetros
			stmt.setString(1, usuario.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getString(1);
			}

			return null;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarIpCliente.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * Calcular o intervalo de tempo entre duas datas (em dias,horas, minutos e
	 * segundos)
	 * 
	 * @param dthrFinal
	 * @param dthrInicial
	 * 
	 * @return Intervalo de tempo
	 * 
	 */
	public String calcularIntervaloEntreDatas(String dthrFinal, String dthrInicial) {

		PreparedStatement stmt = null;

		try {

			// Preparar o comando
			stmt = getConnection().prepareStatement("SELECT CAST(? AS TIMESTAMP) - CAST(? AS TIMESTAMP) ");

			// Parâmetros
			stmt.setString(1, dthrFinal.trim());
			stmt.setString(2, dthrInicial.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getString(1).replace("day", "dia");
			}

			return null;

		} catch (SQLException e) {
			log.error("Erro ao calcular o intervalo entre datas.", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * Insere registro na tabela de alterações
	 * 
	 * @param vo
	 * @param parametro
	 */
	public void inserirHistorico(ProMergeHistoricoAlteracoes vo) {
		PreparedStatement stmt = null;
		try {

			StringBuilder sql = new StringBuilder();

			sql.append("INSERT INTO promerge_historico_alteracoes( \n");
			sql.append(" cod_historico,\n");
			sql.append(" des_arquivo,  \n");
			sql.append(" txt_arquivo,  \n");
			sql.append(" des_usuario)  \n");
			sql.append(" VALUES(nextval('promerge_historico_alteracoes_cod_historico_seq'), ?, ?, ?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, vo.getDesArquivo().trim());
			stmt.setString(2, vo.getTxtArquivo().trim());
			stmt.setString(3, vo.getDesUsuario().trim());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar inserir registro na base de dados.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * Atualizar registro na tabela de alterações
	 * 
	 * @param vo
	 * @param parametro
	 */
	public void atualizarHistorico(ProMergeHistoricoAlteracoes vo) {
		PreparedStatement stmt = null;
		try {

			StringBuilder sql = new StringBuilder();

			sql.append("UPDATE promerge_historico_alteracoes \n");
			sql.append("   SET txt_arquivo   = ? \n");
			sql.append(" WHERE cod_historico = ?");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, vo.getTxtArquivo().trim());
			stmt.setInt(2, vo.getCodHistorico());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar alterar registros na base de dados.", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * Busca histórico por pk
	 * 
	 * @param vo
	 */
	public ProMergeHistoricoAlteracoes buscarHistoricoPorPK(String usuario, String desArquivo) {

		PreparedStatement stmt = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_historico, \n");
			sql.append("       des_arquivo,   \n");
			sql.append("       txt_arquivo,   \n");
			sql.append("       des_usuario    \n");
			sql.append("  FROM promerge_historico_alteracoes \n");
			sql.append(" WHERE lower(des_usuario) = lower(?) \n");
			sql.append("   AND lower(des_arquivo) = lower(?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, usuario.trim());
			stmt.setString(2, desArquivo.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				ProMergeHistoricoAlteracoes retorno = new ProMergeHistoricoAlteracoes();

				retorno.setCodHistorico(rs.getInt(1));
				retorno.setDesArquivo(rs.getString(2));
				retorno.setTxtArquivo(rs.getString(3));
				retorno.setDesUsuario(rs.getString(4));

				return retorno;
			}

			return null;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarHistoricoPorPK.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * Busca histórico por pk
	 * 
	 * @param vo
	 */
	public ProMergeHistoricoAlteracoes buscarHistoricoPorPK(Integer codHistorico) {

		PreparedStatement stmt = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_historico, \n");
			sql.append("       des_arquivo,   \n");
			sql.append("       txt_arquivo,   \n");
			sql.append("       des_usuario    \n");
			sql.append("  FROM promerge_historico_alteracoes \n");
			sql.append(" WHERE cod_historico = ?");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, codHistorico);

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				ProMergeHistoricoAlteracoes retorno = new ProMergeHistoricoAlteracoes();

				retorno.setCodHistorico(rs.getInt(1));
				retorno.setDesArquivo(rs.getString(2));
				retorno.setTxtArquivo(rs.getString(3));
				retorno.setDesUsuario(rs.getString(4));

				return retorno;
			}

			return null;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarHistoricoPorPK.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * 
	 * @param vo
	 * @return
	 */
	public ProMergeHistoricoAlteracoes saveOrUpdateHistorico(ProMergeHistoricoAlteracoes vo) {
		// Busca registro por pk
		ProMergeHistoricoAlteracoes pk = buscarHistoricoPorPK(vo.getDesUsuario(), vo.getDesArquivo());

		if (pk != null) {

			pk.setTxtArquivo(vo.getTxtArquivo());

			atualizarHistorico(pk);

			vo.setCodHistorico(pk.getCodHistorico());

		} else {

			inserirHistorico(vo);

			vo = buscarHistoricoPorPK(vo.getDesUsuario(), vo.getDesArquivo());
		}

		return vo;
	}

	/**
	 * 
	 * @param usuario
	 * @param desArquivo
	 * @param mesmoWorkspace
	 * @return
	 */
	public List<ProMergeHistoricoAlteracoes> buscarArquivoWorkspaces(String usuario, String desArquivo, boolean mesmoWorkspace)

	{
		List<ProMergeHistoricoAlteracoes> lista = new ArrayList<ProMergeHistoricoAlteracoes>();
		PreparedStatement stmt = null;

		try {
			String _complemento = " WHERE (lower(des_usuario) != lower(?) AND lower(des_usuario) != lower('sistemas'))";

			if (mesmoWorkspace)
				_complemento = " WHERE lower(des_usuario) = lower(?)";

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_historico,\n");
			sql.append("       des_arquivo,  \n");
			sql.append("       txt_arquivo,  \n");
			sql.append("       des_usuario   \n");
			sql.append("  FROM promerge_historico_alteracoes \n");
			sql.append(_complemento);
			sql.append(" AND lower(des_arquivo) = lower(?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, usuario.trim());
			stmt.setString(2, desArquivo.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();
			ProMergeHistoricoAlteracoes retorno = null;

			while (rs.next()) {
				// Inicializar o objeto
				retorno = new ProMergeHistoricoAlteracoes();

				// Setar as propriedades
				retorno.setCodHistorico(rs.getInt(1));
				retorno.setDesArquivo(rs.getString(2));
				retorno.setTxtArquivo(rs.getString(3));
				retorno.setDesUsuario(rs.getString(4));

				// Adicionar o objeto
				lista.add(retorno);
			}

			return lista;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarArquivoWorkspaces.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * 
	 * @param vo
	 * @param metodosVO
	 * @return
	 */
	public List<ProMergeHistoricoAlteracoes> buscarConflitosIndiretos(ProMergeHistoricoAlteracoes vo, MetodosVO metodosVO) {

		// 1) Esta consulta deverá buscar registros da
		// ProMergeHistoricoAlteracoes que possuam os métodos informados no
		// parâmetro
		PreparedStatement stmt = null;

		try {
			List<MetodosVO> metodosInfluenciados = metodosVO.getMetodosInfluenciados();
			List<ProMergeHistoricoAlteracoes> listaRetorno = new ArrayList<ProMergeHistoricoAlteracoes>(0);

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT DISTINCT a.cod_historico as cod_historico  \n");
			sql.append("  FROM promerge_historico_alteracoes a            \n");
			sql.append("       INNER JOIN promerge_historico_metodos b    \n");
			sql.append("       ON (a.cod_historico = b.cod_historico) 	  \n");
			sql.append(" WHERE (lower(a.des_usuario) != lower(?)          \n");
			sql.append("   AND  lower(a.des_usuario) != lower('sistemas'))\n");

			for (MetodosVO metodo : metodosInfluenciados) {

				String[] _dadosProjeto = metodo.getNomeClasse().split("src");
				String _pacoteEClasse = _dadosProjeto[1].replace(File.separator, "%");

				sql.append(" AND lower(a.des_arquivo) like lower('" + _pacoteEClasse + "%') \n");
				sql.append(" AND lower(b.des_metodo) = lower('" + metodo.getNomeMetodo() + "')");
			}

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, vo.getDesUsuario().trim().toLowerCase());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();
			ProMergeHistoricoAlteracoes retorno = null;

			while (rs.next()) {
				// Inicializar o objeto
				retorno = new ProMergeHistoricoAlteracoes();

				// Setar as propriedades
				retorno.setCodHistorico(rs.getInt(1));

				// Adicionar o objeto
				listaRetorno.add(retorno);
			}

			return listaRetorno;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarConflitosIndiretos.", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * Insere registro na tabela de Resumo de conflitos
	 * 
	 * @param vo
	 * @param parametro
	 */
	public void inserirResumoConflito(ProMergeResumosConflitos vo) {
		PreparedStatement stmt = null;

		try {
			StringBuilder sql = new StringBuilder();

			sql.append("INSERT INTO promerge_resumos_conflitos( \n");
			sql.append(" cod_conflito,  \n");
			sql.append(" des_usuario,   \n");
			sql.append(" des_arquivo,   \n");
			sql.append(" tip_conflito,  \n");
			sql.append(" comitado,      \n");
			sql.append(" severidade,    \n");
			sql.append(" dta_alteracao) \n");
			sql.append(" VALUES(nextval('promerge_resumos_conflitos_cod_conflito_seq'), ?, ?, ?, ?, ?, Now())");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, vo.getDesUsuario());
			stmt.setString(2, vo.getDesArquivo());
			stmt.setInt(3, vo.getTipConflito());
			stmt.setInt(4, vo.getComitado());
			stmt.setInt(5, vo.getSeveridade());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar inserir registro na base de dados.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * Atualizar registro na tabela de Resumo de conflitos
	 * 
	 * @param vo
	 * @param parametro
	 */
	public void atualizarResumoConflito(ProMergeResumosConflitos vo) {
		PreparedStatement stmt = null;
		try {
			StringBuilder sql = new StringBuilder();

			sql.append("UPDATE promerge_resumos_conflitos \n");
			sql.append("   SET tip_conflito  = ?,         \n");
			sql.append("       comitado      = ?,         \n");
			sql.append("       severidade    = ?,         \n");
			sql.append("       dta_alteracao = Now()      \n");
			sql.append(" WHERE cod_conflito  = ?");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, vo.getTipConflito());
			stmt.setInt(2, vo.getComitado());
			stmt.setInt(3, vo.getSeveridade());
			stmt.setInt(4, vo.getCodConflito());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar alterar registros na base de dados.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * Busca resumo conflito por pk
	 * 
	 * @param vo
	 */
	public ProMergeResumosConflitos buscarResumoConflitosPorPK(String usuario, String desArquivo) {
		PreparedStatement stmt = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_conflito, \n");
			sql.append("       des_usuario,  \n");
			sql.append("       des_arquivo,  \n");
			sql.append("       tip_conflito, \n");
			sql.append("       comitado,     \n");
			sql.append("       severidade,   \n");
			sql.append("       dta_alteracao \n");
			sql.append("  FROM promerge_resumos_conflitos    \n");
			sql.append(" WHERE lower(des_usuario) = lower(?) \n");
			sql.append("   AND lower(des_arquivo) = lower(?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, usuario.trim());
			stmt.setString(2, desArquivo.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();
			SimpleDateFormat _fmtDataHMS = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

			if (rs.next()) {

				ProMergeResumosConflitos retorno = new ProMergeResumosConflitos();

				retorno.setCodConflito(rs.getInt(1));
				retorno.setDesUsuario(rs.getString(2));
				retorno.setDesArquivo(rs.getString(3));
				retorno.setTipConflito(rs.getInt(4));
				retorno.setComitado(rs.getInt(5));
				retorno.setSeveridade(rs.getInt(6));
				retorno.setDtaAlteracao(_fmtDataHMS.format(rs.getTimestamp(7)));

				return retorno;
			}

			return null;

		} catch (SQLException e) {
			log.error("Erro ao executar buscarResumoConflitosPorPK.", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * 
	 * @param revisao
	 * @param usuario
	 * @return
	 */
	public int buscarSequenciaCommitsPorPK(int revisao, String usuario) {
		PreparedStatement stmt = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT Coalesce(Max(seq), 0) AS Seq \n");
			sql.append("  FROM promerge_historico_commits   \n");
			sql.append(" WHERE revisao        = ?           \n");
			sql.append("   AND lower(usuario) = lower(?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, revisao);
			stmt.setString(2, usuario.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return (rs.getInt(1) + 1);

			}

		} catch (SQLException e) {
			log.error("Erro ao executar buscarSequenciaCommitsPorPK.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}

		// Primeiro registro de commit
		return 1;

	}

	/**
	 * 
	 * @param revisaoI
	 * @param revisaoF
	 * @param dataI
	 * @param dataF
	 * @return
	 */
	public List<ProMergeHistoricoCommits> listarHistoricosCommits(int revisaoI, int revisaoF, String dataI, String dataF) {
		List<ProMergeHistoricoCommits> lista = new ArrayList<ProMergeHistoricoCommits>();
		PreparedStatement stmt = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT revisao,                   \n");
			sql.append("       usuario,                   \n");
			sql.append("       seq,                       \n");
			sql.append("       status,                    \n");
			sql.append("       dthrcommit,                \n");
			sql.append("       mensagem                   \n");
			sql.append("  FROM promerge_historico_commits \n");
			sql.append(" WHERE revisao BETWEEN ? AND ?    \n");
			sql.append("   AND CAST(dthrcommit AS DATE) BETWEEN CAST(? AS DATE) AND CAST(? AS DATE) \n");
			sql.append(" ORDER BY revisao, seq");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, revisaoI);
			stmt.setInt(2, revisaoF);
			stmt.setString(3, dataI);
			stmt.setString(4, dataF);

			// Executar o comando
			ResultSet rs = stmt.executeQuery();
			ProMergeHistoricoCommits dados = null;

			while (rs.next()) {
				// Inicializar o objeto
				dados = new ProMergeHistoricoCommits();

				// Setar as propriedades
				dados.setRevisao(rs.getInt(1));
				dados.setUsuario(rs.getString(2));
				dados.setSeq(rs.getInt(3));
				dados.setStatus(rs.getInt(4));
				dados.setDtHrCommit(rs.getTimestamp(5));
				dados.setMensagem(rs.getString(6));

				// Adicionar o registro
				lista.add(dados);

			}

		} catch (SQLException e) {
			log.error("Erro ao executar listarHistoricosCommits.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}

		// Retornar a lista de registros
		return lista;

	}

	/**
	 * Salva registro na tabela promerge_resumos_conflitos
	 * 
	 * 
	 * @param vo
	 * @param arquivoConflitante
	 * @param status
	 * @param comitado
	 * 
	 */
	public void saveOrUpdateResumoConflitos(ProMergeHistoricoAlteracoes arquivoConflitante, int status, boolean comitado) {

		// Busca registro por pk
		ProMergeResumosConflitos pk = buscarResumoConflitosPorPK(arquivoConflitante.getDesUsuario(), arquivoConflitante.getDesArquivo());

		if (pk != null) {

			pk.setTipConflito(status);
			pk.setComitado(comitado ? 0 : 1);
			pk.setSeveridade(arquivoConflitante.getSeveridade());

			atualizarResumoConflito(pk);

		} else {

			ProMergeResumosConflitos novoReg = new ProMergeResumosConflitos();

			novoReg.setDesUsuario(arquivoConflitante.getDesUsuario());
			novoReg.setDesArquivo(arquivoConflitante.getDesArquivo());
			novoReg.setComitado(comitado ? 0 : 1);
			novoReg.setTipConflito(status);
			novoReg.setSeveridade(arquivoConflitante.getSeveridade());

			inserirResumoConflito(novoReg);

		}
	}

	/**
	 * Insere registro na tabela de alterações
	 * 
	 * @param vo
	 * @param parametro
	 */
	public void inserirHistoricoMetodos(ProMergeHistoricoMetodos vo) {
		PreparedStatement stmt = null;
		try {
			StringBuilder sql = new StringBuilder();

			sql.append("INSERT INTO promerge_historico_metodos(\n");
			sql.append("   cod_historico     \n");
			sql.append(" , des_metodo        \n");
			sql.append(" , num_linha_inicial \n");
			sql.append(" , num_linha_final   \n");
			sql.append(" , seq  			 \n");
			sql.append(" , classe)	    	 \n");
			sql.append(" VALUES(?, ?, ?, ?, ?, ?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, vo.getCodHistorico());
			stmt.setString(2, vo.getDesMetodo());
			stmt.setInt(3, vo.getNumLinhaInicial());
			stmt.setInt(4, vo.getNumLinhaFinal());
			stmt.setInt(5, vo.getSequencial());
			stmt.setString(6, vo.getNomeClasse());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar inserir registro na base de dados.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * Atualizar registro na tabela de alterações
	 * 
	 * @param vo
	 * @param parametro
	 */
	public void atualizarHistoricoMetodos(ProMergeHistoricoMetodos vo) {
		PreparedStatement stmt = null;
		try {

			StringBuilder sql = new StringBuilder();

			sql.append("UPDATE promerge_historico_metodos \n");
			sql.append("   SET num_linha_inicial = ?,     \n");
			sql.append("       num_linha_final   = ?      \n");
			sql.append(" WHERE cod_historico     = ?      \n");
			sql.append("   AND lower(des_metodo) = lower(?)");
			sql.append("   AND lower(classe)     = lower(?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, vo.getNumLinhaInicial());
			stmt.setInt(2, vo.getNumLinhaFinal());
			stmt.setInt(3, vo.getCodHistorico());
			stmt.setString(4, vo.getDesMetodo().trim());
			stmt.setString(5, vo.getNomeClasse().trim());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar alterar registros na base de dados.", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * Busca resumo conflito por pk
	 * 
	 * @param vo
	 */
	public ProMergeHistoricoMetodos buscarHistoricoMetodosPorPK(Integer codHistorico, String desMetodo) {

		PreparedStatement stmt = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_historico,    		  \n");
			sql.append("       des_metodo,      		  \n");
			sql.append("       num_linha_inicial,         \n");
			sql.append("       num_linha_final,           \n");
			sql.append("       classe			          \n");
			sql.append("  FROM promerge_historico_metodos \n");
			sql.append(" WHERE cod_historico     = ?      \n ");
			sql.append("   AND lower(des_metodo) = lower(?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, codHistorico);
			stmt.setString(2, desMetodo.trim());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				ProMergeHistoricoMetodos retorno = new ProMergeHistoricoMetodos();

				retorno.setCodHistorico(rs.getInt(1));
				retorno.setDesMetodo(rs.getString(2));
				retorno.setNumLinhaInicial(rs.getInt(3));
				retorno.setNumLinhaFinal(rs.getInt(4));
				retorno.setNomeClasse(rs.getString(5));

				return retorno;
			}

			return null;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarHistoricoMetodosPorPK.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * 
	 * @param linha
	 * @param classe
	 * @return
	 */
	public ProMergeHistoricoMetodos buscarHistoricoMetodo(Integer linha, String classe) {

		PreparedStatement stmt = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT des_metodo, classe \n");
			sql.append("  FROM promerge_historico_metodos \n");
			sql.append(" WHERE " + linha + " BETWEEN num_linha_inicial AND num_linha_final \n");
			sql.append("   AND lower(classe) LIKE lower('%" + classe.trim() + "%')");
			sql.append("   AND cod_historico IN (SELECT MAX(H.cod_historico)\n");
			sql.append("                           FROM promerge_historico_metodos H \n");
			sql.append("                          WHERE lower(classe) LIKE lower('%" + classe.trim() + "%'))");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				ProMergeHistoricoMetodos retorno = new ProMergeHistoricoMetodos();

				retorno.setDesMetodo(rs.getString(1));
				retorno.setNomeClasse(rs.getString(2));

				return retorno;
			}

			return null;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarHistoricoMetodo.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	public List<ProMergeHistoricoMetodos> buscarHistoricoMetodosPorCodigo(Integer codHistorico) {

		PreparedStatement stmt = null;
		List<ProMergeHistoricoMetodos> listaRetorno = new ArrayList<ProMergeHistoricoMetodos>(0);

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_historico,    \n");
			sql.append("       des_metodo,       \n");
			sql.append("       num_linha_inicial,\n");
			sql.append("       num_linha_final,  \n");
			sql.append("       classe			 \n");
			sql.append("  FROM promerge_historico_metodos \n");
			sql.append(" WHERE cod_historico = ? ");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, codHistorico);

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				ProMergeHistoricoMetodos retorno = new ProMergeHistoricoMetodos();

				retorno.setCodHistorico(rs.getInt(1));
				retorno.setDesMetodo(rs.getString(2));
				retorno.setNumLinhaInicial(rs.getInt(3));
				retorno.setNumLinhaFinal(rs.getInt(4));
				retorno.setNomeClasse(rs.getString(5));

				// Adicionar o método
				listaRetorno.add(retorno);
			}

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarHistoricoMetodosPorPK.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return listaRetorno;
	}

	/**
	 * Insere registro na tabela de alterações
	 * 
	 * @param vo
	 * @param parametro
	 */
	public void inserirHistoricoCommit(ProMergeHistoricoCommits vo) {
		PreparedStatement stmt = null;
		try {

			// Limitar a mensagem de erro em 30000 caracteres
			String _mensagem = vo.getMensagem();
			if (_mensagem.length() > 30000)
				_mensagem = _mensagem.substring(0, 30000);

			StringBuilder sql = new StringBuilder();

			sql.append("INSERT INTO ProMerge_Historico_Commits(\n");
			sql.append(" revisao,    \n");
			sql.append(" usuario,    \n");
			sql.append(" seq,        \n");
			sql.append(" status,     \n");
			sql.append(" dtHrCommit, \n");
			sql.append(" mensagem)   \n");
			sql.append(" VALUES(?, ?, ?, ?, Now(), ?)");

			// Comando de inserção dos registros
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, vo.getRevisao());
			stmt.setString(2, vo.getUsuario().trim());
			stmt.setInt(3, vo.getSeq());
			stmt.setInt(4, vo.getStatus());
			stmt.setString(5, _mensagem.trim());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar inserir registro na base de dados.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * Buscar a conplexidade do conflito por PK
	 * 
	 * 1 - Baixa ( < 50 pontos) 2 - Média ( > 50 E <= 100 pontos) 3 - Alta ( >
	 * 100 E <= 300 pontos) 4 - Grave ( > 300 pontos)
	 * 
	 * @param _classe
	 * @param _metodo
	 * @param _qtdVezes
	 * @return
	 */
	public ProMergeAvaliacaoComplexidade buscarComplexidadePorPK(String _classe, String _metodo) {

		// Se não foi passado algum dos parâmetros retorna Complexidade baixa
		if (_classe.trim() == "" || _metodo.trim() == "") {
			return null;
		}

		// Não reconhece o caminho da "barra" na consulta
		int _pos = _classe.lastIndexOf("\\") + 1;
		if (_pos == 0)
			_pos = _classe.lastIndexOf("/") + 1;

		PreparedStatement stmt = null;
		ProMergeAvaliacaoComplexidade dados = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT classe,         \n");
			sql.append("       metodo,         \n");
			sql.append("       complexidade,   \n");
			sql.append("       qtdreferencias, \n");
			sql.append("       qtdpontos       \n");
			sql.append("  FROM promerge_avaliacao_complexidade \n");
			sql.append(" WHERE lower(classe) LIKE lower('%" + _classe.trim().substring(_pos) + "%') \n");
			// sql.append("   AND lower(metodo) LIKE lower('%" + _metodo.trim()
			// + "%')");
			sql.append("   AND lower(metodo) LIKE lower('" + _metodo.trim() + "%')");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			// Se encontrou registro (Retorna os dados)
			if (rs.next()) {
				dados = new ProMergeAvaliacaoComplexidade();

				// Atribuir as propriedades
				dados.setClasse(rs.getString(1));
				dados.setMetodo(rs.getString(2));
				dados.setSeveridade(rs.getInt(3));
				dados.setQtdOcorrencias(rs.getInt(4));
				dados.setQtdPontos(rs.getInt(5));

			}

		} catch (SQLException e) {
			log.error("Erro ao executar ProMergeAvaliacaoComplexidade().", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}

		return dados;

	}

	/**
	 * 
	 * @param _classe
	 *            - Nome da Classe
	 * @param _metodo
	 *            - Nome do Método
	 * 
	 * @return - Retorna último histórico do método
	 * 
	 */
	public ProMergeAvaliacaoComplexidade buscarHistoricoMetodoPorComplexidade(String _classe, String _metodo) {
		// Se não foi passado algum dos parâmetros retorna Complexidade baixa
		if (_classe.trim() == "" || _metodo.trim() == "") {
			return null;
		}

		// Não reconhece o caminho da "barra" na consulta
		int _pos = _classe.lastIndexOf("\\") + 1;
		if (_pos == 0)
			_pos = _classe.lastIndexOf("/") + 1;

		PreparedStatement stmt = null;
		ProMergeAvaliacaoComplexidade dados = null;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT cod_historico,     \n");
			sql.append("       seq,               \n");
			sql.append("       des_metodo,        \n");
			sql.append("       num_linha_inicial, \n");
			sql.append("       num_linha_final,   \n");
			sql.append("       classe             \n");
			sql.append("  FROM promerge_historico_metodos \n");
			sql.append(" WHERE LOWER(des_metodo) LIKE LOWER('%" + _metodo.trim() + "%') \n");
			sql.append("   AND LOWER(classe)     LIKE LOWER('%" + _classe.trim().substring(_pos) + "%') \n");
			sql.append("   AND cod_historico IN (SELECT MAX(x.cod_historico)         \n");
			sql.append("                           FROM promerge_historico_metodos x \n");
			sql.append("                          WHERE LOWER(x.des_metodo) LIKE LOWER('%" + _metodo.trim() + "%') \n");
			sql.append("                            AND LOWER(x.classe)     LIKE LOWER('%" + _classe.trim().substring(_pos) + "%') \n");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			// Se encontrou registro (Retorna os dados)
			if (rs.next()) {
				dados = new ProMergeAvaliacaoComplexidade();

				// Atribuir as propriedades
				dados.setClasse(rs.getString(1));
				dados.setMetodo(rs.getString(2));
				dados.setSeveridade(rs.getInt(3));
				dados.setQtdOcorrencias(rs.getInt(4));
				dados.setQtdPontos(rs.getInt(5));

			}

		} catch (SQLException e) {
			log.error("Erro ao executar ProMergeAvaliacaoComplexidade().", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}

		return dados;

	}

	/**
	 * 
	 * @param vo
	 */
	public void insertComplexidade(ProMergeAvaliacaoComplexidade vo) {
		PreparedStatement stmt = null;
		try {

			StringBuilder sql = new StringBuilder();

			sql.append("INSERT INTO promerge_avaliacao_complexidade( \n");
			sql.append(" classe,        \n");
			sql.append(" metodo,        \n");
			sql.append(" complexidade,  \n");
			sql.append(" qtdreferencias,\n");
			sql.append(" qtdpontos)     \n");
			sql.append(" VALUES(?, ?, ?, ?, ?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setString(1, vo.getClasse().trim());
			stmt.setString(2, vo.getMetodo().trim());
			stmt.setInt(3, vo.getSeveridade());
			stmt.setInt(4, vo.getQtdOcorrencias());
			stmt.setDouble(5, vo.getQtdPontos());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar executar inserirComplexidade.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * 
	 * @param vo
	 */
	public void updateComplexidadePorPK(ProMergeAvaliacaoComplexidade vo) {
		PreparedStatement stmt = null;
		try {

			StringBuilder sql = new StringBuilder();

			sql.append("UPDATE promerge_avaliacao_complexidade \n");
			sql.append("   SET qtdreferencias = ? \n");
			sql.append("     , complexidade   = ? \n");
			sql.append("     , qtdpontos      = ? \n");
			sql.append(" WHERE lower(classe)  = lower(?) \n");
			sql.append("   AND lower(metodo)  = lower(?)");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Parâmetros
			stmt.setInt(1, vo.getQtdOcorrencias());
			stmt.setInt(2, vo.getSeveridade());
			stmt.setDouble(3, vo.getQtdPontos());
			stmt.setString(4, vo.getClasse().trim());
			stmt.setString(5, vo.getMetodo().trim());

			// Executar o comando
			stmt.execute();

		} catch (SQLException e) {
			log.error("Erro ao tentar executar updateComplexidadePorPK.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}

	}

	/**
	 * 
	 * @param conteudo
	 * @return
	 */
	public ArrayList<String> buscarChamadores(String conteudo) {

		PreparedStatement stmt = null;
		ArrayList<String> lista = new ArrayList<String>(0);

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT distinct(A.des_arquivo) \n");
			sql.append("  FROM ProMerge_historico_alteracoes A \n");
			sql.append(" WHERE lower(a.txt_arquivo) LIKE lower('%." + conteudo.trim() + "(%') \n");
			sql.append(" ORDER BY A.des_arquivo");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				lista.add(rs.getString(1));
			}

			return lista;

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarChamadores.", e);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return null;
	}

	/**
	 * 
	 * @param metodos
	 */
	public void saveOrUpdateMetodosComplexidade(List<ProMergeAvaliacaoComplexidade> metodos) {

		if (metodos == null || metodos.isEmpty()) {
			return;
		}

		for (ProMergeAvaliacaoComplexidade metodo : metodos) {
			// Busca registro por pk
			ProMergeAvaliacaoComplexidade pk = buscarComplexidadePorPK(metodo.getClasse(), metodo.getMetodo());

			// Se existir, atualiza o registro
			// -----------------------------------------------------------------------------------------------------------------------
			// OBSERVAÇÃO: A pontuação da complecidade poderá ter sido alterada,
			// para isto, será calculado o valor a partir dos dados
			// existentes
			if (pk != null) {
				// Pontuação por grau de complexidade
				double _valorMedio = (pk.getQtdPontos() / pk.getQtdOcorrencias());
				double _qtdPontos = (metodo.getQtdOcorrencias() * _valorMedio);

				// Atualizar a quantidade de pontos com base no registro já
				// existente
				metodo.setQtdPontos(_qtdPontos);
				metodo.setSeveridade(definirSeveridade(_qtdPontos));

				// Atualizar o registro
				updateComplexidadePorPK(metodo);

			} else {

				int _qtdOcorrencias = metodo.getQtdOcorrencias();

				if (_qtdOcorrencias == 0) {
					metodo.setQtdOcorrencias(1);
					metodo.setQtdPontos(50);
				}

				// Definir a Seveidade pelo total de pontos
				metodo.setSeveridade(definirSeveridade(metodo.getQtdPontos()));

				// Inserir o registro
				insertComplexidade(metodo);

			}

		}

	}

	/**
	 * 
	 * @param _qtdPontos
	 * @return
	 */
	private static int definirSeveridade(double _qtdPontos) {
		// Definição da severidade
		int _severidade = 1; // Baixa
		if (_qtdPontos > 50 && _qtdPontos <= 200)
			_severidade = 2; // Média
		else if (_qtdPontos > 200 && _qtdPontos <= 400)
			_severidade = 3; // Alta
		else if (_qtdPontos > 400)
			_severidade = 4; // Grave

		return _severidade;
	}

	/**
	 * Salva registro na tabela promerge_resumos_conflitos
	 * 
	 * 
	 * @param vo
	 * @param arquivoConflitante
	 * @param status
	 * @param comitado
	 */
	public void saveOrUpdateMetodosConflitos(ProMergeHistoricoAlteracoes alteracao, List<ProMergeHistoricoMetodos> metodos) {

		if (metodos == null || metodos.isEmpty()) {
			return;
		}

		for (ProMergeHistoricoMetodos metodo : metodos) {

			// Busca registro por pk
			ProMergeHistoricoMetodos pk = buscarHistoricoMetodosPorPK(metodo.getCodHistorico(), metodo.getDesMetodo());

			if (pk != null) {

				atualizarHistoricoMetodos(pk);

			} else {

				metodo.setCodHistorico(alteracao.getCodHistorico());

				inserirHistoricoMetodos(metodo);
			}

		}

	}

	/**
	 * 
	 * @param vo
	 * @param metodosVO
	 * @return
	 */
	public int buscarHistoricoOriginalClasse(String pacoteEClasse) {

		// 1) Esta consulta deverá buscar registros da
		// ProMergeHistoricoAlteracoes que possuam os métodos informados no
		// parâmetro
		PreparedStatement stmt = null;
		int codHistorico = 0;

		try {

			StringBuilder sql = new StringBuilder();

			sql.append("SELECT DISTINCT a.cod_historico as cod_historico \n");
			sql.append("  FROM promerge_historico_alteracoes a           \n");
			sql.append("       INNER JOIN promerge_historico_metodos b   \n");
			sql.append("       ON (a.cod_historico = b.cod_historico) 	 \n");
			sql.append(" WHERE lower(a.des_usuario) = lower('sistemas')  \n");
			sql.append(" AND lower(b.des_metodo) = lower('" + pacoteEClasse + "')");

			// Preparar o comando
			stmt = getConnection().prepareStatement(sql.toString());

			// Executar o comando
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// Inicializar o objeto
				codHistorico = rs.getInt(1);

			}

		} catch (SQLException e) {
			log.error("Erro ao buscar conflitos - buscarConflitosIndiretos.", e);

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
		return codHistorico;
	}

	public static DaoGenerico getInstance() throws Exception {

		if (dao == null) {
			dao = new DaoGenerico();
		}

		return dao;

	}
}