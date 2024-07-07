package com.dvl.core.entitys;

import java.io.Serializable;
import java.util.List;
import com.dvl.core.vos.MetodosVO;

@SuppressWarnings("serial")
public class ProMergeHistoricoAlteracoes implements Serializable {

	private Integer codHistorico;
	private String desArquivo;
	private String desUsuario;
	private String txtArquivo;
	private List<MetodosVO> listaMetodos;
	private boolean conflitou;
	private String arquivoResultanteIntegracao;
	private int severidade;

	public Integer getCodHistorico() {
		return codHistorico;
	}

	public void setCodHistorico(Integer codHistorico) {
		this.codHistorico = codHistorico;
	}

	public String getDesArquivo() {
		return desArquivo;
	}

	public void setDesArquivo(String desArquivo) {
		this.desArquivo = desArquivo;
	}

	public String getTxtArquivo() {
		return txtArquivo;
	}

	public void setTxtArquivo(String txtArquivo) {
		this.txtArquivo = txtArquivo;
	}

	public String getDesUsuario() {
		return desUsuario;
	}

	public void setDesUsuario(String desUsuario) {
		this.desUsuario = desUsuario;
	}

	public List<MetodosVO> getListaMetodos() {
		return listaMetodos;
	}

	public void setListaMetodos(List<MetodosVO> listaMetodos) {
		this.listaMetodos = listaMetodos;
	}

	public boolean isConflitou() {
		return conflitou;
	}

	public void setConflitou(boolean conflitou) {
		this.conflitou = conflitou;
	}

	public String getArquivoResultanteIntegracao() {
		return arquivoResultanteIntegracao;
	}

	public void setArquivoResultanteIntegracao(String arquivoResultanteIntegracao) {
		this.arquivoResultanteIntegracao = arquivoResultanteIntegracao;
	}

	public int getSeveridade() {
		return severidade;
	}

	public void setSeveridade(int severidade) {
		this.severidade = severidade;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codHistorico == null) ? 0 : codHistorico.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProMergeHistoricoAlteracoes other = (ProMergeHistoricoAlteracoes) obj;
		if (codHistorico == null) {
			if (other.codHistorico != null)
				return false;
		} else if (!codHistorico.equals(other.codHistorico))
			return false;
		return true;
	}

}
