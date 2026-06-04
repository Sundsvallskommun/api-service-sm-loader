package se.sundsvall.smloader.service;

import generated.se.sundsvall.supportmanagement.ErrandAttachment;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.openemapper.attachment.Attachment;
import se.sundsvall.smloader.integration.openemapper.attachment.AttachmentMultiPartFile;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

import static se.sundsvall.smloader.integration.util.XPathUtil.evaluateXPath;

@Service
public class AttachmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentService.class);
	private final OpenEService openEService;
	private final SupportManagementClient supportManagementClient;

	public AttachmentService(final OpenEService openEService, final SupportManagementClient supportManagementClient) {
		this.openEService = openEService;
		this.supportManagementClient = supportManagementClient;
	}

	public List<String> handleAttachments(final byte[] xml, final CaseEntity caseEntity, final String errandId) {
		final var attachmentHeaders = supportManagementClient.getAttachments(
			caseEntity.getCaseMetaData().getMunicipalityId(),
			caseEntity.getCaseMetaData().getNamespace(),
			errandId);

		return getFileIds(xml).stream()
			.map(attachment -> {
				try (final var fileStream = openEService.getFile(caseEntity.getExternalCaseId(), attachment.getFileId(), attachment.getQueryId(), caseEntity.getCaseMetaData().getInstance(), caseEntity.getCaseMetaData().getMunicipalityId()).body()
					.asInputStream()) {
					if (fileStream == null) {
						LOGGER.info("Failed to fetch file for case: {} with file id: {}", caseEntity.getExternalCaseId(), attachment.getFileId());
						return attachment.getFileId();
					}
					if (!attachmentExists(attachmentHeaders, attachment, caseEntity, errandId, fileStream)) {
						final AttachmentMultiPartFile multiPartFile = AttachmentMultiPartFile.create(attachment, fileStream);
						final var response = supportManagementClient.createAttachment(caseEntity.getCaseMetaData().getMunicipalityId(), caseEntity.getCaseMetaData().getNamespace(), errandId, multiPartFile);

						if (response.getStatusCode().isError()) {
							LOGGER.info("Failed to create attachment for case: {} with file id: {}", caseEntity.getExternalCaseId(), attachment.getFileId());
							return attachment.getFileId();
						}
					}
				} catch (final Exception e) {
					LOGGER.error("Error handling attachment: {}", e.getMessage(), e);
					return attachment.getFileId();
				}
				return null;
			})
			.filter(Objects::nonNull)
			.toList();
	}

	private boolean attachmentExists(final List<ErrandAttachment> errandAttachments, final Attachment attachment, final CaseEntity caseEntity, final String errandId, final InputStream inputStream) {
		return errandAttachments.stream()
			.filter(errandAttachment -> errandAttachment.getFileName().equals(attachment.getFileName()))
			.map(errandAttachment -> supportManagementClient.getAttachment(caseEntity.getCaseMetaData().getMunicipalityId(), caseEntity.getCaseMetaData().getNamespace(), errandId, errandAttachment.getId()))
			.map(ResponseEntity::getBody)
			.filter(Objects::nonNull)
			.anyMatch(inputStreamResource -> {
				try {
					return IOUtils.contentEquals(inputStream, inputStreamResource.getInputStream());
				} catch (final IOException e) {
					LOGGER.error("Error comparing attachments with filename: {}", attachment.getFileName(), e);
					throw new RuntimeException(e);
				}
			});
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
