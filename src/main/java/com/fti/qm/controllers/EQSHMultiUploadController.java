package com.fti.qm.controllers;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.equipmentQualityStandardH.EQSHAttachmentFields;
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
public class EQSHMultiUploadController extends AbstractQualityStandardUploadController{
    @Override
    protected String getHeaderModelName() {
        return QMConstants.MODEL_EQUIPMENT_QUALITY_STANDARD_H;
    }

    @Override
    protected String getAttachmentModelName() {
        return QMConstants.MODEL_EQSH_ATTACHMENT;
    }

    @Override
    protected String getForeignKeyField() {
        return EQSHAttachmentFields.EQUIPMENT_QUALITY_STANDARD_H;
    }

    @Override
    protected Class<?> getAttachmentFieldsClass() {
        return EQSHAttachmentFields.class;
    }

    @ResponseBody
    @RequestMapping(value = "/multiUploadForEquipmentFiles", method = RequestMethod.POST)
    public void upload(final MultipartHttpServletRequest request, final HttpServletResponse response) {
        handleMultiUpload(request, "eQSHId");
    }

    @RequestMapping(value = "/getEquipmentAttachment.html", method = RequestMethod.GET)
    public void getAttachment(@RequestParam("id") final Long[] ids, final HttpServletResponse response) {
        handleGetAttachment(ids, response);
    }
}
