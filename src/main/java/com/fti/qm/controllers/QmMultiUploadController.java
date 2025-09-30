
package com.fti.qm.controllers;

import com.google.common.io.Files;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.IQSHAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.mes.basic.MultiUploadHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;

@Controller
@RequestMapping("/qm")
public class QmMultiUploadController {

    private static final Logger logger = LoggerFactory.getLogger(QmMultiUploadController.class);
    private static final int L_SCALE = 2;

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @ResponseBody
    @RequestMapping(value = "/multiUploadFiles", method = RequestMethod.POST)
    public void upload(MultipartHttpServletRequest request, HttpServletResponse response) {
        Long iqshId = Long.parseLong(request.getParameter("techId")); // Changed from iQSHId to techId to match frontend
        Entity iqsh = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_INCOMING_QUALITY_STANDARD_H)
                .get(iqshId);

        DataDefinition attachmentDD = dataDefinitionService
                .get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_IQSH_ATTACHMENT);

        Iterator<String> itr = request.getFileNames();

        while (itr.hasNext()) {
            MultipartFile mpf = request.getFile(itr.next());
            String path = "";

            try {
                path = fileService.upload(mpf);
            } catch (IOException e) {
                logger.error("Unable to upload attachment.", e);
                continue;
            }

            if (MultiUploadHelper.EXTS.contains(Files.getFileExtension(path).toUpperCase())) {
                Entity attachment = attachmentDD.create();
                attachment.setField(IQSHAttachmentFields.ATTACHMENT, path);
                attachment.setField(IQSHAttachmentFields.NAME, mpf.getOriginalFilename());
                attachment.setField(IQSHAttachmentFields.INCOMING_QUALITY_STANDARD_H, iqsh);
                attachment.setField(IQSHAttachmentFields.EXT, Files.getFileExtension(path));

                BigDecimal fileSize = new BigDecimal(mpf.getSize(), numberService.getMathContext());
                BigDecimal divider = new BigDecimal(1024, numberService.getMathContext());
                BigDecimal size = fileSize.divide(divider, L_SCALE, BigDecimal.ROUND_HALF_UP);

                attachment.setField(IQSHAttachmentFields.SIZE, size);
                attachmentDD.save(attachment);
            }
        }
    }

    @RequestMapping(value = "/getAttachment.html", method = RequestMethod.GET)
    public void getAttachment(@RequestParam("id") final Long[] ids, HttpServletResponse response) {
        DataDefinition attachmentDD = dataDefinitionService
                .get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_IQSH_ATTACHMENT);
        Entity attachment = attachmentDD.get(ids[0]);

        InputStream is = fileService.getInputStream(attachment.getStringField(IQSHAttachmentFields.ATTACHMENT));

        try {
            if (is == null) {
                response.sendRedirect("/error.html?code=404");
                return;
            }

            response.setHeader("Content-disposition",
                    "inline; filename=" + attachment.getStringField(IQSHAttachmentFields.NAME));
            response.setContentType(
                    fileService.getContentType(attachment.getStringField(IQSHAttachmentFields.ATTACHMENT)));

            int bytes = IOUtils.copy(is, response.getOutputStream());
            response.setContentLength(bytes);

            response.flushBuffer();
        } catch (IOException e) {
            logger.error("Unable to copy attachment file to response stream.", e);
        }
    }
}
