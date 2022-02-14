package org.zhuqigong.blogservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhuqg.dict.app.MysqlHelper;
import org.zhuqg.dict.entity.DatabaseConfig;
import org.zhuqigong.blogservice.config.Configs;
import org.zhuqigong.blogservice.model.TableNameRes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author k
 */
@Service
public class MysqlService {
    private static final Logger LOG = LoggerFactory.getLogger(MysqlService.class);
    @Autowired
    private Configs configs;

    public List<TableNameRes> getTableNameList(String databaseName) {
        return new MysqlHelper().getTableNameList(configs.getDatabaseConfig(databaseName))
                .stream().map(tableName -> new TableNameRes(tableName, tableName))
                .collect(Collectors.toList());
    }

    public void exportDocument(DatabaseConfig databaseConfig, List<TableNameRes> tableNames, String fileName, String website) {
        databaseConfig.setExportFileName(fileName);
        databaseConfig.setTableName(tableNames.stream().map(TableNameRes::getValue).collect(Collectors.toList()));
        databaseConfig.setCopyRightWebsite(website);
        new MysqlHelper().generateDocument(databaseConfig);
    }
}
