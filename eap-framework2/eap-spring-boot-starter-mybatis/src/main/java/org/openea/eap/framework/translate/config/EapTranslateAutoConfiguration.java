package org.openea.eap.framework.translate.config;

import com.fhs.trans.service.impl.TransService;
import org.openea.eap.framework.translate.core.TranslateUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class EapTranslateAutoConfiguration {

    @Bean
    @SuppressWarnings({"InstantiationOfUtilityClass", "SpringJavaInjectionPointsAutowiringInspection"})
    public TranslateUtils translateUtils(TransService transService) {
        TranslateUtils.init(transService);
        return new TranslateUtils();
    }

}
