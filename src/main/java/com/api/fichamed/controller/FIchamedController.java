package com.api.fichamed.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.api.fichamed.dto.FichamedDTO;
import com.api.fichamed.model.FichamedModel;
import com.api.fichamed.repository.FichaMedRepository;

@RestController
@RequestMapping("/paciente")
public class FIchamedController {
    @Autowired
    FichaMedRepository repository;
    
    public static boolean isAllNullOrBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return false;  // Retorna false se algum valor não for null ou não estiver em branco
            }
        }
        return true;  // Retorna true se todos os valores forem null ou estiverem em branco
    }

    
    @GetMapping
    public String mensagem(){
        return "Api Ficha Médica está funcionando! Verifique e este utilizando os endpoints.";
    }

    @GetMapping("listar")
    public List<FichamedModel> listarPacientes(){   
        return repository.findAll();
    }
    
    @GetMapping("/listar/{id}")
    public ResponseEntity<FichamedModel> listarUm(@PathVariable Long id){   
        return repository.findById(id)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado com ID: " + id));
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody FichamedDTO user){
    	FichamedModel paciente = new FichamedModel(user);
    	boolean resultado = isAllNullOrBlank(paciente.getNome(), paciente.getTelefone(), paciente.getGenero(), paciente.getNascimento(), paciente.getCpf());
    	if(resultado) {
    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha todos os campos obrgatórios."));
    	}
    }
   
}
