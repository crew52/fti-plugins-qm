package com.fti.qm.listeners;

import com.fti.qm.constants.IQSHAttachmentFields;
import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class IQSHAttachmentsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(IQSHAttachmentsListeners.class);

    @Autowired
    private FileService fileService;

    public void downloadAttachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("attachmentsGrid");

        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().isEmpty()) {
            state.addMessage("qm.incomingQualityStandardHDetails.window.ribbon.attachments.nonSelectedAttachment",
                    ComponentState.MessageType.INFO);
            return;
        }

        List<Entity> selectedAttachments = grid.getSelectedEntities();

        if (selectedAttachments.size() == 1) {
            Entity attachment = selectedAttachments.get(0);
            File file = new File(attachment.getStringField(IQSHAttachmentFields.ATTACHMENT));

            if (!file.exists()) {
                state.addMessage("qm.incomingQualityStandardHDetails.window.ribbon.attachments.fileNotFound",
                        ComponentState.MessageType.FAILURE);
                return;
            }

            view.redirectTo(fileService.getUrl(file.getAbsolutePath()), true, false);
        } else {
            List<File> filesToZip = Lists.newArrayList();

            for (Entity attachment : selectedAttachments) {
                File file = new File(attachment.getStringField(IQSHAttachmentFields.ATTACHMENT));

                if (file.exists()) {
                    filesToZip.add(file);
                } else {
                    LOG.warn("File not found for attachment entity ID: {}", attachment.getId());
                }
            }

            if (filesToZip.isEmpty()) {
                state.addMessage("qm.incomingQualityStandardHDetails.window.ribbon.attachments.noValidFiles",
                        ComponentState.MessageType.FAILURE);
                return;
            }

            try {
                File zipFile = fileService.compressToZipFile(filesToZip, false);
                view.redirectTo(fileService.getUrl(zipFile.getAbsolutePath()) + "?clean", true, false);
            } catch (IOException e) {
                LOG.error("Error while compressing files to zip", e);
                state.addMessage("qm.incomingQualityStandardHDetails.window.ribbon.attachments.zipError",
                        ComponentState.MessageType.FAILURE);
            }
        }

        // Optional: Reset grid selection
        state.performEvent(view, "reset", new String[0]);
    }
}
