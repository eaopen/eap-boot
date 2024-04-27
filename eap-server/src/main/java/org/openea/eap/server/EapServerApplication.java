package org.openea.eap.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目的启动类
 *
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${eap.info.base-package}
//@SpringBootApplication(scanBasePackages = {"${eap.info.base-package}.server", "${eap.info.base-package}.module"})
@ComponentScan(basePackages={"org.openea.eap"
        //,"org.openbpm",
        //,"${eap.info.base-package}.server", "${eap.info.base-package}.module"
},
        excludeFilters={
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                })
        })
@SpringBootApplication(scanBasePackages = {"${eap.info.base-package}.server", "${eap.info.base-package}.module"})
@EnableScheduling // 启用定时任务
public class EapServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(EapServerApplication.class, args);
//        new SpringApplicationBuilder(EapServerApplication.class)
//                .applicationStartup(new BufferingApplicationStartup(20480))
//                .run(args);

    }

}
