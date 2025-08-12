package se.sundsvall.smloader.integration.openemapper.orderingrecruitmentsupport;

import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADDITIONAL_INFORMATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADDITIONAL_SUPPORT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADD_SELECTION_QUESTIONS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADVERTISEMENT_CONTACTS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADVERTISEMENT_PACKAGE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADVERTISEMENT_TIME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADVERTISEMENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ADVERTISING;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_DEPARTMENT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_IN_DEPTH_INTERVIEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_JOB_TASK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_MEDIA_CHOISES;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_MUNICIPALITY_OR_COMPANY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_NUMBER_OF_POSITIONS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_ORDER_DETAILS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_QUALIFICATIONS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_QUESTION1;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_QUESTION2;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_QUESTION3;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_READY_PROFILE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_RECRUITMENT_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_RECRUITMENT_EMPLOYMENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_RECRUITMENT_POSITION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_RECRUITMENT_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_RECRUITMENT_SUPPORT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_REFERENCE_NUMBER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SCOPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_SECURITY_CLASSIFICATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_TEST_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_UNION_CONTACTS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_USER_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_WORKPLACE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_WORKPLACE_INFO;
import static se.sundsvall.smloader.integration.util.ErrandConstants.DISPLAY_WORKPLACE_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADDITIONAL_INFORMATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADDITIONAL_SUPPORT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADD_SELECTION_QUESTIONS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADMINISTRATION_NAME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADVERTISEMENT_CONTACTS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADVERTISEMENT_PACKAGE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADVERTISEMENT_TIME;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADVERTISEMENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ADVERTISING;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_CASE_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_DEPARTMENT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_EMPLOYMENT_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_END_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_FAMILY_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_IN_DEPTH_INTERVIEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_JOB_TASK;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_MEDIA_CHOISES;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_MUNICIPALITY_OR_COMPANY;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_NUMBER_OF_POSITIONS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_ORDER_DETAILS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_POSITION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_QUALIFICATIONS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_QUESTION1;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_QUESTION2;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_QUESTION3;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_READY_PROFILE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_RECRUITMENT_ACCESS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_RECRUITMENT_SUPPORT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_REFERENCE_NUMBER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SCOPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_SECURITY_CLASSIFICATION;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_START_DATE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_TEST_TYPE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_TITLE_UPPERCASE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_UNION_CONTACTS;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_USER_ID;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_WORKPLACE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_WORKPLACE_INFO;
import static se.sundsvall.smloader.integration.util.ErrandConstants.KEY_WORKPLACE_TITLE;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_APPLICANT;
import static se.sundsvall.smloader.integration.util.ErrandConstants.ROLE_RECRUITING_MANAGER;
import static se.sundsvall.smloader.integration.util.ErrandConstants.STATUS_NEW;
import static se.sundsvall.smloader.integration.util.ErrandConstants.TITLE_ORDERING_RECRUITMENT_SUPPORT;
import static se.sundsvall.smloader.integration.util.annotation.XPathAnnotationProcessor.extractValue;
import static se.sundsvall.smloader.service.mapper.SupportManagementMapper.toParameter;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import generated.se.sundsvall.supportmanagement.Parameter;
import generated.se.sundsvall.supportmanagement.Priority;
import generated.se.sundsvall.supportmanagement.Stakeholder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperBase;
import se.sundsvall.smloader.integration.openemapper.OpenEMapperProperties;
import se.sundsvall.smloader.integration.util.XPathException;

@Component
public class OrderingRecruitmentSupportProvider extends OpenEMapperBase {

	public static final String CATEGORY_COMPLETE_RECRUITMENT = "COMPLETE_RECRUITMENT";
	public static final String CATEGORY_PARTIAL_PACKAGE = "PARTIAL_PACKAGE";
	public static final String TYPE_PARTIAL_PACKAGE_EMPLOYEE = "PARTIAL_PACKAGE.EMPLOYEE";
	public static final String TYPE_COMPLETE_RECRUITMENT_MANAGER = "COMPLETE_RECRUITMENT.MANAGER";
	public static final String TYPE_COMPLETE_RECRUITMENT_EMPLOYEE = "COMPLETE_RECRUITMENT.EMPLOYEE";
	public static final String TYPE_COMPLETE_RECRUITMENT_VOLUME = "COMPLETE_RECRUITMENT.VOLUME";
	public static final String TYPE_COMPLETE_RECRUITMENT_RETAKE = "COMPLETE_RECRUITMENT.RETAKE";
	private static final String COMPLETE_RECRUITMENT_PROCESS = "Fullständig rekryteringsprocess";
	private final OpenEMapperProperties properties;

	public OrderingRecruitmentSupportProvider(final @Qualifier("orderingrecruitmentsupport") OpenEMapperProperties properties) {
		this.properties = properties;
	}

	@Override
	public String getSupportedFamilyId() {
		return properties.getFamilyId();
	}

