package com.cuidar.api.controller;

import com.cuidar.domain.Exceptions.PacienteNotFoundException;
import com.cuidar.domain.model.PacienteED.PacienteED;
import com.cuidar.domain.service.PacienteRN.PacienteRN;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PacienteREST {


    @Autowired
    private PacienteRN pacienteRN;
    public PacienteREST(PacienteRN pacienteRN) {
        this.pacienteRN = pacienteRN;
    }

    @Operation(summary = "Obtem paciente por id")
    @GetMapping("/{pacienteId}")
    public ResponseEntity<PacienteED> obterPacientePorId(@PathVariable("pacienteId") Long id) {
        return ResponseEntity.ok(pacienteRN.obterPacientePorId(id).get());
    }


    @Operation(summary = "Obtem todos os pacientes, ativos ou não")
    @GetMapping
    public ResponseEntity<List<PacienteED>> obterTodosPacientes() {
        return ResponseEntity.ok(pacienteRN.obterTodosPacientes());
    }

    @Operation(summary = "Obtem todos os pacientes por isAtivo, true ou falso ")
    @GetMapping("/isAtivo/{isAtivo}")
    public ResponseEntity<List<PacienteED>> obterTodosPacientesAtivos(@PathVariable("isAtivo") Boolean isAtivo) {
        return ResponseEntity.ok(pacienteRN.obterTodosPacientesAtivos(isAtivo));
    }

    @Operation(summary = "Obtem todos os pacientes, por nome")
    @GetMapping("/nomePaciente/{nome}")
    public ResponseEntity<List<PacienteED>> obterTodosPacientesPorNome(@PathVariable("nome") String nome) {
        return ResponseEntity.ok(pacienteRN.obterTodosPacientesPorNome(nome));
    }

    @Operation(summary = "Obtem todos os pacientes, por nome do exame")
    @GetMapping("/nomeExame/{nome}")
    public ResponseEntity<List<PacienteED>> obterTodosPacientesPorExame(@PathVariable("nomeExame") String nomeExame) {
        return ResponseEntity.ok(pacienteRN.obterTodosPacientesPorExame(nomeExame));
    }

    @Operation(summary = "Obtem todos os pacientes, por nome da rua")
    @GetMapping("/nomeRua/{nome}")
    public ResponseEntity<List<PacienteED>> obterTodosPacientesPorEndereco(@PathVariable("nomeRua") String nomeRua) {
        return ResponseEntity.ok(pacienteRN.obterTodosPacientesPorEndereco(nomeRua));
    }


    @Operation(summary = "Cadastra um novo paciente")
    @PostMapping
    public ResponseEntity<PacienteED> cadastrarPaciente(@Valid @RequestBody PacienteED pacienteED) {
        return ResponseEntity.ok(pacienteRN.salvarPaciente(pacienteED));
    }

    @Operation(summary = "atualiza um paciente")
    @PutMapping
    public ResponseEntity<PacienteED> atualizarPaciente(@Valid @RequestBody PacienteED pacienteED) {
        PacienteED pacienteBanco = pacienteRN.obterPacientePorId(pacienteED.getId())
                .orElseThrow(() -> new PacienteNotFoundException("", pacienteED.getId()));

//        BeanUtils.copyProperties(pacienteBanco, pacienteED,
//                "id", "idade", "endereco","atendente","medicoAtendente","exames","local", "dataCadastro");

        return ResponseEntity.ok(pacienteRN.salvarPaciente(pacienteED));
    }




    @Operation(summary = "Desativa um paciente, delete lógico")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaciente(@PathVariable Long id) {
        pacienteRN.deletarLogicoPaciente(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}