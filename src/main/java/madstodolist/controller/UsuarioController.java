package madstodolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import madstodolist.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String detalleUsuario(@PathVariable(value="id") Long idUsuario, Model model) {
        model.addAttribute("usuario", usuarioService.findById(idUsuario));
        return "detalleUsuario";
    }
}