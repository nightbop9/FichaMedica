package com.api.fichamed.model;

import com.api.fichamed.dto.FichamedDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
@Entity
@Table(name = "paciente")
public class FichamedModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
	@NotBlank(message = "O campo 'nome' é obrigatório.")
    private String nome;
	@Column(unique = true)
	private String email;
	@Column(unique = true)
	@NotBlank(message = "O campo 'telefone' é obrigatório.")
    private String telefone;
	@Column(unique = true)
	@NotBlank(message = "O campo cpf é obrigatório.")
    private String cpf;
	@Column(name = "nome_imagem")
	private String nomeImagem;
	public FichamedModel(){}
    public FichamedModel(FichamedDTO paciente) {
		this.id = paciente.id();
        this.nome = paciente.nome();
        this.telefone = paciente.telefone();
        this.email = paciente.email();
        this.cpf = paciente.cpf();
        this.nomeImagem = paciente.nomeImagem();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getNomeImagem() {
		return nomeImagem;
	}
	public void setNomeImagem(String nomeImagem) {
		this.nomeImagem = nomeImagem;
	}
    
}
