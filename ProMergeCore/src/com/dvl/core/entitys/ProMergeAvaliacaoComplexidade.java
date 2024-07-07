package com.dvl.core.entitys;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ProMergeAvaliacaoComplexidade implements Serializable {

	private String classe;
	private String metodo;
	private int severidade;
	private int qtdOcorrencias;
	private double qtdPontos;

	public String getClasse() {
		return classe;
	}

	public void setClasse(String _classe) {
		this.classe = _classe;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String _metodo) {
		this.metodo = _metodo;
	}

	public int getSeveridade() {
		return severidade;
	}

	public void setSeveridade(int _severidade) {
		this.severidade = _severidade;
	}

	public int getQtdOcorrencias() {
		return qtdOcorrencias;
	}

	public void setQtdOcorrencias(int _qtdOcorrencias) {
		this.qtdOcorrencias = _qtdOcorrencias;
	}

	public double getQtdPontos() {
		return qtdPontos;
	}

	public void setQtdPontos(double _qtdpontos) {
		this.qtdPontos = _qtdpontos;
	}
}