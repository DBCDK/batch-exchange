/*
Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
See license text in LICENSE.txt
*/

ALTER TABLE entry ADD COLUMN priority INTEGER NOT NULL DEFAULT 4;
CREATE INDEX entry_priority_index ON entry(priority);

CREATE OR REPLACE FUNCTION claim_pending_entries(maxNumberOfEntries INTEGER) RETURNS SETOF entry AS $$
  UPDATE entry SET status = 'ACTIVE'
    WHERE id IN (
      SELECT id FROM entry WHERE status = 'PENDING' ORDER BY priority DESC, id ASC LIMIT maxNumberOfEntries FOR UPDATE SKIP LOCKED)
    RETURNING *;
$$ LANGUAGE sql;
