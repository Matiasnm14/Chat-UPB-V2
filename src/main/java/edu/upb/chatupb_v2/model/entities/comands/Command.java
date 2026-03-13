package edu.upb.chatupb_v2.model.entities.comands;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
public abstract class Command {
    @Getter
    String ID;
    public abstract String createFormat();
    public void setID(String ID) {
        throw new UnsupportedOperationException("No se puede cambiar el ID de un comando");

    }
//    public void execute(socketClient client) throws exception {
//        throw new UnsupportedOperationException("No se puede ejecutar un comando sin implementar el método execute");
//    }

}
// patron comando , para que una clase abstracta que tenga un metodo execute y cada clase hija implemente ese metodo con su propia logica , asi se puede ejecutar el comando sin importar la clase hija , solo se necesita llamar al metodo execute del comando y este se encargara de ejecutar la logica correspondiente segun la clase hija que se haya instanciado.