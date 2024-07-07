package com.dvl.core.entitys;

import java.sql.Timestamp;

public class ProMergeHistoricoCommits {

	// Propriedades da classe
	private Integer revisao;
	private String usuario;
	private Integer seq;
	private Integer status;
	private Timestamp dtHrCommit;
	private String mensagem;

	public Integer getRevisao() {
		return revisao;
	}

	public void setRevisao(Integer revisao) {
		this.revisao = revisao;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Timestamp getDtHrCommit() {
		return dtHrCommit;
	}

	public void setDtHrCommit(Timestamp timestamp) {
		this.dtHrCommit = timestamp;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
}