package madstodolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import madstodolist.authentication.ManagerUserSession;
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
        model.addAttribute("usuario", idUsuario != null ? usuarioService.findById(idUsuario) : null);
        model.addAttribute("usuarios", usuarioService.findAll());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String detalleUsuario(@PathVariable(value="id") Long idUsuario, Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        model.addAttribute("usuario", idUsuarioLogeado != null ? usuarioService.findById(idUsuarioLogeado) : null);
        model.addAttribute("usuarioDetalle", usuarioService.findById(idUsuario));
        return "detalleUsuario";
    }
}