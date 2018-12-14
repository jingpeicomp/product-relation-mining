package com.jinpei.product.relation.service;

import com.jinpei.product.relation.config.AppConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.fpm.FPGrowth;
import org.apache.spark.mllib.fpm.FPGrowthModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 机器学习模型训练
 * Created by liuzhaoming on 2018/12/13.
 */
@Slf4j
@Component
public class Trainer {

    @Autowired
    private JavaSparkContext sparkContext;

    @Autowired
    private AppConfigProperties appConfigProperties;

    /**
     * 挖掘商品关联规则列表
     *
     * @return 商品关联规则列表
     */
    public List<RelationRule> mineProductRelation() {
        FPGrowthModel<String> model;
        if (isModelExist()) {
            model = loadModel();
        } else {
            model = train();
        }

        return calculateRule(model);
    }

    /**
     * 使用FPGrowth算法训练模型
     *
     * @return FPGrowth算法模型
     */
    private FPGrowthModel<String> train() {
        log.info("Start training model .......");
        clearModel();
        long startTime = System.currentTimeMillis();

        List<List<String>> features = loadFeature();
        JavaRDD<List<String>> transactions = sparkContext.parallelize(features);
        FPGrowth fpg = new FPGrowth()
                .setMinSupport(appConfigProperties.getMinSupport())
                .setNumPartitions(8);
        FPGrowthModel<String> model = fpg.run(transactions);
        model.save(sparkContext.sc(), appConfigProperties.getModelPath());
        log.info("Train spends {} ms", (System.currentTimeMillis() - startTime));
        return model;
    }

    /**
     * 从FPGrowthModel 获取商品关联规则
     *
     * @param model FPGrowth算法模型
     * @return 商品关联规则列表
     */
    private List<RelationRule> calculateRule(FPGrowthModel<String> model) {
        List<RelationRule> relationRules = new ArrayList<>();
        model.generateAssociationRules(appConfigProperties.getMinConfidence())
                .toJavaRDD()
                .collect()
                .forEach(rule -> {
                    if (rule.javaAntecedent().size() > 1) {
                        return;
                    }
                    for (String antecedent : rule.javaAntecedent()) {
                        for (String consequent : rule.javaConsequent()) {
                            relationRules.add(new RelationRule(antecedent, consequent, rule.confidence()));
                        }
                    }
                });
        return relationRules;
    }

    /**
     * 从本地加载FPGrowth model
     *
     * @return FPGrowth model
     */
    @SuppressWarnings("unchecked")
    private FPGrowthModel<String> loadModel() {
        return (FPGrowthModel<String>) FPGrowthModel.load(sparkContext.sc(), appConfigProperties.getFpgModelFilePath());
    }

    /**
     * 加载购物车数据
     *
     * @return 购物车数据
     */
    private List<List<String>> loadFeature() {
        String shoppingCartFile = String.join(File.separator, appConfigProperties.getDataPath(), "shoppingCart.data");
        try {
            return Files.lines(Paths.get(shoppingCartFile))
                    .map(line -> Arrays.asList(line.split(" ")))
                    .filter(skuIdList -> skuIdList.size() > 1)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Cannot load feature {}", shoppingCartFile, e);
        }

        return Collections.emptyList();
    }

    /**
     * 删除本地训练好的模型
     */
    private void clearModel() {
        try {
            FileUtils.deleteDirectory(new File(appConfigProperties.getFpgModelFilePath()));
        } catch (IOException e) {
            log.error("Cannot delete model file {}", appConfigProperties.getFpgModelFilePath());
        }
    }

    /**
     * 判断模型是否存在
     *
     * @return boolean 存在返回true， 反之false
     */
    private boolean isModelExist() {
        File file = new File(appConfigProperties.getFpgModelFilePath());
        return file.exists();
    }
}
