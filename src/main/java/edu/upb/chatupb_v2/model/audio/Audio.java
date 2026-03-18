package edu.upb.chatupb_v2.model.audio;

import edu.upb.chatupb_v2.model.audio.enums.AudioState;
import edu.upb.chatupb_v2.model.audio.enums.DurationType;
import lombok.Data;

@Data
public class Audio {
    private String name;
    private DurationType durationType;
    private AudioState state;

    public Audio(String name, DurationType durationType) {
        this.name = name;
        this.durationType = durationType;
        this.state = AudioState.NEW;
    }
}
