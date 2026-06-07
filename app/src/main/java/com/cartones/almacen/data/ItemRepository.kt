package com.cartones.almacen.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ItemRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    // ─── Colecciones ─────────────────────────────────────────────────────
    private val itemsCol    = db.collection("items")
    private val warehousesCol = db.collection("warehouses")
    private val classesCol  = db.collection("classes")
    private val categoriesCol = db.collection("categories")
    private val machinesCol = db.collection("machines")
    private val unitsCol    = db.collection("units")
    private val areasDeptsCol = db.collection("areas_depts")

    // ─── Items ────────────────────────────────────────────────────────────

    val allItems: Flow<List<ItemEntity>> = callbackFlow {
        val listener = itemsCol
            .orderBy("name")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(ItemEntity::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(item: ItemEntity): String {
        val ref = if (item.id.isBlank()) itemsCol.document() else itemsCol.document(item.id)
        ref.set(item.copy(id = ref.id, updatedAt = System.currentTimeMillis())).await()
        return ref.id
    }

    suspend fun update(item: ItemEntity) {
        itemsCol.document(item.id)
            .set(item.copy(updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun delete(item: ItemEntity) {
        itemsCol.document(item.id).delete().await()
    }

    suspend fun getById(id: String): ItemEntity? {
        val doc = itemsCol.document(id).get().await()
        return doc.toObject(ItemEntity::class.java)?.copy(id = doc.id)
    }

    fun getByIdFlow(id: String): Flow<ItemEntity?> = callbackFlow {
        val listener = itemsCol.document(id).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.toObject(ItemEntity::class.java)?.copy(id = snap.id))
        }
        awaitClose { listener.remove() }
    }

    private fun smartMatch(query: String, target: String?): Boolean {
        if (target == null) return false
        val cleanQuery = query.lowercase().trim()
        if (cleanQuery.isEmpty()) return false

        val delimiters = Regex("[\\s\\-_/]+")
        val queryWords = cleanQuery.split(delimiters).filter { it.isNotEmpty() }
        val targetWords = target.lowercase().trim().split(delimiters).filter { it.isNotEmpty() }

        if (queryWords.isEmpty()) return false

        return queryWords.all { qw ->
            targetWords.any { tw ->
                tw.startsWith(qw) && qw.length >= (tw.length / 2.0)
            } || target.lowercase().contains(qw)
        }
    }

    fun search(query: String): Flow<List<ItemEntity>> = callbackFlow {
        val listener = itemsCol
            .orderBy("name")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(ItemEntity::class.java)?.copy(id = doc.id)
                }?.filter { item ->
                    smartMatch(query, item.name) ||
                    smartMatch(query, item.code) ||
                    smartMatch(query, item.partNumber) ||
                    smartMatch(query, item.machine) ||
                    smartMatch(query, item.extraInfo) ||
                    smartMatch(query, item.location) ||
                    smartMatch(query, item.subCategory)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ─── Warehouses ───────────────────────────────────────────────────────

    val allWarehouses: Flow<List<WarehouseEntity>> = callbackFlow {
        val listener = warehousesCol.orderBy("code")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(WarehouseEntity::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertWarehouse(w: WarehouseEntity) {
        warehousesCol.document(w.code).set(w).await()
    }

    suspend fun deleteWarehouse(w: WarehouseEntity) {
        warehousesCol.document(w.code).delete().await()
    }

    // ─── Classes ──────────────────────────────────────────────────────────

    val allClasses: Flow<List<ClassEntity>> = callbackFlow {
        val listener = classesCol.orderBy("code")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(ClassEntity::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertClass(c: ClassEntity) {
        classesCol.document(c.code).set(c).await()
    }

    suspend fun deleteClass(c: ClassEntity) {
        classesCol.document(c.code).delete().await()
    }

    // ─── Categories ───────────────────────────────────────────────────────

    val allCategories: Flow<List<CategoryEntity>> = callbackFlow {
        val listener = categoriesCol.orderBy("code")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(CategoryEntity::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertCategory(cat: CategoryEntity) {
        val docId = "${cat.classCode}-${cat.code}"
        categoriesCol.document(docId).set(cat).await()
    }

    suspend fun deleteCategory(cat: CategoryEntity) {
        val docId = "${cat.classCode}-${cat.code}"
        categoriesCol.document(docId).delete().await()
    }

    // ─── Machines ─────────────────────────────────────────────────────────

    val allMachines: Flow<List<MachineEntity>> = callbackFlow {
        val listener = machinesCol.orderBy("code")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(MachineEntity::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertMachine(m: MachineEntity) {
        machinesCol.document(m.code).set(m).await()
    }

    suspend fun deleteMachine(m: MachineEntity) {
        machinesCol.document(m.code).delete().await()
    }

    // ─── Units ────────────────────────────────────────────────────────────

    val allUnits: Flow<List<UnitEntity>> = callbackFlow {
        val listener = unitsCol.orderBy("name")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(UnitEntity::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertUnit(u: UnitEntity) {
        unitsCol.document(u.name).set(u).await()
    }

    suspend fun deleteUnit(u: UnitEntity) {
        unitsCol.document(u.name).delete().await()
    }

    // ─── Areas/Depts ──────────────────────────────────────────────────────

    val allAreasDepts: Flow<List<AreaDeptEntity>> = callbackFlow {
        val listener = areasDeptsCol.orderBy("code")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(AreaDeptEntity::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertAreaDept(a: AreaDeptEntity) {
        areasDeptsCol.document(a.code).set(a).await()
    }

    suspend fun deleteAreaDept(a: AreaDeptEntity) {
        areasDeptsCol.document(a.code).delete().await()
    }
}
