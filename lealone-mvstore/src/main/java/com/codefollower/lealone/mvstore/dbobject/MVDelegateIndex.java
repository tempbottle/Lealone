/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.codefollower.lealone.mvstore.dbobject;

import com.codefollower.lealone.dbobject.index.BaseIndex;
import com.codefollower.lealone.dbobject.index.Cursor;
import com.codefollower.lealone.dbobject.index.IndexType;
import com.codefollower.lealone.dbobject.table.Column;
import com.codefollower.lealone.dbobject.table.IndexColumn;
import com.codefollower.lealone.engine.Session;
import com.codefollower.lealone.message.DbException;
import com.codefollower.lealone.result.Row;
import com.codefollower.lealone.result.SearchRow;
import com.codefollower.lealone.result.SortOrder;

/**
 * An index that delegates indexing to another index.
 */
public class MVDelegateIndex extends BaseIndex {

    private final MVPrimaryIndex mainIndex;

    public MVDelegateIndex(MVTable table, int id, String name,
            MVPrimaryIndex mainIndex,
            IndexType indexType) {
        IndexColumn[] cols = IndexColumn.wrap(new Column[] { table.getColumn(mainIndex.getMainIndexColumn())});
        this.initBaseIndex(table, id, name, cols, indexType);
        this.mainIndex = mainIndex;
        if (id < 0) {
            throw DbException.throwInternalError("" + name);
        }
    }

    public void add(Session session, Row row) {
        // nothing to do
    }

    public boolean canGetFirstOrLast() {
        return true;
    }

    public void close(Session session) {
        // nothing to do
    }

    public Cursor find(Session session, SearchRow first, SearchRow last) {
        long min = mainIndex.getKey(first, Long.MIN_VALUE, Long.MIN_VALUE);
        // ifNull is MIN_VALUE as well, because the column is never NULL
        // so avoid returning all rows (returning one row is OK)
        long max = mainIndex.getKey(last, Long.MAX_VALUE, Long.MIN_VALUE);
        return mainIndex.find(session, min, max);
    }

    public Cursor findFirstOrLast(Session session, boolean first) {
        return mainIndex.findFirstOrLast(session, first);
    }

    public int getColumnIndex(Column col) {
        if (col.getColumnId() == mainIndex.getMainIndexColumn()) {
            return 0;
        }
        return -1;
    }

    public double getCost(Session session, int[] masks, SortOrder sortOrder) {
        return 10 * getCostRangeIndex(masks, mainIndex.getRowCount(session), sortOrder);
    }

    public boolean needRebuild() {
        return false;
    }

    public void remove(Session session, Row row) {
        // nothing to do
    }

    public void remove(Session session) {
        mainIndex.setMainIndexColumn(-1);
    }

    public void truncate(Session session) {
        // nothing to do
    }

    public void checkRename() {
        // ok
    }

    public long getRowCount(Session session) {
        return mainIndex.getRowCount(session);
    }

    public long getRowCountApproximation() {
        return mainIndex.getRowCountApproximation();
    }

    public long getDiskSpaceUsed() {
        return 0;
    }

}
