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
  incompleteRecords   INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX batch_status_index ON batch(status);

CREATE TYPE record_status AS ENUM ('PENDING','ACTIVE', 'OK', 'FAILED', 'IGNORED');

CREATE TABLE record (
  id                  SERIAL PRIMARY KEY,
  status              record_status NOT NULL DEFAULT 'PENDING',
  timeOfCreation      TIMESTAMP DEFAULT now(),
  timeOfCompletion    TIMESTAMP,
  trackingId          TEXT NOT NULL,
  batch               INTEGER REFERENCES batch(id) ON DELETE CASCADE,
  isContinued         BOOLEAN DEFAULT FALSE,
  content             BYTEA NOT NULL,
  metadata            JSONB,
  diagnostics         JSONB
);
CREATE INDEX record_status_index ON record(status);
CREATE INDEX record_batch_index ON record(batch);
CREATE INDEX record_pending_index ON record(id) WHERE status = 'PENDING';

CREATE FUNCTION update_batch_incomplete() RETURNS trigger AS $$
DECLARE batch_id INTEGER;
BEGIN
  IF NEW.status = 'PENDING'::record_status OR NEW.status = 'ACTIVE'::record_status THEN
    -- Increment number of incomplete records for batch
    SELECT id INTO batch_id FROM batch WHERE id = NEW.batch FOR UPDATE;
    UPDATE batch SET incompleteRecords = incompleteRecords + 1 WHERE id = NEW.batch;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER record_insert_trigger
  AFTER INSERT ON record
  FOR EACH ROW
  EXECUTE PROCEDURE update_batch_incomplete();

CREATE FUNCTION mark_as_completed() RETURNS trigger AS $$
DECLARE batch_id INTEGER;
DECLARE batch_incomplete INTEGER;
BEGIN
  IF OLD.timeOfCompletion IS NOT NULL THEN
    RAISE EXCEPTION 'Cannot update an already completed record';
  END IF;

  IF (OLD.status = 'PENDING'::record_status OR OLD.status = 'ACTIVE'::record_status)
     AND NEW.status != 'PENDING'::record_status
     AND NEW.status != 'ACTIVE'::record_status
  THEN
    -- Mark record as completed, decrement number of incomplete records for batch
    NEW.timeOfCompletion = clock_timestamp();
    SELECT id INTO batch_id FROM batch WHERE id = NEW.batch FOR UPDATE;
    UPDATE batch SET incompleteRecords = batch.incompleteRecords - 1 WHERE id = NEW.batch
      RETURNING incompleteRecords INTO batch_incomplete;
    IF batch_incomplete <= 0 THEN
      UPDATE batch SET status = 'COMPLETED' WHERE id = NEW.batch;
    END IF;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER record_status_update_trigger
  BEFORE UPDATE ON record
  FOR EACH ROW
  EXECUTE PROCEDURE mark_as_completed();