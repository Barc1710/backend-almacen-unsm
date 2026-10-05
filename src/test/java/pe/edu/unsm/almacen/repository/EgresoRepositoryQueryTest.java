package pe.edu.unsm.almacen.repository;

import java.util.Arrays;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jpa.repository.Query;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;
import pe.edu.unsm.almacen.entity.Egreso;

/** Validates both HQL queries against the real entity mappings and MySQL dialect without a database. */
class EgresoRepositoryQueryTest {
    @Test
    void consultaYConteoSonValidosConElDialectoMySql() throws Exception {
        var configuration = new Configuration()
                .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
                .setProperty("hibernate.boot.allow_jdbc_metadata_access", "false")
                .setProperty("hibernate.hbm2ddl.auto", "none");
        var types = new PersistenceManagedTypesScanner(new PathMatchingResourcePatternResolver())
                .scan("pe.edu.unsm.almacen.entity");
        for (String name : types.getManagedClassNames()) {
            configuration.addAnnotatedClass(Class.forName(name));
        }
        var method = Arrays.stream(EgresoRepository.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("listarPaginado")).findFirst().orElseThrow();
        var query = method.getAnnotation(Query.class);
        var ingresoMethod = Arrays.stream(IngresoRepository.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("listarPaginado")).findFirst().orElseThrow();
        var ingresoQuery = ingresoMethod.getAnnotation(Query.class);
        try (var factory = configuration.buildSessionFactory(); var session = factory.openSession()) {
            session.createQuery(query.value(), Egreso.class);
            session.createQuery(query.countQuery(), Long.class);
            session.createQuery(ingresoQuery.value(), pe.edu.unsm.almacen.entity.Ingreso.class);
            session.createQuery(ingresoQuery.countQuery(), Long.class);
        }
    }
}
