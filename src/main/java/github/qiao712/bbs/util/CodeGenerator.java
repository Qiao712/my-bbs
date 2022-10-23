package github.qiao712.bbs.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://114.116.245.83:3306/my-bbs?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "lty0712";
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("qiao712")   // 设置作者
                            .enableSwagger()    // 开启 swagger 模式
                            .outputDir("D://desktop//gen"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("github.qiao712.bbs") // 设置父包名
//                            .moduleName("") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "D://desktop//gen")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("t_conversation") // 设置需要生成的表名
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}
