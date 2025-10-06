package com.fti.qm.controllers;

import com.fti.qm.constants.outgoingQualityStandard.OQSHAttachmentFields;
import com.google.common.io.Files;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.file.FileService;
import com.fti.qm.constants.QMConstants;
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
public class OQSHMultiUploadController {

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    private static final Integer L_SCALE = 2;

    @ResponseBody
    @RequestMapping(value = "/multiUploadForOutgoingFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        Long oQSHId = Long.parseLong(request.getParameter("oQSHId"));

        Entity oQSH = dataDefinitionService
                .get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_OUTGOING_QUALITY_STANDARD_H)
                .get(oQSHId);

        DataDefinition attachmentDD = dataDefinitionService
                .get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_OQSH_ATTACHMENT);

        Iterator<String> itr = request.getFileNames();
        while (itr.hasNext()) {
            MultipartFile mpf = request.getFile(itr.next());

            try {
                String path = fileService.upload(mpf);
                Entity attachment = attachmentDD.create();
                attachment.setField(OQSHAttachmentFields.ATTACHMENT, path);
                attachment.setField(OQSHAttachmentFields.NAME, mpf.getOriginalFilename());
                attachment.setField(OQSHAttachmentFields.OUTGOING_QUALITY_STANDARD_H, oQSH); // Đổi tên field
                attachment.setField(OQSHAttachmentFields.EXT, Files.getFileExtension(path));

                BigDecimal fileSize = new BigDecimal(mpf.getSize(), numberService.getMathContext());
                BigDecimal size = fileSize.divide(new BigDecimal(1024), L_SCALE, BigDecimal.ROUND_HALF_UP);
                attachment.setField(OQSHAttachmentFields.SIZE, size);

                attachmentDD.save(attachment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/getOutgoingAttachment.html", method = RequestMethod.GET)
    public final void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        DataDefinition attachmentDD = dataDefinitionService
                .get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_OQSH_ATTACHMENT);

        Entity attachment = attachmentDD.get(ids[0]);
        InputStream is = fileService.getInputStream(attachment.getStringField(OQSHAttachmentFields.ATTACHMENT));

        try {
            if (is == null) {
                response.sendRedirect("/error.html?code=404");
                return;
            }

            response.setHeader("Content-disposition", "inline; filename=" + attachment.getStringField(OQSHAttachmentFields.NAME));
            response.setContentType(fileService.getContentType(attachment.getStringField(OQSHAttachmentFields.ATTACHMENT)));
            int bytes = IOUtils.copy(is, response.getOutputStream());
            response.setContentLength(bytes);
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
