package edu.upb.chatupb_v2.Model.entities.comands;

import edu.upb.chatupb_v2.Model.network.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter

public class NewFriend extends Command{
    private String id_sent;
    private String name_user;
    private String ip_user;

    public NewFriend(String my_id,String name_user, String ip_user) {
        super("020");
        this.name_user = name_user;
        this.ip_user = ip_user;
        this.id_sent = my_id;
    }

    @Override
    public String createFormat() {
        return getID() + "|" + id_sent +"|"+ name_user +"|"+ ip_user+System.lineSeparator();
    }

    @Override
    public void execute(SocketClient sc) {

    }

    public static NewFriend parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 4){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new NewFriend(parses[0], parses[1], parses[2]);
    }
}
