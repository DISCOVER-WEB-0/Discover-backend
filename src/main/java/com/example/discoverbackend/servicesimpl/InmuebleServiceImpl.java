package com.example.discoverbackend.servicesimpl;

import com.example.discoverbackend.dtos.*;
import com.example.discoverbackend.entities.*;
import com.example.discoverbackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.discoverbackend.services.InmuebleService;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class InmuebleServiceImpl implements InmuebleService {

    @Autowired
    InmuebleRepository inmuebleRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    UbigeoRepository ubigeoRepository;
    @Autowired
    CaracteristicaRepository caracteristicaRepository;
    @Autowired
    InmuebleCaracteristicaRepository inmuebleCaracteristicaRepository;
    @Autowired
    InmuebleFotoRepository inmuebleFotoRepository;
    @Autowired
    UsuarioServiceImpl usuarioService;
    @Autowired
    AlquilerRepository alquilerRepository;
    @Autowired
    OpinionRepository opinionRepository;

    @Autowired
    FotoRepository fotoRepository;
    public List<PrincipalInmueblesResponse> listAll(){
        List<PrincipalInmueblesResponse> propertiesResponse = new ArrayList<PrincipalInmueblesResponse>();
        List<Inmueble> properties = inmuebleRepository.findAll();
        for(Inmueble i: properties){
            String linkPhotoUser = i.getUsuario().getLinkPhotoProfile();
            String fullName = i.getUsuario().getFirstName() + i.getUsuario().getLastNameDad() + i.getUsuario().getLastNameMom();
            String province = i.getUbigeo().getProvincia();
            String department = i.getUbigeo().getDepartamento();
            String district = i.getUbigeo().getDistrito();
            String linkPhotoProperty = i.getInmuebleFotoList().get(0).getFoto().getPhotoLink();
            Double price = i.getPrice();
            Integer squareMeter = i.getSquareMeter();
            Integer numBedrooms = i.getNumBedrooms();
            Integer numBathrooms = i.getNumBathrooms();
            String description = i.getDescription();
            propertiesResponse.add(new PrincipalInmueblesResponse(linkPhotoUser, fullName, province, department, district, linkPhotoProperty, price, squareMeter, numBedrooms, numBathrooms, description));
        }
        return propertiesResponse;
    }
    public List<DTOIconCaracteristica> getInmuebleCharacteristics(Inmueble inmueble) {
        List<InmuebleCaracteristica> inmuebleCaracteristicas = inmueble.getCaracteristicaList();
        List<DTOIconCaracteristica> dtoIconCaracteristicas = new ArrayList<>();
        for (InmuebleCaracteristica inmuebleCaracteristica : inmuebleCaracteristicas) {
            Caracteristica caracteristica = inmuebleCaracteristica.getCaracteristica();
            dtoIconCaracteristicas.add(new DTOIconCaracteristica(caracteristica.getName(), caracteristica.getIcon()));
        }
        return dtoIconCaracteristicas;
    }
    public List<DTOOpinion> getInmuebleOpinions(Inmueble inmueble) {
        List<Opinion> inmuebleOpiniones = inmueble.getOpiniones();
        List<DTOOpinion> dtoOpinions = new ArrayList<>();
        for (Opinion opinion : inmuebleOpiniones) {
            dtoOpinions.add(new DTOOpinion(opinion.getObservaciones(), opinion.getCalificacion()));
        }
        return dtoOpinions;
    }
    public ShowInmuebleResponse listDataInmueble(Long id){
        Inmueble i = inmuebleRepository.findById(id).get();
        List<InmuebleFoto> inmuebleFotos = inmuebleFotoRepository.findByInmueble_Id(id);
        List<String> photoUrls = new ArrayList<>();
        for (InmuebleFoto inmuebleFoto : inmuebleFotos) {
            photoUrls.add(inmuebleFoto.getFoto().getPhotoLink());
        }
        List<DTOIconCaracteristica> listCaracteristaInmuebleIcons = getInmuebleCharacteristics(i);
        DTOContactoUsuario owner =usuarioService.listContactoUsuario(i.getUsuario().getId());
        List<DTOOpinion> listOpinions = getInmuebleOpinions(i);

        ShowInmuebleResponse showInmuebleResponse = new ShowInmuebleResponse(i.getAddress(), i.getTimeAntiquity(),photoUrls, i.getPrice(),i.getNumGuests(),listCaracteristaInmuebleIcons,owner,i.getUsuario().getLinkPhotoProfile(),i.getNumBedrooms(),i.getNumBathrooms(), i.getSquareMeter(),i.getDescription(),listOpinions);
        return showInmuebleResponse;

    }

    @Transactional
    public Inmueble save(InmuebleRequest inmueble){
        Usuario usuario = usuarioRepository.findById(inmueble.getUsuario_id()).get();
        Ubigeo ubigeo = ubigeoRepository.findUbigeoByDepartamentoAndProvinciaAndDistrito(inmueble.getDepartamento(), inmueble.getProvincia(), inmueble.getDistrito());
        Inmueble newInmueble = inmuebleRepository.save(new Inmueble(inmueble.getPropertyType(), inmueble.getSharedRoom(), inmueble.getAddress(), inmueble.getPrice(), inmueble.getNumBedrooms(), inmueble.getNumBathrooms(), inmueble.getNumGuests(), inmueble.getSquareMeter(), inmueble.getTimeAntiquity(), inmueble.getDescription(),usuario, ubigeo));
        List<Foto> foto = new ArrayList<>();
        for (String f: inmueble.getFoto()){
           Foto newFoto = fotoRepository.save(new Foto(f));
           foto.add(newFoto);
        }
        List<InmuebleFoto> inmuebleFotos = new ArrayList<>();
        for(Foto foto1 : foto){
           InmuebleFoto inmuebleFoto = inmuebleFotoRepository.save(new InmuebleFoto(newInmueble,foto1));
           inmuebleFotos.add(inmuebleFoto);
        }

        for(Long c: inmueble.getCaracteristicasIds()){
           Caracteristica newCaracteristica = caracteristicaRepository.findById(c).get();
           inmuebleCaracteristicaRepository.save(new InmuebleCaracteristica(newInmueble, newCaracteristica));
        }
        newInmueble.setInmuebleFotoList(inmuebleFotos);
        newInmueble.getUsuario().setInmuebles(null);
        newInmueble.getUsuario().setOpiniones(null);
        newInmueble.getUsuario().setRoles(null);
        newInmueble.setOpiniones(null);
        newInmueble.getUbigeo().setInmuebleZonaList(null);
        for(InmuebleFoto fotos: newInmueble.getInmuebleFotoList()){
            fotos.setInmueble(null);
            fotos.getFoto().setInmuebleFotos(null);
        }
        for (Long caracteristicaId : inmueble.getCaracteristicasIds()){
            InmuebleCaracteristica inmuebleCaracteristica = new InmuebleCaracteristica(newInmueble, caracteristicaRepository.findById(caracteristicaId).get());
            inmuebleCaracteristicaRepository.save(inmuebleCaracteristica);
        }
        return newInmueble;
    }
    @Transactional
    public void delete(Long id, boolean forced) {
        alquilerRepository.deleteAllByInmueble_Id(id);
        inmuebleCaracteristicaRepository.deleteAllByInmueble_Id(id);
        opinionRepository.deleteAllByInmueble_Id(id);
        List<InmuebleFoto> inmuebleFotos = inmuebleFotoRepository.findByInmueble_Id(id);
        List<Foto> fotos = new ArrayList<Foto>();
        for(InmuebleFoto ifo: inmuebleFotos){
            Foto foto = ifo.getFoto();
            fotos.add(foto);
        }
        inmuebleFotoRepository.deleteAllByInmueble_Id(id);
        fotoRepository.deleteAll(fotos);
        Inmueble inmueble = inmuebleRepository.findById(id).get();
        inmuebleRepository.delete(inmueble);
    }

}
