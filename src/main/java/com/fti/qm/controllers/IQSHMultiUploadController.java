package com.fti.qm.controllers;

import com.fti.qm.controllers.base.AbstractQualityStandardUploadController;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.IQSHAttachmentFields;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/qm")
public class IQSHMultiUploadController extends AbstractQualityStandardUploadController {
    @Override
    protected String getHeaderModelName() {
        return QMConstants.MODEL_INCOMING_QUALITY_STANDARD_H;
    }

    @Override
    protected String getAttachmentModelName() {
        return QMConstants.MODEL_IQSH_ATTACHMENT;
    }

    @Override
    protected String getForeignKeyField() {
        return IQSHAttachmentFields.INCOMING_QUALITY_STANDARD_H;
    }

    @Override
    protected Class<?> getAttachmentFieldsClass() {
        return IQSHAttachmentFields.class;
    }

    @ResponseBody
    @RequestMapping(value = "/multiUploadForIncomingFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        handleMultiUpload(request, "iQSHId");
    }

    @RequestMapping(value = "/getIncomingAttachment.html", method = RequestMethod.GET)
    public void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        handleGetAttachment(ids, response);
    }
}