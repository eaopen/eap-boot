package org.openea.eap.framework.i18n.core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class EapMessageResource extends StaticMessageSource implements ResourceLoaderAware, InitializingBean {

    private MessageSource messageSource;

    public EapMessageResource(){

    }

    public EapMessageResource(MessageSource messageSource){
        this.messageSource = messageSource;
        if(this.messageSource!=null){
            this.setParentMessageSource(this.messageSource);
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO
    }
}
