package org.example.logging;

import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.example.PersistenceManager;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.internal.SessionFactoryImpl;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Log4j2 ConnectionSource that uses a JPA EntityManagerFactory's Hibernate connection pool.
 */
@Plugin(name = "JpaHibernateConnectionSource", category = "Core", printObject = true)
public class JpaHibernateConnectionSource implements ConnectionSource {

    private final EntityManagerFactory emf;

    public JpaHibernateConnectionSource(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // Unwrap JPA EntityManagerFactory to Hibernate SessionFactory
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);

        // Get Hibernate's internal SessionFactoryImpl
        SessionFactoryImpl sfi = sessionFactory.unwrap(SessionFactoryImpl.class);

        // Get JDBC services
        JdbcServices jdbcServices = sfi.getServiceRegistry().getService(JdbcServices.class);

        // Get connection access object
        JdbcConnectionAccess connectionAccess = jdbcServices.getBootstrapJdbcConnectionAccess();

        // Return a pooled connection
        return connectionAccess.obtainConnection();
    }

    @Override
    public String toString() {
        return "JpaHibernateConnectionSource using Hibernate's pool";
    }

    @PluginFactory
    public static JpaHibernateConnectionSource createConnectionSource() {
        return new JpaHibernateConnectionSource(PersistenceManager.getEntityManagerFactory());
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }
}
