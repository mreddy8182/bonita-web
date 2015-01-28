/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.console.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.console.common.server.utils.TenantFolder;
import org.bonitasoft.console.common.server.utils.UnauthorizedFolderException;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.common.model.ImportStatusMessages;
import org.bonitasoft.web.rest.server.BonitaRestAPIServlet;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;
import org.bonitasoft.web.toolkit.server.ServiceException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Julien Mege
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationsImportServiceTest {

    @Mock
    private ApplicationAPI applicationAPI;

    @Mock
    private TenantFolder tenantFolder;

    @Spy
    private ApplicationsImportService spiedApplicationImportService;

    @BeforeClass
    public static void initEnvironnement() {
        I18n.getInstance();
        new BonitaRestAPIServlet();
    }

    @Before
    public void init() throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        MockitoAnnotations.initMocks(this);
        doReturn(LOCALE.en).when(spiedApplicationImportService).getLocale();
        Mockito.doReturn(applicationAPI).when(spiedApplicationImportService).getApplicationAPI();
    }

    @Test
    public void should_importFileContent_call_ApplicationAPI_with_valid_param() throws Exception {
        spiedApplicationImportService.importFileContent(new byte[0], "FAIL_ON_DUPLICATES");
        verify(applicationAPI).importApplications(new byte[0], ApplicationImportPolicy.FAIL_ON_DUPLICATES);
    }

    @Test
    public void should_importFileContent_return_ImportStatusMessages() throws Exception {
        final ArrayList<ImportStatus> statusList = new ArrayList<ImportStatus>();
        statusList.add(new ImportStatus("status"));
        doReturn(statusList).when(applicationAPI).importApplications(new byte[0], ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        final ImportStatusMessages importStatusMessages = spiedApplicationImportService.importFileContent(new byte[0], "FAIL_ON_DUPLICATES");
        assertEquals(importStatusMessages.getImported().size(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_importFileContent_with_invalid_policy_throw_error() throws Exception {
        spiedApplicationImportService.importFileContent(new byte[0], "NOT_AUTHORIZED_POLICY");
    }

    @Test
    public void should_Logger_log_using_expected_class_name() throws Exception {
        assertEquals(spiedApplicationImportService.getLogger().getName(), "org.bonitasoft.console.server.service.ApplicationsImportService");
    }

    @Test
    public void should_FileReadingError_talk_about_application() throws Exception {
        assertEquals(spiedApplicationImportService.getFileReadingError(), "Error during Application import file reading.");
    }

    @Test
    public void should_getFileFormatExceptionMessage_talk_about_application() throws Exception {
        assertEquals(spiedApplicationImportService.getFileFormatExceptionMessage(), "Can't import Applications.");
    }

    @Test
    public void should_AlreadyExistsExceptionMessage_talk_about_application() throws Exception {
        final AlreadyExistsException alreadyExistsException = new AlreadyExistsException("name", "token");
        assertEquals(spiedApplicationImportService.getAlreadyExistsExceptionMessage(alreadyExistsException),
                "Can't import applications. An application 'token' already exists");
    }

    @Test
    public void should_getToken_return_expected_name() throws Exception {
        assertEquals(spiedApplicationImportService.getToken(), "/application/import");
    }

    @Test
    public void should_getFileUploadParamName_return_expected_name() throws Exception {
        assertEquals(spiedApplicationImportService.getFileUploadParamName(), "applicationsDataUpload");
    }


    @Test
    public void should_verify_authorisation_for_the_given_location_param() throws
    Exception {

        doReturn(tenantFolder).when(spiedApplicationImportService).getTenantFolder();
        doReturn("../../../file.xml").when(spiedApplicationImportService).getFileUploadParamValue();
        doReturn(1L).when(spiedApplicationImportService).getTenantId();
        doThrow(new UnauthorizedFolderException("error")).when(tenantFolder).getTempFile(any(String.class), any(Long.class));

        try {
            spiedApplicationImportService.run();
        } catch (final ServiceException e) {
            assertTrue(e.getCause().getMessage().startsWith("error"));
        }
    }

}
