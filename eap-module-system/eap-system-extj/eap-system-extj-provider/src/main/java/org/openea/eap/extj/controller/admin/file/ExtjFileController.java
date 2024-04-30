package org.openea.eap.extj.controller.admin.file;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openea.eap.extj.base.ActionResult;
import org.openea.eap.extj.base.NoDataSourceBind;
import org.openea.eap.extj.base.vo.DownloadVO;
import org.openea.eap.extj.config.ConfigValueUtil;
import org.openea.eap.extj.constant.FileTypeConstant;
import org.openea.eap.extj.constant.MsgCode;
import org.openea.eap.extj.consts.DeviceType;
import org.openea.eap.extj.exception.DataException;
import org.openea.eap.extj.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 文件控制器
 *
 */
@Tag(name = "公共", description = "file")
@RestController
@RequestMapping("/extjfile")
public class ExtjFileController {

    @Autowired
    private ConfigValueUtil configValueUtil;


    /**
     * 获取下载文件链接
     *
     * @return
     */
    @NoDataSourceBind()
    @Operation(summary = "获取下载文件链接")
    @GetMapping("/Download/{type}/{fileName}")
    @Parameters({
            @Parameter(name = "type", description = "类型",required = true),
            @Parameter(name = "fileName", description = "文件名称",required = true),
    })
    public ActionResult downloadUrl(@PathVariable("type") String type, @PathVariable("fileName") String fileName) {
        type = XSSEscape.escape(type);
        fileName = XSSEscape.escape(fileName);
        boolean exists = FileUploadUtils2.exists(type, fileName);
        if (exists) {
            DownloadVO vo = DownloadVO.builder().name(fileName).url(UploaderUtil.uploaderFile(fileName + "#" + type)).build();
            return ActionResult.success(vo);
        }
        return ActionResult.fail(MsgCode.FA018.get());
    }

    /**
     * 下载文件链接
     *
     * @return
     */
    @NoDataSourceBind()
    @Operation(summary = "下载文件链接")
    @GetMapping("/Download")
    public void downloadFile() throws DataException {
        HttpServletRequest request = ServletUtil.getRequest();
        String reqJson = request.getParameter("encryption");
        String name = request.getParameter("name");
        String fileNameAll = DesUtil.aesDecode(reqJson);
        if (!StringUtil.isEmpty(fileNameAll)) {
            fileNameAll = fileNameAll.replaceAll("\n", "");
            String[] data = fileNameAll.split("#");
            String cacheKEY = data.length > 0 ? data[0] : "";
            String fileName = data.length > 1 ? data[1] : "";
            String type = data.length > 2 ? data[2] : "";
            Object ticketObj = TicketUtil.parseTicket(cacheKEY);
            //验证缓存
            if (ticketObj != null) {
                //某些手机浏览器下载后会有提示窗口, 会访问两次下载地址
                if(UserProvider.getDeviceForAgent().equals(DeviceType.APP) && "".equals(ticketObj)){
                    //TicketUtil.updateTicket(cacheKEY, "1", 30L);
                }else{
                    TicketUtil.deleteTicket(cacheKEY);
                }
                //下载文件
                String typePath =FilePathUtil.getFilePath(type.toLowerCase());
                if(fileName.indexOf(",") >= 0) {
                    typePath += fileName.substring(0, fileName.lastIndexOf(",")+1).replaceAll(",", "/");
                    fileName = fileName.substring(fileName.lastIndexOf(",")+1);
                }
//                String filePath = FilePathUtil.getFilePath(type.toLowerCase());
                byte[] bytes = FileUploadUtils2.downloadFileByte(typePath, fileName, false);
                FileDownloadUtil.downloadFile(bytes, fileName, name);
                if(FileTypeConstant.FILEZIPDOWNTEMPPATH.equals(type)) { //删除打包的临时文件，释放存储
                    FileUploadUtils2.deleteFileByPathAndFileName(typePath, fileName);
                }
            } else {
                if(FileTypeConstant.FILEZIPDOWNTEMPPATH.equals(type)) { //删除打包的临时文件，释放存储
                    String typePath = FilePathUtil.getFilePath(type);
                    FileUploadUtils2.deleteFileByPathAndFileName(typePath, fileName);
                }
                throw new DataException("链接已失效");
            }
        } else {
            throw new DataException("链接已失效");
        }
    }

    /**
     * 下载文件链接
     *
     * @return
     */
    @NoDataSourceBind()
    @Operation(summary = "下载模板文件链接")
    @GetMapping("/DownloadModel")
    public void downloadModel() throws DataException {
        HttpServletRequest request = ServletUtil.getRequest();
        String reqJson = request.getParameter("encryption");
        String fileNameAll = DesUtil.aesDecode(reqJson);
        if (!StringUtil.isEmpty(fileNameAll)) {
            String token = fileNameAll.split("#")[0];
            if (TicketUtil.parseTicket(token) != null) {
                TicketUtil.deleteTicket(token);
                String fileName = fileNameAll.split("#")[1];
                String filePath = configValueUtil.getTemplateFilePath();
                // 下载文件
                byte[] bytes = FileUploadUtils2.downloadFileByte(filePath, fileName, true);
                FileDownloadUtil.downloadFile(bytes, fileName, null);
            } else {
                throw new DataException("链接已失效");
            }
        } else {
            throw new DataException("链接已失效");
        }
    }

}
