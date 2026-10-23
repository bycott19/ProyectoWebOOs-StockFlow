package cl.baeza_munoz.stockflow.service;

import cl.baeza_munoz.stockflow.exception.ResourceNotFoundException;
import cl.baeza_munoz.stockflow.model.Cliente;
import cl.baeza_munoz.stockflow.repository.ClienteRepository;
import cl.baeza_munoz.stockflow.util.RutValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Transactional
    public Cliente crearCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (cliente.getRut() == null || cliente.getRut().isBlank()) {
            throw new IllegalArgumentException("El RUT del cliente es obligatorio.");
        }
        if (!RutValidator.esValido(cliente.getRut())) {
            throw new IllegalArgumentException("El RUT ingresado ('" + cliente.getRut() + "') no es válido según el algoritmo chileno Módulo 11.");
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo del cliente es obligatorio.");
        }

        // Formatear RUT estandarizado (ej: 12.345.678-5)
        cliente.setRut(RutValidator.formatear(cliente.getRut()));

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizarCliente(Long id, Cliente datos) {
        Cliente cliente = obtenerPorId(id);
        if (datos.getRut() != null && !datos.getRut().isBlank()) {
            if (!RutValidator.esValido(datos.getRut())) {
                throw new IllegalArgumentException("El RUT ingresado ('" + datos.getRut() + "') no es válido según el algoritmo chileno Módulo 11.");
            }
            cliente.setRut(RutValidator.formatear(datos.getRut()));
        }
        cliente.setNombre(datos.getNombre());
        cliente.setEmail(datos.getEmail());
        cliente.setTelefono(datos.getTelefono());
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = obtenerPorId(id);
        clienteRepository.delete(cliente);
    }

    public List<Cliente> buscar(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }
        return clienteRepository.findByNombreContainingIgnoreCaseOrRutContainingIgnoreCase(termino, termino);
    }
}
