package madstodolist.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
public class DetalleUsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void detalleUsuarioMuestraDatosCorrectos() throws Exception {
        // GIVEN
        // Un usuario en el sistema
        Long usuarioId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(usuarioId);
        usuario.setEmail("test@example.com");
        usuario.setNombre("Usuario Test");
        usuario.setFechaNacimiento(parseDate("1990-01-01"));

        when(usuarioService.findById(usuarioId)).thenReturn(usuario);
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // Al acceder a la página de detalles del usuario, se muestran sus datos
        this.mockMvc.perform(get("/registrados/" + usuarioId))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Detalles del Usuario"),
                        containsString("test@example.com"),
                        containsString("Usuario Test"),
                        containsString("01/01/1990")
                )))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    public void detalleUsuarioConLoginMuestraNavbarCorrecto() throws Exception {
        // GIVEN
        // Un usuario logueado y un usuario a ver
        Long usuarioLogeadoId = 1L;
        Long usuarioVistoId = 2L;

        UsuarioData usuarioLogeado = new UsuarioData();
        usuarioLogeado.setId(usuarioLogeadoId);
        usuarioLogeado.setNombre("Usuario Logeado");
        usuarioLogeado.setEmail("logeado@example.com");

        UsuarioData usuarioVisto = new UsuarioData();
        usuarioVisto.setId(usuarioVistoId);
        usuarioVisto.setNombre("Usuario Visto");
        usuarioVisto.setEmail("visto@example.com");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioLogeadoId);
        when(usuarioService.findById(usuarioLogeadoId)).thenReturn(usuarioLogeado);
        when(usuarioService.findById(usuarioVistoId)).thenReturn(usuarioVisto);

        // WHEN, THEN
        // Al acceder a la página de detalles, se muestra el navbar con el usuario logueado
        this.mockMvc.perform(get("/registrados/" + usuarioVistoId))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Usuario Logeado"),
                        containsString("Tareas"),
                        containsString("Cuenta")
                )));
    }

    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(dateStr);
    }
} 