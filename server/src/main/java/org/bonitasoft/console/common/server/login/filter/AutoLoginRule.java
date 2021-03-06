/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.console.common.server.login.filter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.auth.AuthenticationFailedException;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.LoginFailedException;
import org.bonitasoft.console.common.server.login.LoginManager;
import org.bonitasoft.console.common.server.login.TenantIdAccessor;
import org.bonitasoft.console.common.server.login.datastore.AutoLoginCredentials;
import org.bonitasoft.console.common.server.login.datastore.UserLogger;
import org.bonitasoft.console.common.server.preferences.properties.ProcessIdentifier;
import org.bonitasoft.console.common.server.preferences.properties.SecurityProperties;
import org.bonitasoft.engine.exception.TenantStatusException;

public class AutoLoginRule extends AuthenticationRule {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(AutoLoginRule.class.getName());

    @Override
    public boolean doAuthorize(final HttpServletRequestAccessor request, HttpServletResponse response, final TenantIdAccessor tenantIdAccessor) throws ServletException {
        final long tenantId = tenantIdAccessor.ensureTenantId();
        return isAutoLogin(request, tenantId) && doAutoLogin(request, response, tenantId);
    }

    private boolean doAutoLogin(final HttpServletRequestAccessor request, HttpServletResponse response,
                                final long tenantId) throws ServletException {
        try {
            final AutoLoginCredentials userCredentials = new AutoLoginCredentials(getSecurityProperties(request, tenantId), tenantId);
            final LoginManager loginManager = getLoginManager();
            loginManager.login(request, response, createUserLogger(), userCredentials);
            return true;
        } catch (final AuthenticationFailedException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Authentication failed : " + e.getMessage(), e);
            }
            return false;
        } catch (final LoginFailedException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "login exception : " + e.getMessage(), e);
            }
            return false;
        } catch (final TenantStatusException e) {
            throw new TenantIsPausedRedirectionToMaintenancePageException(e.getMessage(), tenantId);
        }
    }

    private boolean isAutoLogin(final HttpServletRequestAccessor request, final long tenantId) {
        return request.isAutoLoginRequested()
                && getSecurityProperties(request, tenantId).allowAutoLogin();
    }

    protected UserLogger createUserLogger() {
        return new UserLogger();
    }

    protected SecurityProperties getSecurityProperties(final HttpServletRequestAccessor httpRequest, final long tenantId) {
        return new SecurityProperties(tenantId, new ProcessIdentifier(httpRequest.getAutoLoginScope()));
    }
}
