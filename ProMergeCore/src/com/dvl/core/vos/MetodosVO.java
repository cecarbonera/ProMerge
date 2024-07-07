package com.dvl.core.vos;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class MetodosVO implements Serializable {
	// Propriedades
	private String nomeProjeto;
	private String nomeClasse;
	private String nomeMetodo;
	private Integer rangeInicial;
	private Integer rangeFinal;
	private List<MetodosVO> metodosInfluenciados;

	public String getNomeProjeto() {
		return nomeProjeto;
	}

	public void setNomeProjeto(String nomeProjeto) {
		this.nomeProjeto = nomeProjeto;
	}

	public String getNomeClasse() {
		return nomeClasse;
	}

	public void setNomeClasse(String nomeClasse) {
		this.nomeClasse = nomeClasse;
	}

	public String getNomeMetodo() {
		return nomeMetodo;
	}

	public void setNomeMetodo(String nomeMetodo) {
		this.nomeMetodo = nomeMetodo;
	}

	public Integer getRangeInicial() {
		return rangeInicial;
	}

	public void setRangeInicial(Integer rangeInicial) {
		this.rangeInicial = rangeInicial;
	}

	public Integer getRangeFinal() {
		return rangeFinal;
	}

	public void setRangeFinal(Integer rangeFinal) {
		this.rangeFinal = rangeFinal;
	}

	public List<MetodosVO> getMetodosInfluenciados() {
		return metodosInfluenciados;
	}

	public void setMetodosInfluenciados(List<MetodosVO> metodosInfluenciados) {
		this.metodosInfluenciados = metodosInfluenciados;
	}
}