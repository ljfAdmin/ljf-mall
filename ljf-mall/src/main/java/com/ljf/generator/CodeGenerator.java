package com.ljf.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.ljf.constant.mysql.MysqlGeneratorMagicValueConstant;

public class CodeGenerator {
    public static void main(String[] args) {
        AutoGenerator mpg = new AutoGenerator();

        // 2、全局配置
        GlobalConfig gc = new GlobalConfig();
        // String projectPath = System.getProperty("user.dir");
        // gc.setOutputDir(projectPath + "/src/main/java");
        gc.setOutputDir(MysqlGeneratorMagicValueConstant.OUTPUT_DIR + MysqlGeneratorMagicValueConstant.OUTPUT_DIR_MODULE);
        gc.setAuthor(MysqlGeneratorMagicValueConstant.AUTHOR);
        gc.setOpen(false); //生成后是否打开资源管理器
        gc.setFileOverride(false); //重新生成时文件是否覆盖

        gc.setServiceName(MysqlGeneratorMagicValueConstant.SERVICE_NAME);	//去掉Service接口的首字母I
        gc.setIdType(IdType.AUTO); //主键策略
        gc.setDateType(DateType.ONLY_DATE);//定义生成的实体类中日期类型
        gc.setSwagger2(true);//开启Swagger2模式

        mpg.setGlobalConfig(gc);

        // 3、数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(MysqlGeneratorMagicValueConstant.MYSQL_URL);
        dsc.setDriverName(MysqlGeneratorMagicValueConstant.MYSQL_DRIVER_NAME);
        dsc.setUsername(MysqlGeneratorMagicValueConstant.MYSQL_USERNAME);
        dsc.setPassword(MysqlGeneratorMagicValueConstant.MYSQL_PASSWORD);
        dsc.setDbType(DbType.MYSQL);
        mpg.setDataSource(dsc);

        // 4、包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent(MysqlGeneratorMagicValueConstant.PARENT_PACKAGE);
        pc.setModuleName(MysqlGeneratorMagicValueConstant.MODULE_NAME); //模块名
        pc.setController(MysqlGeneratorMagicValueConstant.CONTROLLER);
        pc.setEntity(MysqlGeneratorMagicValueConstant.ENTITY);
        pc.setService(MysqlGeneratorMagicValueConstant.SERVICE);
        pc.setMapper(MysqlGeneratorMagicValueConstant.MAPPER);
        mpg.setPackageInfo(pc);

        // 5、策略配置
        StrategyConfig strategy = new StrategyConfig();
        String[] tables = new String[MysqlGeneratorMagicValueConstant.TABLE_NAMES.size()];
        int index = 0;
        for (String tableName : MysqlGeneratorMagicValueConstant.TABLE_NAMES) {
            tables[index++] = tableName;
        }
        strategy.setInclude(tables);
        strategy.setNaming(NamingStrategy.underline_to_camel);//数据库表映射到实体的命名策略
        strategy.setTablePrefix(pc.getModuleName() + "_"); //生成实体时去掉表前缀

        strategy.setColumnNaming(NamingStrategy.underline_to_camel);//数据库表字段映射到实体的命名策略
        strategy.setEntityLombokModel(true); // lombok 模型 @Accessors(chain = true) setter链式操作

        strategy.setRestControllerStyle(true); //restful api风格控制器
        strategy.setControllerMappingHyphenStyle(true); //url中驼峰转连字符

        mpg.setStrategy(strategy);

        // 6、执行
        mpg.execute();
    }

}
