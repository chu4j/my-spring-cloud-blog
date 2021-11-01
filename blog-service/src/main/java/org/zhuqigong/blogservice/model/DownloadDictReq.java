package org.zhuqigong.blogservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadDictReq {
    private List<TableNameRes> tables;
    private String fileName;
}
