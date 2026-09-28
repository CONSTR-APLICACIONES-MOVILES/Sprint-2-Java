package com.example.parchapp.data;

import android.util.Log;

import com.example.parchapp.domain.model.Group;
import com.example.parchapp.domain.model.Member;
import com.example.parchapp.domain.model.TimeBlock;
import com.example.parchapp.util.Callback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/* Groups stored in the {@code groups} collection. Reads try the local cache first and then the server; live updates come from one snapshot listener per observed group.
 */
class FirestoreGroupRepository extends BaseGroupRepository {
    private static final String TAG = "FirestoreGroupRepo";
    private static final String COLLECTION = "groups";

    private final CollectionReference collection;
    private final Map<String, ListenerRegistration> registrations = new HashMap<>();

    FirestoreGroupRepository(FirebaseFirestore db) {
        collection = db.collection(COLLECTION);
    }

    @Override
    public void getGroup(String groupId, Callback<Group> callback) {
        DocumentReference ref = collection.document(groupId);
        ref.get(Source.CACHE).addOnCompleteListener(cacheTask -> {
            if (cacheTask.isSuccessful() && cacheTask.getResult().exists()) {
                callback.onSuccess(fromSnapshot(cacheTask.getResult()));
                return;
            }
            ref.get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            callback.onSuccess(fromSnapshot(snapshot));
                            return;
                        }
                        Group seeded = snapshot.getMetadata().isFromCache() ? null : seed(groupId);
                        if (seeded != null) {
                            callback.onSuccess(seeded);
                        } else {
                            callback.onError(new NoSuchElementException("Group " + groupId + " not found"));
                        }
                    })
                    .addOnFailureListener(callback::onError);
        });
    }

    @Override
    protected void startObserving(String groupId) {
        ListenerRegistration registration = collection.document(groupId).addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.w(TAG, "Listening to group " + groupId + " failed", error);
                return;
            }
            if (snapshot == null) {
                return;
            }
            if (snapshot.exists()) {
                notifyListeners(fromSnapshot(snapshot));
            } else if (!snapshot.getMetadata().isFromCache()) {
                // Only seed when the server confirms the group is missing, never from an offline
                // cache miss. Writing the seed triggers this listener again with the new document.
                seed(groupId);
            }
        });
        registrations.put(groupId, registration);
    }

    @Override
    protected void stopObserving(String groupId) {
        ListenerRegistration registration = registrations.remove(groupId);
        if (registration != null) {
            registration.remove();
        }
    }

    @Override
    public void setRsvp(String groupId, String userId, boolean going) {
        collection.document(groupId)
                .update("goingIds", going ? FieldValue.arrayUnion(userId) : FieldValue.arrayRemove(userId),
                        "maybeIds", going ? FieldValue.arrayRemove(userId) : FieldValue.arrayUnion(userId))
                .addOnFailureListener(e -> Log.w(TAG, "RSVP update failed", e));
    }

    @Override
    public void addInvite(String groupId, String email) {
        collection.document(groupId)
                .update("pendingInvites", FieldValue.arrayUnion(email))
                .addOnFailureListener(e -> Log.w(TAG, "Adding invite failed", e));
    }

    @Override
    public void removeInvite(String groupId, String email) {
        collection.document(groupId)
                .update("pendingInvites", FieldValue.arrayRemove(email))
                .addOnFailureListener(e -> Log.w(TAG, "Removing invite failed", e));
    }

    private Group seed(String groupId) {
        Group demo = DemoData.groups().get(groupId);
        if (demo != null) {
            collection.document(groupId).set(toMap(demo))
                    .addOnFailureListener(e -> Log.w(TAG, "Seeding group " + groupId + " failed", e));
        }
        return demo;
    }

    static Map<String, Object> toMap(Group group) {
        List<Map<String, Object>> members = new ArrayList<>();
        for (Member member : group.getMembers()) {
            List<Map<String, Object>> busy = new ArrayList<>();
            for (TimeBlock block : member.getBusyBlocks()) {
                Map<String, Object> blockMap = new HashMap<>();
                blockMap.put("start", block.getStartMinute());
                blockMap.put("end", block.getEndMinute());
                busy.add(blockMap);
            }
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("id", member.getId());
            memberMap.put("name", member.getName());
            memberMap.put("initials", member.getInitials());
            memberMap.put("preferredStart", member.getPreferredStartMinute());
            memberMap.put("preferredEnd", member.getPreferredEndMinute());
            memberMap.put("busy", busy);
            members.add(memberMap);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", group.getName());
        map.put("icon", group.getIcon());
        map.put("planTitle", group.getPlanTitle());
        map.put("members", members);
        map.put("goingIds", group.getGoingIds());
        map.put("maybeIds", group.getMaybeIds());
        map.put("pendingInvites", group.getPendingInvites());
        return map;
    }

    @SuppressWarnings("unchecked")
    static Group fromSnapshot(DocumentSnapshot snapshot) {
        List<Member> members = new ArrayList<>();
        List<Map<String, Object>> rawMembers = (List<Map<String, Object>>) snapshot.get("members");
        if (rawMembers != null) {
            for (Map<String, Object> raw : rawMembers) {
                List<TimeBlock> busy = new ArrayList<>();
                List<Map<String, Object>> rawBusy = (List<Map<String, Object>>) raw.get("busy");
                if (rawBusy != null) {
                    for (Map<String, Object> block : rawBusy) {
                        busy.add(new TimeBlock(asInt(block.get("start")), asInt(block.get("end"))));
                    }
                }
                members.add(new Member(asString(raw.get("id")), asString(raw.get("name")),
                        asString(raw.get("initials")), busy,
                        asInt(raw.get("preferredStart")), asInt(raw.get("preferredEnd"))));
            }
        }
        return new Group(snapshot.getId(), asString(snapshot.get("name")), asString(snapshot.get("icon")),
                asString(snapshot.get("planTitle")), members, stringList(snapshot.get("goingIds")),
                stringList(snapshot.get("maybeIds")), stringList(snapshot.get("pendingInvites")));
    }

    private static int asInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                result.add(asString(item));
            }
        }
        return result;
    }
}
