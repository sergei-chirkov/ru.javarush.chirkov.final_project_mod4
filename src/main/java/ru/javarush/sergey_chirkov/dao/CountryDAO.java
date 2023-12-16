package ru.javarush.sergey_chirkov.dao;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.javarush.sergey_chirkov.entity.Country;

import java.util.List;

public class CountryDAO {
    private final SessionFactory sessionFactory;

    public CountryDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    public List<Country> getAll(){
        Query<Country> query = sessionFactory.getCurrentSession().createQuery("select c from Country c join fetch c.language", Country.class);
        return query.list();

    }
}
