package com.cartones.almacen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cartones.almacen.data.CategoryEntity
import com.cartones.almacen.data.ClassEntity
import com.cartones.almacen.data.ItemEntity
import com.cartones.almacen.data.ItemRepository
import com.cartones.almacen.data.MachineEntity
import com.cartones.almacen.data.UnitEntity
import com.cartones.almacen.data.WarehouseEntity
import com.cartones.almacen.data.AreaDeptEntity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ─── Mapping maps ─────────────────────────────────────────────────────

    val warehousesMap: StateFlow<Map<String, String>> = repository.allWarehouses
        .map { list -> list.associate { it.code to it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val classesMap: StateFlow<Map<String, ClassEntity>> = repository.allClasses
        .map { list -> list.associateBy { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val categoriesMap: StateFlow<Map<String, CategoryEntity>> = repository.allCategories
        .map { list -> list.associateBy { "${it.classCode}-${it.code}" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val machinesMap: StateFlow<Map<String, String>> = repository.allMachines
        .map { list -> list.associate { it.code to it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // ─── Full lists ───────────────────────────────────────────────────────

    val allWarehouses = repository.allWarehouses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMachines: StateFlow<List<MachineEntity>> = repository.allMachines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUnits: StateFlow<List<UnitEntity>> = repository.allUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAreasDepts: StateFlow<List<AreaDeptEntity>> = repository.allAreasDepts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Mapas Útiles para UI ────────────────────────────────────────────────────────────

    val totalItems: StateFlow<Int> = repository.allItems
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWarehouses: StateFlow<Int> = repository.allWarehouses
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalClasses: StateFlow<Int> = repository.allClasses
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCategories: StateFlow<Int> = repository.allCategories
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMachines: StateFlow<Int> = repository.allMachines
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ─── Smart search (debounced, min 3 chars) ───────────────────────────

    @OptIn(FlowPreview::class)
    val items: StateFlow<List<ItemEntity>> = _searchQuery
        .debounce(300)
        .map { it.trim() }
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) { _searchQuery.value = query }

    // ─── Item CRUD ────────────────────────────────────────────────────────

    fun insert(item: ItemEntity) {
        viewModelScope.launch { repository.insert(item) }
    }

    fun update(item: ItemEntity) {
        viewModelScope.launch { repository.update(item) }
    }

    fun delete(item: ItemEntity) {
        viewModelScope.launch { repository.delete(item) }
    }

    suspend fun getById(id: String): ItemEntity? = repository.getById(id)

    fun getByIdFlow(id: String): Flow<ItemEntity?> = repository.getByIdFlow(id)

    // ─── Catalog CRUD ─────────────────────────────────────────────────────

    fun insertWarehouse(code: String, name: String) {
        viewModelScope.launch { repository.insertWarehouse(WarehouseEntity(code, name)) }
    }
    fun deleteWarehouse(warehouse: WarehouseEntity) {
        viewModelScope.launch { repository.deleteWarehouse(warehouse) }
    }
    fun insertClass(code: String, name: String, warehouseCode: String) {
        viewModelScope.launch { repository.insertClass(ClassEntity(code, warehouseCode, name)) }
    }
    fun deleteClass(cls: ClassEntity) {
        viewModelScope.launch { repository.deleteClass(cls) }
    }
    fun insertCategory(code: String, name: String, classCode: String) {
        viewModelScope.launch { repository.insertCategory(CategoryEntity(code, classCode, name)) }
    }
    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }
    fun insertMachine(code: String, name: String) {
        viewModelScope.launch { repository.insertMachine(MachineEntity(code, name)) }
    }
    fun deleteMachine(machine: MachineEntity) {
        viewModelScope.launch { repository.deleteMachine(machine) }
    }
    fun insertUnit(name: String) {
        viewModelScope.launch { repository.insertUnit(UnitEntity(name)) }
    }
    fun deleteUnit(unit: UnitEntity) {
        viewModelScope.launch { repository.deleteUnit(unit) }
    }

    fun insertAreaDept(code: String, name: String) {
        viewModelScope.launch { repository.insertAreaDept(AreaDeptEntity(code, name)) }
    }
    fun deleteAreaDept(areaDept: AreaDeptEntity) {
        viewModelScope.launch { repository.deleteAreaDept(areaDept) }
    }
}

class ItemViewModelFactory(private val repository: ItemRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase de ViewModel desconocida")
    }
}
