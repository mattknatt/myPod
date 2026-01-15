package org.example;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;
import org.hibernate.jpa.HibernatePersistenceConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Factory utility for creating a JPA {@link EntityManagerFactory}.
 *
 * <p>This class programmatically configures Hibernate without relying
 * on a {@code persistence.xml} file. All JPA entities are discovered
 * automatically via classpath scanning.</p>
 *
 * <p>The factory supports additional configuration properties that
 * can be supplied at runtime.</p>
 */
public class EntityManagerFactoryProvider {

    /**
     * Creates and configures an {@link EntityManagerFactory}.
     *
     * <p>The method scans the specified entity package for classes
     * annotated with {@link jakarta.persistence.Entity}, registers them
     * with Hibernate, and applies the provided JDBC and Hibernate
     * configuration properties.</p>
     *
     * @param jdbcUrl    JDBC connection URL
     * @param username   database username
     * @param password   database password
     * @param extraProps additional Hibernate configuration properties
     * @return a fully initialized {@link EntityManagerFactory}
     */
    public static EntityManagerFactory create(
        String jdbcUrl,
        String username,
        String password,
        Map<String, String> extraProps
    ) {
        List<Class<?>> entities = scanEntities("org.example.entity");

        PersistenceConfiguration cfg =
            new HibernatePersistenceConfiguration("emf")
                .jdbcUrl(jdbcUrl)
                .jdbcUsername(username)
                .jdbcPassword(password)
                .managedClasses(entities);

        extraProps.forEach(cfg::property);

        return cfg.createEntityManagerFactory();
    }

    /**
     * Scans the classpath for JPA entity classes.
     *
     * <p>All classes annotated with {@link Entity} within the specified
     * package are discovered and loaded.</p>
     *
     * @param pkg base package to scan
     * @return list of entity classes
     */
    private static List<Class<?>> scanEntities(String pkg) {
        try (ScanResult scanResult =
                 new ClassGraph()
                     .enableAnnotationInfo()
                     .acceptPackages(pkg)
                     .scan()) {

            return scanResult.getClassesWithAnnotation(Entity.class).loadClasses();
        }
    }
}
