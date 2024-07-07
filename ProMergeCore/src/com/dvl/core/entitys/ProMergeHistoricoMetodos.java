package com.dvl.core.entitys;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ProMergeHistoricoMetodos implements Serializable {

	private Integer codHistorico;
	private String nomeProjeto;
	private String desMetodo;
	private Integer numLinhaInicial;
	private Integer numLinhaFinal;
	private Integer sequencial;
	private String nomeClasse;
	
	public Integer getCodHistorico() {
		return codHistorico;
	}

	public void setCodHistorico(Integer codHistorico) {
		this.codHistorico = codHistorico;
	}

	public String getDesMetodo() {
		return desMetodo;
	}

	public void setDesMetodo(String desMetodo) {
		this.desMetodo = desMetodo;
	}

	public Integer getNumLinhaInicial() {
		return numLinhaInicial;
	}

	public void setNumLinhaInicial(Integer numLinhaInicial) {
		this.numLinhaInicial = numLinhaInicial;
	}

	public Integer getNumLinhaFinal() {
		return numLinhaFinal;
	}

	public void setNumLinhaFinal(Integer numLinhaFinal) {
		this.numLinhaFinal = numLinhaFinal;
	}

	public String getNomeProjeto() {
		return nomeProjeto;
	}

	public void setNomeProjeto(String nomeProjeto) {
		this.nomeProjeto = nomeProjeto;
	}

	public Integer getSequencial() {
		return sequencial;
	}

	public void setSequencial(Integer Sequencial) {
		this.sequencial = Sequencial;
	}

	public String getNomeClasse() {
		return nomeClasse;
	}

	public void setNomeClasse(String nomeClasse) {
		this.nomeClasse = nomeClasse;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codHistorico == null) ? 0 : codHistorico.hashCode());
		result = prime * result + ((desMetodo == null) ? 0 : desMetodo.hashCode());
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
		ProMergeHistoricoMetodos other = (ProMergeHistoricoMetodos) obj;
		if (codHistorico == null) {
			if (other.codHistorico != null)
				return false;
		} else if (!codHistorico.equals(other.codHistorico))
			return false;
		if (desMetodo == null) {
			if (other.desMetodo != null)
				return false;
		} else if (!desMetodo.equals(other.desMetodo))
			return false;
		return true;
	}

}
