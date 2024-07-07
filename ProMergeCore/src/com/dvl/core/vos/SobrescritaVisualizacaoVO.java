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
public class SobrescritaVisualizacaoVO implements Serializable {

	// Propriedades
	private String nomeCompletoArquivo;
	private ProMergeResumosConflitos conflito;
	private String nomeUsuarioRequisitante;
	private String conteudoArquivoUsuario;

	public String getNomeCompletoArquivo() { 
		return nomeCompletoArquivo; 
	}

	public void setNomeCompletoArquivo(String nomeCompletoArquivo) {
		this.nomeCompletoArquivo = nomeCompletoArquivo;
	}

	public ProMergeResumosConflitos getConflito() {
		return conflito;
	}

	public void setConflito(ProMergeResumosConflitos conflito) {
		this.conflito = conflito;
	}

	public String getConteudoArquivoUsuario() {
		return conteudoArquivoUsuario;
	}

	public void setConteudoArquivoUsuario(String conteudoArquivoUsuario) {
		this.conteudoArquivoUsuario = conteudoArquivoUsuario;
	}

	public String getNomeUsuarioRequisitante() {
		return nomeUsuarioRequisitante;
	}

	public void setNomeUsuarioRequisitante(String nomeUsuarioRequisitante) {
		this.nomeUsuarioRequisitante = nomeUsuarioRequisitante;
	}
}