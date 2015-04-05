/* * Copyright 2013 The Sculptor Project Team, including the original  * author or authors. *  * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at *  *      http://www.apache.org/licenses/LICENSE-2.0 *  * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package org.sculptor.framework.context;import java.io.Serializable;import java.util.HashSet;import java.util.Map;import java.util.Map.Entry;import java.util.Set;import javax.servlet.http.HttpServletRequest;import com.google.appengine.api.NamespaceManager;import com.google.appengine.api.users.User;import com.google.appengine.api.users.UserService;import com.google.appengine.api.users.UserServiceFactory;import com.google.apphosting.api.ApiProxy;/** * Use this together with servlet filter {@link ServiceContextServletFilter}. *  * @author Patrik Nordwall */public class AppEngineServiceContextFactory extends ServletContainerServiceContextFactory {    public AppEngineServiceContextFactory() {        super();    }    @Override    protected ServiceContext createServiceContextImpl(HttpServletRequest request) {        ServiceContext context = ServiceContextStore.get();        if (context != null) {            return context;        }        String sessionId = request.getRequestedSessionId();        String appUrl = appUrl(request);        context = createServiceContextFromAppengineEnvironment(sessionId, appUrl);        return context;    }    protected ServiceContext createServiceContextFromAppengineEnvironment(String sessionId, String appUrl) {        ServiceContext context;        String applicationId = ApiProxy.getCurrentEnvironment().getAppId();        String userId = userName();        Set<String> roles = new HashSet<String>();        if (ApiProxy.getCurrentEnvironment().isAdmin()) {            roles.add("admin");        }        if (ApiProxy.getCurrentEnvironment().isLoggedIn()) {            roles.add("loggedIn");        }        context = new ServiceContext(userId, sessionId, applicationId, roles);        populateAttributesFromAppengineEnvironment(context);        context.setProperty("appUrl", appUrl);        return context;    }    protected String appUrl(HttpServletRequest request) {        if (isRunningLocally(request)) {            return "http://localhost:" + request.getLocalPort();        } else {            return System.getProperty("appUrl", "http://" + ApiProxy.getCurrentEnvironment().getAppId()                    + ".appspot.com");        }    }    private boolean isRunningLocally(HttpServletRequest request) {        // TODO is there a better way?        return (request.getLocalPort() > 1024);    }    protected void populateAttributesFromAppengineEnvironment(ServiceContext context) {        context.setProperty("authDomain", ApiProxy.getCurrentEnvironment().getAuthDomain());        context.setProperty("requestNamespace", NamespaceManager.getGoogleAppsNamespace());        context.setProperty("versionId", ApiProxy.getCurrentEnvironment().getVersionId());        context.setProperty("user.email", ApiProxy.getCurrentEnvironment().getEmail());        User user = user();        if (user != null) {            context.setProperty("user.nickName", user.getNickname());        }        Map<String, Object> attributes = ApiProxy.getCurrentEnvironment().getAttributes();        for (Entry<String, Object> each : attributes.entrySet()) {            if (each.getValue() instanceof Serializable) {                context.setProperty(each.getKey(), (Serializable) each.getValue());            }        }    }    private String userName() {        User user = user();        if (user == null) {            return GUEST_USER;        }        return user.getNickname();    }    private User user() {        UserService userService = UserServiceFactory.getUserService();        User user = userService.getCurrentUser();        return user;    }}