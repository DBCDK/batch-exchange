/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

@Startup
@Singleton
public class BatchExchangeDatabaseMigrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchExchangeDatabaseMigrator.class);

    @Resource(lookup = "jdbc/batch-exchange")
    DataSource dataSource;

    public BatchExchangeDatabaseMigrator() {}

    public BatchExchangeDatabaseMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migrate() {
        final Flyway flyway = Flyway.configure()
                .table("schema_version")
                .baselineOnMigrate(true)
                .dataSource(dataSource)
                .locations("classpath:dk.dbc.batchexchange.db.migration")
                .load();
        for (MigrationInfo info : flyway.info().all()) {
            LOGGER.info("database migration {} : {} from file '{}'",
                    info.getVersion(), info.getDescription(), info.getScript());
        }
        flyway.migrate();
    }
}