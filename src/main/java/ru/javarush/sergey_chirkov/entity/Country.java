package ru.javarush.sergey_chirkov.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "country", schema = "world")

public class Country {
    @Id
    private Integer id;
    private String code;
    @Column(name = "code_2")
    private String codeTwo;
    private String name;
    @Enumerated(EnumType.ORDINAL)
    private Continent continent;
    private String region;
    @Column(name = "surface_area")
    private BigDecimal surfaceArea;
    @Column(name = "indep_year")
    private Short independenceYear;
    private Integer population;
    @Column(name = "life_expectancy")
    private BigDecimal lifeExpectancy;
    private BigDecimal gnp;
    @Column(name = "gnpo_id")
    private BigDecimal gnpId;
    @Column(name = "local_name")
    private String localName;
    @Column(name = "government_form")
    private String governmentForm;
    @Column(name = "head_of_state")
    private String headOfState;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capital")
    private City city;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Set<CountryLanguage> language;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeTwo() {
        return codeTwo;
    }

    public void setCodeTwo(String codeTwo) {
        this.codeTwo = codeTwo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Continent getContinent() {
        return continent;
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getSurfaceArea() {
        return surfaceArea;
    }

    public void setSurfaceArea(BigDecimal surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public Short getIndependenceYear() {
        return independenceYear;
    }

    public void setIndependenceYear(Short independenceYear) {
        this.independenceYear = independenceYear;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public BigDecimal getLifeExpectancy() {
        return lifeExpectancy;
    }

    public void setLifeExpectancy(BigDecimal lifeExpectancy) {
        this.lifeExpectancy = lifeExpectancy;
    }

    public BigDecimal getDnp() {
        return gnp;
    }

    public void setDnp(BigDecimal dnp) {
        this.gnp = dnp;
    }

    public BigDecimal getGnpId() {
        return gnpId;
    }

    public void setGnpId(BigDecimal gnpId) {
        this.gnpId = gnpId;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getGovernmentForm() {
        return governmentForm;
    }

    public void setGovernmentForm(String governmentForm) {
        this.governmentForm = governmentForm;
    }

    public String getHeadOfState() {
        return headOfState;
    }

    public void setHeadOfState(String headOfState) {
        this.headOfState = headOfState;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Set<CountryLanguage> getLanguage() {
        return language;
    }

    public void setLanguage(Set<CountryLanguage> language) {
        this.language = language;
    }
}
