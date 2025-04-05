public with sharing class MessengerController {
  private static HelpshiftAdminAppSyncSetting__c hsAdminSetting = HelpshiftAdminAppSyncSetting__c.getValues(
    'HSSyncAdminSetting'
  );

  private static Boolean showNickNameOnMessager = false;
  public static Map<String, String> mapProfileIdVsName = new Map<String, String>();

  @AuraEnabled
  public static Boolean checkIfFooterButtonsVisible(){
    Boolean areFooterButtonsVisible = true;
    try {
      String footerButtonsConfigValue = Util.getConfigSettingValue('hideFooterButtons');

      if (String.isNotBlank(footerButtonsConfigValue) && footerButtonsConfigValue.equalsIgnoreCase('true')) {
        areFooterButtonsVisible = false;
      }
    } catch (Exception e) {
      Logger.getInstance().log('MessengerController.checkIfFooterButtonsVisible', e);
      throw new AuraHandledException(e.getMessage());
    }

    return areFooterButtonsVisible;
  }

  @AuraEnabled
  public static Map<String, String> getOnLoadData(
    String issueId,
    String hsIssueId
  ) {
    Map<String, String> responseMap = new Map<String, String>();
    try {
      String hsEndPointUrl = '';
      Key_Configuration__mdt keyConfig = Key_Configuration__mdt.getInstance(
        'keyconfig'
      );
      responseMap.put(
        'ctrl_enter_cmd_enter',
        String.valueOf(keyConfig.ctrl_enter_cmd_enter__c)
      );
      responseMap.put('enter', String.valueOf(keyConfig.enter__c));

      Case caseRec = fetchCaseData(issueId, false);

      if (caseRec != null) {
        if (caseRec.ownerId.getSobjectType() == Group.SobjectType) {
          Group queue = [select Name from group where id = :caseRec.ownerId]; // NOPMD

          if (queue != null) {
            responseMap.put('queueName', queue.Name);
          }
        }

        String languageField = getCaseLanguageField();

        if (String.isNotBlank(languageField)) {
        	responseMap.put('language', (String)caseRec.get(languageField));
        } else {
            responseMap.put('language', '');
        }

      } else {
        responseMap.put('language', '');
      }

      //endPointData
      if (hsAdminSetting != null) {
        responseMap.put(
          'hsAccessKey', // NOPMD
          EncodingUtil.base64Encode(
            Blob.valueOf(hsAdminSetting.HSAccessKey__c + ':')
          )
        );
        hsEndPointUrl =
          hsAdminSetting.FAQEndpointPrefix__c + hsAdminSetting.HSDomain__c;

        responseMap.put('hsAPIbaseUrl', hsEndPointUrl);
      }

      Dynamic_Mapping__c statusMapping = Dynamic_Mapping__c.getInstance(
        'status'
      );
      if (statusMapping != null) {
        responseMap.put('dynamicMapping_status', JSON.serialize(statusMapping));
      }

      responseMap.put('hsSettings_all', JSON.serialize(HSSettings__c.getAll()));

      Map<String, String> csatMap = new Map<String, String>();

      csatMap.put(
        'rating',
        Dynamic_Mapping__c.getInstance('csat-rating') != null
          ? Dynamic_Mapping__c.getInstance('csat-rating').SF_Fieldname__c
          : ''
      );
      csatMap.put(
        'comment',
        Dynamic_Mapping__c.getInstance('csat-comment') != null
          ? Dynamic_Mapping__c.getInstance('csat-comment').SF_Fieldname__c
          : ''
      );
      csatMap.put(
        'label',
        Dynamic_Mapping__c.getInstance('csat-label') != null
          ? Dynamic_Mapping__c.getInstance('csat-label').SF_Fieldname__c
          : ''
      );

      responseMap.put('csat', JSON.serialize(csatMap));

      User currentUser = [
        SELECT id, isHSUser__c, Name, HSUserId__c, CommunityNickname
        FROM user
        WHERE id = :UserInfo.getUserId()
        WITH SECURITY_ENFORCED
      ];

      showNickNameOnMessager = getNicknameForMessengerFlag();

      responseMap.put('hsUserValidationMessage', '');

      if (!showNickNameOnMessager) {
        responseMap.put('currentUserName', currentUser.Name);
      } else {
        responseMap.put('currentUserName', currentUser.CommunityNickname);
      }

      responseMap.put('hsUserId', currentUser.HSUserId__c);

      if (currentUser.HSUserId__c != null && currentUser.HSUserId__c != '') {
        hsEndPointUrl += '/sf-sync/issues/' + hsIssueId + '/messages/agent';
      } else {
        hsEndPointUrl += '/sf/issues/' + hsIssueId + '/messages';
      }

      responseMap.put('hsEndPointUrl', hsEndPointUrl);
      responseMap.put('nameSpace', Constants.NAMESPACE);

      String tabSettingValue = Util.getConfigSettingValue('EnableTabHighlighting');
      if(String.isNotBlank(tabSettingValue) && tabSettingValue.equalsIgnoreCase('true')){
        responseMap.put('EnableTabHighlighting','true');
      }
      else {
        responseMap.put('EnableTabHighlighting','false');
      }

      responseMap.put('chatHeaderIsVisible', chatHeaderIsVisible());

    } catch (Exception e) {
      Logger.getInstance().log('MessengerController.getOnLoadData', e);
      throw new AuraHandledException(e.getMessage());
    }

    return responseMap;
  }

  @AuraEnabled
  public static Boolean getDisableBottomScroll() {
    try {
      String disableScrollToBottomValue = Util.getConfigSettingValue('DisableScrollToBottom');
      if(String.isNotBlank(disableScrollToBottomValue) && disableScrollToBottomValue.equalsIgnoreCase('true')){
        return true;
      }
    } catch (Exception e) {
      return false;
    }

    return false;
  }

  @AuraEnabled
  public static List<Message> getMessages(
    String caseId,
    String scrollMessageDateTimeHS,
    String polingDateTimeHS
  ) {
    if (caseId == null)
      return null;
    try {
      List<Message> messageList = new List<Message>();
      Case_Comment__c acceptedComment = null;
      String statusField = getCaseStatusField();
      Boolean isScrollToBottomDisabled = getDisableBottomScroll();
      String queryOrderClause = ' ORDER BY CreatedDate DESC ,Name DESC LIMIT 10';

      if (isScrollToBottomDisabled) {
        queryOrderClause = ' ORDER BY CreatedDate DESC ,Name DESC LIMIT 1000';
      }

      String query =
        'SELECT' +
        ' Id,' +
        'Message_Type__c,' +
        'Faqs__c,' +
        'Action_Cards__c,' +
        'Origin__c,' +
        'Comment__c,' +
        'Author__c,' +
        'Nickname__c,' +
        'CreatedBy.FirstName,' +
        'CreatedBy.CommunityNickname,' +
        'CreatedBy.Email,' +
        'CreatedDate,' +
        'AttachmentUrls__c,' +
        'Case__r.isClosed,' +
        'External_ID__c,' +
        'Rating__c,' +
        'Resolution__c,' +
        'Created_Timestamp__c,' +
        'Message_Read__c,' +
        'Message_Sent__c,' +
        'Translated_Message__c,'+
        '(SELECT ContentDocumentId, ContentDocument.LatestPublishedVersionId,ContentDocument.Title,ContentDocument.FileExtension FROM ContentDocumentLinks)' +
        ' FROM Case_Comment__c' +
        ' WHERE Case__c = \'' +
        String.escapeSingleQuotes(caseId) +
        '\'' +
        ( String.isNotBlank(scrollMessageDateTimeHS)
          ? ' AND CreatedDate < ' +
            scrollMessageDateTimeHS+'+00:00'
          : '') +
        ( String.isNotBlank(polingDateTimeHS)
          ? ' AND CreatedDate > ' +
          (polingDateTimeHS+'+00:00')
          : '') +
        ' WITH SECURITY_ENFORCED' +
        queryOrderClause;
      System.debug(query);
      showNickNameOnMessager = getNicknameForMessengerFlag();

      for (Case_Comment__c caseComment : Database.query(query)) {// NOPMD
        try {
          if(caseComment.Message_Type__c == 'intents_with_option_input' && String.isNotBlank(caseComment.Comment__c) && caseComment.Comment__c.contains('body_text')){
            String caseCommentBody = (String) ((Map<String, Object>)JSON.deserializeUntyped(caseComment.Comment__c)).get('body_text');
            Case_Comment__c newCaseCommentRec = caseComment.clone(true, true, true, true);
            newCaseCommentRec.Comment__c = caseCommentBody;
            newCaseCommentRec.Message_Type__c = 'text';
            messageList.add(new Message(newCaseCommentRec));
          }
        } catch(Exception exp) {
          Logger.getInstance().log('MessengerController.getMssages - Intent with option input', exp);
        }
        messageList.add(new Message(caseComment));
      }

      return messageList;
    } catch (Exception e) {
      System.debug('error-> '+e.getStackTraceString());
      Logger.getInstance().log('MessengerController.getMessages', e);
      throw new AuraHandledException(e.getMessage());
    }
  }

  @AuraEnabled
  public static Case fetchCaseData(
    String caseId,
    boolean areCsatDetailsAvailable
  ) {
    try {
      Case caseRecord = null;
      String statusField = getCaseStatusField();
      String languageField = getCaseLanguageField();
      String caseQuery = 'SELECT CreatedDate,is_Assignee_Bot__c,OwnerId,' + statusField;

      if (areCsatDetailsAvailable) {
        String csatFields = getCsatFieldString();

        if (String.isNotBlank(csatFields)) {
          caseQuery += (',' + csatFields);
        }
      }

        if (String.isNotBlank(languageField)) {
            caseQuery += (',' + languageField);
        }

      if(caseQuery.endsWith(',')){

        caseQuery = caseQuery.removeEnd(',');
        System.debug('caseQuery2 : '+caseQuery);
      }

      if (String.isNotBlank(statusField)) {
        caseQuery += (' from Case where id= \'' +
        String.escapeSingleQuotes(CaseId) +
        '\'');

		System.debug('caseQuery : '+caseQuery);
        caseRecord = Database.query(String.valueof(caseQuery));// NOPMD
      }

      return caseRecord;
    } catch (Exception e) {
      throw new AuraHandledException(e.getMessage());
    }
  }

  private static String getCsatFieldString() {
    String csatQueryFields = '';
    String ratingField;
    String commentField;
    String labelField;

    Dynamic_Mapping__c ratingMapping = Dynamic_Mapping__c.getInstance(
      'csat-rating'
    );
    Dynamic_Mapping__c commentMapping = Dynamic_Mapping__c.getInstance(
      'csat-comment'
    );
    Dynamic_Mapping__c labelMapping = Dynamic_Mapping__c.getInstance(
      'csat-label'
    );

    if (
      ratingMapping != null && String.isNotEmpty(ratingMapping.SF_Fieldname__c)
    ) {
      ratingField = ratingMapping.SF_Fieldname__c;
    }

    if (
      commentMapping != null &&
      String.isNotEmpty(commentMapping.SF_Fieldname__c)
    ) {
      commentField = commentMapping.SF_Fieldname__c;
    }

    if (
      labelMapping != null && String.isNotEmpty(labelMapping.SF_Fieldname__c)
    ) {
      labelField = labelMapping.SF_Fieldname__c;
    }

    csatQueryFields = String.isNotBlank(ratingField) ? ratingField : '';
    csatQueryFields += String.isNotBlank(commentField)
      ? ',' + commentField
      : '';
    csatQueryFields += String.isNotBlank(labelField) ? ',' + labelField : '';
    System.debug('csatQueryFields ' + csatQueryFields);
    return csatQueryFields;
  }

  private static String getCaseStatusField() {
    String statusField = '';
    Dynamic_Mapping__c statusMapping = Dynamic_Mapping__c.getInstance('status');

    if (
      statusMapping != null && String.isNotEmpty(statusMapping.SF_Fieldname__c)
    ) {
      statusField = statusMapping.SF_Fieldname__c;
    }

    return statusField;
  }

  private static String getCaseLanguageField() {
    String languageField = '';

    Dynamic_Mapping__c languageMapping = Dynamic_Mapping__c.getInstance('language');

    if (
      languageMapping != null && String.isNotEmpty(languageMapping.SF_Fieldname__c)
    ) {
      languageField = languageMapping.SF_Fieldname__c;
    }

    return languageField;
  }

  @AuraEnabled
  public static String createCaseComment(String caseId, String message, String translatedMsg) {
    try {
      Case_Comment__c newCaseComment = new Case_Comment__c(
        Case__c = caseId,
        Comment__c = message,
        Origin__c = 'user',
        Message_Type__c = 'text',
        External_ID__c = getTimeStampInHSFormat(),
        Created_Timestamp__c = getTimeStampInHSFormat(),
        Translated_Message__c = translatedMsg
      );

      Schema.SObjectType objType = new Case_Comment__c().getSObjectType();
      Boolean isVerified = HSAccessValidator.isAuthorizedToCreate(objType, new List<String>{Constants.NAMESPACE + 'Case__c',Constants.NAMESPACE + 'Comment__c',Constants.NAMESPACE + 'Origin__c',Constants.NAMESPACE + 'Message_Type__c',Constants.NAMESPACE + 'External_ID__c',Constants.NAMESPACE + 'Created_Timestamp__c'});
      if(isVerified == true){
        Util.tryInsert(new List<sObject>{ newCaseComment }, true);
      }


      return newCaseComment.Id;
    } catch (Exception e) {
      Logger.getInstance()
        .log('HelpshiftMessengerLightningController.createCaseComment', e);
      throw new AuraHandledException(e.getMessage());
    }
  }

  @AuraEnabled
  public static String deleteCaseComment(String caseCommentId) {
    try {
      Case_Comment__c newCaseComment = new Case_Comment__c(Id = caseCommentId);
      Schema.SObjectType objType = new Case_Comment__c().getSObjectType();
      Boolean isVerifiedForDelete = HSAccessValidator.isAuthorizedToDelete(objType);
      if(isVerifiedForDelete == true) {
        Util.tryDelete(new List<sObject>{ newCaseComment }, true);
      }


      return 'Case Comment Deleted Successfully!!';
    } catch (Exception e) {
      Logger.getInstance()
        .log('HelpshiftMessengerLightningController.deleteCaseComment', e);
      throw new AuraHandledException(e.getMessage());
    }
  }

  @AuraEnabled
  /* sfge-disable-stack ApexFlsViolationRule */
  public static Case_Comment__c updateCaseCommentWithParams(
    String hsMessageId,
    String caseCommentId,
    String attachmentURL
  ) {
    Case_Comment__c caseComment = new Case_Comment__c();
    try {
      if (
        String.isNotBlank(caseCommentId) &&
        String.valueOf(Id.valueOf(caseCommentId).getSObjectType())
          .containsIgnoreCase('Case_Comment') &&
        hsAdminSetting != null &&
        hsAdminSetting.DomainExists__c &&
        String.isNotBlank(hsAdminSetting.HSDomain__c) &&
        String.isNotBlank(HSMessageId) &&
        HSMessageId.containsIgnoreCase(hsAdminSetting.HSDomain__c + '_')
      ) {
        caseComment.Id = Id.valueOf(caseCommentId);
        caseComment.Created_Timestamp__c = Case_CommentTriggerHandler.convertHSCreatedDateTime(
          HSMessageId
        );
        caseComment.External_ID__c = HSMessageId;
        caseComment.Message_Sent__c = true;
      } else {
        caseComment.Id = Id.valueOf(caseCommentId);
        caseComment.Message_Sent__c = true;
        caseComment.Created_Timestamp__c = getTimeStampInHSFormat();
        if (String.isNotBlank(HSMessageId)) {
          caseComment.External_ID__c = HSMessageId;
        }
      }
      caseComment.Author__c = UserInfo.getName();
      caseComment.AttachmentUrls__c = attachmentURL;
    } catch (Exception e) {
      Logger.getInstance()
        .log(
          'HelpshiftMessengerLightningController.updateCaseCommentWithCreatedDate',
          e
        );
      caseComment = new Case_Comment__c();
      caseComment.Id = Id.valueOf(caseCommentId);
      caseComment.Created_Timestamp__c = getTimeStampInHSFormat();
      if (String.isNotBlank(HSMessageId)) {
        caseComment.External_ID__c = HSMessageId;
      }
    } finally {
      if(caseComment != null){
        Schema.SObjectType objType = new Case_Comment__c().getSObjectType();
        Boolean isVerified = HSAccessValidator.isAuthorizedToUpdate(objType, new List<String>{Constants.NAMESPACE+'External_ID__c', Constants.NAMESPACE+'Created_Timestamp__c', Constants.NAMESPACE+'AttachmentUrls__c',
          Constants.NAMESPACE+'Author__c', Constants.NAMESPACE+'Message_Sent__c'});
        if(isVerified == true){
          TriggerUtility.turnOff('CaseCommentTrigger');
          Util.tryUpdate(new List<Case_Comment__c>{caseComment}, true);
          TriggerUtility.turnOn('CaseCommentTrigger');
        }
      }
    }
    return caseComment;
  }

  @AuraEnabled
  /* sfge-disable-stack ApexFlsViolationRule*/
  public static string saveTranslation(String caseCommentId, String translatedMessage) {

    try {
      if (String.isNotBlank(caseCommentId) && String.isNotBlank(translatedMessage)) {
        Case_Comment__c caseCommentRec = new Case_Comment__c(Id = caseCommentId, Translated_Message__c = translatedMessage);
        Schema.SObjectType objType = new Case_Comment__c().getSObjectType();
        Boolean isVerified = HSAccessValidator.isAuthorizedToUpdate(objType, new List<String>{Constants.NAMESPACE+'Translated_Message__c'});
        if(isVerified == true){
          TriggerUtility.turnOff('CaseCommentTrigger');
          Util.tryUpdate(new List<Case_Comment__c>{caseCommentRec}, true);
          TriggerUtility.turnOn('CaseCommentTrigger');
        }

        return 'success';
      } else {
        return 'Invalid data';
      }
    } catch (Exception e) {
      Logger.getInstance()
        .log(
          'MessengerController.saveTranslation',
          e
        );
      throw new AuraHandledException(e.getMessage());
    }

    }


  public static String getTimeStampInHSFormat() {
    Datetime dt = Datetime.now();
    return dt.formatGMT('yyyyMMddHHmmssSSS');
  }

  @AuraEnabled
  /* sfge-disable-stack ApexFlsViolationRule */
  public static String redactCaseComment(
    String caseCommentIdList,
    String privateNote,
    String caseCommentAttachmentIdList
  ) {
    try {
      System.debug('redactCaseComment start');
      List<Redaction_Request__c> redactRequestRecordToCreateList = new List<Redaction_Request__c>();
      List<Case_Comment__c> listCaseCommentsToUpdate = new List<Case_Comment__c>();
      List<Id> contentDocumentIdList = new List<Id>();
      Set<Id> caseCommentAttachmentIdSet = new Set<Id>();
      Set<Id> caseCommentIdSet = (Set<Id>) JSON.deserialize(
        caseCommentIdList,
        Set<Id>.class
      );
      Map<Id, List<Id>> caseCommentAttachmentListMap = new Map<Id, List<Id>>();

      if (String.isNotBlank(caseCommentAttachmentIdList)) {
        caseCommentAttachmentIdSet = (Set<Id>) JSON.deserialize(
          caseCommentAttachmentIdList,
          Set<Id>.class
        );
      }

      Set<Id> commentIdNotNeedToDelete = new Set<Id>();

      if (caseCommentAttachmentIdSet.size() > 0) {
        List<ContentDocumentLink> contentDocumentList = [// NOPMD
          SELECT
            Id,
            ContentDocumentId,
            LinkedEntityId,
            ContentDocument.LatestPublishedVersionId
          FROM ContentDocumentLink
          WHERE ContentDocumentId IN :caseCommentAttachmentIdSet
        ];
        if (contentDocumentList.size() > 0) {
          for (ContentDocumentLink cdrec : contentDocumentList) {
            if (
              !String.valueOf(cdrec.LinkedEntityId).startsWithIgnoreCase('005')
            ) {
              contentDocumentIdList.add(cdrec.ContentDocumentId);
              if (
                !caseCommentAttachmentListMap.containskey(cdrec.LinkedEntityId)
              ) {
                caseCommentAttachmentListMap.put(
                  cdrec.LinkedEntityId,
                  new List<Id>{ cdrec.Id }
                );
              } else {
                caseCommentAttachmentListMap.get(cdrec.LinkedEntityId)
                  .add(cdrec.Id);
              }
              commentIdNotNeedToDelete.add(cdrec.LinkedEntityId);
            }
          }
        }
      }

      List<Case_Comment__c> CaseCommentsList = [
        SELECT
          Id,
          Comment__c,
          Message_Type__c,
          Case__c,
          Case__r.HSIssueId__c,
          Origin__c,
          Message_Read__c,
          External_ID__c,
          AttachmentUrls__c
        FROM Case_Comment__c
        WHERE
          (ID IN :caseCommentIdSet
          OR ID IN :commentIdNotNeedToDelete)
          AND Case__c != NULL
          WITH SECURITY_ENFORCED
        LIMIT 50000
      ];

      StaticResource resourceList = [
        SELECT Name, NamespacePrefix, SystemModStamp
        FROM StaticResource
        WHERE Name = 'helpshift'
        WITH SECURITY_ENFORCED
        LIMIT 1
      ];
      String namespace = resourceList.NamespacePrefix;
      String fullFileURL =
        URL.getSalesforceBaseUrl().toExternalForm() +
        '/resource/' +
        resourceList.SystemModStamp.getTime() +
        '/' +
        (namespace != null && namespace != '' ? namespace + '__' : '') +
        'helpshift/images/ImageDeleteWarning.png';

      redactRequestRecordToCreateList.add(
        new Redaction_Request__c(Redaction_Request_Note__c = privateNote)
      );

      Schema.SObjectType objType = new Redaction_Request__c().getSObjectType();
      Boolean isVerified = HSAccessValidator.isAuthorizedToCreate(objType, new List<String>{Constants.NAMESPACE + 'Redaction_Request_Note__c'});
      if(isVerified == true){
        Util.tryInsert(redactRequestRecordToCreateList, true);
      }


      if (CaseCommentsList.size() > 0) {
        for (Case_Comment__c caseCommentRec : CaseCommentsList) {
          Case_Comment__c newCaseComment = new Case_Comment__c();
          newCaseComment.Id = caseCommentRec.Id;

          if (caseCommentRec.AttachmentUrls__c != null) {
            newCaseComment.AttachmentUrls__c =
              '[{"url":"' +
              fullFileURL +
              '","file_name":"istockphoto-1131163852-612x612.jpg","content_type":"image/jpg"}]';
          }

          if (caseCommentIdSet.contains(caseCommentRec.Id)) {
            newCaseComment.Comment__c = 'This message was deleted';
            newCaseComment.Redaction_Request__c = redactRequestRecordToCreateList[0]
              .Id;
          }
          listCaseCommentsToUpdate.add(newCaseComment);
        }

        //System.debug('caseComment - ' + listCaseCommentsToUpdate[0].Comment__c);
        if(listCaseCommentsToUpdate != null && listCaseCommentsToUpdate.size() > 0){
          Schema.SObjectType caseCommentObjType = new Case_Comment__c().getSObjectType();
          Boolean isVerifiedForUpdate = HSAccessValidator.isAuthorizedToUpdate(caseCommentObjType, new List<String>{Constants.NAMESPACE+'AttachmentUrls__c', Constants.NAMESPACE+'Comment__c', Constants.NAMESPACE+'Redaction_Request__c'});
          if(isVerifiedForUpdate == true){
            Util.tryUpdate(listCaseCommentsToUpdate, true);
          }
        }
      }
      if (contentDocumentIdList.size() > 0) {
        List<ContentDocument> attechmentsToDelete = [// NOPMD
          SELECT ID
          FROM ContentDocument
          WHERE Id IN :contentDocumentIdList
        ];
        Util.tryDelete(attechmentsToDelete, true);

      }

      System.debug('before if');
      if (CaseCommentsList.size() + caseCommentAttachmentListMap.size() < 50) {
        if (CaseCommentsList.size() > 0) {
          String caseCommentsListString = JSON.serialize(CaseCommentsList);
          String caseCommentAttachmentListMapString = '';
          System.debug('before case comment sync');
          sendHelpshiftAsyncCaseComment(caseCommentIdSet);
        }
        if (caseCommentAttachmentListMap.size() > 0) {
          Set<String> attachmentNeedToDelete = new Set<String>();

          String caseCommentsListString = '';
          String caseCommentAttachmentListMapString = JSON.serialize(
            caseCommentAttachmentListMap
          );
          sendHelpshiftAsync(
            caseCommentAttachmentListMapString,
            caseCommentIdSet
          );
        }
      } else {
        throw new CustomException('You can remove only 50 Messages at once.');
      }

      return 'Successfully redacted';
    } catch (Exception e) {
      Logger.getInstance()
        .log('HelpshiftMessengerLightningController.redactCaseComment', e);
      System.debug(String.valueOf(e.getStackTraceString()));
      System.debug(String.valueOf(e));
      throw new AuraHandledException(e.getMessage());
    }
  }

  public class CustomException extends Exception {
  }

  @future(callout=true)
  public static void sendHelpshiftAsyncCaseComment(Set<Id> caseCommentIdSet) {
    List<String> messageTypesList = new List<String>{'text','si_intent','si_with_text','rsp_intents_with_option_input'};
    List<Case_Comment__c> CaseCommentsList = [
      SELECT
        Id,
        Comment__c,
        Message_Type__c,
        Case__c,
        Case__r.HSIssueId__c,
        Origin__c,
        Message_Read__c,
        External_ID__c
      FROM Case_Comment__c
      WHERE
        ID IN :caseCommentIdSet
        AND Message_Type__c IN :messageTypesList
        AND Case__c != NULL
        WITH SECURITY_ENFORCED
      LIMIT 50000
    ];
    for (Case_Comment__c caseCommentRec : CaseCommentsList) {
      HttpResponse resp = HS_credentials.makeApiCall(
        'DELETE',
        '/issues/' +
        caseCommentRec.Case__r.HSIssueId__c +
        '/messages/' +
        caseCommentRec.External_ID__c,
        null
      );
    }
  }

  @future(callout=true)
  public static void sendHelpshiftAsync(
    String caseCommentAttachmentListMapString,
    Set<Id> caseCommentIdSet
  ) {
    Map<Id, List<String>> caseCommentAttachmentListMap = new Map<Id, List<String>>();

    if (String.isNotBlank(caseCommentAttachmentListMapString)) {
      caseCommentAttachmentListMap = (Map<Id, List<String>>) JSON.deserialize(
        caseCommentAttachmentListMapString,
        Map<Id, List<String>>.class
      );
    }

    List<Case_Comment__c> CaseCommentsList = [
      SELECT
        Id,
        Comment__c,
        Message_Type__c,
        Case__c,
        Case__r.HSIssueId__c,
        Origin__c,
        Message_Read__c,
        External_ID__c
      FROM Case_Comment__c
      WHERE
        ID IN :caseCommentAttachmentListMap.keyset()
        AND Message_Type__c = 'text'
        AND Case__c != NULL
        WITH SECURITY_ENFORCED
      LIMIT 50000
    ];

    for (Case_Comment__c caseCommentRec : CaseCommentsList) {
      if (!caseCommentIdSet.contains(caseCommentRec.Id)) {
        String reqBody =
          'content-doc-link-ids=' +
          JSON.serialize(caseCommentAttachmentListMap.get(caseCommentRec.Id));
        HttpResponse resp = HS_credentials.makeApiCall(
          'DELETE',
          '/issues/' +
          caseCommentRec.Case__r.HSIssueId__c +
          '/messages/' +
          caseCommentRec.External_ID__c,
          reqBody
        );
      }
    }
  }

  @AuraEnabled(cacheable=true)
  public static List<QuickText> getQuickTexts(String searchParam) {
    try {
      if (String.isNotBlank(searchParam)) {
        searchParam = '%' + searchParam + '%';
        return [
          SELECT Id, Name, Message
          FROM QuickText
          WHERE Name LIKE :searchParam
          WITH SECURITY_ENFORCED
          ORDER BY CreatedDate ASC
        ];
      } else {
        return [
          SELECT Id, Name, Message
          FROM QuickText
          WHERE OwnerId = :UserInfo.getUserId()
          WITH SECURITY_ENFORCED
          ORDER BY CreatedDate ASC
          LIMIT 10
        ];
      }
    } catch (Exception e) {
      throw new AuraHandledException(e.getMessage());
    }
  }

  @AuraEnabled
  /* sfge-disable-stack ApexFlsViolationRule */
  public static String updateCase(String caseId, String field, String value) {
    try {
      SObject objCase = new Case(Id = caseId);

      if (!field.containsIgnoreCase('is_Assignee_Bot__c')) {
        objCase.put(field, value);
        objCase.put('Update_from_HS__c', true);
      } else {
        objCase.put(field, Boolean.valueOf(value));
      }

      if(objCase != null){
        Schema.SObjectType objType = new Case().getSObjectType();
        Boolean isVerified = HSAccessValidator.isAuthorizedToUpdate(objType, new List<String>{Constants.NAMESPACE+'Update_from_HS__c', Constants.NAMESPACE+field});
        if(isVerified == true){
          Util.tryUpdate(new List<sObject>{ objCase }, true);
        }
      }



      return 'Case Updated!';
    } catch (Exception e) {
      Logger.getInstance().log('MessengerController.updateCaseStatus', e);
      throw new AuraHandledException(e.getMessage());
    }
  }

  @AuraEnabled
  public static String getCaseCommentDataForRedaction(String caseId) {
    try {
      Set<String> excludedMessageTypes = new Set<String>{
        'confirmation_accepted',
        'confirmation_rejected',
        'csat_rating',
        'empty_msg_with_intent_auto_assignment',
        'intents_with_option_input'
      };

      Map<String, Object> responseMap = new Map<String, Object>();
      List<Id> caseCommentIds = new List<Id>();
      List<ContentDocumentLink> attachments = new List<ContentDocumentLink>();
      Map<Id, Case_comment__c> caseCommentMap = new Map<Id, Case_comment__c>(
        [
          SELECT Id, (SELECT ContentDocumentId FROM ContentDocumentLinks)
          FROM Case_comment__c
          WHERE
            Case__c = :caseId
            AND Message_Type__c NOT IN :excludedMessageTypes
            AND Redaction_Request__c = NULL
          WITH SECURITY_ENFORCED
          ORDER BY CreatedDate DESC, Name DESC
        ]
      );

      if (caseCommentMap != null && !caseCommentMap.isEmpty()) {
        caseCommentIds.addAll(caseCommentMap.keySet());
        responseMap.put('caseCommentIds', caseCommentIds);

        for (Case_Comment__c caseCommentRec : caseCommentMap.values()) {
          if (caseCommentRec.ContentDocumentLinks != null) {
            attachments.addAll(caseCommentRec.ContentDocumentLinks);
          }
        }
        responseMap.put('attachments', attachments);
      }

      return JSON.serialize(responseMap);
    } catch (Exception e) {
      throw new AuraHandledException(e.getMessage());
    }
  }

  private static Boolean getNicknameForMessengerFlag() {
    Boolean showNicknameInMessenger = false;
    list<Key_Configuration__mdt> keyList = [// NOPMD
      SELECT Show_Nickname_in_Messager__c
      FROM Key_Configuration__mdt
      LIMIT 1
    ];

    if (keyList != null && keyList.size() > 0) {
      showNicknameInMessenger = keyList[0].Show_Nickname_in_Messager__c;
    }

    return showNicknameInMessenger;
  }

  private static String chatHeaderIsVisible() {
    String isChatHeaderVisible = 'true';
    try {
      String chatHeaderConfigValue = Util.getConfigSettingValue('hideMessengerChatHeader');

      if (String.isNotBlank(chatHeaderConfigValue) && chatHeaderConfigValue.equalsIgnoreCase('true')) {
        isChatHeaderVisible = 'false';
      }

      return isChatHeaderVisible;
    } catch (Exception e) {
      Logger.getInstance().log('MessengerController.chatHeaderIsVisible', e);
    }
    return 'true';
  }

  @AuraEnabled(cacheable=true)
  public static List<Message> getMessagesArchived( String caseId, String hsIssueId) {
      List<Message> messageList = new List<Message>();

      //API call for custom fields
      HttpResponse resp = HS_credentials.makeApiCall(
            'GET',
            '/sf/issues/'+hsIssueId,
            null
      );

      String strResponse = resp.getBody().replace('hsPackage__', '');
      ArchivedResponse mapResponse = (ArchivedResponse) JSON.deserialize(
            strResponse,
            ArchivedResponse.class
      );
      System.debug('mapResponse : '+mapResponse);
      for (Case_Comment__c caseComment : mapResponse.messages) {
            mapProfileIdVsName.put(caseComment.CreatedById, null);
      }

      for (User user : [ SELECT Id,HSUserId__c,Name FROM User WHERE HSUserId__c IN :mapProfileIdVsName.keySet()]) {// NOPMD
            mapProfileIdVsName.put(user.HSUserId__c, user.Name);
      }

      for (Case_Comment__c caseComment : mapResponse.messages) {
            messageList.add(new Message(caseComment));
      }

      return messageList;
  }

  public class ArchivedResponse {
    public List<Case_Comment__c> messages;
  }

  public class Message {
    @AuraEnabled
    public String id;

    @AuraEnabled
    public String content;

    @AuraEnabled
    public string messageType;

    @AuraEnabled
    public string externalId;

    @AuraEnabled
    public Boolean messageRead;

    @AuraEnabled
    public Boolean feedbackBotEnabled;

    @AuraEnabled
    public Boolean messageSent;

    @AuraEnabled
    public string origin;

    @AuraEnabled
    public String createdTimestamp;

    @AuraEnabled
    public string createdDateTimeSF;

    @AuraEnabled
    public string author;

    @AuraEnabled
    public string loggedInUserDateTime;

    @AuraEnabled
    public string attachmentUrl;

    @AuraEnabled
    public string csatLabel;

    @AuraEnabled
    public Decimal csatRating;

    @AuraEnabled
    public String translatedMessage;

    @AuraEnabled
    public List<Attachment> attachmentList;

    @AuraEnabled
    public List<FAQWrapper> faqWrapperList;

    @AuraEnabled
    public List<ActionCardWrapper> acWrapperList;

    @AuraEnabled
    public List<string> documentIdList;

    @AuraEnabled
    public List<AttachmentWrapper> attachmentWrapperList;

    public Message() {
    }
    public Message(Case_Comment__c caseComment) {
      this.id = caseComment.Id;
      this.content = caseComment.Comment__c;
      this.messageType = caseComment.Message_Type__c;
      this.externalId = caseComment.External_ID__c;
      this.messageRead = caseComment.Message_Read__c;
      this.messageSent = caseComment.Message_Sent__c;
      this.origin = caseComment.Origin__c;
      this.createdTimestamp = caseComment?.CreatedDate != null ? String.valueOf(caseComment.CreatedDate.dateGMT())+'T'+String.valueOf(caseComment.CreatedDate.timeGMT()).replace('Z','') : '';
      this.attachmentUrl = caseComment.AttachmentUrls__c;
      this.translatedMessage = caseComment.Translated_Message__c;
      if (caseComment.Message_Type__c == 'csat_rating') {
        this.csatLabel = caseComment.Comment__c;
        this.csatRating = caseComment.Rating__c;
      }

      this.createdDateTimeSF = caseComment?.CreatedDate != null ?  String.valueOf(caseComment.CreatedDate.getTime()) : String.valueOf(caseComment.Created_Timestamp__c);

      this.populateAuthor(caseComment);

      this.loggedInUserDateTime = caseComment?.CreatedDate != null ? caseComment.createdDate.format() : DateTime.newInstance(Long.valueOf(caseComment.Created_Timestamp__c)).format();

      if (caseComment.Faqs__c != null) {
        this.faqWrapperList = (List<FAQWrapper>) JSON.deserialize(
          caseComment.Faqs__c,
          List<FAQWrapper>.class
        );
      }

      if (caseComment.Action_Cards__c != null) {
        this.acWrapperList = (List<ActionCardWrapper>) JSON.deserialize(
          caseComment.Action_Cards__c,
          List<ActionCardWrapper>.class
        );
      }

      this.populateAttachments(caseComment);
    }

    void populateAuthor(Case_Comment__c caseComment) {
      if(caseComment.Author__c != null && mapProfileIdVsName.containsKey(caseComment.CreatedById)){
        this.author = mapProfileIdVsName.get(caseComment.CreatedById) != null ? mapProfileIdVsName.get(caseComment.CreatedById) : (String.isNotBlank(caseComment.Author__c) && caseComment.Author__c.contains('@') ? caseComment.Author__c.split('@')[0] : caseComment.Author__c);
      }
      else if (this.origin == 'mobile') {
        if (showNickNameOnMessager) {
          this.author = caseComment.Nickname__c != null
            ? caseComment.Nickname__c
            : caseComment.Author__c;
        } else {
          this.author = caseComment.Author__c;
        }
      } else if (this.origin == 'bot') {
        if (showNickNameOnMessager) {
          this.author = caseComment.Nickname__c != null
            ? caseComment.Nickname__c
            : caseComment.Author__c;
        } else {
          this.author = caseComment.Author__c;
        }
      } else if (this.origin == 'admin' && caseComment.Author__c != null) {
        if (showNickNameOnMessager) {
          this.author = caseComment.Nickname__c != null
            ? caseComment.Nickname__c
            : caseComment.Author__c;
        } else {
          this.author = caseComment.Author__c;
        }
      } else {
        if (showNickNameOnMessager) {
          this.author = caseComment.CreatedBy.CommunityNickname;
        } else {
          this.author = caseComment.CreatedBy.FirstName;
        }
      }
    }

    void populateAttachments(Case_Comment__c caseComment) {
      Boolean isAttachmentUrlEmpty = String.isEmpty(
        caseComment.AttachmentUrls__c
      );

      if (caseComment.ContentDocumentLinks.size() > 0) {
        documentIdList = new List<string>();
        this.attachmentWrapperList = new List<AttachmentWrapper>();
        for (ContentDocumentLink cd : caseComment.ContentDocumentLinks) {
          AttachmentWrapper wrap = new AttachmentWrapper();

          if (!isAttachmentUrlEmpty) {
            documentIdList.add(cd.ContentDocumentId);
            wrap.Id = cd.ContentDocumentId;
          } else {
            documentIdList.add(cd.ContentDocument.LatestPublishedVersionId);
            wrap.Id = cd.ContentDocument.LatestPublishedVersionId;
          }

          wrap.Id = cd.ContentDocument.LatestPublishedVersionId;
          wrap.title = cd.ContentDocument.title;
          wrap.fileExtension = cd.ContentDocument.FileExtension;
          wrap.docId = cd.ContentDocumentId;
          wrap.isSelected = false;
          this.attachmentWrapperList.add(wrap);
        }

        System.debug('attachmentWrapper ' + this.attachmentWrapperList);
      }

      if (!isAttachmentUrlEmpty) {
        if (!caseComment.AttachmentUrls__c.Contains('[')) {
          Integer iIndex = 1;
          for (String url : caseComment.AttachmentUrls__c.Split(',')) {
            MessengerController.Attachment attachmentfile = new MessengerController.Attachment();
            attachmentfile.url = url;
            attachmentfile.fileName = 'Attachment ' + iIndex;
            this.attachmentList.add(attachmentfile);
            iIndex++;
          }
        } else {
          this.attachmentList = (List<MessengerController.Attachment>) JSON.deserialize(
            caseComment.AttachmentUrls__c,
            List<MessengerController.Attachment>.class
          );
          if (this.attachmentList != null) {
            for (
              MessengerController.Attachment hsAttach : this.attachmentList
            ) {
              if (
                hsAttach.content_type != null &&
                hsAttach.content_type.contains('image')
              )
                hsAttach.filetype = 'image';
              else
                hsAttach.filetype = 'other';
            }
          }
        }
      }
    }
  }

  public class Attachment {
    @AuraEnabled
    public string fileName;

    @AuraEnabled
    public string url;

    @AuraEnabled
    public string content_type;

    @AuraEnabled
    public string fileType;
  }

  public class FAQWrapper {
    @AuraEnabled
    public string title;
    @AuraEnabled
    public string faq_link;
  }

  public class ActionCardWrapper {
    @AuraEnabled
    public string title;
    @AuraEnabled
    public string image_url;
    @AuraEnabled
    public string display_text;
    @AuraEnabled
    public string link;
    @AuraEnabled
    public string type;
    @AuraEnabled
    public string phone_number;
  }

  public class AttachmentWrapper {
    @AuraEnabled
    public string Id;
    @AuraEnabled
    public Boolean isSelected;
    @AuraEnabled
    public string title;
    @AuraEnabled
    public string fileExtension;
    @AuraEnabled
    public string docId;
  }
}