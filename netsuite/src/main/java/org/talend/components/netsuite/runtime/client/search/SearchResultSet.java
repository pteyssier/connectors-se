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
package org.talend.components.netsuite.runtime.client.search;

import org.talend.components.netsuite.runtime.client.NetSuiteClientService;
import org.talend.components.netsuite.runtime.client.NsSearchResult;
import org.talend.components.netsuite.runtime.client.ResultSet;
import org.talend.components.netsuite.runtime.model.BasicRecordType;
import org.talend.components.netsuite.runtime.model.RecordTypeDesc;
import org.talend.components.netsuite.runtime.model.SearchRecordTypeDesc;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toCollection;

/**
 * Result set for search results.
 *
 * @see SearchQuery#search()
 * @see NetSuiteClientService#search(Object)
 */
public class SearchResultSet<R> implements ResultSet<R> {

    /** NetSuite client which this result set is owned by. */
    private NetSuiteClientService<?> clientService;

    /** Descriptor of target record type. */
    private RecordTypeDesc recordTypeDesc;

    /** Descriptor of target search record type. */
    private SearchRecordTypeDesc searchRecordTypeDesc;

    /** NetSuite specific identifier of search. */
    private String searchId;

    /** Current search result being processed. */
    private NsSearchResult<R> result;

    /** List of records for current search result. */
    private List<R> recordList;

    /** Iterator of records for current search result. */
    private Iterator<R> recordIterator;

    /** Last retrieved record. */
    private R current;

    public SearchResultSet(NetSuiteClientService<?> clientService, RecordTypeDesc recordTypeDesc,
            SearchRecordTypeDesc searchRecordTypeDesc, NsSearchResult<R> result) {

        this.clientService = clientService;
        this.recordTypeDesc = recordTypeDesc;
        this.searchRecordTypeDesc = searchRecordTypeDesc;
        this.result = result;

        searchId = result.getSearchId();
        recordList = prepareRecordList();
        recordIterator = recordList.iterator();
    }

    public NetSuiteClientService<?> getClientService() {
        return clientService;
    }

    public RecordTypeDesc getRecordTypeDesc() {
        return recordTypeDesc;
    }

    public SearchRecordTypeDesc getSearchRecordTypeDesc() {
        return searchRecordTypeDesc;
    }

    public String getSearchId() {
        return searchId;
    }

    @Override
    public boolean next() {
        if (!recordIterator.hasNext() && hasMore()) {
            recordList = getMoreRecords();
            recordIterator = recordList.iterator();
        }
        if (recordIterator.hasNext()) {
            current = recordIterator.next();
            return true;
        }
        return false;
    }

    @Override
    public R get() {
        return current;
    }

    /**
     * Check whether search has more results that can be retrieved.
     *
     * @return {@code true} if there are more results, {@code false} otherwise
     */
    protected boolean hasMore() {
        if (this.result == null || result.getPageIndex() == null || result.getTotalPages() == null) {
            return false;
        }
        return result.getPageIndex().intValue() < result.getTotalPages().intValue();
    }

    /**
     * Retrieve next page of search results.
     *
     * @return list of records from retrieved search result
     */
    protected List<R> getMoreRecords() {
        if (searchId != null) {
            int nextPageIndex = result.getPageIndex().intValue() + 1;
            NsSearchResult<R> nextPageResult = clientService.searchMoreWithId(searchId, nextPageIndex);
            if (!nextPageResult.isSuccess()) {
                NetSuiteClientService.checkError(nextPageResult.getStatus());
            }
            result = nextPageResult;
            return prepareRecordList();
        }
        return Collections.emptyList();
    }

    /**
     * Filter list of records before returning to a caller.
     *
     * @return list of records ready for consuming
     */
    protected List<R> prepareRecordList() {
        List<R> recordList = result.getRecordList();
        if (recordList == null || recordList.isEmpty()) {
            return Collections.emptyList();
        }
        Predicate<R> checkRecordTypeClass = r -> r.getClass() == recordTypeDesc.getRecordClass();
        return BasicRecordType.ITEM.getType().equals(searchRecordTypeDesc.getType())
                ? recordList.stream().filter(checkRecordTypeClass).collect(toCollection(LinkedList::new))
                : recordList;
    }

}
