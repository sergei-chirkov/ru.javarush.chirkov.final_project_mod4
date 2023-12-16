import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import ru.javarush.sergey_chirkov.dao.*;
import ru.javarush.sergey_chirkov.entity.City;
import ru.javarush.sergey_chirkov.entity.Country;
import ru.javarush.sergey_chirkov.entity.CountryLanguage;
import ru.javarush.sergey_chirkov.redis.CityCountry;
import ru.javarush.sergey_chirkov.redis.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class Main {
    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;
    private final ObjectMapper mapper;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;

    public Main() {
        sessionFactory = prepareRelationDb();
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        redisClient = prepareRedisClient();
        mapper = new ObjectMapper();

    }
    public static void main(String[] args){
        Main main = new Main();
        List<City> cities = main.fetchData(main);
        List<CityCountry> preparedData = main.transformData(cities);
        main.pushToRedis(preparedData);

        main.sessionFactory.getCurrentSession().close();

        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 3458, 1189, 10, 102);
        long startRedis = System.currentTimeMillis();
        main.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        main.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));
        main.shutdown();
    }

    private SessionFactory prepareRelationDb(){
        final SessionFactory sessionFactory;
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/world");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        properties.put(Environment.STATEMENT_BATCH_SIZE, "100");


        sessionFactory = new Configuration()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addProperties(properties)
                .buildSessionFactory();
        return sessionFactory;
    }
    private RedisClient prepareRedisClient(){
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try(StatefulRedisConnection<String, String> connect = redisClient.connect()){
            System.out.println("\nConnected to Redis\n");
        }
        return redisClient;
    }

    private void shutdown(){
        if(nonNull(sessionFactory)){
            sessionFactory.close();
        }
        if(nonNull(redisClient)){
            redisClient.shutdown();
        }
    }
    private List<City> fetchData (Main main){
        try (Session currentSession = main.sessionFactory.getCurrentSession()){
            List<City> allCities = new ArrayList<>();
            currentSession.beginTransaction();
            List<Country> countries = main.countryDAO.getAll();
            int totalCount = main.cityDAO.getTotalCount();
            int step = 500;
            for(int i = 0; i < totalCount; i +=step){
                allCities.addAll(main.cityDAO.getItems(i,step));
            }
            currentSession.getTransaction().commit();
            return allCities;
        }
    }
    private List<CityCountry> transformData(List<City> cities){
        return cities.stream().map(city -> {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setPopulation(city.getPopulation());
            res.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            res.setCodeTwo(country.getCodeTwo());
            res.setContinent(country.getContinent());
            res.setCountryCode(country.getCode());
            res.setCountName(country.getName());
            res.setCountryPopulation(country.getPopulation());
            res.setCountryRegion(country.getRegion());
            res.setCountrySurfaceArea(country.getSurfaceArea());
            Set<CountryLanguage> countryLanguages = country.getLanguage();
            Set<Language> languages = countryLanguages.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setOfficial(cl.getOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguage(languages);

            return res;
        }).collect(Collectors.toList());
    }
    private void pushToRedis(List<CityCountry> data){
        try(StatefulRedisConnection<String, String> connect = redisClient.connect()){
            RedisCommands<String, String> sync = connect.sync();
            for(CityCountry cityCountry : data){
                try{
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void testRedisData(List<Integer> ids){
        try(StatefulRedisConnection<String, String> connect = redisClient.connect()){
            RedisCommands<String, String> sync = connect.sync();
            for(Integer id :ids){
                String value = sync.get(String.valueOf(id));
                try{
                    mapper.readValue(value,CityCountry.class);
                }
                catch (JsonProcessingException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void testMysqlData(List<Integer> ids){
        try(Session currentSession = sessionFactory.getCurrentSession()){
            currentSession.beginTransaction();
            for(Integer id : ids){
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguage();
            }
            currentSession.getTransaction().commit();
        }
    }
}
