/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.netsuite.runtime.v2018_2.client;

import com.netsuite.webservices.v2018_2.platform.ExceededRequestSizeFault;
import com.netsuite.webservices.v2018_2.platform.InsufficientPermissionFault;
import com.netsuite.webservices.v2018_2.platform.InvalidCredentialsFault;
import com.netsuite.webservices.v2018_2.platform.InvalidSessionFault;
import com.netsuite.webservices.v2018_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2018_2.platform.NetSuiteService;
import com.netsuite.webservices.v2018_2.platform.UnexpectedErrorFault;
import com.netsuite.webservices.v2018_2.platform.core.BaseRef;
import com.netsuite.webservices.v2018_2.platform.core.DataCenterUrls;
import com.netsuite.webservices.v2018_2.platform.core.GetServerTimeResult;
import com.netsuite.webservices.v2018_2.platform.core.Passport;
import com.netsuite.webservices.v2018_2.platform.core.Record;
import com.netsuite.webservices.v2018_2.platform.core.RecordRef;
import com.netsuite.webservices.v2018_2.platform.core.SearchRecord;
import com.netsuite.webservices.v2018_2.platform.core.SearchResult;
import com.netsuite.webservices.v2018_2.platform.core.Status;
import com.netsuite.webservices.v2018_2.platform.core.StatusDetail;
import com.netsuite.webservices.v2018_2.platform.core.TokenPassport;
import com.netsuite.webservices.v2018_2.platform.core.TokenPassportSignature;
import com.netsuite.webservices.v2018_2.platform.messages.AddListRequest;
import com.netsuite.webservices.v2018_2.platform.messages.ApplicationInfo;
import com.netsuite.webservices.v2018_2.platform.messages.DeleteListRequest;
import com.netsuite.webservices.v2018_2.platform.messages.GetDataCenterUrlsRequest;
import com.netsuite.webservices.v2018_2.platform.messages.GetDataCenterUrlsResponse;
import com.netsuite.webservices.v2018_2.platform.messages.GetServerTimeRequest;
import com.netsuite.webservices.v2018_2.platform.messages.LoginRequest;
import com.netsuite.webservices.v2018_2.platform.messages.LoginResponse;
import com.netsuite.webservices.v2018_2.platform.messages.LogoutRequest;
import com.netsuite.webservices.v2018_2.platform.messages.Preferences;
import com.netsuite.webservices.v2018_2.platform.messages.ReadResponse;
import com.netsuite.webservices.v2018_2.platform.messages.ReadResponseList;
import com.netsuite.webservices.v2018_2.platform.messages.SearchMoreWithIdRequest;
import com.netsuite.webservices.v2018_2.platform.messages.SearchPreferences;
import com.netsuite.webservices.v2018_2.platform.messages.SearchRequest;
import com.netsuite.webservices.v2018_2.platform.messages.SessionResponse;
import com.netsuite.webservices.v2018_2.platform.messages.UpdateListRequest;
import com.netsuite.webservices.v2018_2.platform.messages.UpsertListRequest;
import com.netsuite.webservices.v2018_2.platform.messages.WriteResponse;
import com.netsuite.webservices.v2018_2.platform.messages.WriteResponseList;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.talend.components.netsuite.runtime.NetSuiteErrorCode;
import org.talend.components.netsuite.runtime.client.CustomMetaDataSource;
import org.talend.components.netsuite.runtime.client.DefaultCustomMetaDataSource;
import org.talend.components.netsuite.runtime.client.DefaultMetaDataSource;
import org.talend.components.netsuite.runtime.client.NetSuiteClientService;
import org.talend.components.netsuite.runtime.client.NetSuiteCredentials;
import org.talend.components.netsuite.runtime.client.NetSuiteException;
import org.talend.components.netsuite.runtime.client.NsPreferences;
import org.talend.components.netsuite.runtime.client.NsReadResponse;
import org.talend.components.netsuite.runtime.client.NsSearchPreferences;
import org.talend.components.netsuite.runtime.client.NsSearchResult;
import org.talend.components.netsuite.runtime.client.NsStatus;
import org.talend.components.netsuite.runtime.client.NsWriteResponse;
import org.talend.components.netsuite.runtime.model.BasicMetaData;
import org.talend.components.netsuite.runtime.v2018_2.model.BasicMetaDataImpl;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 *
 */
public class NetSuiteClientServiceImpl extends NetSuiteClientService<NetSuitePortType> {

    private static final String WSDL_2018_2_NETSUITE_WSDL = "/wsdl/2018.2/netsuite.wsdl";

