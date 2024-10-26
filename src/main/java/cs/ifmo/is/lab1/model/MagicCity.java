package cs.ifmo.is.lab1.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "magic_cities")
@NamedQuery(name="MagicCity.selectAll", query="SELECT p FROM MagicCity p")
public class MagicCity implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double area;

    @Column(nullable = false)
    private Integer population;

    @Temporal(TemporalType.TIMESTAMP)
    private Date establishmentDate;

    @Enumerated(EnumType.STRING)
    private BookCreatureType governor;

    private boolean capital;

    @Column(nullable = false)
    private int populationDensity = 1;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public MagicCity(String name, Double area, Integer population, Date establishmentDate, BookCreatureType governor, boolean capital, int populationDensity, User user) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.establishmentDate = establishmentDate;
        this.governor = governor;
        this.capital = capital;
        this.populationDensity = populationDensity;
        this.user = user;
    }

    public int getPopulationDensity() {
        return populationDensity;
    }

    public void setPopulationDensity(int populationDensity) {
        this.populationDensity = populationDensity;
    }

    public boolean isCapital() {
        return capital;
    }

    public void setCapital(boolean capital) {
        this.capital = capital;
    }

    public BookCreatureType getGovernor() {
        return governor;
    }

    public void setGovernor(BookCreatureType governor) {
        this.governor = governor;
    }

    public Date getEstablishmentDate() {
        return establishmentDate;
    }

    public void setEstablishmentDate(Date establishmentDate) {
        this.establishmentDate = establishmentDate;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

//    public static Date parseDate(String dateStr) throws ParseException {
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
//        return sdf.parse(dateStr);
//    }

    @PrePersist
    public void prePersist() {
        if (establishmentDate != null) {
            String formattedDate = formatDate(establishmentDate);
            try {
                establishmentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }



}