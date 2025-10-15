package com.fti.qm.controllers.base;

import com.fti.qm.constants.QMConstants;
import com.google.common.io.Files;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.file.FileService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;

public abstract  class AbstractQualityStandardUploadController {
    @Autowired
    protected FileService fileService;

    @Autowired
    protected DataDefinitionService dataDefinitionService;

    @Autowired
    protected NumberService numberService;

    protected static final Integer L_SCALE = 2;

    // abstract để subclass truyền vào các model/field cụ thể
    protected abstract String getHeaderModelName();
    protected abstract String getAttachmentModelName();
    protected abstract String getForeignKeyField();
    protected abstract Class<?> getAttachmentFieldsClass();

    // xử lý upload file chung
    protected void handleMultiUpload(MultipartHttpServletRequest request, String idParamName) {
        Long headerId = Long.parseLong(request.getParameter(idParamName));
        Entity header = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, getHeaderModelName()).get(headerId);
        DataDefinition attachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, getAttachmentModelName());

        Iterator<String> itr = request.getFileNames();
        while (itr.hasNext()) {
            MultipartFile mpf = request.getFile(itr.next());
            try {
                String path = fileService.upload(mpf);
                Entity attachment = attachmentDD.create();

                // Reflection để lấy field constants
                setAttachmentFields(attachment, mpf, header, path);

                attachmentDD.save(attachment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setAttachmentFields(Entity attachment, MultipartFile mpf, Entity header, String path) {
        try {
            Class<?> fieldsClass = getAttachmentFieldsClass();
            attachment.setField(fieldsClass.getField("ATTACHMENT").get(null).toString(), path);
            attachment.setField(fieldsClass.getField("NAME").get(null).toString(), mpf.getOriginalFilename());
            attachment.setField(fieldsClass.getField("EXT").get(null).toString(), Files.getFileExtension(path));
            attachment.setField(fieldsClass.getField("SIZE").get(null).toString(),
                    new BigDecimal(mpf.getSize(), numberService.getMathContext())
                            .divide(new BigDecimal(1024), L_SCALE, BigDecimal.ROUND_HALF_UP));
            attachment.setField(getForeignKeyField(), header);
        } catch (Exception e) {
            throw new RuntimeException("Error setting attachment fields", e);
        }
    }

    protected void handleGetAttachment(Long[] ids, HttpServletResponse response) {
        DataDefinition attachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, getAttachmentModelName());
        Entity attachment = attachmentDD.get(ids[0]);
        InputStream is = fileService.getInputStream(
                attachment.getStringField(getFieldValue("ATTACHMENT"))
        );

        try {
            if (is == null) {
                response.sendRedirect("/error.html?code=404");
                return;
            }

            response.setHeader("Content-disposition", "inline; filename=" + attachment.getStringField(getFieldValue("NAME")));
            response.setContentType(fileService.getContentType(attachment.getStringField(getFieldValue("ATTACHMENT"))));

            int bytes = IOUtils.copy(is, response.getOutputStream());
            response.setContentLength(bytes);
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFieldValue(String fieldName) {
        try {
            return (String) getAttachmentFieldsClass().getField(fieldName).get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
