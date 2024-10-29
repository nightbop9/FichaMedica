package com.api.fichamed.dto;

import java.time.LocalDate;

public record FichamedDTO(String nome, String email, String telefone, String genero, LocalDate nascimento, String cpf, String medicamentos, String alergias, String imglink) {

}
