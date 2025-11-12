package com.fti.qm.controllers;

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
public class QSHMultiUploadController extends AbstractQualityStandardUploadController {
    @Override
    protected String getHeaderModelName() {
        return "qualityStandardH";
    }

    @Override
    protected String getAttachmentModelName() {
        return "qSHAttachment";
    }

    @Override
    protected String getForeignKeyField() {
        return "qualityStandardH";
    }

    @Override
    protected Class<?> getAttachmentFieldsClass() {
        return QSHAttachmentFields.class;
    }

    @ResponseBody
    @RequestMapping(value = "/multiUploadForQualityStandardHFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        handleMultiUpload(request, "iQSHId");
    }

    @RequestMapping(value = "/getQualityStandardHAttachment.html", method = RequestMethod.GET)
    public void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        handleGetAttachment(ids, response);
    }
}
