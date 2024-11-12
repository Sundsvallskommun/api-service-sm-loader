package se.sundsvall.smloader.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.smloader.integration.db.model.CaseEntity;
import se.sundsvall.smloader.integration.db.model.CaseMetaDataEntity;
import se.sundsvall.smloader.integration.db.model.enums.Instance;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

	@Mock
	private OpenEService openEService;
	@InjectMocks
	private AttachmentService attachmentService;

	@Test
	void handleAttachments() {
		// Arrange
		final var queryId = "queryId1";
		final var fileId = "fileId1";
		final var externalCaseId = "externalCaseId";
		final var instance = Instance.EXTERNAL;
		final byte[] xml = """
			<FlowInstance>
				<Values>
				<SomethingElse>
					<QueryID>queryId1</QueryID>
					<File>
						<Value>fileId1</Value>
						<Name>fileName1</Name>
					</File>
				</SomethingElse>
				</Values>
			</FlowInstance>
			""".getBytes();

		final var caseEntity = CaseEntity.create()
			.withExternalCaseId(externalCaseId)
			.withCaseMetaData(CaseMetaDataEntity.create().withInstance(instance));

		final byte[] fileBytes = new byte[] {
			1, 2, 3
		};
		when(openEService.getFile(externalCaseId, fileId, queryId, instance)).thenReturn(fileBytes);

		// Act
		attachmentService.handleAttachments(xml, caseEntity);

		// Assert
		verify(openEService).getFile(externalCaseId, fileId, queryId, instance);

	}
}
