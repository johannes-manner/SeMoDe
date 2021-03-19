package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.IBenchmarkVersionAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenchmarkConfigRepository extends JpaRepository<BenchmarkConfig, Long> {

    @Query(value = "select bc.version_number as versionNumber, count(lre.id) as localEvents, count(pe.id) as providerEvents\n" +
            "from benchmark_config bc \n" +
            "\tleft outer join localrestevent lre on bc.id = lre.benchmark_config_id\n" +
            "\tleft outer join provider_event pe on lre.provider_event_id = pe.id\n" +
            "where setup_name =  ?1 \n" +
            "group by bc.version_number\n" +
            "order by bc.version_number desc", nativeQuery = true)
    public List<IBenchmarkVersionAggregate> countEventsByGroupingThemOnTheirVersionNumber(String setupName);

}
