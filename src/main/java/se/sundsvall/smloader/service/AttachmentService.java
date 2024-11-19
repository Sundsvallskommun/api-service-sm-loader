package se.sundsvall.smloader.service;

import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.openemapper.attachment.Attachment;
import se.sundsvall.smloader.integration.openemapper.attachment.AttachmentMultiPartFile;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

@Service
public class AttachmentService {

	private static final Logger log = Logger.getLogger(AttachmentService.class.getName());
	private final OpenEService openEService;
	private final SupportManagementClient supportManagementClient;

	public AttachmentService(final OpenEService openEService, final SupportManagementClient supportManagementClient) {
		this.openEService = openEService;
		this.supportManagementClient = supportManagementClient;
	}

	public List<String> handleAttachments(final byte[] xml, final CaseEntity caseEntity, final String errandId) {
		return getFileIds(xml).stream()
			.map(attachment -> {
				final var filesAsBytes = openEService.getFile(caseEntity.getExternalCaseId(), attachment.getFileId(), attachment.getQueryId(), caseEntity.getCaseMetaData().getInstance());

				if (filesAsBytes == null) {
					log.info("Failed to fetch file for case: " + caseEntity.getExternalCaseId() + " with file id: " + attachment.getFileId());
					return attachment.getFileId();
				}

				final var multiPartFile = AttachmentMultiPartFile.create(attachment, filesAsBytes);
				final var response = supportManagementClient.createAttachment(caseEntity.getCaseMetaData().getMunicipalityId(), caseEntity.getCaseMetaData().getNamespace(), errandId, multiPartFile);

				if (response.getStatusCode().isError()) {
					log.info("Failed to create attachment for case: " + caseEntity.getExternalCaseId() + " with file id: " + attachment.getFileId());
					return attachment.getFileId();
				}

				return null;
			})
			.filter(Objects::nonNull)
			.toList();

	}

	private List<Attachment> getFileIds(final byte[] xml) {
		final var result = evaluateXPath(xml, "/FlowInstance/Values//File");

		return result.stream()
			.map(fileElement -> Attachment.create()
				.withFileId(evaluateXPath(fileElement, "/ID").text())
				.withFileName(evaluateXPath(fileElement, "/Name").text())
				.withQueryId(evaluateXPath(fileElement.parent(), "/QueryID").text()))
			.toList();
	}

}
