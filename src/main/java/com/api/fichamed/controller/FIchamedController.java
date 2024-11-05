package com.api.fichamed.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.api.fichamed.dto.FichamedDTO;
import com.api.fichamed.model.FichamedModel;
import com.api.fichamed.repository.FichamedRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("paciente")
public class FichamedController {

	private static String caminhoImagens = "C:\\Users\\teixe\\Documents\\imgs\\";

	@Autowired
	FichamedRepository repository;

	@GetMapping("/index")
	public String index() {
		return "index";
	}
	@GetMapping("/listar")
	public List<FichamedModel> listarPacientes() {
		return repository.findAll();
	}
	@GetMapping("/listar/{id}")
	public ResponseEntity<FichamedModel> listarUm(@PathVariable Long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado com ID: " + id));
	}

	@PostMapping("/cadastrar")
	public ResponseEntity<?> cadastrar(@Valid FichamedDTO user, BindingResult result,
			@RequestParam("file") MultipartFile arquivo) {
		FichamedModel paciente = new FichamedModel(user);
	
		if (repository.existsByCpf(paciente.getCpf())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CPF já cadastrado.");
		}
		if (repository.existsByEmail(paciente.getEmail())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado.");
		}
		if (repository.existsByTelefone(paciente.getTelefone())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Número já cadastrado.");
		}
		
		repository.save(paciente);

		try {
			if(!arquivo.isEmpty()) {
				byte[] bytes = arquivo.getBytes();
				Path caminho = Paths.get(caminhoImagens+String.valueOf(paciente.getId())+arquivo.getOriginalFilename());
				Files.write(caminho, bytes);
				
				paciente.setNomeImagem(String.valueOf(paciente.getId())+arquivo.getOriginalFilename());
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		try {
			repository.save(paciente);
			return ResponseEntity.status(HttpStatus.CREATED).body("Paciente cadastrado com sucesso.");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Algum campo já existe no banco de dados.");
		}
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<?> atualizar(@RequestBody FichamedDTO user, @PathVariable Long id) {
		FichamedModel paciente = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Erro ao atualizar: paciente não encontrado com ID: " + id));
		if (repository.existsByCpf(paciente.getCpf())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("CPF já cadastrado.");
		}
		if (repository.existsByEmail(paciente.getEmail())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado.");
		}
		if (repository.existsByTelefone(paciente.getTelefone())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Número já cadastrado.");
		}
		

		paciente.setNome(user.nome());
		paciente.setEmail(user.email());
		paciente.setTelefone(user.telefone());
		paciente.setCpf(user.cpf());
		paciente.setNomeImagem(user.nomeImagem());
		repository.save(paciente);
		return ResponseEntity.status(200).body("Paciente atualizado com sucesso.");
	}

	@DeleteMapping("/deletar/{id}")
	public ResponseEntity<?> deletar(@PathVariable Long id) {
		try {
			repository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado com ID: " + id));
			repository.deleteById(id);
			return ResponseEntity.status(200).body("Paciente deletado com sucesso.");
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
		}

	}
}
