package org.zhuqigong.blogservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zhuqg.dict.entity.DatabaseConfig;

@Configuration
public class Configs {
    @Value("${MYSQL_DICT_PWD}")
    private String password;

    @Bean
    public DatabaseConfig getDatabaseConfig() {
        return DatabaseConfig.builder()
                .url("jdbc:mysql://103.215.44.36:8890/ehc-saas?autoReconnect=true&failOverReadOnly=false&useSSL=false&serverTimezone=Asia/Shanghai")
                .user("root")
                .password(password)
                .tableSchema("ehc-saas")
                .exportFilePath("/tmp/")
                .build();
    }
}
