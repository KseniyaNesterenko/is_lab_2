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
@Table(name = "book_creatures")
@NamedQuery(name="BookCreature.selectAll", query="SELECT p FROM BookCreature p")
public class BookCreature implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Coordinates coordinates = new Coordinates(2, 2);

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(nullable = false)
    private Long age;

    @Enumerated(EnumType.STRING)
    private BookCreatureType creatureType;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private MagicCity creatureLocation = new MagicCity();

    @Column(nullable = false)
    private Float attackLevel;

    private Float defenseLevel;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Ring ring = new Ring();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    public BookCreature(String name, Coordinates coordinates, Date creationDate, Long age, BookCreatureType creatureType, MagicCity creatureLocation, Float attackLevel, Float defenseLevel, Ring ring, User user) {
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.age = age;
        this.creatureType = creatureType;
        this.creatureLocation = creatureLocation;
        this.attackLevel = attackLevel;
        this.defenseLevel = defenseLevel;
        this.ring = ring;
        this.user = user;
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
        if (creationDate != null) {
            String formattedDate = formatDate(creationDate);
            try {
                creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


}
