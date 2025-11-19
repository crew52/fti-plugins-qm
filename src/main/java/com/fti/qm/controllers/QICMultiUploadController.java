package com.fti.qm.controllers;

import com.fti.qm.constants.QICAttachmentFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSHAttachmentFields;
import com.fti.qm.controllers.base.AbstractQualityStandardUploadController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/qm")
public class QICMultiUploadController extends AbstractQualityStandardUploadController {
    @Override
    protected String getHeaderModelName() {
        return QMConstants.MODEL_QUALITY_INSPECTION_COMMAND;
    }

    @Override
    protected String getAttachmentModelName() {
        return QMConstants.MODEL_QIC_ATTACHMENT;
    }

    @Override
    protected String getForeignKeyField() {
        return QICAttachmentFields.QUALITY_INSPECTION_COMMAND;
    }

    @Override
    protected Class<?> getAttachmentFieldsClass() {
        return QICAttachmentFields.class;
    }

    @ResponseBody
    @RequestMapping(value = "/multiUploadForQICFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        handleMultiUpload(request, "qicId");
    }

    @RequestMapping(value = "/getQICAttachment.html", method = RequestMethod.GET)
    public void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        handleGetAttachment(ids, response);
    }
}
