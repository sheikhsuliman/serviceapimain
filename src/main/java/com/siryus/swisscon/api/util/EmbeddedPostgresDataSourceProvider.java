package com.siryus.swisscon.api.util;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@ComponentScan
@Profile("dev")
public class EmbeddedPostgresDataSourceProvider {
        @Bean
        public DataSource inMemoryDS() throws Exception {
            DataSource embeddedPostgresDS = EmbeddedPostgres.builder()
                    .setPort(65432)
                    .start().getPostgresDatabase();

            return embeddedPostgresDS;
        }
}
