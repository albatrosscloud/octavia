package pe.albatross.octavia.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Alumno {

    private Long id;
    private String codigo;
    private Persona persona;
    private Facultad facultad;
    private EstadoAcademico estadoAcademico;
}
