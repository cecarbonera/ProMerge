package com.dvl.core.vos;

import java.io.Serializable;

import com.dvl.core.entitys.ProMergeResumosConflitos;

/**
 * VO de transporte para ação Integração
 * 
 * @author daniel.armino
 * 
 */
@SuppressWarnings("serial")
public class IntegracaoVO implements Serializable {

	// Propriedades
	private String nomeCompletoArquivo;
	private String conteudoArquivo;
	private ProMergeResumosConflitos conflito;
	private String nomeUsuarioRequisicao;
	private String conteudoArquivoIntegrado;

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

	public ProMergeResumosConflitos getConflito() {
		return conflito;
	}

	public void setConflito(ProMergeResumosConflitos conflito) {
		this.conflito = conflito;
	}

	public String getConteudoArquivoIntegrado() {
		return conteudoArquivoIntegrado;
	}

	public void setConteudoArquivoIntegrado(String conteudoArquivoIntegrado) {
		this.conteudoArquivoIntegrado = conteudoArquivoIntegrado;
	}

	public String getNomeUsuarioRequisicao() {
		return nomeUsuarioRequisicao;
	}

	public void setNomeUsuarioRequisicao(String nomeUsuarioRequisicao) {
		this.nomeUsuarioRequisicao = nomeUsuarioRequisicao;
	}
}