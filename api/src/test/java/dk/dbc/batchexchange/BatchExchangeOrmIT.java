/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.batchexchange;

import dk.dbc.batchexchange.dto.Batch;
import dk.dbc.batchexchange.dto.BatchEntry;
import dk.dbc.batchexchange.dto.Diagnostic;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dk.dbc.commons.testutil.Assert.assertThat;
import static dk.dbc.commons.testutil.Assert.isThrowing;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class BatchExchangeOrmIT extends IntegrationTest {
    private static Map<String, String> entityManagerProperties = new HashMap<>();
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerProperties.put(JDBC_USER, datasource.getUser());
        entityManagerProperties.put(JDBC_PASSWORD, datasource.getPassword());
        entityManagerProperties.put(JDBC_URL, datasource.getUrl());
        entityManagerProperties.put(JDBC_DRIVER, "org.postgresql.Driver");
        entityManagerProperties.put("eclipselink.logging.level", "FINE");
        entityManagerFactory = Persistence.createEntityManagerFactory("BatchExchangeIT_PU", entityManagerProperties);
    }

    @Before
    public void populateDatabase() throws URISyntaxException {
        final URL resource = BatchExchangeOrmIT.class.getResource("/populate.sql");
        executeScript(new File(resource.toURI()));
    }

    @Before
    public void createEntityManager() {
        entityManager = entityManagerFactory.createEntityManager(entityManagerProperties);
    }

    @After
    public void clearEntityManagerCache() {
        entityManager.clear();
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    /**
     * When: a batch is persisted
     * Then: timeOfCreation is defaulted
     *  And: status is set to PENDING
     */
    @Test
    public void batch_defaults() {
        final Batch batch = entityManager.find(Batch.class, 1);
        assertThat("timeOfCreation", batch.getTimeOfCreation(), is(notNullValue()));
        assertThat("status", batch.getStatus(), is(Batch.Status.PENDING));
    }

    /**
     * When: a batch is persisted
     * Then: status is set
     *  And: name is set
     */
    @Test
    public void batch_creation() {
        final Batch batch = new Batch()
                .withName("new_batch");

        transaction_scoped(() -> entityManager.persist(batch));
        entityManager.refresh(batch);

        assertThat("status", batch.getStatus(), is(Batch.Status.PENDING));
        assertThat("name", batch.getName(), is("new_batch"));
        assertThat("incompleteEntries", batch.getIncompleteEntries(), is(0));
    }

    /**
     * When: a batch entry is persisted
     * Then: timeOfCreation is defaulted
     *  And: timeOfCompletion is not set
     *  And: status is set to PENDING
     *  And: isContinued is set to false
     *  And: no diagnostics are set
     */
    @Test
    public void batchEntry_defaults() {
        final BatchEntry batchEntry = entityManager.find(BatchEntry.class, 1);
        assertThat("timeOfCreation", batchEntry.getTimeOfCreation(), is(notNullValue()));
        assertThat("timeOfCompletion", batchEntry.getTimeOfCompletion(), is(nullValue()));
        assertThat("status", batchEntry.getStatus(), is(BatchEntry.Status.PENDING));
        assertThat("isContinued", batchEntry.getContinued(), is(false));
        assertThat("diagnostics", batchEntry.getDiagnostics().isEmpty(), is(true));
    }

    /**
     * When: a batch entry is persisted
     * Then: batch is set
     *  And: status is set
     *  And: metadata is set
     *  And: content is set
     *  And: trackingId is set
     *  And: incompleteEntries is incremented for the batch
     */
    @Test
    public void batchEntry_creation() {
        final String metadata = "{\"id\": \"1_11\"}";
        final String content = "data1_11";
        final String trackingId = "batch1_11";

        final BatchEntry batchEntry = new BatchEntry()
                .withBatch(1)
                .withMetadata(metadata)
                .withContent(content.getBytes(StandardCharsets.UTF_8))
                .withTrackingId(trackingId);

        transaction_scoped(() -> entityManager.persist(batchEntry));
        entityManager.refresh(batchEntry);

        assertThat("status", batchEntry.getStatus(), is(BatchEntry.Status.PENDING));
        assertThat("content", batchEntry.getContent(), is(content.getBytes(StandardCharsets.UTF_8)));
        assertThat("metadata", batchEntry.getMetadata(), is(metadata));
        assertThat("trackingId", batchEntry.getTrackingId(), is(trackingId));

        final Batch batch = entityManager.find(Batch.class, 1);
        assertThat("incompleteEntries", batch.getIncompleteEntries(), is(11));

        transaction_scoped(() -> batchEntry.withStatus(BatchEntry.Status.ACTIVE));
        entityManager.refresh(batch);

        assertThat("incompleteEntries after status update", batch.getIncompleteEntries(), is(11));
    }

    /**
     * When: a batch entry is persisted with a status not in {PENDING, ACTIVE}
     * Then: incompleteEntries is not incremented for the batch
     */
    @Test
    public void batchEntry_createdAsComplete() {
        final BatchEntry ignored = new BatchEntry()
                .withBatch(1)
                .withStatus(BatchEntry.Status.IGNORED)
                .withMetadata("")
                .withContent("ignored".getBytes(StandardCharsets.UTF_8))
                .withTrackingId("ignored");
        final BatchEntry ok = new BatchEntry()
                .withBatch(1)
                .withStatus(BatchEntry.Status.OK)
                .withContent("ok".getBytes(StandardCharsets.UTF_8))
                .withTrackingId("ok");
        final BatchEntry failed = new BatchEntry()
                .withBatch(1)
                .withStatus(BatchEntry.Status.FAILED)
                .withContent("failed".getBytes(StandardCharsets.UTF_8))
                .withTrackingId("failed");

        transaction_scoped(() -> {
                entityManager.persist(ignored);
                entityManager.persist(ok);
                entityManager.persist(failed);
        });

        final Batch batch = entityManager.find(Batch.class, 1);
        assertThat("incompleteEntries", batch.getIncompleteEntries(), is(10));
    }

    /**
     * When: get_batch_entries function is called with a batch ID
     * Then: all batch entries belonging to that batch are returned ordered by entry ID
     */
    @Test
    public void gettingBatchEntries() {
        @SuppressWarnings("unchecked")
        final List<BatchEntry> entries = (List<BatchEntry>) entityManager
                .createNamedQuery(BatchEntry.GET_BATCH_ENTRIES_QUERY_NAME)
                .setParameter(1, 2)
                .getResultList();

        assertThat("number of batch entries", entries.size(), is(10));
        int id = 11;
        for (BatchEntry entry : entries) {
            assertThat("batch entry ID", entry.getId(), is(id++));
        }
    }

    /**
     * When: claiming pending batch entries
     * Then: the claimed batch entries have their status set to ACTIVE
     *  And: the claimed batch entries are returned ordered by ID
     * When: resetting claimed batch entries
     * Then: then claimed batch entries have their status set back to PENDING
     */
    @Test
    @SuppressWarnings("unchecked")
    public void claimingBatchEntries() {
        final List<BatchEntry> entries = transaction_scoped(() -> (List<BatchEntry>) entityManager
            .createNamedQuery(BatchEntry.CLAIM_PENDING_ENTRIES_QUERY_NAME)
            .setParameter(1, 10)
            .getResultList());

        assertThat("number of claimed batch entries", entries.size(), is(10));
        int id = 1;
        for (BatchEntry entry : entries) {
            assertThat("batch entry ID", entry.getId(), is(id++));
            assertThat("batch entry status", entry.getStatus(), is(BatchEntry.Status.ACTIVE));
        }
        assertThat("number of batch entries marked as ACTIVE", getNumberOfActiveBatchEntries(), is(10));

        final Integer numberOfReset = transaction_scoped(() -> (Integer) entityManager
                .createNamedQuery(BatchEntry.RESET_CLAIMED_ENTRIES_QUERY_NAME)
                .getSingleResult());

        assertThat("number of reset batch entries", numberOfReset, is(10));
        assertThat("number of batch entries marked as ACTIVE after reset", getNumberOfActiveBatchEntries(), is(0));
        assertThat("number of batch entries marked as PENDING after reset", getNumberOfPendingBatchEntries(), is(30));
    }

    /**
     * When: claiming pending batch entries and none exist
     * Then: no batch entries are claimed
     */
    @Test
    @SuppressWarnings("unchecked")
    public void claimingPendingBatchEntries_whenNoneExist() {
        // First claim every batch entry
        transaction_scoped(() -> entityManager
            .createNamedQuery(BatchEntry.CLAIM_PENDING_ENTRIES_QUERY_NAME)
            .setParameter(1, 1000)
            .getResultList());

        // Then try to claim some more
        final List<BatchEntry> entries = transaction_scoped(() -> (List<BatchEntry>) entityManager
            .createNamedQuery(BatchEntry.CLAIM_PENDING_ENTRIES_QUERY_NAME)
            .setParameter(1, 10)
            .getResultList());

        assertThat("number of claimed batch entries", entries.size(), is(0));

        transaction_scoped(() -> (Integer) entityManager
                .createNamedQuery(BatchEntry.RESET_CLAIMED_ENTRIES_QUERY_NAME)
                .getSingleResult());
    }


    /**
     * When: updating all existing batch entries for a batch to status OK or FAILED
     * Then: the batch is also updated to completed
     */
    @Test
    public void batchEntryStatusUpdatesCompleteBatch() {
        @SuppressWarnings("unchecked")
        final List<BatchEntry> entries = (List<BatchEntry>) entityManager
                .createNamedQuery(BatchEntry.GET_BATCH_ENTRIES_QUERY_NAME)
                .setParameter(1, 1)
                .getResultList();

        transaction_scoped(() -> {
            for (BatchEntry entry : entries) {
                final BatchEntry managedEntry = entityManager.merge(entry);
                if (managedEntry.getId() % 2 == 0) {
                    managedEntry.withStatus(BatchEntry.Status.OK);
                } else {
                    managedEntry.withStatus(BatchEntry.Status.FAILED)
                                .withDiagnostics(Collections.singletonList(Diagnostic.createError("I failed")));
                }
            }
        });

        // Force BatchEntryDiagnosticsConverter call with diagnostic
        final BatchEntry entryWithDiagnostics = entityManager.find(BatchEntry.class, 1);
        entityManager.refresh(entryWithDiagnostics);

        final Batch batch = (Batch) entityManager
                .createNamedQuery(Batch.GET_COMPLETED_BATCH_QUERY_NAME)
                .getSingleResult();
        assertThat("batch status", batch.getStatus(), is(Batch.Status.COMPLETED));
        assertThat("batch incompleteEntries", batch.getIncompleteEntries(), is(0));
    }

    @Test
    public void deleteOfBatchCascadesToEntries() {
        final Batch batch = entityManager.find(Batch.class, 1);
        transaction_scoped(() -> entityManager.remove(entityManager.merge(batch)));
        final long numberOfBatchEntriesRemaining = (long) entityManager
                .createNativeQuery("SELECT count(id) FROM entry WHERE batch = ?")
                .setParameter(1, batch.getId())
                .getSingleResult();
        assertThat(numberOfBatchEntriesRemaining, is(0L));
    }

    /**
     * When: a batch entry with a status not in {PENDING, ACTIVE} is updated
     * Then: an exception is thrown since further updates are forbidden
     */
    @Test
    public void batchEntryCanNotBeUpdatedWhenInCompletedStatus() {
        final BatchEntry batchEntry = entityManager.find(BatchEntry.class, 1);
        transaction_scoped(() -> batchEntry.withStatus(BatchEntry.Status.OK));
        assertThat(() -> transaction_scoped(() -> batchEntry.withStatus(BatchEntry.Status.ACTIVE)),
                isThrowing(RollbackException.class));
    }

    private <T> T transaction_scoped(CodeBlockExecution<T> codeBlock) {
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            return codeBlock.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            transaction.commit();
        }
    }

    private void transaction_scoped(CodeBlockVoidExecution codeBlock) {
        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            codeBlock.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            transaction.commit();
        }
    }

    /**
     * Represents a code block execution with return value
     * @param <T> return type of the code block execution
     */
    @FunctionalInterface
    interface CodeBlockExecution<T> {
        T execute() throws Exception;
    }

    /**
     * Represents a code block execution without return value
     */
    @FunctionalInterface
    interface CodeBlockVoidExecution {
        void execute() throws Exception;
    }
}
