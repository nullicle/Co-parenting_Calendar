import { FieldValue } from "firebase-admin/firestore";
import { db } from "./admin";

/**
 * Upserts families/{familyId}/notificationQueue/pending - the single rolling summary a burst of
 * rapid writes (e.g. assigning 30 parenting days in a row) collapses into, rather than sending
 * one notification per write. The scheduled function in notify.ts sends one push once this has
 * gone quiet for a while, then deletes it.
 */
export async function queueChange(familyId: string, actorUid: string): Promise<void> {
  const ref = db
    .collection("families")
    .doc(familyId)
    .collection("notificationQueue")
    .doc("pending");

  await db.runTransaction(async (tx) => {
    const snapshot = await tx.get(ref);
    const changeCount = (snapshot.exists ? (snapshot.data()?.changeCount as number | undefined) ?? 0 : 0) + 1;
    tx.set(ref, {
      changedByUid: actorUid,
      changeCount,
      lastChangeAt: FieldValue.serverTimestamp(),
    });
  });
}
