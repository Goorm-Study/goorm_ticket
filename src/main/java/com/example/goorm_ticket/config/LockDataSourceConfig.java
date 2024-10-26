package com.example.goorm_ticket.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.goorm_ticket.lockdomain",  // 락 관련 리포지토리 경로
        entityManagerFactoryRef = "lockEntityManagerFactory",
        transactionManagerRef = "lockTransactionManager"
)
public class LockDataSourceConfig {
    @Bean(name = "lock-DataSourceConfig")
    @ConfigurationProperties("spring.datasource.lock")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }
    @Bean(name = "lockDataSource")
    @ConfigurationProperties("spring.datasource.lock.hikari")
    public DataSource lockDataSource(@Qualifier("lock-DataSourceConfig") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "lockEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean lockEntityManagerFactory(@Qualifier("lockDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.goorm_ticket.lockdomain");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String, Object> prop = new HashMap<>();
        prop.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        prop.put("hibernate.hbm2ddl.auto", "create");
        prop.put("hibernate.show_sql", true);
        prop.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        emf.setJpaPropertyMap(prop);

        return emf;
    }

    @Bean(name = "lockTransactionManager")
    public PlatformTransactionManager lockTransactionManager(
            @Qualifier("lockEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public TransactionTemplate transactionTemplate(
            @Qualifier("lockTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
