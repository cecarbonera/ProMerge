package com.dvl.core.entitys;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ProMergeResumosConflitos implements Serializable {

	// Propriedades da classe
	private Integer codConflito;
	private String desUsuario;
	private String desArquivo;
	private Integer tipConflito;
	private Integer comitado;
	private Integer severidade;
	private String dtaAlteracao;

	public Integer getCodConflito() {
		return codConflito;
	}

	public void setCodConflito(Integer codConflito) {
		this.codConflito = codConflito;
	}

	public String getDesUsuario() {
		return desUsuario;
	}

	public void setDesUsuario(String desUsuario) {
		this.desUsuario = desUsuario;
	}

	public String getDesArquivo() {
		return desArquivo;
	}

	public void setDesArquivo(String desArquivo) {
		this.desArquivo = desArquivo;
	}

	public Integer getTipConflito() {
		return tipConflito;
	}

	public void setTipConflito(Integer tipConflito) {
		this.tipConflito = tipConflito;
	}

	public Integer getComitado() {
		return comitado;
	}

	public void setComitado(Integer comitado) {
		this.comitado = comitado;
	}

	public Integer getSeveridade() {
		return severidade;
	}

	public void setSeveridade(Integer severidade) {
		this.severidade = severidade;
	}

	public String getDtaAlteracao() {
		return dtaAlteracao;
	}

	public void setDtaAlteracao(String dtaAlteracao) {
		this.dtaAlteracao = dtaAlteracao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((codConflito == null) ? 0 : codConflito.hashCode());

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

		ProMergeResumosConflitos other = (ProMergeResumosConflitos) obj;

		if (codConflito == null) {
			if (other.codConflito != null)
				return false;

		} else if (!codConflito.equals(other.codConflito))
			return false;

		return true;
	}

}
