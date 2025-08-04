package se.sundsvall.smloader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import feign.Request;
import feign.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;
import se.sundsvall.smloader.integration.openemapper.attachment.AttachmentMultiPartFile;
import se.sundsvall.smloader.integration.supportmanagement.SupportManagementClient;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

	@Mock
	private SupportManagementClient supportManagementClient;
	@Mock
	private OpenEService openEService;
	@InjectMocks
	private AttachmentService attachmentService;

	@Captor
	private ArgumentCaptor<AttachmentMultiPartFile> attachmentMultiPartFileCaptor;

	@Test
	void handleAttachments() throws IOException {

		// Arrange
		final var queryId = "queryId1";
		final var fileId = "fileId1";
		final var fileName = "fileName1";
		final var externalCaseId = "externalCaseId";
		final var instance = Instance.EXTERNAL;
		final var errandId = "errandId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final byte[] xml = """
			<FlowInstance>
				<Values>
				<SomethingElse>
					<QueryID>queryId1</QueryID>
					<File>
						<ID>fileId1</ID>
						<Name>fileName1</Name>
					</File>
				</SomethingElse>
				</Values>
			</FlowInstance>
			""".getBytes();

		final var caseEntity = CaseEntity.create()
			.withExternalCaseId(externalCaseId)
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withInstance(instance));

		final byte[] fileBytes = new byte[] {
			1, 2, 3
		};
		final var stream = new ByteArrayInputStream(fileBytes);
		final var response = Response.builder()
			.request(Request.create(Request.HttpMethod.GET, "", Map.of(), null, null, null))
			.body(stream, fileBytes.length)
			.build();
		when(openEService.getFile(externalCaseId, fileId, queryId, instance, municipalityId)).thenReturn(response);
		when(supportManagementClient.createAttachment(eq(municipalityId), eq(namespace), eq(errandId), any())).thenReturn(ResponseEntity.ok().build());

		// Act
		attachmentService.handleAttachments(xml, caseEntity, errandId);

		// Assert
		verify(openEService).getFile(externalCaseId, fileId, queryId, instance, municipalityId);
		verify(supportManagementClient).getAttachments(municipalityId, namespace, errandId);
		verify(supportManagementClient).createAttachment(eq(municipalityId), eq(namespace), eq(errandId), attachmentMultiPartFileCaptor.capture());

		final var attachmentMultiPartFile = attachmentMultiPartFileCaptor.getValue();
		assertThat(attachmentMultiPartFile.getBytes()).isEqualTo(fileBytes);
		assertThat(attachmentMultiPartFile.getContentType()).isEmpty();
		assertThat(attachmentMultiPartFile.getInputStream()).isEqualTo(stream);
		assertThat(attachmentMultiPartFile.getName()).isEqualTo(fileName);
		assertThat(attachmentMultiPartFile.getOriginalFilename()).isEqualTo(fileName);
	}

	@Test
	void handleAttachmentsNoFiles() {

		final var municipalityId = "municipalityId";
		final var namespace = "namespace";

		// Arrange
		final var errandId = "errandId";

		final byte[] xml = """
			<FlowInstance>
				<Values>
				<SomethingElse>
					<QueryID>queryId1</QueryID>
				</SomethingElse>
				</Values>
			</FlowInstance>
			""".getBytes();

		final var caseEntity = CaseEntity.create().withCaseMetaData(CaseMetaDataEntity.create().withMunicipalityId(municipalityId).withNamespace(namespace));

		// Act
		attachmentService.handleAttachments(xml, caseEntity, errandId);

		// Assert
		verify(supportManagementClient).getAttachments(municipalityId, namespace, errandId);
		verifyNoMoreInteractions(openEService, supportManagementClient);
	}

	@Test
	void handleAttachmentsFailedToFetchFile() {

		// Arrange
		final var queryId = "queryId1";
		final var fileId = "fileId1";
		final var externalCaseId = "externalCaseId";
		final var instance = Instance.EXTERNAL;
		final var errandId = "errandId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final byte[] xml = """
			<FlowInstance>
				<Values>
				<SomethingElse>
					<QueryID>queryId1</QueryID>
					<File>
						<ID>fileId1</ID>
						<Name>fileName1</Name>
					</File>
				</SomethingElse>
				</Values>
			</FlowInstance>
			""".getBytes();

		final var caseEntity = CaseEntity.create()
			.withExternalCaseId(externalCaseId)
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withInstance(instance));

		when(openEService.getFile(externalCaseId, fileId, queryId, instance, municipalityId)).thenReturn(null);

		// Act
		final var result = attachmentService.handleAttachments(xml, caseEntity, errandId);

		// Assert
		assertThat(result).containsExactly(fileId);
		verify(openEService).getFile(externalCaseId, fileId, queryId, instance, municipalityId);
		verify(supportManagementClient).getAttachments(municipalityId, namespace, errandId);
		verifyNoMoreInteractions(supportManagementClient);
	}

	@Test
	void handleAttachmentsFailedToCreateAttachment() {

		// Arrange
		final var queryId = "queryId1";
		final var fileId = "fileId1";
		final var externalCaseId = "externalCaseId";
		final var instance = Instance.EXTERNAL;
		final var errandId = "errandId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final byte[] xml = """
			<FlowInstance>
				<Values>
				<SomethingElse>
					<QueryID>queryId1</QueryID>
					<File>
						<ID>fileId1</ID>
						<Name>fileName1</Name>
					</File>
				</SomethingElse>
				</Values>
			</FlowInstance>
			""".getBytes();

		final var caseEntity = CaseEntity.create()
			.withExternalCaseId(externalCaseId)
			.withCaseMetaData(CaseMetaDataEntity.create()
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withInstance(instance));

		final byte[] fileBytes = new byte[] {
			1, 2, 3
		};
		final var stream = new ByteArrayInputStream(fileBytes);

		final var response = Response.builder()
			.request(Request.create(Request.HttpMethod.GET, "", Map.of(), null, null, null))
			.body(stream, fileBytes.length)
			.build();
		when(openEService.getFile(externalCaseId, fileId, queryId, instance, municipalityId)).thenReturn(response);
		when(supportManagementClient.createAttachment(eq(municipalityId), eq(namespace), eq(errandId), any())).thenReturn(ResponseEntity.badRequest().build());

		// Act
		final var result = attachmentService.handleAttachments(xml, caseEntity, errandId);

		// Assert
		assertThat(result).containsExactly(fileId);
		verify(openEService).getFile(externalCaseId, fileId, queryId, instance, municipalityId);
		verify(supportManagementClient).getAttachments(municipalityId, namespace, errandId);
		verify(supportManagementClient).createAttachment(eq(municipalityId), eq(namespace), eq(errandId), attachmentMultiPartFileCaptor.capture());
	}
}
