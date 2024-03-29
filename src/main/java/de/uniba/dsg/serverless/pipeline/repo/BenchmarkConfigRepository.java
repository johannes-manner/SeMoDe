package de.uniba.dsg.serverless.pipeline.repo;

import de.uniba.dsg.serverless.pipeline.model.config.BenchmarkConfig;
import de.uniba.dsg.serverless.pipeline.repo.projection.IBenchmarkDetail;
import de.uniba.dsg.serverless.pipeline.repo.projection.IBenchmarkVersionAggregate;
import de.uniba.dsg.serverless.pipeline.repo.projection.IBenchmarkVisible;
import de.uniba.dsg.serverless.pipeline.repo.projection.IPointDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenchmarkConfigRepository extends JpaRepository<BenchmarkConfig, Long> {

    @Query(value = "select bc.id as id, bc.version_number as versionNumber, count(pd.id) as performanceEvents\n" +
            "from benchmark_config bc \n" +
            "\tleft outer join performance_data pd on bc.id = pd.benchmark_config_id\n" +
            "where setup_name =  ?1 \n" +
            "group by bc.id, bc.version_number\n" +
            "order by bc.version_number desc", nativeQuery = true)
    public List<IBenchmarkVersionAggregate> countEventsByGroupingThemOnTheirVersionNumber(String setupName);

    BenchmarkConfig findBenchmarkConfigBySetupNameAndVersionNumber(String setup, Integer version);

    @Query(value = "SELECT pd.memory_size as x, pd.precise_duration as y\n" +
            "FROM benchmark_config bc LEFT OUTER JOIN  performance_data pd ON bc.id = pd.benchmark_config_id\n" +
            "WHERE bc.setup_name = ?1 AND bc.version_number = ?2", nativeQuery = true)
    public List<IPointDto> getBenchmarkExecutionPointsProviderView(String setupName, Integer version);

    @Query(value = "SELECT pd.memory_size as memory, pd.precise_duration as duration, cpu_model as cpu, cpu_model_name as name, vm_identification as vm, cold\n" +
            "FROM benchmark_config bc LEFT OUTER JOIN  performance_data pd ON bc.id = pd.benchmark_config_id inner join provider_event pe on pd.id = pe.performance_data_id\n" +
            "WHERE bc.setup_name = ?1 AND bc.version_number = ?2", nativeQuery = true)
    public List<IBenchmarkDetail> getBenchmarkExecutionDetailData(String setupName, Integer version);

    @Query(value = "SELECT id, setup_name as setupName, description " +
            "FROM benchmark_config " +
            "WHERE version_visible = true", nativeQuery = true)
    List<IBenchmarkVisible> getBenchmarksPubliclyVisible();

    @Query(value = "SELECT * FROM benchmark_config WHERE setup_name = ?1 ORDER BY version_number DESC LIMIT 1", nativeQuery = true)
    BenchmarkConfig findBySetupNameOrderByVersionNumberDesc(String setup);
}
