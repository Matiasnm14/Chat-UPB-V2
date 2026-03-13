package edu.upb.chatupb_v2.Model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Cobro {
    private String qr;
    private double importe;
    private  String red;
}

