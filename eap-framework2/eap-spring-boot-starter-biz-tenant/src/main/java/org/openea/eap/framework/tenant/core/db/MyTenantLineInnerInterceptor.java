package org.openea.eap.framework.tenant.core.db;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import org.openea.eap.framework.tenant.config.TenantProperties;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyTenantLineInnerInterceptor extends TenantLineInnerInterceptor {

    private final Set<String> extjTables = new HashSet<>();
    private final String extjTenantColumn;

    public MyTenantLineInnerInterceptor(TenantDatabaseInterceptor tenantDatabaseInterceptor, TenantProperties properties) {
        super(tenantDatabaseInterceptor);
        extjTables.addAll(properties.getExtjTables());
        extjTenantColumn = properties.getExtjTenantColumn();
    }

    protected boolean isExtjTable(String tableName){
        return extjTables.contains(tableName);
    }
    protected String getTenantIdColumn(String tableName){
        String tenantIdColumn = this.getTenantLineHandler().getTenantIdColumn();
        if(isExtjTable(tableName)){
            tenantIdColumn = extjTenantColumn;
        }
        return tenantIdColumn;
    }

    protected void processInsert(Insert insert, int index, String sql, Object obj) {
        if (!this.getTenantLineHandler().ignoreTable(insert.getTable().getName())) {
            List<Column> columns = insert.getColumns();
            if (!CollectionUtils.isEmpty(columns)) {
                String tenantIdColumn = getTenantIdColumn(insert.getTable().getName());
                if (!this.getTenantLineHandler().ignoreInsert(columns, tenantIdColumn)) {
                    columns.add(new Column(tenantIdColumn));
                    List<Expression> duplicateUpdateColumns = insert.getDuplicateUpdateExpressionList();
                    if (CollectionUtils.isNotEmpty(duplicateUpdateColumns)) {
                        EqualsTo equalsTo = new EqualsTo();
                        equalsTo.setLeftExpression(new StringValue(tenantIdColumn));
                        equalsTo.setRightExpression(this.getTenantLineHandler().getTenantId());
                        duplicateUpdateColumns.add(equalsTo);
                    }

                    Select select = insert.getSelect();
                    if (select != null && select.getSelectBody() instanceof PlainSelect) {
                        this.processInsertSelect(select.getSelectBody(), (String)obj);
                    } else {
                        if (insert.getItemsList() == null) {
                            throw ExceptionUtils.mpe("Failed to process multiple-table update, please exclude the tableName or statementId", new Object[0]);
                        }

                        ItemsList itemsList = insert.getItemsList();
                        Expression tenantId = this.getTenantLineHandler().getTenantId();
                        if (itemsList instanceof MultiExpressionList) {
                            ((MultiExpressionList)itemsList).getExpressionLists().forEach((el) -> {
                                el.getExpressions().add(tenantId);
                            });
                        } else {
                            List<Expression> expressions = ((ExpressionList)itemsList).getExpressions();
                            if (CollectionUtils.isNotEmpty(expressions)) {
                                int len = expressions.size();

                                for(int i = 0; i < len; ++i) {
                                    Expression expression = (Expression)expressions.get(i);
                                    if (expression instanceof RowConstructor) {
                                        ((RowConstructor)expression).getExprList().getExpressions().add(tenantId);
                                    } else if (expression instanceof Parenthesis) {
                                        RowConstructor rowConstructor = (new RowConstructor()).withExprList(new ExpressionList(new Expression[]{((Parenthesis)expression).getExpression(), tenantId}));
                                        expressions.set(i, rowConstructor);
                                    } else if (len - 1 == i) {
                                        expressions.add(tenantId);
                                    }
                                }
                            } else {
                                expressions.add(tenantId);
                            }
                        }
                    }

                }
            }
        }
    }

    protected void processInsertSelect(SelectBody selectBody, final String whereSegment) {
        PlainSelect plainSelect = (PlainSelect)selectBody;
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table) {
            this.processPlainSelect(plainSelect, whereSegment);
            this.appendSelectItem(plainSelect.getSelectItems(), ((Table)fromItem).getName());
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect)fromItem;
            // will fix check table?
            this.appendSelectItem2(plainSelect.getSelectItems());
            this.processInsertSelect(subSelect.getSelectBody(), whereSegment);
        }
    }

    protected void appendSelectItem2(List<SelectItem> selectItems) {
        if (!CollectionUtils.isEmpty(selectItems)) {
           // todo
            throw new UnsupportedOperationException("appendSelectItem2");
        }
    }
    protected void appendSelectItem(List<SelectItem> selectItems, String tableName) {
        if (!CollectionUtils.isEmpty(selectItems)) {
            if (selectItems.size() == 1) {
                SelectItem item = (SelectItem)selectItems.get(0);
                if (item instanceof AllColumns || item instanceof AllTableColumns) {
                    return;
                }
            }
            // fix TenantIdColumn
            selectItems.add(new SelectExpressionItem(new Column(getTenantIdColumn(tableName))));
        }
    }

    @Override
    protected Column getAliasColumn(Table table) {
        StringBuilder column = new StringBuilder();
        if (table.getAlias() != null) {
            column.append(table.getAlias().getName()).append(".");
        }
        column.append(getTenantIdColumn(table.getName()));
        return new Column(column.toString());
    }
}
