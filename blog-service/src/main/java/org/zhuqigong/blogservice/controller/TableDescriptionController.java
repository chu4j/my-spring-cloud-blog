package org.zhuqigong.blogservice.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zhuqg.dict.entity.DatabaseConfig;
import org.zhuqigong.blogservice.config.Configs;
import org.zhuqigong.blogservice.model.DownloadDictReq;
import org.zhuqigong.blogservice.model.TableNameRes;
import org.zhuqigong.blogservice.service.MysqlService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author k
 */
@RestController
@RequestMapping("/blog/table")
public class TableDescriptionController {
    @Autowired
    private MysqlService mysqlService;
    @Autowired
    private Configs configs;

    @GetMapping("/tables/{databaseName}")
    public List<TableNameRes> getTables(@PathVariable("databaseName") String databaseName) {
        return mysqlService.getTableNameList(databaseName);
    }

    @PostMapping("download/{databaseName}")
    public ResponseEntity<Resource> downloadMysqlDict(@PathVariable("databaseName") String databaseName, @RequestBody DownloadDictReq req) throws FileNotFoundException {
        Objects.requireNonNull(req);
        final String fileName = StringUtils.isBlank(req.getFileName()) ? "mysql-dictionary.docx" : req.getFileName() + ".docx";
        final String uuidFileName = UUID.randomUUID() + ".docx";
        final DatabaseConfig databaseConfig = configs.getDatabaseConfig(databaseName);
        mysqlService.exportDocument(databaseConfig, req.getTables(), uuidFileName, req.getWebsite());
        final File file = new File(databaseConfig.getExportFilePath() + uuidFileName);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
        final InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(httpHeaders)
                .contentLength(file.length())
                .body(resource);
    }
}
