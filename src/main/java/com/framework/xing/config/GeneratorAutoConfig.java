package com.framework.xing.config;

import com.framework.xing.utils.RRException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiexingxing
 * @Created by 2020-09-30 11:00.
 */
@ComponentScan(value = "com.framework.xing")
@MapperScan("com.framework.**.dao")
@Configuration
public class GeneratorAutoConfig {


    @Bean("propertiesConfiguration")
    org.apache.commons.configuration.Configuration configuration(){
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RRException("获取配置文件失败，", e);
        }
    }
}