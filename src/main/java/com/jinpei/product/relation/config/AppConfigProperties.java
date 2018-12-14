package com.jinpei.product.relation.config;

import lombok.Data;

import java.io.File;
import java.io.Serializable;

/**
 * 项目配置参数
 * Created by liuzhaoming on 2018/12/13.
 */
@Data
public class AppConfigProperties implements Serializable {
    /**
     * 最小支持度
     */
    private double minSupport = 0.2;

    /**
     * 最小置信度
     */
    private double minConfidence = 0.5;

    /**
     * 模型文件存放路径
     */
    private String modelPath;

    /**
     * 训练数据文件存放路径
     */
    private String dataPath;

    /**
     * 获取FPGrowth模型文件路径
     *
     * @return FPGrowth模型文件路径
     */
    public String getFpgModelFilePath() {
        return String.join(File.separator, modelPath, "fpg");
    }

    /**
     * 获取商品信息文件
     *
     * @return 商品信息文件路径
     */
    public String getProductDataFilePath() {
        return String.join(File.separator, dataPath, "product.data");
    }

    /**
     * 获取购物车信息文件
     *
     * @return 购物车信息文件路径
     */
    public String getShoppingCartDataFilePath() {
        return String.join(File.separator, dataPath, "shoppingCart.data");
    }
}
