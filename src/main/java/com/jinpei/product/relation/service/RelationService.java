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
import java.util.stream.Stream;

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
    private Map<String, String> nameByIds = new HashMap<>();

    /**
     * 商品被加入到购物车的用户数<ID, 购物车中有该商品的用户数目>
     */
    private Map<String, Integer> customerNumByIds = new HashMap<>();

    /**
     * 分页查询商品关联关系
     *
     * @param page 页码，从0开始
     * @param size 每页数目
     * @return 商品关联关系列表
     */
    public List<RelationRule> query(int page, int size) {
        return relationRules.stream()
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * 查询商品的关联商品
     *
     * @param id 商品ID
     * @return 商品关联关系列表
     */
    public List<RelationRule> query(String id) {
        return relationRules.stream()
                .filter(rule -> rule.getAntecedentId().equalsIgnoreCase(id))
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void init() {
        try {
            Files.lines(Paths.get(configProperties.getProductDataFilePath()))
                    .forEach(line -> {
                        String[] idAndNames = line.split("\\|&\\|");
                        if (idAndNames.length != 2) {
                            log.error("Invalid product info {}", line);
                            return;
                        }

                        nameByIds.put(StringUtils.strip(idAndNames[0]), StringUtils.strip(idAndNames[1]));
                    });

            customerNumByIds = Files.lines(Paths.get(configProperties.getShoppingCartDataFilePath()))
                    .flatMap(line -> Stream.of(line.split(" ")))
                    .map(StringUtils::strip)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.groupingBy($ -> $))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        } catch (Exception e) {
            log.error("Load data file {}, {} error ", configProperties.getProductDataFilePath(),
                    configProperties.getShoppingCartDataFilePath(), e);
        }

        List<RelationRule> ruleList = trainer.mineProductRelation();
        relationRules = ruleList.stream()
                .peek(rule -> {
                    rule.setAntecedentName(nameByIds.get(rule.getAntecedentId()));
                    rule.setConsequentName(nameByIds.get(rule.getConsequentId()));
                    rule.setAntecedentCustomerNum(customerNumByIds.get(rule.getAntecedentId()));
                })
                .sorted(Comparator.comparing($ -> (RelationRule) $).reversed())
                .collect(Collectors.toList());
    }
}
