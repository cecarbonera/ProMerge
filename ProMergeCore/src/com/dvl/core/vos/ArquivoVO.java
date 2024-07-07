package com.dvl.core.vos;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ArquivoVO implements Serializable {
	private String nomeCompletoArquivo;
	private String conteudoArquivo;
	private String nomeUsuario;
	private String nomeProjeto;

	public String getNomeCompletoArquivo() {
		return nomeCompletoArquivo;
	}

	public void setNomeCompletoArquivo(String nomeCompletoArquivo) {
		this.nomeCompletoArquivo = nomeCompletoArquivo;
	}

	public String getConteudoArquivo() {
		return conteudoArquivo;
	}

	public void setConteudoArquivo(String conteudoArquivo) {
		this.conteudoArquivo = conteudoArquivo;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public String getNomeProjeto() {
		return nomeProjeto;
	}

	public void setNomeProjeto(String nomeProjeto) {
		this.nomeProjeto = nomeProjeto;
	}
}