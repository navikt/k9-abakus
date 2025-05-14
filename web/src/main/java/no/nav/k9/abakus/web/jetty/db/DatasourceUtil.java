package no.nav.k9.abakus.web.jetty.db;

import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import no.nav.vault.jdbc.hikaricp.VaultError;

public class DatasourceUtil {

    public static HikariDataSource createDatasource(String datasourceName, DatasourceRole role, EnvironmentClass environmentClass, int maxPoolSize) {
        String rolePrefix = getRolePrefix(datasourceName);
        if (EnvironmentClass.LOCALHOST.equals(environmentClass)) {
            final HikariConfig config = initConnectionPoolConfig(datasourceName, null, maxPoolSize);
            final String password = Environment.current().getProperty(datasourceName + ".password");
            return createLocalDatasource(config, "public", rolePrefix, password);
        } else {
            final String dbRole = getRole(rolePrefix, role);
            final HikariConfig config = initConnectionPoolConfig(datasourceName, dbRole, maxPoolSize);
            return createVaultDatasource(config, environmentClass.mountPath(), dbRole);
        }
    }

    private static String getRole(String rolePrefix, DatasourceRole role) {
        return String.format("%s-%s", rolePrefix, role.name().toLowerCase());
    }

    public static String getDbRole(String datasoureName, DatasourceRole role) {
        return String.format("%s-%s", getRolePrefix(datasoureName), role.name().toLowerCase());
    }

    private static String getRolePrefix(String datasourceName) {
        return Environment.current().getProperty(datasourceName + ".username");
    }

    private static HikariConfig initConnectionPoolConfig(String dataSourceName, String dbRole, int maxPoolSize) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Environment.current().getProperty(dataSourceName + ".url"));

        config.setMinimumIdle(0);
        config.setMaximumPoolSize(maxPoolSize);
        config.setIdleTimeout(10001);
        config.setMaxLifetime(30001);
        config.setConnectionTestQuery("select 1");
        config.setDriverClassName("org.postgresql.Driver");

        if (dbRole != null) {
            final String initSql = String.format("SET ROLE \"%s\"", dbRole);
            config.setConnectionInitSql(initSql);
        }

        // optimaliserer inserts for postgres
        var dsProperties = new Properties();
        dsProperties.setProperty("reWriteBatchedInserts", "true");
        config.setDataSourceProperties(dsProperties);

        // skrur av autocommit her, da kan vi bypasse dette senere når hibernate setter opp entitymanager for bedre conn mgmt
        config.setAutoCommit(false);

        return config;
    }

    private static HikariDataSource createVaultDatasource(HikariConfig config, String mountPath, String role) {
        try {
            return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, role);
        } catch (VaultError vaultError) {
            throw new RuntimeException("Vault feil ved opprettelse av databaseforbindelse", vaultError);
        }
    }

    private static HikariDataSource createLocalDatasource(HikariConfig config, String schema, String username, String password) {
        config.setUsername(username);
        config.setPassword(password); // NOSONAR false positive
        if (schema != null && !schema.trim().isEmpty()) {
            config.setSchema(schema);
        }
        return new HikariDataSource(config);
    }
}
