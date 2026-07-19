import { onSchedule } from "firebase-functions/v2/scheduler";
import { logger } from "firebase-functions/v2";
import { Timestamp } from "firebase-admin/firestore";
import { db, messaging } from "./admin";

const QUIET_PERIOD_MS = 60_000;

interface DeviceToken {
  uid: string;
  token: string;
}

/**
 * Runs every minute: finds every family whose pending-changes queue (see queue.ts) has gone
 * quiet for at least QUIET_PERIOD_MS, sends one push per family summarising the burst, then
 * clears the queue. This is what turns "30 rapid parent-day assignments" into a single
 * notification instead of 30.
 */
export const sendPendingNotifications = onSchedule("every 1 minutes", async () => {
  const cutoff = Timestamp.fromMillis(Date.now() - QUIET_PERIOD_MS);
  const pending = await db
    .collectionGroup("notificationQueue")
    .where("lastChangeAt", "<=", cutoff)
    .get();

  if (pending.empty) return;

  for (const doc of pending.docs) {
    const familyId = doc.ref.parent.parent?.id;
    if (!familyId) continue;

    const changedByUid = doc.data().changedByUid as string;
    const changeCount = (doc.data().changeCount as number | undefined) ?? 1;

    try {
      await notifyFamily(familyId, changedByUid, changeCount);
    } catch (error) {
      logger.error(`Failed to notify family ${familyId}`, error);
    } finally {
      await doc.ref.delete();
    }
  }
});

export async function notifyFamily(familyId: string, changedByUid: string, changeCount: number): Promise<void> {
  const familyDoc = await db.collection("families").doc(familyId).get();
  if (!familyDoc.exists) return;

  const memberUids = (familyDoc.data()?.memberUids as string[] | undefined) ?? [];
  const recipientUids = memberUids.filter((uid) => uid !== changedByUid);
  if (recipientUids.length === 0) return;

  const [actorName, deviceTokens] = await Promise.all([
    resolveDisplayName(changedByUid),
    collectTokens(recipientUids),
  ]);
  if (deviceTokens.length === 0) return;

  const body =
    changeCount === 1
      ? `${actorName} made a change to your family calendar`
      : `${actorName} made ${changeCount} changes to your family calendar`;

  const response = await messaging.sendEachForMulticast({
    tokens: deviceTokens.map((t) => t.token),
    notification: { title: "Family calendar updated", body },
  });

  await cleanUpInvalidTokens(deviceTokens, response.responses);
}

async function resolveDisplayName(uid: string): Promise<string> {
  const userDoc = await db.collection("users").doc(uid).get();
  const data = userDoc.data();
  return (data?.displayName as string | undefined) || (data?.email as string | undefined) || "Someone";
}

async function collectTokens(uids: string[]): Promise<DeviceToken[]> {
  const tokenLists = await Promise.all(
    uids.map(async (uid) => {
      const snapshot = await db.collection("users").doc(uid).collection("fcmTokens").get();
      return snapshot.docs.map((tokenDoc) => ({ uid, token: tokenDoc.id }));
    })
  );
  return tokenLists.flat();
}

/** FCM reports dead tokens per-send; delete just those so the next send doesn't keep retrying them. */
async function cleanUpInvalidTokens(
  deviceTokens: DeviceToken[],
  responses: { success: boolean; error?: { code?: string } }[]
): Promise<void> {
  const deletions = responses.flatMap((result, index) => {
    if (result.success) return [];
    const code = result.error?.code;
    const isInvalid =
      code === "messaging/invalid-registration-token" || code === "messaging/registration-token-not-registered";
    if (!isInvalid) return [];

    const { uid, token } = deviceTokens[index];
    return [db.collection("users").doc(uid).collection("fcmTokens").doc(token).delete()];
  });

  await Promise.all(deletions);
}
