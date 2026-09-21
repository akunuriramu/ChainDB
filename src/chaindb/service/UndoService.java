package chaindb.service;

import chaindb.blockchain.Block;
import chaindb.engine.DatabaseEngine;
import chaindb.event.CreateTableEvent;
import chaindb.event.DatabaseEvent;
import chaindb.event.DeleteEvent;
import chaindb.event.InsertEvent;
import chaindb.event.UpdateEvent;
import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.TableNotFoundException;

import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Bonus feature: implements UNDO using compensating events rather than
 * rewriting blockchain history. Undoing an operation appends a *new*
 * block whose event reverses the effect of the last mutating event,
 * preserving the immutability of the chain.
 */
public class UndoService {

    private final DatabaseEngine engine;

    public UndoService(DatabaseEngine engine) {
        this.engine = engine;
    }

    public String undoLast() {
        Block lastEventBlock = findLastEventBlock();
        if (lastEventBlock == null) {
            return "Nothing to undo.";
        }

        DatabaseEvent last = lastEventBlock.getEvent();
        try {
            if (last instanceof InsertEvent) {
                return undoInsert((InsertEvent) last);
            } else if (last instanceof DeleteEvent) {
                return undoDelete((DeleteEvent) last);
            } else if (last instanceof UpdateEvent) {
                return undoUpdate((UpdateEvent) last);
            } else if (last instanceof CreateTableEvent) {
                return "Cannot undo CREATE_TABLE (dropping tables is not supported).";
            }
            return "Unknown event type; cannot undo.";
        } catch (TableNotFoundException | ColumnNotFoundException e) {
            return "Undo failed: " + e.getMessage();
        }
    }

    private Block findLastEventBlock() {
        var chain = engine.getBlockchain().getChain();
        for (int i = chain.size() - 1; i >= 0; i--) {
            if (chain.get(i).getEvent() != null) {
                return chain.get(i);
            }
        }
        return null;
    }

    private String undoInsert(InsertEvent event) throws TableNotFoundException, ColumnNotFoundException {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>(event.getValues());
        String firstColumn = values.keySet().iterator().next();
        Object firstValue = values.get(firstColumn);
        DeleteEvent compensating = new DeleteEvent(event.getTableName(), firstColumn, firstValue,
                Collections.singletonList(values));
        engine.commitEvent(compensating);
        return "Undo applied: compensating DELETE for last INSERT on '" + event.getTableName() + "'.";
    }

    private String undoDelete(DeleteEvent event) throws TableNotFoundException, ColumnNotFoundException {
        if (event.getDeletedRows().isEmpty()) {
            return "Cannot undo: original DELETE removed no rows.";
        }
        for (LinkedHashMap<String, Object> row : event.getDeletedRows()) {
            InsertEvent compensating = new InsertEvent(event.getTableName(), new LinkedHashMap<>(row));
            engine.commitEvent(compensating);
        }
        return "Undo applied: compensating INSERT(s) for last DELETE on '" + event.getTableName() + "'.";
    }

    private String undoUpdate(UpdateEvent event) throws TableNotFoundException, ColumnNotFoundException {
        if (event.getPreviousRows().isEmpty()) {
            return "Cannot undo: original UPDATE affected no rows.";
        }
        String whereColumn = event.getWhereColumn();
        for (LinkedHashMap<String, Object> oldRow : event.getPreviousRows()) {
            Object keyValue = oldRow.get(whereColumn);
            LinkedHashMap<String, Object> restoreValues = new LinkedHashMap<>(oldRow);
            UpdateEvent compensating = new UpdateEvent(event.getTableName(), whereColumn, keyValue,
                    restoreValues, Collections.emptyList());
            engine.commitEvent(compensating);
        }
        return "Undo applied: compensating UPDATE(s) restoring previous values on '" + event.getTableName() + "'.";
    }
}
