-- 增加国际化字段 hasI18n(F_HasI18n)
ALTER TABLE `base_visualdev`
    ADD COLUMN `F_HasI18n` int NULL COMMENT '启用国际化' AFTER `F_WebType`;

ALTER TABLE `base_visualdev_release`
    ADD COLUMN `F_HasI18n` int NULL COMMENT '启用国际化' AFTER `F_WebType`;

-- merge jeecg/jnpf data

update base_visualdev t
set t.F_FormData=REPLACE(t.F_FormData,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_FormData)>0;

update base_visualdev t
set t.F_ColumnData=REPLACE(t.F_ColumnData,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_ColumnData)>0;

update base_visualdev t
set t.F_AppColumnData=REPLACE(t.F_AppColumnData,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_AppColumnData)>0;

update base_visualdev_release t
set t.F_FormData=REPLACE(t.F_FormData,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_FormData)>0;

update base_visualdev_release t
set t.F_ColumnData=REPLACE(t.F_ColumnData,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_ColumnData)>0;

update base_visualdev_release t
set t.F_AppColumnData=REPLACE(t.F_AppColumnData,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_AppColumnData)>0;
