UPDATE coach_connection_requests request
SET status = 'ACCEPTED',
    responded_at = CURRENT_TIMESTAMP,
    member_read_at = NULL
WHERE request.status = 'PENDING'
  AND EXISTS (
      SELECT 1
      FROM coach_member_assignments assignment
      WHERE assignment.coach_id = request.coach_id
        AND assignment.member_id = request.member_id
        AND assignment.status = 'ACTIVE'
        AND assignment.starts_on <= CURRENT_DATE
        AND (assignment.ends_on IS NULL OR assignment.ends_on >= CURRENT_DATE)
  );

INSERT INTO coach_connection_requests
    (member_id, coach_id, message, status, responded_at)
SELECT assignment.member_id, assignment.coach_id,
       'Assigned by gym administration', 'ACCEPTED', CURRENT_TIMESTAMP
FROM coach_member_assignments assignment
WHERE assignment.status = 'ACTIVE'
  AND assignment.starts_on <= CURRENT_DATE
  AND (assignment.ends_on IS NULL OR assignment.ends_on >= CURRENT_DATE)
  AND NOT EXISTS (
      SELECT 1
      FROM coach_connection_requests request
      WHERE request.member_id = assignment.member_id
        AND request.coach_id = assignment.coach_id
        AND request.status = 'ACCEPTED'
  );
