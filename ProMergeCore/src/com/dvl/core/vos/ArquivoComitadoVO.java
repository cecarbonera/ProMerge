package com.dvl.core.vos;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ArquivoComitadoVO implements Serializable {
	//Propriedades
	private String nomeCompletoArquivo;
	private String conteudoArquivo;
	private String nomeUsuario;
	private String revisao;

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

	public String getRevisao() {
		return revisao;
	}

	public void setRevisao(String revisao) {
		this.revisao = revisao;
	}
}