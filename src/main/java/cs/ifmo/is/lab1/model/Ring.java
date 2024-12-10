package cs.ifmo.is.lab1.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "rings")
@NamedQuery(name="Ring.selectAll", query="SELECT p FROM Ring p")
public class Ring implements Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer power;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public Ring(String name, Integer power, User user) {
        this.name = name;
        this.power = power;
        this.user = user;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}