    public static final String DEFAULT_ENDPOINT_URL = "https://webservices.netsuite.com/services/NetSuitePort_2018_2";

    public static final String NS_URI_PLATFORM_MESSAGES = "urn:messages_2018_2.platform.webservices.netsuite.com";

    private TokenPassport nativeTokenPassport;

    public NetSuiteClientServiceImpl() {
        super();

        portAdapter = new PortAdapterImpl();
        metaDataSource = new DefaultMetaDataSource(this);
    }

    @Override
    public BasicMetaData getBasicMetaData() {
        BasicMetaData metadata = BasicMetaDataImpl.getInstance();
        metadata.setI18n(i18n);
        return metadata;
    }

    @Override
    public CustomMetaDataSource createDefaultCustomMetaDataSource() {
        return new DefaultCustomMetaDataSource<>(this, new CustomMetaDataRetrieverImpl(this, i18n));
    }

    @Override
    protected void doLogout() throws NetSuiteException {
        try {
            LogoutRequest request = new LogoutRequest();
            port.logout(request);
        } catch (Exception e) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), i18n.cannotLogoutFromNetSuite(),
                    e);
        }
    }

    @Override
    protected void doLogin() throws NetSuiteException {
        port = getNetSuitePort(endpointUrl, credentials != null ? credentials.getAccount() : tokenPassport.getAccount());

        setHttpClientPolicy(port);

        PortOperation<?, NetSuitePortType> loginOp;
        if (credentials != null) {
            if (!credentials.isUseSsoLogin()) {
                setLoginHeaders(port);
                final Passport passport = createNativePassport(credentials);
                loginOp = new PortOperation<SessionResponse, NetSuitePortType>() {

                    @Override
                    public SessionResponse execute(NetSuitePortType port) throws Exception {
                        LoginRequest request = new LoginRequest();
                        request.setPassport(passport);
                        LoginResponse response = port.login(request);
                        return response.getSessionResponse();
                    }
                };
            } else {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.OPERATION_NOT_SUPPORTED),
                        i18n.ssoLoginNotSupported());
            }
        } else {
            loginOp = (portType) -> {
                refreshTokenSignature();
                return portType.getServerTime(new GetServerTimeRequest()).getGetServerTimeResult();
            };
        }

        Status status = null;
        Object response;
        String exceptionMessage = null;
        for (int i = 0; i < getRetryCount(); i++) {
            try {
                response = loginOp.execute(port);
                status = response instanceof SessionResponse ? ((SessionResponse) response).getStatus()
                        : ((GetServerTimeResult) response).getStatus();

            } catch (InvalidCredentialsFault f) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR), f.getFaultInfo().getMessage());
            } catch (UnexpectedErrorFault f) {
                exceptionMessage = f.getFaultInfo().getMessage();
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }

            if (status != null) {
                break;
            }

            if (i != getRetryCount() - 1) {
                waitForRetryInterval();
            }
        }

        checkLoginError(toNsStatus(status), exceptionMessage);

        if (credentials != null) {
            removeLoginHeaders(port);
        }
    }

    @Override
    protected String getPlatformMessageNamespaceUri() {
        return NS_URI_PLATFORM_MESSAGES;
    }

    @Override
    protected Preferences createNativePreferences(NsPreferences nsPreferences) {
        Preferences preferences = new Preferences();
        try {
            BeanUtils.copyProperties(preferences, nsPreferences);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
        return preferences;
    }

    @Override
    protected SearchPreferences createNativeSearchPreferences(NsSearchPreferences nsSearchPreferences) {
        SearchPreferences searchPreferences = new SearchPreferences();
        try {
            BeanUtils.copyProperties(searchPreferences, nsSearchPreferences);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
        return searchPreferences;
    }

    @Override
    protected ApplicationInfo createNativeApplicationInfo(NetSuiteCredentials credentials) {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setApplicationId(credentials.getApplicationId());
        return applicationInfo;
    }

    @Override
    protected Passport createNativePassport(NetSuiteCredentials nsCredentials) {
        RecordRef roleRecord = new RecordRef();
        roleRecord.setInternalId(nsCredentials.getRoleId());

        final Passport passport = new Passport();
        passport.setEmail(nsCredentials.getEmail());
        passport.setPassword(nsCredentials.getPassword());
        passport.setRole(roleRecord);
        passport.setAccount(nsCredentials.getAccount());

        return passport;
    }

    @Override
    protected TokenPassport createNativeTokenPassport() {
        final TokenPassportSignature signature = new TokenPassportSignature();
        signature.setValue(tokenPassport.refresh());
        signature.setAlgorithm(tokenPassport.getSignature().getAlgorithm().name());

        final TokenPassport tokenPass = new TokenPassport();
        tokenPass.setSignature(signature);
        tokenPass.setAccount(tokenPassport.getAccount());
        tokenPass.setConsumerKey(tokenPassport.getConsumerKey());
        tokenPass.setToken(tokenPassport.getToken());
        tokenPass.setNonce(tokenPassport.getNonce());
        tokenPass.setTimestamp(tokenPassport.getTimestamp());
        return tokenPass;
    }

    @Override
    protected void refreshTokenSignature() {
        nativeTokenPassport.getSignature().setValue(tokenPassport.refresh());
        nativeTokenPassport.setNonce(tokenPassport.getNonce());
        nativeTokenPassport.setTimestamp(tokenPassport.getTimestamp());
    }

    @Override
    protected NetSuitePortType getNetSuitePort(String defaultEndpointUrl, String account) throws NetSuiteException {
        try {
            URL wsdlLocationUrl = this.getClass().getResource(WSDL_2018_2_NETSUITE_WSDL);

            NetSuiteService service = new NetSuiteService(wsdlLocationUrl, NetSuiteService.SERVICE);

            List<WebServiceFeature> features = new ArrayList<>(2);
            if (isMessageLoggingEnabled()) {
                features.add(new LoggingFeature());
            }
            NetSuitePortType port = service.getNetSuitePort(features.toArray(new WebServiceFeature[features.size()]));

            BindingProvider provider = (BindingProvider) port;
            Map<String, Object> requestContext = provider.getRequestContext();
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, defaultEndpointUrl);
            if (tokenPassport != null) {
                nativeTokenPassport = createNativeTokenPassport();
                Header tokenHeader = new Header(new QName(getPlatformMessageNamespaceUri(), "tokenPassport"), nativeTokenPassport,
                        new JAXBDataBinding(nativeTokenPassport.getClass()));
                Optional.ofNullable((List<Header>) requestContext.get(Header.HEADER_LIST)).orElseGet(() -> {
                    List<Header> list = new ArrayList<>();
                    requestContext.put(Header.HEADER_LIST, list);
                    return list;
                }).add(tokenHeader);
            }

            GetDataCenterUrlsRequest dataCenterRequest = new GetDataCenterUrlsRequest();
            dataCenterRequest.setAccount(account);
            DataCenterUrls urls = null;
            GetDataCenterUrlsResponse response = port.getDataCenterUrls(dataCenterRequest);
            if (response != null && response.getGetDataCenterUrlsResult() != null) {
                urls = response.getGetDataCenterUrlsResult().getDataCenterUrls();
            }
            if (urls == null) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                        i18n.couldNotGetWebServiceDomain(defaultEndpointUrl));
            }

            String wsDomain = urls.getWebservicesDomain();
            String endpointUrl = wsDomain.concat(new URL(defaultEndpointUrl).getPath());

            requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);

            return port;
        } catch (WebServiceException | MalformedURLException | InsufficientPermissionFault | InvalidCredentialsFault
                | InvalidSessionFault | UnexpectedErrorFault | ExceededRequestSizeFault | JAXBException e) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    i18n.failedToInitClient(e.getLocalizedMessage()), e);
        }
    }

    @Override
    protected boolean errorCanBeWorkedAround(Throwable t) {
        return t instanceof RemoteException || t instanceof SOAPFaultException || t instanceof SocketException;
    }

    @Override
    protected boolean errorRequiresNewLogin(Throwable t) {
        return t instanceof SocketException;
    }

    public static <RefT> List<NsWriteResponse<?>> toNsWriteResponseList(WriteResponseList writeResponseList) {
        return writeResponseList.getWriteResponse().stream().map(NetSuiteClientServiceImpl::toNsWriteResponse).collect(toList());
    }

    public static <RecT> List<NsReadResponse<RecT>> toNsReadResponseList(ReadResponseList readResponseList) {
        return readResponseList.getReadResponse().stream()
                .map(readResponse -> (NsReadResponse<RecT>) toNsReadResponse(readResponse)).collect(toList());
    }

    public static <RecT> NsSearchResult<RecT> toNsSearchResult(SearchResult result) {
        NsSearchResult nsResult = new NsSearchResult(toNsStatus(result.getStatus()));
        nsResult.setSearchId(result.getSearchId());
        nsResult.setTotalPages(result.getTotalPages());
        nsResult.setTotalRecords(result.getTotalRecords());
        nsResult.setPageIndex(result.getPageIndex());
        nsResult.setPageSize(result.getPageSize());
        if (result.getRecordList() != null) {
            List<Record> nsRecordList = new ArrayList<>(result.getRecordList().getRecord().size());
            for (Record record : result.getRecordList().getRecord()) {
                nsRecordList.add(record);
            }
            nsResult.setRecordList(nsRecordList);
        } else {
            nsResult.setRecordList(Collections.emptyList());
        }
        return nsResult;
    }

    public static <RefT> NsWriteResponse<RefT> toNsWriteResponse(WriteResponse writeResponse) {
        return new NsWriteResponse(toNsStatus(writeResponse.getStatus()), writeResponse.getBaseRef());
    }

    public static <RecT> NsReadResponse<RecT> toNsReadResponse(ReadResponse readResponse) {
        return new NsReadResponse(toNsStatus(readResponse.getStatus()), readResponse.getRecord());
    }

    public static <RecT> List<Record> toRecordList(List<RecT> nsRecordList) {
        return nsRecordList.stream().map(Record.class::cast).collect(toList());
    }

    public static <RefT> List<BaseRef> toBaseRefList(List<RefT> nsRefList) {
        return nsRefList.stream().map(BaseRef.class::cast).collect(toList());
    }

    public static NsStatus toNsStatus(Status status) {
        if (status == null) {
            return null;
        }
        NsStatus nsStatus = new NsStatus();
        nsStatus.setSuccess(status.getIsSuccess());
        nsStatus.setDetails(status.getStatusDetail().stream().map(NetSuiteClientServiceImpl::toNsStatusDetail).collect(toList()));
        return nsStatus;
    }

    /**
     * Convert response {@link StatusDetail} into internal {@link NsStatus.Detail} representation
     * 
     * @param detail - response detail
     * @return internal status detail
     */
    public static NsStatus.Detail toNsStatusDetail(StatusDetail detail) {
        NsStatus.Detail nsDetail = new NsStatus.Detail();
        if (detail.getType() != null) {
            nsDetail.setType(NsStatus.Type.valueOf(detail.getType().value()));
        }
        if (detail.getCode() != null) {
            nsDetail.setCode(detail.getCode().value());
        }
        nsDetail.setMessage(detail.getMessage());
        return nsDetail;
    }

    protected class PortAdapterImpl implements PortAdapter<NetSuitePortType> {

        @Override
        public <RecT, SearchT> NsSearchResult<RecT> search(final NetSuitePortType port, final SearchT searchRecord)
                throws Exception {
            SearchRequest request = new SearchRequest();
            SearchRecord sr = (SearchRecord) searchRecord;
            request.setSearchRecord(sr);

            SearchResult result = port.search(request).getSearchResult();
            return toNsSearchResult(result);
        }

        @Override
        public <RecT> NsSearchResult<RecT> searchMoreWithId(final NetSuitePortType port, final String searchId,
                final int pageIndex) throws Exception {
            SearchMoreWithIdRequest request = new SearchMoreWithIdRequest();
            request.setSearchId(searchId);
            request.setPageIndex(pageIndex);

            SearchResult result = port.searchMoreWithId(request).getSearchResult();
            return toNsSearchResult(result);
        }

        @Override
        public <RecT, RefT> List<NsWriteResponse<?>> addList(final NetSuitePortType port, final List<RecT> records)
                throws Exception {
            AddListRequest request = new AddListRequest();
            request.getRecord().addAll(toRecordList(records));

            WriteResponseList writeResponseList = port.addList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

        @Override
        public <RecT, RefT> List<NsWriteResponse<?>> updateList(final NetSuitePortType port, final List<RecT> records)
                throws Exception {
            UpdateListRequest request = new UpdateListRequest();
            request.getRecord().addAll(toRecordList(records));

            WriteResponseList writeResponseList = port.updateList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

        @Override
        public <RecT, RefT> List<NsWriteResponse<?>> upsertList(final NetSuitePortType port, final List<RecT> records)
                throws Exception {
            UpsertListRequest request = new UpsertListRequest();
            request.getRecord().addAll(toRecordList(records));

            WriteResponseList writeResponseList = port.upsertList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

        @Override
        public <RefT> List<NsWriteResponse<?>> deleteList(final NetSuitePortType port, final List<?> refs) throws Exception {
            DeleteListRequest request = new DeleteListRequest();
            request.getBaseRef().addAll(toBaseRefList(refs));

            WriteResponseList writeResponseList = port.deleteList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

    }
}