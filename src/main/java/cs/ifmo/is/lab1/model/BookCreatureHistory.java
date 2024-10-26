package cs.ifmo.is.lab1.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "book_creature_history")
@NamedQuery(name="BookCreatureHistory.selectAll", query="SELECT p FROM BookCreatureHistory p")
@NamedQuery(name = "BookCreatureHistory.count", query = "SELECT COUNT(h) FROM BookCreatureHistory h")
public class BookCreatureHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer bookCreatureId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjectType objectType;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date changeDate;

    @Column(nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeType changeType;

    public BookCreatureHistory(Integer bookCreatureId, ObjectType objectType, Date changeDate, Integer userId, ChangeType changeType) {
        this.bookCreatureId = bookCreatureId;
        this.objectType = objectType;
        this.changeDate = changeDate;
        this.userId = userId;
        this.changeType = changeType;
    }

    public BookCreatureHistory(Integer bookCreatureId, Date changeDate, Integer userId, ChangeType changeType) {
        this.bookCreatureId = bookCreatureId;
        this.changeDate = changeDate;
        this.userId = userId;
        this.changeType = changeType;
    }

    public enum ChangeType {
        CREATE, UPDATE, DELETE
    }
    public enum ObjectType {
        RING, MAGIC_CITY, BOOK_CREATURE
    }
}
