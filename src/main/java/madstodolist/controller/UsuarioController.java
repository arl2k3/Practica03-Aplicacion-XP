package madstodolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }
        
        UsuarioData usuario = usuarioService.findById(idUsuario);
        if (!usuario.isEsAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarios", usuarioService.findAll());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String detalleUsuario(@PathVariable(value="id") Long idUsuario, Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuarioLogeado = usuarioService.findById(idUsuarioLogeado);
        if (!usuarioLogeado.isEsAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        model.addAttribute("usuario", usuarioLogeado);
        model.addAttribute("usuarioDetalle", usuarioService.findById(idUsuario));
        return "detalleUsuario";
    }

    @PostMapping("/registrados/{id}/bloquear")
    public String cambiarEstadoBloqueo(@PathVariable(value="id") Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuarioLogeado = usuarioService.findById(idUsuarioLogeado);
        if (!usuarioLogeado.isEsAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        usuarioService.cambiarEstadoBloqueo(idUsuario);
        return "redirect:/registrados";
    }
}