package org.openea.eap.extj.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.openea.eap.extj.base.entity.SuperBaseEntity;
import org.openea.eap.extj.database.model.entity.DbLinkEntity;
import org.openea.eap.extj.database.plugins.*;
import org.openea.eap.extj.database.source.DbBase;
import org.openea.eap.extj.database.util.DataSourceUtil;
import org.openea.eap.extj.database.util.DbTypeUtil;
import org.openea.eap.extj.model.MultiTenantType;
import org.openea.eap.extj.util.ClassUtil;
import org.openea.eap.extj.util.data.DataSourceContextHolder;
import org.openea.eap.framework.mybatis.config.EapMybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * MybatisPlus配置类
 * extend config
 */
@Slf4j
@Configuration
@AutoConfigureBefore(EapMybatisAutoConfiguration.class)
public class ExtjMybatisPlusConfig {

    @Bean
    public Interceptor myMasterSlaveAutoRoutingPlugin(DataSource dataSource){
        return  new MyMasterSlaveAutoRoutingPlugin(dataSource);
    }

    @Bean
    public Interceptor myDynamicDataSourceAutoRollbackInterceptor(){
        return new MyDynamicDataSourceAutoRollbackInterceptor();
    }

    @Bean
    public Interceptor resultSetInterceptor(){
        return new ResultSetInterceptor();
    }

    @Bean
    public ISqlInjector sqlInjector(){
        return new MyDefaultSqlInjector();
    }


    /**
     * 对接数据库的实体层
     */
    static final String ALIASES_PACKAGE = "org.openea.eap.*.entity;com.xxl.job.admin.core.model";

    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private ConfigValueUtil configValueUtil;


    /**
     * 服务中查询其他服务的表数据, 未引用Mapper无法初始化MybatisPlus的TableInfo对象, 无法判断逻辑删除情况, 初始化MybatisPlus所有Entity对象
     * 微服务的情况才进行扫描
     * @param sqlSessionFactory
     * @return
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient")
    public Object scanAllEntity(SqlSessionFactory sqlSessionFactory){
        Set<Class<?>> classes =  ClassUtil.scanCandidateComponents("org.openea.eap", c->
                !Modifier.isAbstract(c.getModifiers()) && SuperBaseEntity.SuperTBaseEntity.class.isAssignableFrom(c)
        );
        for (Class<?> aClass : classes) {
            MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(sqlSessionFactory.getConfiguration(), "resource");
            builderAssistant.setCurrentNamespace(aClass.getName());
            TableInfoHelper.initTableInfo(builderAssistant, aClass);
        }
        return null;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        try{
            //判断是否多租户
            if (configValueUtil.isMultiTenancy()) {
                if(configValueUtil.getMultiTenantType().eq(MultiTenantType.COLUMN)){
                    interceptor.addInnerInterceptor(myTenantLineInnerInterceptor());
                }else if(configValueUtil.getMultiTenantType().eq(MultiTenantType.SCHEMA)){
                    interceptor.addInnerInterceptor(mySchemaInnerInterceptor());
                }else{
                    throw new IllegalArgumentException("config.MultiTenantType 多租户模式设置错误, 支持：SCHEMA, COLUMN");
                }
            }
            //开启逻辑删除插件功能
            if(configValueUtil.isEnableLogicDelete()) {
                interceptor.addInnerInterceptor(myLogicDeleteInnerInterceptor());
            }
            // 新版本分页必须指定数据库，否则分页不生效
            // 不指定会动态生效 多数据源不能指定数据库类型
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

            //乐观锁
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }catch (Exception e){
            e.printStackTrace();
        }
        return interceptor;
    }


    @Bean("myLogicDeleteInnerInterceptor")
    @ConditionalOnProperty(prefix = "config", name = "EnableLogicDelete", havingValue = "true", matchIfMissing = false)
    public MyLogicDeleteInnerInterceptor myLogicDeleteInnerInterceptor(){
        MyLogicDeleteInnerInterceptor myLogicDeleteInnerInterceptor = new MyLogicDeleteInnerInterceptor();
        myLogicDeleteInnerInterceptor.setLogicDeleteHandler(new LogicDeleteHandler() {
            @Override
            public Expression getNotDeletedValue() {
                return new NullValue();
            }

            @Override
            public String getLogicDeleteColumn() {
                return configValueUtil.getLogicDeleteColumn();
            }
        });
        return myLogicDeleteInnerInterceptor;
    }

    @Bean("myTenantLineInnerInterceptor")
    @ConditionalOnProperty(prefix = "config", name = "MultiTenantType", havingValue = "COLUMN", matchIfMissing = false)
    public TenantLineInnerInterceptor myTenantLineInnerInterceptor(){
        TenantLineInnerInterceptor tenantLineInnerInterceptor = new MyTenantLineInnerInterceptor();
        tenantLineInnerInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                return new StringValue(DataSourceContextHolder.getDatasourceName());
            }

            @Override
            public String getTenantIdColumn() {
                return configValueUtil.getMultiTenantColumn();
            }
        });
        return tenantLineInnerInterceptor;
    }

    @Bean("mySchemaInnerInterceptor")
    @ConditionalOnProperty(prefix = "config", name = "MultiTenantType", havingValue = "SCHEMA", matchIfMissing = false)
    public DynamicTableNameInnerInterceptor mySchemaInnerInterceptor() throws Exception {
        DbLinkEntity dbLinkEntity = dataSourceUtil.init();
        DbBase dbBase = DbTypeUtil.getDb(dbLinkEntity);
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new MySchemaInnerInterceptor();
        HashMap<String, TableNameHandler> map = new HashMap<>(150) ;
        // null空库保护
        List<String> tableNames = new ArrayList<>() ;
//        JdbcUtil.queryCustomMods(SqlComEnum.TABLES.getPrepSqlDto(dbLinkEntity, null), DbTableFieldModel.class)
//                .forEach(dbTableModel-> tableNames.add(dbTableModel.getTable().toLowerCase()));
        //将当前连接库的所有表保存, 在列表中的表才进行切库, 所有表名转小写, 后续比对转小写
        DbBase.dynamicAllTableName = tableNames;
        dynamicTableNameInnerInterceptor.setTableNameHandler(dbBase.getDynamicTableNameHandler());
        return dynamicTableNameInnerInterceptor;
    }









}
