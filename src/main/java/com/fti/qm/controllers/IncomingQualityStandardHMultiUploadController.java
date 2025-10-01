package com.fti.qm.controllers;

import com.google.common.io.Files;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.file.FileService;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.IQSHAttachmentFields;
import org.apache.commons.io.IOUtils;
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
public class IncomingQualityStandardHMultiUploadController {

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    private static final Integer L_SCALE = 2;

    @ResponseBody
    @RequestMapping(value = "/multiUploadFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        Long iQSHId = Long.parseLong(request.getParameter("iQSHId"));

        Entity iQSH = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_INCOMING_QUALITY_STANDARD_H).get(iQSHId);
        DataDefinition attachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_IQSH_ATTACHMENT);

        Iterator<String> itr = request.getFileNames();
        while (itr.hasNext()) {
            MultipartFile mpf = request.getFile(itr.next());

            try {
                String path = fileService.upload(mpf);
                Entity attachment = attachmentDD.create();
                attachment.setField(IQSHAttachmentFields.ATTACHMENT, path);
                attachment.setField(IQSHAttachmentFields.NAME, mpf.getOriginalFilename());
                attachment.setField(IQSHAttachmentFields.INCOMING_QUALITY_STANDARD_H, iQSH);
                attachment.setField(IQSHAttachmentFields.EXT, Files.getFileExtension(path));

                BigDecimal fileSize = new BigDecimal(mpf.getSize(), numberService.getMathContext());
                BigDecimal size = fileSize.divide(new BigDecimal(1024), L_SCALE, BigDecimal.ROUND_HALF_UP);
                attachment.setField(IQSHAttachmentFields.SIZE, size);

                attachmentDD.save(attachment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/getAttachment.html", method = RequestMethod.GET)
    public final void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        DataDefinition attachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_IQSH_ATTACHMENT);
        Entity attachment = attachmentDD.get(ids[0]);
        InputStream is = fileService.getInputStream(attachment.getStringField(IQSHAttachmentFields.ATTACHMENT));

        try {
            if (is == null) {
                response.sendRedirect("/error.html?code=404");
                return;
            }

            response.setHeader("Content-disposition", "inline; filename=" + attachment.getStringField(IQSHAttachmentFields.NAME));
            response.setContentType(fileService.getContentType(attachment.getStringField(IQSHAttachmentFields.ATTACHMENT)));
            int bytes = IOUtils.copy(is, response.getOutputStream());
            response.setContentLength(bytes);
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}