	@Override
	public Errand mapToErrand(final byte[] xml) {
		final var result = extractValue(xml, OrderingRecruitmentSupport.class);

		if (result == null) {
			throw new XPathException("extractValue for OrderingRecruitmentSupport returned null");
		}

		return new Errand()
			.status(STATUS_NEW)
			.title(getTitle(result))
			.priority(Priority.fromValue(properties.getPriority()))
			.stakeholders(getStakeholders(result))
			.classification(getClassification(result))
			.labels(mapLabels(getClassification(result)))
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.businessRelated(false)
			.parameters(getParameters(result))
			.externalTags(Set.of(new ExternalTag().key(KEY_CASE_ID).value(result.flowInstanceId()),
				new ExternalTag().key(KEY_FAMILY_ID).value(result.familyId())))
			.reporterUserId(result.applicantUserId());
	}

	private String getTitle(final OrderingRecruitmentSupport orderingRecruitmentSupport) {
		return Optional.ofNullable(orderingRecruitmentSupport.applicantTitle())
			.orElse(TITLE_ORDERING_RECRUITMENT_SUPPORT);
	}

	private List<Stakeholder> getStakeholders(final OrderingRecruitmentSupport orderingRecruitmentSupport) {
		return List.of(
			new Stakeholder()
				.role(ROLE_APPLICANT)
				.firstName(orderingRecruitmentSupport.applicantFirstname())
				.lastName(orderingRecruitmentSupport.applicantLastname())
				.contactChannels(getContactChannels(orderingRecruitmentSupport.applicantEmail(), orderingRecruitmentSupport.applicantPhone()))
				.parameters(Stream.of(
					toParameter(KEY_ADMINISTRATION_NAME, orderingRecruitmentSupport.applicantOrganization(), DISPLAY_ADMINISTRATION_NAME),
					toParameter(KEY_TITLE_UPPERCASE, orderingRecruitmentSupport.applicantTitle(), DISPLAY_TITLE),
					toParameter(KEY_USER_ID, orderingRecruitmentSupport.applicantUserId(), DISPLAY_USER_ID))
					.filter(Objects::nonNull)
					.toList()),
			new Stakeholder()
				.role(ROLE_RECRUITING_MANAGER)
				.firstName(orderingRecruitmentSupport.recruitingManagerFirstname())
				.lastName(orderingRecruitmentSupport.recruitingManagerLastname())
				.contactChannels(getContactChannels(orderingRecruitmentSupport.recruitingManagerEmail(), orderingRecruitmentSupport.recruitingManagerPhone()))
				.parameters(Stream.of(
					toParameter(KEY_TITLE_UPPERCASE, orderingRecruitmentSupport.recruitingManagerTitle(), DISPLAY_TITLE))
					.filter(Objects::nonNull)
					.toList()));
	}

	private Classification getClassification(final OrderingRecruitmentSupport order) {
		return Optional.ofNullable(order.orderDetails())
			.map(details -> mapClassification(details, order.position()))
			.orElse(new Classification().category(properties.getCategory()).type(properties.getType()));
	}

	private Classification mapClassification(final String details, final String position) {
		if (details.equalsIgnoreCase(COMPLETE_RECRUITMENT_PROCESS)) {
			return new Classification().category(CATEGORY_COMPLETE_RECRUITMENT).type(mapTypeForCompleteProcess(position));
		} else {
			return new Classification().category(CATEGORY_PARTIAL_PACKAGE).type(TYPE_PARTIAL_PACKAGE_EMPLOYEE);
		}
	}

	private String mapTypeForCompleteProcess(final String position) {
		return switch (position) {
			case "Chef" -> TYPE_COMPLETE_RECRUITMENT_MANAGER;
			case "Medarbetare" -> TYPE_COMPLETE_RECRUITMENT_EMPLOYEE;
			case "Volymrekrytering" -> TYPE_COMPLETE_RECRUITMENT_VOLUME;
			case "Omtag" -> TYPE_COMPLETE_RECRUITMENT_RETAKE;
			default -> throw Problem.valueOf(BAD_REQUEST, "Unsupported recruitment position: " + position);
		};
	}

	private List<String> mapLabels(final Classification classification) {
		return List.of(classification.getCategory(), classification.getType());
	}

