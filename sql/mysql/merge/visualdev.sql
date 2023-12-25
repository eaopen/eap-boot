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
