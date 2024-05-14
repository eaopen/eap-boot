-- 增加国际化字段 hasI18n(F_HasI18n)
ALTER TABLE `base_visual_dev`
    ADD COLUMN `F_HasI18n` int NULL COMMENT '启用国际化' AFTER `F_Web_Type`;

ALTER TABLE `base_visualdev_release`
    ADD COLUMN `F_HasI18n` int NULL COMMENT '启用国际化' AFTER `F_Web_Type`;

-- merge jeecg/jnpf data

update base_visual_dev t
set t.F_Form_Data=REPLACE(t.F_Form_Data,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_Form_Data)>0;

update base_visual_dev t
set t.F_Column_Data=REPLACE(t.F_Column_Data,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_Column_Data)>0;

update base_visual_dev t
set t.F_App_Column_Data=REPLACE(t.F_App_Column_Data,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_App_Column_Data)>0;

update base_visual_release t
set t.F_Form_Data=REPLACE(t.F_Form_Data,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_Form_Data)>0;

update base_visual_release t
set t.F_Column_Data=REPLACE(t.F_Column_Data,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_Column_Data)>0;

update base_visual_release t
set t.F_App_Column_Data=REPLACE(t.F_App_Column_Data,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_App_Column_Data)>0;


update base_portal_data t
set t.F_Form_Data=REPLACE(t.F_Form_Data,'jnpfKey','extnKey')
where locate('jnpfKey',t.F_Form_Data)>0;