	private List<Parameter> getParameters(final OrderingRecruitmentSupport order) {
		return Stream.of(
			singleParameter(order.municipalityOrCompany(), KEY_MUNICIPALITY_OR_COMPANY, DISPLAY_MUNICIPALITY_OR_COMPANY),
			singleParameter(order.department(), KEY_DEPARTMENT, DISPLAY_DEPARTMENT),
			singleParameter(order.referenceNumber(), KEY_REFERENCE_NUMBER, DISPLAY_REFERENCE_NUMBER),
			singleParameter(order.securityClassification(), KEY_SECURITY_CLASSIFICATION, DISPLAY_SECURITY_CLASSIFICATION),
			singleParameter(order.orderDetails(), KEY_ORDER_DETAILS, DISPLAY_ORDER_DETAILS),
			singleParameter(order.position(), KEY_POSITION, DISPLAY_RECRUITMENT_POSITION),
			singleParameter(order.readyProfile(), KEY_READY_PROFILE, DISPLAY_READY_PROFILE),
			singleParameter(order.workplaceTitle(), KEY_WORKPLACE_TITLE, DISPLAY_WORKPLACE_TITLE),
			singleParameter(order.workplace(), KEY_WORKPLACE, DISPLAY_WORKPLACE),
			singleParameter(order.employmentType(), KEY_EMPLOYMENT_TYPE, DISPLAY_RECRUITMENT_EMPLOYMENT_TYPE),
			singleParameter(Optional.ofNullable(order.startingDate()).orElse(order.temporaryStartDate()), KEY_START_DATE, DISPLAY_RECRUITMENT_START_DATE),
			singleParameter(order.temporaryEndDate(), KEY_END_DATE, DISPLAY_END_DATE),
			singleParameter(order.scopePercentage(), KEY_SCOPE, DISPLAY_SCOPE),
			singleParameter(order.numberOfPositions(), KEY_NUMBER_OF_POSITIONS, DISPLAY_NUMBER_OF_POSITIONS),
			multiParameter(order.recruitmentAccess(), joinRecruitmentAccess(), KEY_RECRUITMENT_ACCESS, DISPLAY_RECRUITMENT_ACCESS).group(getGroup(order)),
			singleParameter(order.workplaceInfo(), KEY_WORKPLACE_INFO, DISPLAY_WORKPLACE_INFO),
			singleParameter(order.jobTasks(), KEY_JOB_TASK, DISPLAY_JOB_TASK),
			singleParameter(order.qualifications(), KEY_QUALIFICATIONS, DISPLAY_QUALIFICATIONS),
			singleParameter(order.advertising(), KEY_ADVERTISING, DISPLAY_ADVERTISING),
			multiParameter(order.advertisementContacts(), joinAdvertisementContact(), KEY_ADVERTISEMENT_CONTACTS, DISPLAY_ADVERTISEMENT_CONTACTS).group(getGroupName(order)),
			multiParameter(order.unionContacts(), joinUnionContact(), KEY_UNION_CONTACTS, DISPLAY_UNION_CONTACTS).group(getString(order)),
			singleParameter(order.addSelectionQuestions(), KEY_ADD_SELECTION_QUESTIONS, DISPLAY_ADD_SELECTION_QUESTIONS),
			singleParameter(order.question1(), KEY_QUESTION1, DISPLAY_QUESTION1),
			singleParameter(order.question2(), KEY_QUESTION2, DISPLAY_QUESTION2),
			singleParameter(order.question3(), KEY_QUESTION3, DISPLAY_QUESTION3),
			singleParameter(order.advertisementType(), KEY_ADVERTISEMENT_TYPE, DISPLAY_ADVERTISEMENT_TYPE),
			singleParameter(order.advertisementTime(), KEY_ADVERTISEMENT_TIME, DISPLAY_ADVERTISEMENT_TIME),
			multiParameter(order.additionalSupport(), AdditionalSupport::value, KEY_ADDITIONAL_SUPPORT, DISPLAY_ADDITIONAL_SUPPORT),
			singleParameter(order.mediaChoices(), KEY_MEDIA_CHOISES, DISPLAY_MEDIA_CHOISES),
			singleParameter(order.advertisementPackage(), KEY_ADVERTISEMENT_PACKAGE, DISPLAY_ADVERTISEMENT_PACKAGE),
			singleParameter(order.testType(), KEY_TEST_TYPE, DISPLAY_TEST_TYPE),
			singleParameter(order.inDepthInterview(), KEY_IN_DEPTH_INTERVIEW, DISPLAY_IN_DEPTH_INTERVIEW),
			singleParameter(order.recruitmentSupport(), KEY_RECRUITMENT_SUPPORT, DISPLAY_RECRUITMENT_SUPPORT),
			singleParameter(order.additionalInformation(), KEY_ADDITIONAL_INFORMATION, DISPLAY_ADDITIONAL_INFORMATION))
			.filter(Objects::nonNull)
			.toList();

	}

	private String getString(final OrderingRecruitmentSupport order) {
		if (order.unionContacts() == null || order.unionContacts().isEmpty()) {
			return null;
		}
		return order.unionContactsName();
	}

	private String getGroupName(final OrderingRecruitmentSupport order) {
		if (order.advertisementContacts() == null || order.advertisementContacts().isEmpty()) {
			return null;
		}
		return order.advertisementContactsName();
	}

	private String getGroup(final OrderingRecruitmentSupport order) {
		if (order.recruitmentAccess() == null || order.recruitmentAccess().isEmpty()) {
			return null;
		}
		return order.recruitmentAccessName();
	}

	private Function<RecruitmentAccess, String> joinRecruitmentAccess() {
		return recruitmentAccess -> String.join("|", recruitmentAccess.name(), recruitmentAccess.email(), recruitmentAccess.role());
	}

	private Function<AdvertisementContact, String> joinAdvertisementContact() {
		return advertisementContact -> String.join("|", advertisementContact.name(), advertisementContact.title(), advertisementContact.phoneNumber());
	}

	private Function<UnionContact, String> joinUnionContact() {
		return unionContact -> String.join("|", unionContact.name(), unionContact.union(), unionContact.phoneNumber());
	}
}
