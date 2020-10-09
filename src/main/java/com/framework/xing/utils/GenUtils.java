package com.framework.xing.utils;

import com.framework.xing.entity.ColumnEntity;
import com.framework.xing.entity.TableEntity;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import freemarker.template.TemplateException;

/**
 * 代码生成器 工具类
 */
public class GenUtils {

    public static List<String> getTemplates(boolean onlyBackend, boolean generatorServiceInterface) {
        List<String> templates = new ArrayList<String>();
        templates.add("template/Entity.java.vm");
        templates.add("template/Dao.java.vm");
        templates.add("template/Dao.xml.vm");
        templates.add("template/Controller.java.vm");
        templates.add("template/Service.java.vm");
        if (generatorServiceInterface) {
            templates.add("template/ServiceImpl.java.vm");
        }
        if (!onlyBackend) {
//            templates.add("template/list.html.vm");
//            templates.add("template/list.js.vm");
            templates.add("template/menu.sql.vm");
            templates.add("index.vue.ftl");
            templates.add("edit.vue.ftl");
            templates.add("api.js.ftl");
        }


        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(org.apache.commons.configuration.Configuration config, Map<String, String> table, List<Map<String, String>> columns, ZipArchiveOutputStream zip) throws IOException, TemplateException {
        //是否 输出代码到目录
        Boolean isToDirect = zip == null;
        boolean hasBigDecimal = false;
        // 表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        // 表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getStringArray("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));

        // 列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            // 列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setAttrname(StringUtils.uncapitalize(attrName));

            // 列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);
            if (!hasBigDecimal && attrType.equals("BigDecimal")) {
                hasBigDecimal = true;
            }
            // 是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }

            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        // 没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        // 设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);


        // 封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("package", config.getString("package"));
        map.put("moduleName", config.getString("moduleName"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));

        //实体是否开启lombok 注解
        boolean openLombok = config.getBoolean("openLombok");
        //使用swagger
        boolean openSwagger = config.getBoolean("openSwagger");
        boolean openResponsePackage = config.getBoolean("open-response-package");
        boolean openShiro = config.getBoolean("open-controller-shiro-RequiresPermissions");
        //前端忽略大小写
        boolean openFrontLowercase = config.getBoolean("openFrontLowercase");
        //后端文件输出路径
        String generatorBackendPath = config.getString("generatorBackendPath");
        //前端文件输出目录
        String generatorFrontPath = config.getString("generatorFrontPath");
        //是否生成service  interface
        boolean generatorServiceInterface = config.getBoolean("serviceInterface");
        //是否生成
        boolean onlyBackend = config.getBoolean("onlyBackend");

        map.put("openLombok", openLombok);
        map.put("generatorServiceInterface", generatorServiceInterface);
        map.put("openSwagger", openSwagger);
        map.put("openShiro", openShiro);
        map.put("openFrontLowercase", openFrontLowercase);
        map.put("openResponsePackage", openResponsePackage);
        map.put("baseResponsePackage", config.getString("base-response-package"));

        VelocityContext context = new VelocityContext(map);


        freemarker.template.Configuration configuration = new freemarker.template.Configuration();
        configuration.setClassForTemplateLoading(GenUtils.class, "/template/vue");


        // 获取模板列表
        List<String> templates = getTemplates(onlyBackend, generatorServiceInterface);
        for (String template : templates) {
            // 渲染模板
            try (StringWriter sw = new StringWriter()) {
                if (template.endsWith(".ftl")) {
                    // vue 采用freemarker 替换
                    freemarker.template.Template tp = configuration.getTemplate(template, "UTF-8");
                    tp.process(map, sw);

                } else {
                    // 渲染模板
                    Template tpl = Velocity.getTemplate(template, "UTF-8");
                    tpl.merge(context, sw);
                }

                String fileName = getFileName(template, tableEntity.getClassName(), config.getString("package"), config
                        .getString("moduleName"), openFrontLowercase);

                File file = new File(fileName);


                if (template.endsWith("sql.vm")) {
                    //todo 执行sql
                    continue;
                }

                Boolean isBackend = isBackendTemplate(template);

                if (isToDirect) {
                    //输出到目录
                    String path = isBackend ? generatorBackendPath : generatorFrontPath;
                    generatorFileToDir(path, fileName, sw.toString());
                } else {
                    zip.putArchiveEntry(new ZipArchiveEntry(file, fileName));
                    IOUtils.write(sw.toString(), zip, "UTF-8");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            } catch (TemplateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否后端文件
     *
     * @param template
     * @return
     */
    private static Boolean isBackendTemplate(String template) {
        return template.contains("java.vm") || template.contains("Dao.xml.vm");
    }

    /**
     * 输出文件到目录
     *
     * @param basePath
     * @param filename
     * @param outPutData
     */
    private static void generatorFileToDir(String basePath, String filename, String outPutData) {
        String path = filename.substring(0, filename.lastIndexOf("/"));
        String dirPath = basePath + path;
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = basePath + filename;
        System.out.println(filePath);
        File file1 = new File(filePath);
        if (!file1.exists()) {
            try {
                file1.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file1);
                IOUtils.write(outPutData, fileOutputStream, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String[] prefixs) {
        if (prefixs != null && prefixs.length > 0) {
            int i = tableName.indexOf("_");
            if (i != -1) {
                String thisPrefix = tableName.substring(0, i + 1);
                boolean havePrefix = Arrays.stream(prefixs).anyMatch((prefix) -> thisPrefix.equals(prefix));
                if (havePrefix) {
                    tableName = tableName.substring(i + 1, tableName.length());
                }
            }
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RRException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName, boolean openFrontLowercase) {
        String packagePath = "src" + File.separator + "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName + File.separator;
        }

        if (template.contains("Entity.java.vm")) {
            return packagePath + "entity" + File.separator + className + ".java";
        }

        if (template.contains("Dao.java.vm")) {
            return packagePath + "dao" + File.separator + className + "Dao.java";
        }

        if (template.contains("Service.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("Controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if (template.contains("Dao.xml.vm")) {
            return "src" + File.separator + "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + moduleName + File.separator + className + "Dao.xml";
        }

//        if (template.contains("list.html.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "templates" + File.separator + "modules" + File.separator + moduleName + File.separator + className.toLowerCase() + ".html";
//        }
//
//        if (template.contains("list.js.vm")) {
//            return "main" + File.separator + "resources" + File.separator + "statics" + File.separator + "js" + File.separator + "modules" + File.separator + moduleName + File.separator + className.toLowerCase() + ".js";
//        }

        if (template.contains("menu.sql.vm")) {
            return className.toLowerCase() + "_menu.sql";
        }

        String dirName = className;
        if (openFrontLowercase) {
            dirName = StringUtils.lowerCase(className);
        }

        if (template.contains("index.vue.ftl")) {
            return "src" + File.separator + "view" + File.separator + moduleName + File.separator + dirName + File.separator + "index.vue";
        }
        if (template.contains("edit.vue.ftl")) {
            return "src" + File.separator + "view" + File.separator + moduleName + File.separator + dirName + File.separator + "edit.vue";
        }
        if (template.contains("api.js.ftl")) {
            return "src" + File.separator + "api" + File.separator + dirName + ".js";
        }
        return null;
    }

    /**
     * 生成代码到目录
     *
     * @param configuration
     * @param table
     * @param columns
     */
    public static void generatorCodeToDirect(Configuration configuration, Map<String, String> table, List<Map<String, String>> columns) {
        try {
            generatorCode(configuration, table, columns, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }
}
