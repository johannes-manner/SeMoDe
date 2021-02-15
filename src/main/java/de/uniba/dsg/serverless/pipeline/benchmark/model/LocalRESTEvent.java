package de.uniba.dsg.serverless.pipeline.benchmark.model;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LocalRESTEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    private ProviderEvent providerEvent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean erroneous = false;
    @ManyToOne(cascade = {})
    private BenchmarkConfig benchmarkConfig;

    /**
     * This method indicates if the data from the corresponding platform is already fetched (present in the database).
     * Precondition is, that the ProviderEvent is also fetched from the db.
     *
     * @return true if the data is already fetched
     */
    public boolean dataAlreadyFetched() {
        if (this.providerEvent != null && this.providerEvent.getPerformanceData() != null) {
            return true;
        } else {
            return false;
        }
    }
}
