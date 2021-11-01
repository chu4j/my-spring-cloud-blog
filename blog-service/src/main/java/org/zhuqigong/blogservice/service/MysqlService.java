package org.zhuqigong.blogservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhuqg.dict.app.MysqlHelper;
import org.zhuqg.dict.entity.DatabaseConfig;
import org.zhuqg.dict.exceptions.Exceptions;
import org.zhuqigong.blogservice.model.TableNameRes;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MysqlService {
    @Autowired
    private DatabaseConfig databaseConfig;

    public List<TableNameRes> getTableNameList() {
        return new MysqlHelper().getTableNameList(databaseConfig)
                .stream().map(tableName -> new TableNameRes(tableName, tableName))
                .collect(Collectors.toList());
    }

    public void exportDocument(List<TableNameRes> tableNames, String fileName, HttpServletRequest request) {
        databaseConfig.setExportFileName(fileName);
        databaseConfig.setTableName(tableNames.stream().map(TableNameRes::getValue).collect(Collectors.toList()));
        databaseConfig.setCopyRightWebsite(Exceptions.ignoreExceptionCall(() -> new URL(request.getRequestURL().toString()).getHost()));
        new MysqlHelper().getDict(databaseConfig);
    }
}
