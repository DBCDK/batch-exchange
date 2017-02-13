/*
Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
See license text in LICENSE.txt
*/

CREATE TYPE batch_status AS ENUM ('PENDING','COMPLETED');

CREATE TABLE batch (
  id                  SERIAL PRIMARY KEY,
  name                TEXT,
  status              batch_status NOT NULL DEFAULT 'PENDING',
  timeOfCreation      TIMESTAMP DEFAULT now(),
  incompleteEntries   INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX batch_status_index ON batch(status);
CREATE INDEX batch_completed_index ON batch(id) WHERE status = 'COMPLETED';

CREATE TYPE entry_status AS ENUM ('PENDING','ACTIVE', 'OK', 'FAILED', 'IGNORED');

CREATE TABLE entry (
  id                  SERIAL PRIMARY KEY,
  status              entry_status NOT NULL DEFAULT 'PENDING',
  timeOfCreation      TIMESTAMP DEFAULT now(),
  timeOfCompletion    TIMESTAMP,
  trackingId          TEXT NOT NULL,
  batch               INTEGER REFERENCES batch(id) ON DELETE CASCADE,
  isContinued         BOOLEAN DEFAULT FALSE,
  content             BYTEA NOT NULL,
  metadata            JSONB,
  diagnostics         JSONB
);
CREATE INDEX entry_status_index ON entry(status);
CREATE INDEX entry_batch_index ON entry(batch);
CREATE INDEX entry_pending_index ON entry(id) WHERE status = 'PENDING';

CREATE FUNCTION update_batch_incomplete() RETURNS trigger AS $$
DECLARE batch_id INTEGER;
BEGIN
  IF NEW.status = 'PENDING'::entry_status OR NEW.status = 'ACTIVE'::entry_status THEN
    -- Increment number of incomplete entries for batch
    SELECT id INTO batch_id FROM batch WHERE id = NEW.batch FOR UPDATE;
    UPDATE batch SET incompleteEntries = incompleteEntries + 1 WHERE id = NEW.batch;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER entry_insert_trigger
  AFTER INSERT ON entry
  FOR EACH ROW
  EXECUTE PROCEDURE update_batch_incomplete();

CREATE FUNCTION mark_as_completed() RETURNS trigger AS $$
DECLARE batch_id INTEGER;
DECLARE batch_incomplete INTEGER;
BEGIN
  IF OLD.timeOfCompletion IS NOT NULL THEN
    RAISE EXCEPTION 'Cannot update an already completed entry';
  END IF;

  IF (OLD.status = 'PENDING'::entry_status OR OLD.status = 'ACTIVE'::entry_status)
     AND NEW.status != 'PENDING'::entry_status
     AND NEW.status != 'ACTIVE'::entry_status
  THEN
    -- Mark entry as completed, decrement number of incomplete entries for batch
    NEW.timeOfCompletion = clock_timestamp();
    SELECT id INTO batch_id FROM batch WHERE id = NEW.batch FOR UPDATE;
    UPDATE batch SET incompleteEntries = batch.incompleteEntries - 1 WHERE id = NEW.batch
      RETURNING incompleteEntries INTO batch_incomplete;
    IF batch_incomplete <= 0 THEN
      UPDATE batch SET status = 'COMPLETED' WHERE id = NEW.batch;
    END IF;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER entry_status_update_trigger
  BEFORE UPDATE ON entry
  FOR EACH ROW
  EXECUTE PROCEDURE mark_as_completed();

CREATE FUNCTION get_completed_batch() RETURNS SETOF batch AS $$
  SELECT * FROM batch WHERE status = 'COMPLETED' ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED;
$$ LANGUAGE sql;

CREATE FUNCTION get_batch_entries(batchId INTEGER) RETURNS SETOF entry AS $$
  SELECT * FROM entry WHERE batch = batchId ORDER BY id;
$$ LANGUAGE sql;

CREATE FUNCTION claim_pending_entries(maxNumberOfEntries INTEGER) RETURNS SETOF entry AS $$
  UPDATE entry SET status = 'ACTIVE'
    WHERE id IN (
      SELECT id FROM entry WHERE status = 'PENDING' ORDER BY id LIMIT maxNumberOfEntries FOR UPDATE SKIP LOCKED)
    RETURNING *;
$$ LANGUAGE sql;

CREATE FUNCTION reset_claimed_entries() RETURNS INTEGER AS $$
DECLARE rows_affected INTEGER;
BEGIN
  WITH updated_rows AS (
    UPDATE entry SET status = 'PENDING' WHERE status = 'ACTIVE' RETURNING id
  )
  SELECT count(id) from updated_rows INTO rows_affected;
  RETURN rows_affected;
END;
$$ LANGUAGE plpgsql;