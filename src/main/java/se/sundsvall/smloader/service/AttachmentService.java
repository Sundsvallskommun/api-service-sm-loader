package se.sundsvall.smloader.service;

import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.openemapper.Attachment;

import java.util.List;

import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;

public class AttachmentService {

	private final OpenEService openEService;

	public AttachmentService(final OpenEService openEService) {
		this.openEService = openEService;
	}

	public void handleAttachments(final byte[] xml, final CaseEntity caseEntity) {
		final var result = getFileIds(xml);

		result.forEach(attachment -> {
			final var filesAsBytes = openEService.getFile(caseEntity.getExternalCaseId(), attachment.getFileId(), attachment.getQueryId(), caseEntity.getCaseMetaData().getInstance());
			// TODO: send filesAsBytes to support management (UF-10847)
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
