package com.example.discoverbackend.servicesimpl;

import com.example.discoverbackend.dtos.AlquilerRequest;
import com.example.discoverbackend.dtos.AlquilerResponse;
import com.example.discoverbackend.entities.*;
import com.example.discoverbackend.repositories.AlquilerRepository;
import com.example.discoverbackend.repositories.InmuebleRepository;
import com.example.discoverbackend.repositories.UsuarioRepository;
import com.example.discoverbackend.services.AlquilerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AlquilerServiceImpl implements AlquilerService {

    @Autowired
    AlquilerRepository alquilerRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    InmuebleRepository inmuebleRepository;

    public Alquiler createAlquiler(AlquilerRequest alquiler) {
        Usuario usuario = usuarioRepository.findById(alquiler.getClient_id()).get();
        Inmueble inmueble = inmuebleRepository.findById(alquiler.getInmueble_id()).get();
        Alquiler newAlquiler = new Alquiler(usuario, inmueble, alquiler.getPrice(), alquiler.getTransactionDate(), true);
        Alquiler savedAlquiler = alquilerRepository.save(newAlquiler);
        savedAlquiler.getClient().setInmuebles(null);
        savedAlquiler.getClient().setOpiniones(null);
        for(RoleUser ru: savedAlquiler.getClient().getRoles()){
            ru.setUser(null);
            ru.getRole().setUsers(null);
        }

        savedAlquiler.getInmueble().getUbigeo().setInmuebleZonaList(null);
        savedAlquiler.getInmueble().setOpiniones(null);
        savedAlquiler.getInmueble().getUsuario().setInmuebles(null);
        savedAlquiler.getInmueble().getUsuario().setOpiniones(null);
        for(RoleUser ru: savedAlquiler.getInmueble().getUsuario().getRoles()){
            ru.setUser(null);
            ru.getRole().setUsers(null);
        }
        for(InmuebleCaracteristica ic: savedAlquiler.getInmueble().getCaracteristicaList()){
            ic.setInmueble(null);
            ic.getCaracteristica().getTipoCaracteristica().setCaracteristicas(null);
        }
        for (InmuebleFoto f : savedAlquiler.getInmueble().getInmuebleFotoList()) {
            f.setInmueble(null);
            f.getFoto().setInmuebleFotos(null);
        }
        return savedAlquiler;
    }

    @Override
    public List<AlquilerResponse> listAlquilerByUser(Long id) {
        List<AlquilerResponse> alquilerList = new ArrayList<>();
        List<Alquiler> alquileres = alquilerRepository.findByClient_Id(id);
        for (Alquiler a : alquileres) {
            String location = a.getInmueble().getAddress();
            String fullNameOwner = a.getInmueble().getUsuario().getFirstName() + a.getInmueble().getUsuario().getLastNameDad() + a.getInmueble().getUsuario().getLastNameMom();
            Double price = a.getPrice();
            Date transactionDate = a.getTransactionDate();
            Boolean active = a.getActivate();
            Long property_id = a.getInmueble().getId();

            alquilerList.add(new AlquilerResponse(location, fullNameOwner, price, transactionDate, active, property_id));
        }
        return alquilerList;
    }

    @Override
    public Alquiler updateAlquiler(Long id) {
        Alquiler alquiler = alquilerRepository.findById(id).get();
        alquiler.setActivate(!alquiler.getActivate());
        Alquiler newAlquiler = alquilerRepository.save(alquiler);
        newAlquiler.getClient().setInmuebles(null);
        newAlquiler.getClient().setOpiniones(null);
        newAlquiler.getClient().setRoles(null);
        newAlquiler.getInmueble().getUbigeo().setInmuebleZonaList(null);
        newAlquiler.getInmueble().setUsuario(null);
        newAlquiler.getInmueble().setOpiniones(null);
        newAlquiler.getInmueble().setCaracteristicaList(null);
        newAlquiler.getInmueble().setInmuebleFotoList(null);
        return newAlquiler;
    }
}
