package edu.upb.chatupb_v2.model.repository;

public class CacheContactDAO implements IContactDAO {
    private final IContactDAO ;
    private final Object lock = new Object();
