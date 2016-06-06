/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/
package org.bonitasoft.web.rest.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import org.bonitasoft.web.rest.server.api.bdm.BusinessDataFindByIdsResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataQueryResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferenceResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferencesResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseContextResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.CaseContextResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.CaseInfoResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.ActivityVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.TimerEventTriggerResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.UserTaskContextResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.UserTaskContractResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.UserTaskExecutionResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.ArchivedUserTaskContextResource;
import org.bonitasoft.web.rest.server.api.bpm.process.ProcessContractResource;
import org.bonitasoft.web.rest.server.api.bpm.process.ProcessDefinitionDesignResource;
import org.bonitasoft.web.rest.server.api.bpm.process.ProcessInstantiationResource;
import org.bonitasoft.web.rest.server.api.form.FormMappingResource;
import org.bonitasoft.web.rest.server.api.system.I18nTanslationResource;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.util.Series;

/**
 * @author Matthieu Chaffotte
 */
public class BonitaRestletApplication extends Application {

    public static final String ROUTER_EXTENSION_PREFIX = "/extension/";

    public static final String BDM_BUSINESS_DATA_URL = "/bdm/businessData";

    public static final String BDM_BUSINESS_DATA_REFERENCE_URL = "/bdm/businessDataReference";

    public static final String FORM_MAPPING_URL = "/form/mapping";

    public static final String BPM_PROCESS_URL = "/bpm/process";

    public static final String BPM_USER_TASK_URL = "/bpm/userTask";

    public static final String BPM_ARCHIVED_USER_TASK_URL = "/bpm/archivedUserTask";

    public static final String BPM_TIMER_EVENT_TRIGGER_URL = "/bpm/timerEventTrigger";

    public static final String BPM_ACTIVITY_VARIABLE_URL = "/bpm/activityVariable";

    public static final String BPM_CASE_INFO_URL = "/bpm/caseInfo";

    public static final String BPM_CASE_CONTEXT_URL = "/bpm/case";

    private static final String BPM_ARCHIVED_CASE_CONTEXT_URL = "/bpm/archivedCase";

    private final FinderFactory factory;


    public BonitaRestletApplication(final FinderFactory finderFactory,final ConverterHelper converterHelper ) {
        super();
        factory = finderFactory;
        getMetadataService().setDefaultMediaType(MediaType.APPLICATION_JSON);
        getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
        replaceJacksonConverter(converterHelper);
    }

    private void replaceJacksonConverter(final ConverterHelper converterHelper) {
        final List<ConverterHelper> registeredConverters = Engine.getInstance().getRegisteredConverters();
        registeredConverters.add(converterHelper);
        for (final ConverterHelper registeredConverter : registeredConverters) {
            if (registeredConverter.getClass().equals(JacksonConverter.class)){
                registeredConverters.remove(registeredConverter);
                registeredConverters.add(converterHelper);
            }
        }
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot() {
        return buildRouter();
    }

    protected Router buildRouter() {
        final Context context = getContext();
        final Router router = new Router(context);
        // WARNING: if you add a route you need to declare it in org.bonitasoft.web.rest.server.FinderFactory

        // GET an activityData:
        router.attach(BPM_ACTIVITY_VARIABLE_URL + "/{" + ActivityVariableResource.ACTIVITYDATA_ACTIVITY_ID + "}/{"
                + ActivityVariableResource.ACTIVITYDATA_DATA_NAME
                + "}", factory.create(ActivityVariableResource.class));

        // GET to search timer event triggers:
        router.attach(BPM_TIMER_EVENT_TRIGGER_URL, factory.create(TimerEventTriggerResource.class));
        // PUT to update timer event trigger date:
        router.attach(BPM_TIMER_EVENT_TRIGGER_URL + "/{" + TimerEventTriggerResource.ID_PARAM_NAME + "}", factory.create(TimerEventTriggerResource.class));

        // GET to case info (with task state counter)
        router.attach(BPM_CASE_INFO_URL + "/{" + CaseInfoResource.CASE_ID + "}", factory.create(CaseInfoResource.class));

        // GET to retrieve a case context:
        router.attach(BPM_CASE_CONTEXT_URL + "/{caseId}/context", factory.create(CaseContextResource.class));

        // GET to retrieve an archived case context
        router.attach(BPM_ARCHIVED_CASE_CONTEXT_URL + "/{archivedCaseId}/context", factory.create(ArchivedCaseContextResource.class));

        // GET a task contract:
        router.attach(BPM_USER_TASK_URL + "/{taskId}/contract", factory.create(UserTaskContractResource.class));
        // POST to execute a task with contract:
        router.attach(BPM_USER_TASK_URL + "/{taskId}/execution", factory.create(UserTaskExecutionResource.class));
        // GET to retrieve a task context:
        router.attach(BPM_USER_TASK_URL + "/{taskId}/context", factory.create(UserTaskContextResource.class));

        // GET an archived task context:
        router.attach(BPM_ARCHIVED_USER_TASK_URL + "/{archivedTaskId}/context", factory.create(ArchivedUserTaskContextResource.class));

        // GET a process defintion design :
        router.attach(BPM_PROCESS_URL + "/{processDefinitionId}/design", factory.create(ProcessDefinitionDesignResource.class));
        // GET a process contract:
        router.attach(BPM_PROCESS_URL + "/{processDefinitionId}/contract", factory.create(ProcessContractResource.class));
        // POST to instantiate a process with contract:
        router.attach(BPM_PROCESS_URL + "/{processDefinitionId}/instantiation", factory.create(ProcessInstantiationResource.class));

        // GET to search form mappings:
        router.attach(FORM_MAPPING_URL, factory.create(FormMappingResource.class));

        //GET a BusinessData
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}/findByIds", factory.create(BusinessDataFindByIdsResource.class));
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}", factory.create(BusinessDataQueryResource.class));
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}/{id}", factory.create(BusinessDataResource.class));
        router.attach(BDM_BUSINESS_DATA_URL + "/{className}/{id}/{fieldName}", factory.create(BusinessDataResource.class));

        // GET a Multiple BusinessDataReference
        router.attach(BDM_BUSINESS_DATA_REFERENCE_URL, factory.create(BusinessDataReferencesResource.class));
        // GET a Simple BusinessDataReference
        router.attach(BDM_BUSINESS_DATA_REFERENCE_URL + "/{caseId}/{dataName}", factory.create(BusinessDataReferenceResource.class));

        // api extension
        router.attach(ROUTER_EXTENSION_PREFIX, factory.createExtensionResource(), Template.MODE_STARTS_WITH);

        // GET all translations
        router.attach("/system/i18ntranslation", factory.create(I18nTanslationResource.class));

        return router;
    }

    @Override
    public void handle(final Request request, final Response response) {
        request.setLoggable(false);
        Engine.setLogLevel(Level.OFF);
        Engine.setRestletLogLevel(Level.OFF);

        Series<Header> headers = (Series<Header>) response.getAttributes().get("org.restlet.http.headers");
        if (headers == null) {
            headers = new Series(Header.class);
            response.getAttributes().put("org.restlet.http.headers", headers);
        }
        headers.add("Pragma", "No-cache");
        headers.add("Cache-Control", "no-cache,no-store,no-transform,max-age=0");
        final Date expdate = new Date();
        expdate.setTime(expdate.getTime() - 3600000 * 24);
        final SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        headers.add("Expires", df.format(expdate));

        super.handle(request, response);
    }
}
