package se.sundsvall.smloader.service;

import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.openemapper.attachment.Attachment;
import se.sundsvall.smloader.integration.openemapper.attachment.AttachmentMultiPartFile;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

import java.util.List;

import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;

@Service
public class AttachmentService {

	private final OpenEService openEService;
	private final SupportManagementClient supportManagementClient;

	public AttachmentService(final OpenEService openEService, final SupportManagementClient supportManagementClient) {
		this.openEService = openEService;
		this.supportManagementClient = supportManagementClient;
	}

	public void handleAttachments(final byte[] xml, final CaseEntity caseEntity, final String errandId) {
		final var result = getFileIds(xml);

		result.forEach(attachment -> {
			final var filesAsBytes = openEService.getFile(caseEntity.getExternalCaseId(), attachment.getFileId(), attachment.getQueryId(), caseEntity.getCaseMetaData().getInstance());
			final var multiPartFile = AttachmentMultiPartFile.create(attachment, filesAsBytes);
			supportManagementClient.createAttachment(caseEntity.getCaseMetaData().getMunicipalityId(), caseEntity.getCaseMetaData().getNamespace(), errandId, multiPartFile);
		});
	}

	private List<Attachment> getFileIds(final byte[] xml) {
		final var result = evaluateXPath(xml, "/FlowInstance/Values//File");

		return result.stream()
			.map(fileElement -> Attachment.create()
				.withFileId(evaluateXPath(fileElement, "/Value").text())
				.withFileName(evaluateXPath(fileElement, "/Name").text())
				.withQueryId(evaluateXPath(fileElement.parent(), "/QueryID").text()))
			.toList();
	}

}
