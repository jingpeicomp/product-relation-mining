package com.jinpei.product.relation.service;

import com.jinpei.product.relation.config.AppConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关系服务类
 * Created by liuzhaoming on 2018/12/13.
 */
@Service
@Slf4j
public class RelationService {

    @Autowired
    private Trainer trainer;

    @Autowired
    private AppConfigProperties configProperties;

    /**
     * 商品关联规则集合
     */
    private List<RelationRule> relationRules;

    /**
     * 商品信息<ID, 名称>
     */
    private Map<Long, String> nameByIds = new HashMap<>();

    @PostConstruct
    public void init() {
        List<RelationRule> ruleList = trainer.mineProductRelation();
        relationRules = ruleList.stream()
                .sorted(Comparator.comparing($ -> (RelationRule) $).reversed())
                .collect(Collectors.toList());

        try {
            Files.lines(Paths.get(configProperties.getProductDataFilePath()))
                    .forEach(line -> {
                        String[] idAndNames = line.split("|&|");
                        if (idAndNames.length != 2) {
                            log.error("Invalid product info {}", line);
                            return;
                        }

                        Long id = Long.valueOf(StringUtils.strip(idAndNames[0]));
                        String name = StringUtils.strip(idAndNames[1]);
                        nameByIds.put(id, name);
                    });
        } catch (Exception e) {
            log.error("Load product file {} error ", configProperties.getProductDataFilePath(), e);
        }
    }

}
