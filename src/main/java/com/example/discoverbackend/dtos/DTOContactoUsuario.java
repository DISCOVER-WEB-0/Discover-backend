package com.example.discoverbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class DTOContactoUsuario {
    private Long id;
    private String fullName;
    private String telephone;
    private String email;
    private String dateAfiiliation;
    private String dateBirth;
    private String linkFotoPerfil;

}
