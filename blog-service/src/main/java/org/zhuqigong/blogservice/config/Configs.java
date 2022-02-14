package org.zhuqigong.blogservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.zhuqg.dict.entity.DatabaseConfig;

/**
 * @author k
 */
@Configuration
public class Configs {
    private static final String EHC_SAAS = "ehc-saas";
    private static final String STOREFRONT = "storefront";
    @Value("${MYSQL_DICT_PWD}")
    private String password;
    @Value("${MYSQL_DICT_PWD2}")
    private String password2;

    public DatabaseConfig getDatabaseConfig(String databaseName) {
        if (EHC_SAAS.equalsIgnoreCase(databaseName)) {
            return DatabaseConfig.builder()
                    .url("jdbc:mysql://103.215.44.36:8890/ehc-saas?autoReconnect=true&failOverReadOnly=false&useSSL=false&serverTimezone=Asia/Shanghai")
                    .user("root")
                    .password(password)
                    .tableSchema(EHC_SAAS)
                    .exportFilePath("/tmp/")
                    .build();
        } else if (STOREFRONT.equalsIgnoreCase(databaseName)) {
            return DatabaseConfig.builder()
                    .url("jdbc:mysql://103.215.45.138:3306/storefront?autoReconnect=true&failOverReadOnly=false&useSSL=false&serverTimezone=Asia/Shanghai")
                    .user("root")
                    .password(password2)
                    .tableSchema(STOREFRONT)
                    .exportFilePath("/tmp/")
                    .build();
        }
        return null;
    }
}
