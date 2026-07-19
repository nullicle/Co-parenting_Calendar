import { onDocumentWritten, FirestoreEvent, Change, DocumentSnapshot } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { queueChange } from "./queue";

/**
 * Fires on every create/update/delete under the four family-shared subcollections the app asks
 * to notify on. The acting user isn't otherwise available to a Firestore trigger, so it's read
 * back from `lastModifiedByUid`, a field the Android client stamps onto every write (see
 * core/firebase/LastModifiedBy.kt). Writes with no actor (e.g. ParentRepository's automatic
 * default-parent seeding) are skipped - there's no one to attribute the change to.
 */
async function handleFamilyDocumentWrite(
  event: FirestoreEvent<Change<DocumentSnapshot> | undefined, { familyId: string }>
): Promise<void> {
  const familyId = event.params.familyId;
  const after = event.data?.after;
  const before = event.data?.before;
  const actorUid = (after?.exists ? after.data()?.lastModifiedByUid : before?.data()?.lastModifiedByUid) as
    | string
    | undefined;

  if (!actorUid) {
    logger.debug(`Skipping notification queue - no actor uid for family ${familyId}`);
    return;
  }

  await queueChange(familyId, actorUid);
}

export const onActivityWritten = onDocumentWritten(
  "families/{familyId}/activities/{activityId}",
  handleFamilyDocumentWrite
);

export const onChildWritten = onDocumentWritten(
  "families/{familyId}/children/{childId}",
  handleFamilyDocumentWrite
);

export const onParentWritten = onDocumentWritten(
  "families/{familyId}/parents/{slot}",
  handleFamilyDocumentWrite
);

export const onParentAssignmentWritten = onDocumentWritten(
  "families/{familyId}/parentAssignments/{date}",
  handleFamilyDocumentWrite
);
